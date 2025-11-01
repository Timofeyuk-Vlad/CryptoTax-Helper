package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.UserRegistrationDto;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.entity.UserRole;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Пользователь с email " + registrationDto.getEmail() + " уже существует");
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setIsEnabled(true);

        // ✅ Добавляем роль пользователя по умолчанию
        user.addRole(UserRole.ROLE_USER);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // ✅ Новые методы для управления ролями
    public User addRoleToUser(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.addRole(role);
        return userRepository.save(user);
    }

    public User removeRoleFromUser(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.removeRole(role);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User updateUserSubscription(Long userId, String subscriptionType, Integer months) {
        User user = getUserById(userId);

        user.setSubscriptionType(subscriptionType);
        if (months != null && months > 0) {
            user.setSubscriptionExpires(java.time.LocalDateTime.now().plusMonths(months));
        }

        // Обновляем лимиты в зависимости от подписки
        switch (subscriptionType.toUpperCase()) {
            case "PREMIUM":
                user.setMaxExchangeConnections(10);
                user.setMaxTransactionsPerYear(10000);
                user.addRole(UserRole.ROLE_PREMIUM);
                break;
            case "PRO":
                user.setMaxExchangeConnections(Integer.MAX_VALUE);
                user.setMaxTransactionsPerYear(Integer.MAX_VALUE);
                user.addRole(UserRole.ROLE_PREMIUM);
                break;
            default:
                user.setMaxExchangeConnections(3);
                user.setMaxTransactionsPerYear(1000);
                user.removeRole(UserRole.ROLE_PREMIUM);
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}