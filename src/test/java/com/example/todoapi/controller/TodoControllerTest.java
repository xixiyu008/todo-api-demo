package com.example.todoapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.todoapi.mapper.AppUserMapper;
import com.example.todoapi.model.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.todoapi.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        todoService.clear();
        appUserMapper.delete(null);
        appUserMapper.insert(new AppUser(null, "admin", passwordEncoder.encode("password123"), "ADMIN", true));
        token = loginAndGetToken();
    }

    @Test
    void shouldCreateAndListTodo() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learn Spring Boot"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.data.done").value(false))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data[0].id").value(todoId))
                .andExpect(jsonPath("$.data[0].title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.data[0].done").value(false))
                .andExpect(jsonPath("$.data[0].priority").value("MEDIUM"));
    }

    @Test
    void shouldMarkTodoDone() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Finish API",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(put("/api/todos/{id}/done", todoId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.done").value(true))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void shouldUpdateTodoTitle() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Old title"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New title"
                                  ,"priority": "LOW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.title").value("New title"))
                .andExpect(jsonPath("$.data.done").value(false))
                .andExpect(jsonPath("$.data.priority").value("LOW"));
    }

    @Test
    void shouldRejectInvalidPriorityWhenCreatingTodo() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Priority check",
                                  "priority": "URGENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("priority: priority must be one of HIGH, MEDIUM, LOW"));
    }

    @Test
    void shouldRejectBlankTitleWhenCreatingTodo() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("title: title must not be blank"));
    }

    @Test
    void shouldRejectTooLongTitleWhenUpdatingTodo() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid title"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("title: title must be at most 120 characters"));
    }

    @Test
    void shouldReturnUnifiedNotFoundResponseWhenTodoDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/todos/999/done")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Todo not found: 999"))
                .andExpect(jsonPath("$.errors[0]").value("Todo not found: 999"));
    }

    @Test
    void shouldDeleteTodo() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Delete me"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(delete("/api/todos/{id}", todoId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldOnlyListTodosOwnedByCurrentUser() throws Exception {
        appUserMapper.insert(new AppUser(null, "member-owner", passwordEncoder.encode("password456"), "USER", true));
        String memberToken = loginAndGetToken("member-owner", "password456");

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Admin todo"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Member todo"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Admin todo"));

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Member todo"));
    }

    @Test
    void shouldRejectAccessingAnotherUsersTodo() throws Exception {
        appUserMapper.insert(new AppUser(null, "member-other", passwordEncoder.encode("password456"), "USER", true));
        String memberToken = loginAndGetToken("member-other", "password456");

        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Admin private todo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long todoId = extractTodoId(createResult);

        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Hacked title"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"));

        mockMvc.perform(delete("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"));
    }

    @Test
    void shouldRequireAuthenticationWhenRequestHasNoToken() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new-user",
                                  "password": "password456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.username").value("new-user"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "duplicate-user",
                                  "password": "password456"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "duplicate-user",
                                  "password": "password789"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void shouldFetchCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void shouldAllowAdminToListUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void shouldAllowAdminToDisableAndEnableUser() throws Exception {
        appUserMapper.insert(new AppUser(null, "member-a", passwordEncoder.encode("password456"), "USER", true));
        Long userId = findUser("member-a").getId();

        mockMvc.perform(put("/api/users/{id}/disable", userId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(put("/api/users/{id}/enable", userId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void shouldRejectDisabledUserLogin() throws Exception {
        appUserMapper.insert(new AppUser(null, "member-b", passwordEncoder.encode("password456"), "USER", false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "member-b",
                                  "password": "password456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("用户已被禁用"));
    }

    @Test
    void shouldRejectNonAdminUserListingAllUsers() throws Exception {
        appUserMapper.insert(new AppUser(null, "member-c", passwordEncoder.encode("password456"), "USER", true));
        String userToken = loginAndGetToken("member-c", "password456");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDisablingCurrentAdmin() throws Exception {
        Long adminId = findUser("admin").getId();

        mockMvc.perform(put("/api/users/{id}/disable", adminId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_OPERATION_NOT_ALLOWED"));
    }

    private String loginAndGetToken() throws Exception {
        return loginAndGetToken("admin", "password123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    private String bearerToken() {
        return "Bearer " + token;
    }

    private long extractTodoId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private AppUser findUser(String username) {
        return appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username)
                .last("LIMIT 1"));
    }
}
