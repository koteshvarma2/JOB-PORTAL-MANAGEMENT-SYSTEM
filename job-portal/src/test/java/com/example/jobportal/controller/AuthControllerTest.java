package com.example.jobportal.controller;

import com.example.jobportal.entity.User;
import com.example.jobportal.service.EmailService;
import com.example.jobportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @SuppressWarnings("null")
    void testRegisterUser_Success() throws Exception {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");
        user.setEmail("newuser@example.com");
        user.setName("New User");
        user.setRole("ROLE_STUDENT");

        when(userService.findByUsername("newuser")).thenReturn(null);
        when(userService.findByEmail("newuser@example.com")).thenReturn(null);

        mockMvc.perform(post("/register")
                        .flashAttr("user", user)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify-otp?email=newuser@example.com"));
    }

    @Test
    @SuppressWarnings("null")
    void testRegisterUser_UserExists() throws Exception {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password123");
        user.setEmail("existing@example.com");
        user.setName("Existing User");
        user.setRole("ROLE_STUDENT");

        when(userService.findByUsername("existinguser")).thenReturn(new User());

        mockMvc.perform(post("/register")
                        .flashAttr("user", user)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }
}
