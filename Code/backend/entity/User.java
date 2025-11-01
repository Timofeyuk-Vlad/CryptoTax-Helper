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

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    // ✅ Убедимся что эти поля правильно инициализированы:
    @Column(name = "is_2fa_enabled", nullable = false)
    private Boolean is2faEnabled = false; // По умолчанию false

    @Column(name = "secret_2fa", length = 32)
    private String secret2fa; // Может быть null

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
        this.is2faEnabled = false; // ✅ Явно инициализируем
        this.emailVerified = false;
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
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
}