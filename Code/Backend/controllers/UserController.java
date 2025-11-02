package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.UserDto;
import com.example.lowflightzone.dto.WebPushSubscriptionDto;
import com.example.lowflightzone.security.SecurityUtils;
import com.example.lowflightzone.services.UserService;
import com.example.lowflightzone.services.FlightSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;
    private final FlightSubscriptionService flightSubscriptionService;

    @Autowired
    public UserController(UserService userService, SecurityUtils securityUtils, FlightSubscriptionService flightSubscriptionService) {
        this.userService = userService;
        this.securityUtils = securityUtils;
        this.flightSubscriptionService = flightSubscriptionService;
    }

    @Operation(summary = "Получить всех пользователей")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Получить пользователя по email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

//   @Operation(summary = "Создать нового пользователя")
//   @PostMapping
//   public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
//       UserDto newUser = userService.createUser(userDto);
//       return ResponseEntity.ok(newUser);
//   }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Пользователь успешно удален");
    }

    @PostMapping("/device-token")
    public ResponseEntity<?> saveDeviceToken(@RequestParam String token) {
        Integer userId = securityUtils.getCurrentUserIdOrThrow();
        userService.updateDeviceToken(userId, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/device-subscription")
    public ResponseEntity<?> updateWebPushSubscription(
            @RequestBody WebPushSubscriptionDto dto
    ) {
        Integer userId = securityUtils.getCurrentUserIdOrThrow();
        flightSubscriptionService.updateWebPushSubscription(userId, dto.getEndpoint(), dto.getP256dh(), dto.getAuth());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Integer id,
            @RequestBody UserDto updatedUser
    ) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }
}