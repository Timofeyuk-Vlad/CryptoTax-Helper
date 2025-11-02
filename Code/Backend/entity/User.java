package com.example.lowflightzone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email обязателен")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 100, message = "Пароль должен быть не менее 8 символов")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Пароль должен содержать цифру, заглавную букву, строчную букву и спецсимвол"
    )
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Size(max = 50, message = "Имя должно быть не длиннее 50 символов")
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50, message = "Фамилия должна быть не длиннее 50 символов")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Size(max = 20, message = "Телефон не должен быть длиннее 20 символов")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlightSubscription> subscriptions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
