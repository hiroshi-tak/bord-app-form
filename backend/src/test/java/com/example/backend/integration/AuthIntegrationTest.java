package com.example.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

        @SuppressWarnings("resource")
        @Container
        static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("whiteboard")
                    .withUsername("postgres")
                    .withPassword("password");

        @DynamicPropertySource
        static void configureProperties(
                        DynamicPropertyRegistry registry) {

                registry.add(
                        "spring.datasource.url",
                        postgres::getJdbcUrl);

                registry.add(
                        "spring.datasource.username",
                        postgres::getUsername);

                registry.add(
                        "spring.datasource.password",
                        postgres::getPassword);
        }

        @Autowired
        private MockMvc mockMvc;

        @Test
        void register_login_success()
                        throws Exception {

                // 登録
                mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                                "username":"test",
                                                "password":"password"
                                        }
                                        """))
                        .andExpect(status().isOk());

                // ログイン
                MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                        "username":"test",
                                        "password":"password"
                                }
                                """))
                        .andExpect(status().isOk())
                        .andExpect(cookie().exists("accessToken"))
                        .andExpect(cookie().exists("refreshToken"))
                        .andReturn();

                // JWT取得
                Cookie accessTokenCookie = loginResult.getResponse()
                        .getCookie("accessToken");

                // JWT付きアクセス
                mockMvc.perform(
                        get("/api/boards")
                                .cookie(accessTokenCookie))
                        .andExpect(status().isOk());

        }

}