package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.service.UserService;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.security.JwtService;
import com.example.backend.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

        @Value("${cookie.secure}")
        private boolean cookieSecure;

        private final UserService userService;
        private final JwtService jwtService;

        public AuthController(
                UserService userService,
                        JwtService jwtService) {

                this.userService = userService;
                this.jwtService = jwtService;
        }
    

        // ユーザー登録
        @PostMapping("/register")
        public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

                try {
                        userService.register(request);

                        return ResponseEntity.ok("register success");

                } catch (RuntimeException e) {
                        return ResponseEntity
                                .badRequest()
                                .body(e.getMessage());
                }
        }

        // ログイン
        @PostMapping("/login")
        public ResponseEntity<String> login(@RequestBody LoginRequest request){

                try {
                        User user = userService.login(request);

                        String accessToken = jwtService.generateToken(user);
                        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

                        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path("/")
                                .maxAge(60 * 60)
                                .sameSite("Lax")
                                .build();

                        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path("/")
                                .maxAge(60 * 60 * 24 * 7)
                                .sameSite("Lax")
                                .build();

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                        .body("login ok");
                
                } catch (RuntimeException e) {
                        return ResponseEntity
                                .badRequest()
                                .body(e.getMessage());
                }
        }

        // ログアウト
        @PostMapping("/logout")
        public ResponseEntity<String> logout() {

                ResponseCookie access = ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .path("/")
                        .secure(cookieSecure)
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();

                ResponseCookie refresh = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .path("/")
                        .secure(cookieSecure)
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, access.toString())
                        .header(HttpHeaders.SET_COOKIE, refresh.toString())
                        .body("logout");
        }

        // refresh
        @PostMapping("/refresh")
        public ResponseEntity<?> refresh(HttpServletRequest request) {

                String refreshToken = null;

                Cookie[] cookies = request.getCookies();

                if (cookies != null) {
                refreshToken = Arrays.stream(cookies)
                        .filter(c -> "refreshToken".equals(c.getName()))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
                }

                if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
                return ResponseEntity.status(401).build();
                }

                String username = jwtService.extractUsername(refreshToken);

                User user = userService.findByUsername(username);

                String newAccessToken = jwtService.generateToken(user);

                ResponseCookie cookie = ResponseCookie.from("accessToken", newAccessToken)
                        .httpOnly(true)
                        .path("/")
                        .secure(cookieSecure)
                        .maxAge(60 * 60)
                        .sameSite("Lax")
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body("refreshed");
        }

}