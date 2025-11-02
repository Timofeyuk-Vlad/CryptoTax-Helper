package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxProfileDto;
import com.cryptotax.helper.entity.TaxProfile;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TaxProfileRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxProfileService {

    private final TaxProfileRepository taxProfileRepository;
    private final UserRepository userRepository;

    public TaxProfile createOrUpdateProfile(Long userId, TaxProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        TaxProfile profile = taxProfileRepository.findByUser(user)
                .orElse(new TaxProfile());

        profile.setUser(user);
        profile.setCountry(profileDto.getCountry());
        profile.setTaxIdentificationNumber(profileDto.getTaxIdentificationNumber());
        profile.setExpectedAnnualIncome(profileDto.getExpectedAnnualIncome());
        profile.setApplyTaxFreeAllowance(profileDto.getApplyTaxFreeAllowance());
        profile.setBankAccountNumber(profileDto.getBankAccountNumber());

        return taxProfileRepository.save(profile);
    }

    public TaxProfile getProfileByUserId(Long userId) {
        return taxProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Налоговый профиль не найден"));
    }
}