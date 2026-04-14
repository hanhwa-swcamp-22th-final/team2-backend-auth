package com.team2.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.*;
import com.team2.auth.query.dto.TokenResponse;
import jakarta.servlet.http.Cookie;
import com.team2.auth.command.domain.entity.*;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.*;
import com.team2.auth.security.JwtProvider;
import com.team2.auth.command.application.service.*;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
@WithMockUser(roles = "ADMIN")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AuthService authService;

    private User savedUser;
    private Department savedDept;
    private Position savedPosition;
    private Company savedCompany;

    @BeforeEach
    void setUp() {
        savedDept = departmentRepository.saveAndFlush(new Department("Engineering"));
        savedPosition = positionRepository.saveAndFlush(new Position("팀장", 1));

        User user = User.builder()
                .employeeNo("EMP001")
                .userName("Test User")
                .userEmail("test@example.com")
                .userPw(passwordEncoder.encode("password123"))
                .userRole(Role.ADMIN)
                .userStatus(UserStatus.ACTIVE)
                .build();
        savedUser = userRepository.saveAndFlush(user);

        savedCompany = companyRepository.saveAndFlush(Company.builder()
                .companyName("Test Company")
                .companyAddressEn("123 Test St")
                .companyAddressKr("테스트 주소")
                .companyTel("02-1234-5678")
                .companyFax("02-1234-5679")
                .companyEmail("company@test.com")
                .companyWebsite("https://test.com")
                .companySealImageUrl("https://test.com/seal.png")
                .build());

        entityManager.clear();
    }

    // ========================================================================
    // AuthController + AuthService: login
    // ========================================================================

    @Test
    @DisplayName("로그인 성공 - accessToken body 반환, RT는 HttpOnly 쿠키로 발급")
    void login_success() throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequest("test@example.com", "password123"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("sb_refresh_token"))
                .andExpect(cookie().httpOnly("sb_refresh_token", true))
                .andExpect(cookie().path("sb_refresh_token", "/api/auth"));
    }

    @Test
    @DisplayName("로그인 실패 - 유저 없음")
    void login_userNotFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("nobody@a.com", "pw")))));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void login_wrongPassword_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "wrong")))));
    }

    @Test
    @DisplayName("로그인 실패 - 휴직한 유저")
    void login_inactiveUser_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        user.changeStatus(UserStatus.ON_LEAVE);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "password123")))));
    }

    // ========================================================================
    // AuthController + AuthService: refresh
    // ========================================================================

    @Test
    @DisplayName("토큰 재발급 성공 - RT 쿠키로 새 accessToken 발급 및 쿠키 회전")
    void refresh_success() throws Exception {
        TokenResponse loginResp = authService.login("test@example.com", "password123");
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("sb_refresh_token", loginResp.getRefreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("sb_refresh_token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 → 401")
    void refresh_noCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰 (쿠키)")
    void refresh_invalidToken_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("sb_refresh_token", "invalid"))));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 만료된 토큰 (쿠키)")
    void refresh_expiredToken_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        refreshTokenRepository.saveAndFlush(RefreshToken.builder()
                .user(user).tokenValue("expired-tok").tokenExpiresAt(LocalDateTime.now().minusDays(1)).build());
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("sb_refresh_token", "expired-tok"))));
    }

    // ========================================================================
    // AuthController + AuthService: logout
    // ========================================================================

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        authService.login("test@example.com", "password123");
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LogoutRequest(savedUser.getUserId()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 실패 - 유저 없음")
    void logout_userNotFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LogoutRequest(99999)))));
    }

    // ========================================================================
    // UserController + UserService
    // ========================================================================

    @Test
    @DisplayName("회원등록 성공")
    void createUser_success() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder()
                .name("New").email("new@a.com").password("pw").role(Role.SALES).build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeNo").exists());
    }

    @Test
    @DisplayName("회원등록 실패 - 중복 이메일")
    void createUser_duplicateEmail_throwsViaServlet() {
        CreateUserRequest req = CreateUserRequest.builder()
                .name("Dup").email("test@example.com").password("pw").role(Role.SALES).build();
        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))));
    }

    @Test
    @DisplayName("회원등록 실패 - 중복 이메일 (같은 이메일)")
    void createUser_duplicateEmail2_throwsViaServlet() {
        CreateUserRequest req = CreateUserRequest.builder()
                .name("Dup").email("test@example.com").password("pw").role(Role.SALES).build();
        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))));
    }

    @Test
    @DisplayName("회원목록 조회 성공")
    void getAllUsers_success() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @DisplayName("회원 조회 성공")
    void getUser_success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", savedUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Test User"));
    }

    @Test
    @DisplayName("회원 조회 실패 - 유저 없음")
    void getUser_notFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(get("/api/users/{id}", 99999)));
    }

    @Test
    @DisplayName("회원 수정 성공")
    void updateUser_withDeptAndPosition() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .name("Up").email("up@a.com")
                .departmentId(savedDept.getDepartmentId()).positionId(savedPosition.getPositionId()).build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Up"));
    }

    @Test
    @DisplayName("회원 수정 - 부서 변경과 직급 변경 없이")
    void updateUser_withoutDeptAndPosition() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder().name("OnlyName").build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("OnlyName"));
    }

    @Test
    void updateUser_nullNameAndEmail() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder().build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Test User"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));
    }

    @Test
    void updateUser_deptNotFound_throwsViaServlet() {
        UpdateUserRequest req = UpdateUserRequest.builder().teamId(99999).build();
        assertThrows(ServletException.class, () -> mockMvc.perform(put("/api/users/{id}", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void updateUser_posNotFound_throwsViaServlet() {
        UpdateUserRequest req = UpdateUserRequest.builder().positionId(99999).build();
        assertThrows(ServletException.class, () -> mockMvc.perform(put("/api/users/{id}", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void changeStatus_success() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/status", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChangeStatusRequest(UserStatus.ON_LEAVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userStatus").value("ON_LEAVE"));
    }

    @Test
    void changeStatus_retiredUser_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        user.changeStatus(UserStatus.RETIRED);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(patch("/api/users/{id}/status", savedUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChangeStatusRequest(UserStatus.ACTIVE)))));
    }

    // ========================================================================
    // CompanyController + CompanyService
    // ========================================================================

    @Test
    void getCompany_success() throws Exception {
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Company"));
    }

    @Test
    void getCompany_notFound_throwsViaServlet() {
        companyRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(get("/api/company")));
    }

    @Test
    void updateCompany_allFields() throws Exception {
        UpdateCompanyRequest req = UpdateCompanyRequest.builder()
                .name("Up").addressEn("AE").addressKr("AK").tel("T").fax("F")
                .email("E").website("W").sealImageUrl("S").build();
        mockMvc.perform(put("/api/company").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Up"));
    }

    @Test
    void updateCompany_allNulls() throws Exception {
        UpdateCompanyRequest req = UpdateCompanyRequest.builder().build();
        mockMvc.perform(put("/api/company").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Company"));
    }

    @Test
    void updateCompany_partial() throws Exception {
        UpdateCompanyRequest req = UpdateCompanyRequest.builder().name("Partial").build();
        mockMvc.perform(put("/api/company").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Partial"))
                .andExpect(jsonPath("$.companyAddressEn").value("123 Test St"));
    }

    // ========================================================================
    // DepartmentController + DepartmentService
    // ========================================================================

    @Test
    void createDepartment_success() throws Exception {
        mockMvc.perform(post("/api/departments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateDepartmentRequest("HR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentName").value("HR"));
    }

    @Test
    void getAllDepartments_success() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.departmentList").isArray());
    }

    @Test
    void deleteDepartment_success() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", savedDept.getDepartmentId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDepartment_notFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(delete("/api/departments/{id}", 99999)));
    }

    @Test
    void deleteDepartment_hasUsers_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        user.assignDepartment(savedDept);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(delete("/api/departments/{id}", savedDept.getDepartmentId())));
    }

    // ========================================================================
    // PositionController + PositionService
    // ========================================================================

    @Test
    void createPosition_success() throws Exception {
        mockMvc.perform(post("/api/positions").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePositionRequest("팀원", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.positionName").value("팀원"));
    }

    @Test
    void getAllPositions_success() throws Exception {
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.positionList").isArray());
    }
}
