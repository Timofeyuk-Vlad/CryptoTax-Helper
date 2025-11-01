package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.UserRegistrationDto;
import com.cryptotax.helper.entity.SubscriptionPlan;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.entity.UserRole;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

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

        // ✅ Назначаем роль пользователя по умолчанию
        user.addRole(UserRole.ROLE_USER);

        // ✅ Устанавливаем бесплатный тариф по умолчанию
        user.setSubscriptionType(SubscriptionPlan.FREE.name());
        subscriptionService.applySubscriptionLimits(user, SubscriptionPlan.FREE);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // ✅ Методы для управления ролями
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

        SubscriptionPlan plan = SubscriptionPlan.fromString(subscriptionType);
        user.setSubscriptionType(plan.name());

        if (months != null && months > 0) {
            user.setSubscriptionExpires(LocalDateTime.now().plusMonths(months));
        } else {
            // Для бесплатного тарифа или если months не указан
            user.setSubscriptionExpires(null);
        }

        // Применяем лимиты подписки
        subscriptionService.applySubscriptionLimits(user, plan);

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}