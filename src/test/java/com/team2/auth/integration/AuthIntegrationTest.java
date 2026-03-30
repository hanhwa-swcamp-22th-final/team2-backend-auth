package com.team2.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.*;
import com.team2.auth.entity.*;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.*;
import com.team2.auth.security.JwtProvider;
import com.team2.auth.service.*;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
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
                .name("Test User")
                .email("test@example.com")
                .pw(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .status(UserStatus.재직)
                .build();
        savedUser = userRepository.saveAndFlush(user);

        savedCompany = companyRepository.saveAndFlush(Company.builder()
                .name("Test Company")
                .addressEn("123 Test St")
                .addressKr("테스트 주소")
                .tel("02-1234-5678")
                .fax("02-1234-5679")
                .email("company@test.com")
                .website("https://test.com")
                .sealImageUrl("https://test.com/seal.png")
                .build());

        entityManager.clear();
    }

    // ========================================================================
    // AuthController + AuthService: login
    // ========================================================================

    @Test
    void login_success() throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequest("test@example.com", "password123"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_userNotFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("nobody@a.com", "pw")))));
    }

    @Test
    void login_wrongPassword_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "wrong")))));
    }

    @Test
    void login_inactiveUser_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.changeStatus(UserStatus.휴직);
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
    void refresh_success() throws Exception {
        TokenResponse loginResp = authService.login("test@example.com", "password123");
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(loginResp.getRefreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refresh_invalidToken_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest("invalid")))));
    }

    @Test
    void refresh_expiredToken_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        refreshTokenRepository.saveAndFlush(RefreshToken.builder()
                .user(user).token("expired-tok").expiresAt(LocalDateTime.now().minusDays(1)).build());
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest("expired-tok")))));
    }

    // ========================================================================
    // AuthController + AuthService: logout
    // ========================================================================

    @Test
    void logout_success() throws Exception {
        authService.login("test@example.com", "password123");
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LogoutRequest(savedUser.getId()))))
                .andExpect(status().isOk());
    }

    @Test
    void logout_userNotFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LogoutRequest(99999)))));
    }

    // ========================================================================
    // UserController + UserService
    // ========================================================================

    @Test
    void createUser_success() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("EMP002").name("New").email("new@a.com").password("pw").role(Role.SALES).build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeNo").value("EMP002"));
    }

    @Test
    void createUser_duplicateEmail_throwsViaServlet() {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("EMP003").name("Dup").email("test@example.com").password("pw").role(Role.SALES).build();
        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void createUser_duplicateEmployeeNo_throwsViaServlet() {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("EMP001").name("Dup").email("uniq@a.com").password("pw").role(Role.SALES).build();
        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void getAllUsers_success() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUser_success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getUser_notFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(get("/api/users/{id}", 99999)));
    }

    @Test
    void updateUser_withDeptAndPosition() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .name("Up").email("up@a.com")
                .departmentId(savedDept.getId()).positionId(savedPosition.getId()).build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Up"));
    }

    @Test
    void updateUser_withoutDeptAndPosition() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder().name("OnlyName").build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OnlyName"));
    }

    @Test
    void updateUser_nullNameAndEmail() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder().build();
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void updateUser_deptNotFound_throwsViaServlet() {
        UpdateUserRequest req = UpdateUserRequest.builder().departmentId(99999).build();
        assertThrows(ServletException.class, () -> mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void updateUser_posNotFound_throwsViaServlet() {
        UpdateUserRequest req = UpdateUserRequest.builder().positionId(99999).build();
        assertThrows(ServletException.class, () -> mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void changeStatus_success() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/status", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChangeStatusRequest(UserStatus.휴직))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("휴직"));
    }

    @Test
    void changeStatus_retiredUser_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.changeStatus(UserStatus.퇴직);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(patch("/api/users/{id}/status", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChangeStatusRequest(UserStatus.재직)))));
    }

    // ========================================================================
    // CompanyController + CompanyService
    // ========================================================================

    @Test
    void getCompany_success() throws Exception {
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Company"));
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
                .andExpect(jsonPath("$.name").value("Up"));
    }

    @Test
    void updateCompany_allNulls() throws Exception {
        UpdateCompanyRequest req = UpdateCompanyRequest.builder().build();
        mockMvc.perform(put("/api/company").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Company"));
    }

    @Test
    void updateCompany_partial() throws Exception {
        UpdateCompanyRequest req = UpdateCompanyRequest.builder().name("Partial").build();
        mockMvc.perform(put("/api/company").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partial"))
                .andExpect(jsonPath("$.addressEn").value("123 Test St"));
    }

    // ========================================================================
    // DepartmentController + DepartmentService
    // ========================================================================

    @Test
    void createDepartment_success() throws Exception {
        mockMvc.perform(post("/api/departments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateDepartmentRequest("HR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("HR"));
    }

    @Test
    void getAllDepartments_success() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteDepartment_success() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", savedDept.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDepartment_notFound_throwsViaServlet() {
        assertThrows(ServletException.class, () -> mockMvc.perform(delete("/api/departments/{id}", 99999)));
    }

    @Test
    void deleteDepartment_hasUsers_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.assignDepartment(savedDept);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () -> mockMvc.perform(delete("/api/departments/{id}", savedDept.getId())));
    }

    // ========================================================================
    // PositionController + PositionService
    // ========================================================================

    @Test
    void createPosition_success() throws Exception {
        mockMvc.perform(post("/api/positions").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePositionRequest("팀원", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("팀원"));
    }

    @Test
    void getAllPositions_success() throws Exception {
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
