package com.example.lowflightzone.services;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PushNotificationService {

    private final PushService pushService;

    public PushNotificationService(
            @Value("${vapid.public.key}") String publicKey,
            @Value("${vapid.private.key}") String privateKey,
            @Value("${vapid.subject}") String subject
    ) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    /**
     * Отправка push-уведомления через Web Push API с подробным логированием.
     */
    public boolean sendPush(String endpoint, String p256dh, String auth, String payload) {
        try {
            Notification notification = new Notification(endpoint, p256dh, auth, payload);
            var response = pushService.send(notification);

            // 📜 Подробный лог
            log.info("📨 Push отправлен:");
            log.info("   ➤ Endpoint: {}", endpoint);
            log.info("   ➤ Payload: {}", payload);
            log.info("   ➤ Status line: {}", response.getStatusLine());
            log.info("   ➤ Headers: {}", response.getAllHeaders().length > 0 ? response.getAllHeaders()[0] : "—");

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 404) {
                log.warn("❗️ Подписка недействительна (404). Клиент мог отписаться или endpoint устарел.");
            } else if (statusCode >= 400) {
                log.error("❌ Ошибка при отправке push. Код: {}", statusCode);
            } else {
                log.info("✅ Push доставлен успешно ({}).", statusCode);
            }

            return statusCode >= 200 && statusCode < 300;

        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
            log.error("❌ Ошибка при отправке push: {}", e.getMessage(), e);
            return false;
        }
    }
}
