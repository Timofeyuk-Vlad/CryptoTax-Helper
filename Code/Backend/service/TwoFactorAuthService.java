package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator gAuth;

    public Map<String, String> setup2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        log.info("Generated 2FA secret for user {}: {}", user.getEmail(), secret);

        user.setSecret2fa(secret);
        userRepository.save(user);

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "CryptoTax Helper",
                user.getEmail(),
                key
        );

        Map<String, String> result = new HashMap<>();
        result.put("secret", secret);
        result.put("qrCodeUrl", qrCodeUrl);
        result.put("message", "Отсканируйте QR-код в приложении Google Authenticator");

        return result;
    }

    public boolean verify2FA(Long userId, int verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getSecret2fa() == null) {
            throw new RuntimeException("2FA не настроен для пользователя");
        }

        log.info("Verifying 2FA code {} for user {} with secret: {}",
                verificationCode, user.getEmail(), user.getSecret2fa());

        boolean isValid = gAuth.authorize(user.getSecret2fa(), verificationCode);

        log.info("2FA verification result for user {}: {}", user.getEmail(), isValid);

        if (isValid && !user.getIs2faEnabled()) {
            user.setIs2faEnabled(true);
            userRepository.save(user);
            log.info("2FA enabled for user: {}", user.getEmail());
        }

        return isValid;
    }

    public void disable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setIs2faEnabled(false);
        user.setSecret2fa(null);
        userRepository.save(user);

        log.info("2FA disabled for user: {}", user.getEmail());
    }

    // ✅ Правильное название - is2faEnabled (camelCase)
    public boolean is2faEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean enabled = user.getIs2faEnabled();
        log.info("2FA status for user {}: {}", user.getEmail(), enabled);

        return enabled;
    }

    // ✅ Дополнительный метод для отладки
    public boolean validateCode(Long userId, int verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getSecret2fa() == null) {
            throw new RuntimeException("2FA не настроен для пользователя");
        }

        return gAuth.authorize(user.getSecret2fa(), verificationCode);
    }

    // ✅ Временный метод для тестирования
    public boolean verify2FATest(Long userId, int verificationCode) {
        log.info("TEST MODE: Accepting any 2FA code for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setIs2faEnabled(true);
        userRepository.save(user);

        return true;
    }
}