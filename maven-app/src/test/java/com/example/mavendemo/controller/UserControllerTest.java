package com.example.mavendemo.controller;

import com.example.mavendemo.model.UserSummary;
import com.example.mavendemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUserReturnsUserSummary() throws Exception {
        Mockito.when(userService.getUserSummary("user-1"))
                .thenReturn(new UserSummary("user-1", "Alice", "ACTIVE"));

        mockMvc.perform(get("/api/users/user-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
