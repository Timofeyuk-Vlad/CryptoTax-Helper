package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.ExchangeConnectionDto;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.ExchangeConnectionRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeConnectionService {

    private final ExchangeConnectionRepository exchangeConnectionRepository;
    private final UserRepository userRepository;

    // В реальном приложении используйте безопасное хранение ключей!
    private final String encryptionPassword = "temp-encryption-key";
    private final String encryptionSalt = KeyGenerators.string().generateKey();

    public ExchangeConnection createConnection(Long userId, ExchangeConnectionDto connectionDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (exchangeConnectionRepository.existsByUserAndExchange(user, connectionDto.getExchange())) {
            throw new RuntimeException("Подключение к этой бирже уже существует");
        }

        TextEncryptor encryptor = Encryptors.text(encryptionPassword, encryptionSalt);

        ExchangeConnection connection = new ExchangeConnection();
        connection.setUser(user);
        connection.setExchange(connectionDto.getExchange());

        // Шифруем API ключи
        if (connectionDto.getApiKey() != null) {
            connection.setApiKey(encryptor.encrypt(connectionDto.getApiKey()));
        }
        if (connectionDto.getApiSecret() != null) {
            connection.setApiSecret(encryptor.encrypt(connectionDto.getApiSecret()));
        }

        connection.setIsActive(connectionDto.getIsActive());
        connection.setSyncStatus("NOT_SYNCED");

        return exchangeConnectionRepository.save(connection);
    }

    public List<ExchangeConnection> getUserConnections(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return exchangeConnectionRepository.findByUserAndIsActiveTrue(user);
    }

    public void deleteConnection(Long userId, Long connectionId) {
        ExchangeConnection connection = exchangeConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Подключение не найдено"));

        if (!connection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ запрещен");
        }

        exchangeConnectionRepository.delete(connection);
    }

    public void updateLastSync(Long connectionId) {
        ExchangeConnection connection = exchangeConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Подключение не найдено"));

        connection.setLastSync(LocalDateTime.now());
        connection.setSyncStatus("SYNCED");

        exchangeConnectionRepository.save(connection);
    }
}