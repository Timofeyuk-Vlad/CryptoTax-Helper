package com.cryptotax.helper.util;

import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Возвращает ID текущего аутентифицированного пользователя.
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        System.out.println("🔐 Authentication: " + authentication);
        System.out.println("👤 Principal: " + authentication.getPrincipal());

        String email;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return user.getId();
    }
}
