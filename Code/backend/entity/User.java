package com.cryptotax.helper.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email обязателен")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // ✅ Добавляем поддержку ролей
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private List<UserRole> roles = new ArrayList<>();

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "is_2fa_enabled", nullable = false)
    private Boolean is2faEnabled = false;

    @Column(name = "secret_2fa", length = 32)
    private String secret2fa;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    @Column(name = "reset_token", length = 64)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // ✅ Добавляем поля для подписки
    @Column(name = "subscription_type", length = 20)
    private String subscriptionType = "FREE";

    @Column(name = "subscription_expires")
    private LocalDateTime subscriptionExpires;

    @Column(name = "max_exchange_connections")
    private Integer maxExchangeConnections = 3;

    @Column(name = "max_transactions_per_year")
    private Integer maxTransactionsPerYear = 1000;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Конструктор для удобства создания пользователя
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEnabled = true;
        this.is2faEnabled = false;
        this.emailVerified = false;
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.roles = List.of(UserRole.ROLE_USER); // По умолчанию обычный пользователь
        this.subscriptionType = "FREE";
        this.maxExchangeConnections = 3;
        this.maxTransactionsPerYear = 1000;
    }

    // Методы для работы с ролями
    public void addRole(UserRole role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ROLE_ADMIN) || hasRole(UserRole.ROLE_SUPER_ADMIN);
    }

    public boolean isPremium() {
        return hasRole(UserRole.ROLE_PREMIUM) || isAdmin();
    }

    // Метод для блокировки аккаунта
    public void lockAccount() {
        this.accountLocked = true;
        this.lockTime = LocalDateTime.now();
    }

    // Метод для разблокировки аккаунта
    public void unlockAccount() {
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
        this.lockTime = null;
    }

    // Метод для увеличения счетчика неудачных попыток входа
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount();
        }
    }

    // Метод для успешного входа
    public void successfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockTime = null;
    }

    // Метод для проверки, истекло ли время блокировки (1 час)
    public boolean isLockExpired() {
        if (lockTime == null) return false;
        return LocalDateTime.now().isAfter(lockTime.plusHours(1));
    }

    // Метод для проверки лимитов
    public boolean canAddExchangeConnection(int currentConnections) {
        return currentConnections < maxExchangeConnections || isPremium();
    }

    public boolean canImportTransactions(int currentTransactions) {
        return currentTransactions < maxTransactionsPerYear || isPremium();
    }
}