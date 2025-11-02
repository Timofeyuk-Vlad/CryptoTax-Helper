package com.cryptotax.helper.controller;

import com.cryptotax.helper.dto.TaxProfileDto;
import com.cryptotax.helper.entity.TaxProfile;
import com.cryptotax.helper.service.TaxProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tax-profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaxProfileController {

    private final TaxProfileService taxProfileService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateProfile(@Valid @RequestBody TaxProfileDto profileDto) {
        try {
            // TODO: Добавить аутентификацию и получение userId из токена
            Long userId = 7L; // Временная заглушка

            TaxProfile profile = taxProfileService.createOrUpdateProfile(userId, profileDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Налоговый профиль успешно сохранен");
            response.put("profileId", profile.getId());
            response.put("country", profile.getCountry());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        try {
            // TODO: Добавить аутентификацию
            Long userId = 7L; // Временная заглушка

            TaxProfile profile = taxProfileService.getProfileByUserId(userId);

            return ResponseEntity.ok(profile);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}