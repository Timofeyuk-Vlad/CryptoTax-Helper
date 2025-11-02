package com.example.lowflightzone.security;

import com.example.lowflightzone.dao.UserDao;
import com.example.lowflightzone.entity.User;
import com.example.lowflightzone.exceptions.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserDao userDao;

    /**
     * 📌 Получить текущего аутентифицированного пользователя
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDao.findByEmail(userDetails.getUsername());
        }

        if (authentication.getPrincipal() instanceof String username) {
            return userDao.findByEmail(username);
        }

        return Optional.empty();
    }

    /**
     * 📌 Получить ID текущего пользователя
     */
    public Optional<Integer> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * 📌 Получить текущего пользователя или выбросить исключение
     */
    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new UserException("Пользователь не аутентифицирован"));
    }

    /**
     * 📌 Получить ID текущего пользователя или выбросить исключение
     */
    public Integer getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new UserException("Пользователь не аутентифицирован"));
    }

    /**
     * 📌 ✅ Новый метод: получить email текущего пользователя
     */
    public String getCurrentUserEmailOrThrow() {
        return getCurrentUser()
                .map(User::getEmail)
                .orElseThrow(() -> new UserException("Пользователь не аутентифицирован"));
    }
}
