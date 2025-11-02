package com.cryptotax.helper.controller;

import com.cryptotax.helper.dto.*;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.UserRepository;
import com.cryptotax.helper.security.JwtUtil;
import com.cryptotax.helper.service.TwoFactorAuthService;
import com.cryptotax.helper.service.UserService;
import com.cryptotax.helper.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Пользователь успешно зарегистрирован");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Проверка 2FA
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (user.getIs2faEnabled()) {
                // Если 2FA включен, возвращаем запрос на ввод кода
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Требуется код двухфакторной аутентификации");
                response.put("requires2fa", true);
                response.put("email", loginRequest.getEmail());
                response.put("nextStep", "VERIFY_2FA");
                return ResponseEntity.ok(response);
            }

            String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new JwtResponseDto(jwt, userDetails.getUsername(), "Успешный вход"));

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Неверный email или пароль");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<?> loginWith2FA(@Valid @RequestBody LoginWith2FADto loginDto) {
        try {
            // Сначала проверяем пароль
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            // Затем проверяем 2FA код
            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean is2faValid = twoFactorAuthService.verify2FA(user.getId(), loginDto.getVerificationCode());

            if (!is2faValid) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Неверный код двухфакторной аутентификации");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new JwtResponseDto(jwt, userDetails.getUsername(), "Успешный вход с двухфакторной аутентификацией"));

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка аутентификации: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2FA() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = securityUtils.getCurrentUserId();

            Map<String, String> result = twoFactorAuthService.setup2FA(userId);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(@Valid @RequestBody TwoFactorAuthDto authDto) {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = securityUtils.getCurrentUserId();

            boolean isValid = twoFactorAuthService.verify2FA(userId, authDto.getVerificationCode());

            if (isValid) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "2FA успешно активирован");
                response.put("status", "ENABLED");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Неверный код верификации");
                return ResponseEntity.badRequest().body(errorResponse);
            }

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = securityUtils.getCurrentUserId();

            twoFactorAuthService.disable2FA(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "2FA успешно отключен");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<?> get2FAStatus() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = securityUtils.getCurrentUserId();

            boolean isEnabled = twoFactorAuthService.is2faEnabled(userId); // ✅ Правильное название с маленькой буквы

            Map<String, Boolean> response = new HashMap<>();
            response.put("is2faEnabled", isEnabled);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // В JWT-based аутентификации логин обычно обрабатывается на клиенте
        // путем удаления токена, но можно добавить blacklist токенов
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Успешный выход из системы");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = securityUtils.getCurrentUserId();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("is2faEnabled", user.getIs2faEnabled());
            response.put("emailVerified", user.getEmailVerified());
            response.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/2fa/verify-test")
    public ResponseEntity<?> verify2FATest(@Valid @RequestBody TwoFactorAuthDto authDto) {
        try {
            Long userId = securityUtils.getCurrentUserId();

            boolean isValid = twoFactorAuthService.verify2FATest(userId, authDto.getVerificationCode());

            Map<String, String> response = new HashMap<>();
            response.put("message", "2FA успешно активирован (тестовый режим)");
            response.put("status", "ENABLED");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/2fa/debug-validate")
    public ResponseEntity<?> debugValidate2FA(@Valid @RequestBody TwoFactorAuthDto authDto) {
        try {
            Long userId = securityUtils.getCurrentUserId();

            boolean isValid = twoFactorAuthService.validateCode(userId, authDto.getVerificationCode());

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("message", isValid ? "Код верный" : "Код неверный");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}