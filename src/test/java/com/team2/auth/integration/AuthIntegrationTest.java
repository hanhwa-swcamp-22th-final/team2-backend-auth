package com.team2.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.*;
import com.team2.auth.entity.*;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.*;
import com.team2.auth.security.JwtProvider;
import com.team2.auth.service.*;
import io.jsonwebtoken.Claims;
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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private EntityManager entityManager;

    @Autowired private AuthService authService;
    @Autowired private UserService userService;
    @Autowired private CompanyService companyService;
    @Autowired private DepartmentService departmentService;
    @Autowired private PositionService positionService;

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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("nobody@a.com", "pw")))));
    }

    @Test
    void login_wrongPassword_throwsViaServlet() {
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("test@example.com", "wrong")))));
    }

    @Test
    void login_inactiveUser_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.changeStatus(UserStatus.휴직);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/login")
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("invalid")))));
    }

    @Test
    void refresh_expiredToken_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        refreshTokenRepository.saveAndFlush(RefreshToken.builder()
                .user(user).token("expired-tok").expiresAt(LocalDateTime.now().minusDays(1)).build());
        entityManager.clear();

        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/refresh")
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/auth/logout")
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void createUser_duplicateEmployeeNo_throwsViaServlet() {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("EMP001").name("Dup").email("uniq@a.com").password("pw").role(Role.SALES).build();
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/users/{id}", 99999)));
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))));
    }

    @Test
    void updateUser_posNotFound_throwsViaServlet() {
        UpdateUserRequest req = UpdateUserRequest.builder().positionId(99999).build();
        assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/users/{id}", savedUser.getId())
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

        assertThrows(ServletException.class, () ->
                mockMvc.perform(patch("/api/users/{id}/status", savedUser.getId())
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

        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/company")));
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
        assertThrows(ServletException.class, () ->
                mockMvc.perform(delete("/api/departments/{id}", 99999)));
    }

    @Test
    void deleteDepartment_hasUsers_throwsViaServlet() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.assignDepartment(savedDept);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThrows(ServletException.class, () ->
                mockMvc.perform(delete("/api/departments/{id}", savedDept.getId())));
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

    // ========================================================================
    // Entity: User
    // ========================================================================

    @Test
    void user_canLogin_재직() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        assertThat(u.canLogin()).isTrue();
    }

    @Test
    void user_canLogin_휴직() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.휴직).build();
        assertThat(u.canLogin()).isFalse();
    }

    @Test
    void user_canLogin_퇴직() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.퇴직).build();
        assertThat(u.canLogin()).isFalse();
    }

    @Test
    void user_changeStatus_success() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.changeStatus(UserStatus.휴직);
        assertThat(u.getStatus()).isEqualTo(UserStatus.휴직);
    }

    @Test
    void user_changeStatus_fromRetired_throws() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.퇴직).build();
        assertThatThrownBy(() -> u.changeStatus(UserStatus.재직))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void user_assignDepartment() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        Department d = new Department("D");
        u.assignDepartment(d);
        assertThat(u.getDepartment()).isEqualTo(d);
    }

    @Test
    void user_assignPosition() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        Position p = new Position("P", 1);
        u.assignPosition(p);
        assertThat(u.getPosition()).isEqualTo(p);
    }

    @Test
    void user_hasApprovalAuthority_level1() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.assignPosition(new Position("팀장", 1));
        assertThat(u.hasApprovalAuthority()).isTrue();
    }

    @Test
    void user_hasApprovalAuthority_nonLevel1() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.assignPosition(new Position("팀원", 2));
        assertThat(u.hasApprovalAuthority()).isFalse();
    }

    @Test
    void user_hasApprovalAuthority_noPosition() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        assertThat(u.hasApprovalAuthority()).isFalse();
    }

    @Test
    void user_isAdmin_true() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.ADMIN).status(UserStatus.재직).build();
        assertThat(u.isAdmin()).isTrue();
    }

    @Test
    void user_isAdmin_false() {
        User u = User.builder().employeeNo("X").name("X").email("x@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        assertThat(u.isAdmin()).isFalse();
    }

    @Test
    void user_updateInfo_both() {
        User u = User.builder().employeeNo("X").name("Old").email("old@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.updateInfo("New", "new@x");
        assertThat(u.getName()).isEqualTo("New");
        assertThat(u.getEmail()).isEqualTo("new@x");
    }

    @Test
    void user_updateInfo_nullName() {
        User u = User.builder().employeeNo("X").name("Old").email("old@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.updateInfo(null, "new@x");
        assertThat(u.getName()).isEqualTo("Old");
        assertThat(u.getEmail()).isEqualTo("new@x");
    }

    @Test
    void user_updateInfo_nullEmail() {
        User u = User.builder().employeeNo("X").name("Old").email("old@x").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        u.updateInfo("New", null);
        assertThat(u.getName()).isEqualTo("New");
        assertThat(u.getEmail()).isEqualTo("old@x");
    }

    @Test
    void user_prePersist() {
        User u = User.builder().employeeNo("PP1").name("PP").email("pp@x.com").pw("x").role(Role.SALES).status(UserStatus.재직).build();
        User saved = userRepository.saveAndFlush(u);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void user_preUpdate() {
        User u = userRepository.findById(savedUser.getId()).orElseThrow();
        u.updateInfo("TriggerUpdate", null);
        userRepository.saveAndFlush(u);
        entityManager.clear();
        User reloaded = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    // ========================================================================
    // Entity: Company
    // ========================================================================

    @Test
    void company_updateInfo_allValues() {
        Company c = companyRepository.findById(savedCompany.getId()).orElseThrow();
        c.updateInfo("N", "AE", "AK", "T", "F", "E", "W", "S");
        assertThat(c.getName()).isEqualTo("N");
        assertThat(c.getAddressEn()).isEqualTo("AE");
        assertThat(c.getAddressKr()).isEqualTo("AK");
        assertThat(c.getTel()).isEqualTo("T");
        assertThat(c.getFax()).isEqualTo("F");
        assertThat(c.getEmail()).isEqualTo("E");
        assertThat(c.getWebsite()).isEqualTo("W");
        assertThat(c.getSealImageUrl()).isEqualTo("S");
    }

    @Test
    void company_updateInfo_allNulls() {
        Company c = companyRepository.findById(savedCompany.getId()).orElseThrow();
        c.updateInfo(null, null, null, null, null, null, null, null);
        assertThat(c.getName()).isEqualTo("Test Company");
    }

    @Test
    void company_updateInfo_partial() {
        Company c = companyRepository.findById(savedCompany.getId()).orElseThrow();
        c.updateInfo("Only", null, null, null, null, null, null, null);
        assertThat(c.getName()).isEqualTo("Only");
        assertThat(c.getAddressEn()).isEqualTo("123 Test St");
    }

    @Test
    void company_preUpdate() {
        Company c = companyRepository.findById(savedCompany.getId()).orElseThrow();
        c.updateInfo("Trigger", null, null, null, null, null, null, null);
        companyRepository.saveAndFlush(c);
        entityManager.clear();
        Company reloaded = companyRepository.findById(savedCompany.getId()).orElseThrow();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    // ========================================================================
    // Entity: Department
    // ========================================================================

    @Test
    void department_constructor() {
        Department d = new Department("Finance");
        assertThat(d.getName()).isEqualTo("Finance");
    }

    @Test
    void department_prePersist() {
        Department d = departmentRepository.saveAndFlush(new Department("Sales"));
        assertThat(d.getCreatedAt()).isNotNull();
    }

    // ========================================================================
    // Entity: Position
    // ========================================================================

    @Test
    void position_constructor_success() {
        Position p = new Position("팀원", 2);
        assertThat(p.getName()).isEqualTo("팀원");
        assertThat(p.getLevel()).isEqualTo(2);
    }

    @Test
    void position_constructor_nullName_throws() {
        assertThatThrownBy(() -> new Position(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급명은 필수입니다.");
    }

    @Test
    void position_constructor_blankName_throws() {
        assertThatThrownBy(() -> new Position("  ", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급명은 필수입니다.");
    }

    @Test
    void position_hasApprovalAuthority_level1() {
        assertThat(new Position("팀장", 1).hasApprovalAuthority()).isTrue();
    }

    @Test
    void position_hasApprovalAuthority_nonLevel1() {
        assertThat(new Position("팀원", 2).hasApprovalAuthority()).isFalse();
    }

    @Test
    void position_prePersist() {
        Position p = positionRepository.saveAndFlush(new Position("팀원", 2));
        assertThat(p.getCreatedAt()).isNotNull();
    }

    // ========================================================================
    // Entity: RefreshToken
    // ========================================================================

    @Test
    void refreshToken_isExpired_true() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        RefreshToken t = RefreshToken.builder().user(user).token("t1")
                .expiresAt(LocalDateTime.now().minusHours(1)).build();
        assertThat(t.isExpired()).isTrue();
    }

    @Test
    void refreshToken_isExpired_false() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        RefreshToken t = RefreshToken.builder().user(user).token("t2")
                .expiresAt(LocalDateTime.now().plusHours(1)).build();
        assertThat(t.isExpired()).isFalse();
    }

    @Test
    void refreshToken_prePersist() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        RefreshToken t = RefreshToken.builder().user(user).token("t3")
                .expiresAt(LocalDateTime.now().plusHours(1)).build();
        RefreshToken saved = refreshTokenRepository.saveAndFlush(t);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========================================================================
    // JwtProvider
    // ========================================================================

    @Test
    void jwt_generateAndParseAccessToken() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        String token = jwtProvider.generateAccessToken(user);
        assertThat(token).isNotBlank();

        Claims claims = jwtProvider.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(user.getId()));
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
        assertThat(claims.get("name", String.class)).isEqualTo(user.getName());
        assertThat(claims.get("role", String.class)).isEqualTo(user.getRole().name());
    }

    @Test
    void jwt_generateRefreshToken() {
        String rt = jwtProvider.generateRefreshToken();
        assertThat(rt).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void jwt_getRefreshTokenExpiry() {
        assertThat(jwtProvider.getRefreshTokenExpiry()).isEqualTo(604800000L);
    }

    @Test
    void jwt_validateAccessToken_valid() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        String token = jwtProvider.generateAccessToken(user);
        assertThat(jwtProvider.validateAccessToken(token)).isTrue();
    }

    @Test
    void jwt_validateAccessToken_tampered() {
        // Use a completely invalid string that cannot be parsed as JWT
        assertThat(jwtProvider.validateAccessToken("not.a.valid.jwt.token")).isFalse();
    }

    @Test
    void jwt_validateAccessToken_expired() throws InterruptedException {
        JwtProvider shortProvider = new JwtProvider(
                "testSecretKeyForJwtTestingPurposesMustBe256BitsLongEnough!!", 1, 1);
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        String token = shortProvider.generateAccessToken(user);
        Thread.sleep(10);
        assertThat(shortProvider.validateAccessToken(token)).isFalse();
    }

    // ========================================================================
    // SecurityConfig
    // ========================================================================

    @Test
    void security_csrfDisabled() throws Exception {
        mockMvc.perform(post("/api/departments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDepartmentRequest("CSRF"))))
                .andExpect(status().isCreated());
    }

    @Test
    void security_allRequestsPermitted() throws Exception {
        mockMvc.perform(get("/api/departments")).andExpect(status().isOk());
    }

    @Test
    void security_cors() throws Exception {
        mockMvc.perform(options("/api/departments")
                        .header("Origin", "http://localhost:8001")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void security_passwordEncoderIsBCrypt() {
        String encoded = passwordEncoder.encode("test");
        assertThat(passwordEncoder.matches("test", encoded)).isTrue();
        assertThat(encoded).startsWith("$2a$");
    }

    // ========================================================================
    // Service-layer direct exception tests (covers lambda branches)
    // ========================================================================

    @Test
    void authService_login_userNotFound() {
        assertThatThrownBy(() -> authService.login("no@one.com", "pw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    void authService_login_wrongPassword() {
        assertThatThrownBy(() -> authService.login("test@example.com", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @Test
    void authService_login_inactiveUser() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.changeStatus(UserStatus.휴직);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThatThrownBy(() -> authService.login("test@example.com", "password123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인할 수 없는 상태입니다.");
    }

    @Test
    void authService_refreshToken_notFound() {
        assertThatThrownBy(() -> authService.refreshToken("nope"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    void authService_refreshToken_expired() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        refreshTokenRepository.saveAndFlush(RefreshToken.builder()
                .user(user).token("exp-svc").expiresAt(LocalDateTime.now().minusDays(1)).build());
        entityManager.clear();

        assertThatThrownBy(() -> authService.refreshToken("exp-svc"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("만료된 리프레시 토큰입니다.");
    }

    @Test
    void authService_logout_userNotFound() {
        assertThatThrownBy(() -> authService.logout(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    void userService_createUser_dupEmail() {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("E99").name("D").email("test@example.com").password("pw").role(Role.SALES).build();
        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");
    }

    @Test
    void userService_createUser_dupEmpNo() {
        CreateUserRequest req = CreateUserRequest.builder()
                .employeeNo("EMP001").name("D").email("uu@uu.com").password("pw").role(Role.SALES).build();
        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사번입니다.");
    }

    @Test
    void userService_getUser_notFound() {
        assertThatThrownBy(() -> userService.getUser(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    void userService_updateUser_deptNotFound() {
        UpdateUserRequest req = UpdateUserRequest.builder().departmentId(99999).build();
        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다.");
    }

    @Test
    void userService_updateUser_posNotFound() {
        UpdateUserRequest req = UpdateUserRequest.builder().positionId(99999).build();
        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다.");
    }

    @Test
    void companyService_getCompany_notFound() {
        companyRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> companyService.getCompany())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회사 정보를 찾을 수 없습니다.");
    }

    @Test
    void departmentService_delete_notFound() {
        assertThatThrownBy(() -> departmentService.deleteDepartment(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다.");
    }

    @Test
    void departmentService_delete_hasUsers() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        user.assignDepartment(savedDept);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThatThrownBy(() -> departmentService.deleteDepartment(savedDept.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("소속된 사용자가 있어 삭제할 수 없습니다.");
    }
}
