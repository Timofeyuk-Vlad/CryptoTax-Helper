// com.example.lowflightzone.services.NotificationService
package com.example.lowflightzone.services;

import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.entity.FlightSubscription;
import com.example.lowflightzone.repositories.FlightSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.lowflightzone.entity.FlightSubscription.SubscriptionStatus.ACTIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PushNotificationService push;
    private final FlightSubscriptionRepository subsRepo;

    public void notifySubscribersAboutFlightUpdate(Flight flight) {
        List<FlightSubscription> subs = subsRepo.findAllByFlightIdAndStatus(flight.getId(), ACTIVE);
        if (subs.isEmpty()) {
            log.info("No active subs for flight {}", flight.getFlightNumber());
            return;
        }

        // минимальный JSON для service worker (title/body/click)
        String payload = """
        {
          "title": "Рейс %s",
          "body":  "Статус: %s",
          "clickUrl": "/flights/%d"
        }
        """.formatted(flight.getFlightNumber(), flight.getStatus(), flight.getId());

        for (FlightSubscription s : subs) {
            if (isBlank(s.getEndpoint()) || isBlank(s.getP256dh()) || isBlank(s.getAuth())) {
                // нет браузерной подписки — пропускаем
                continue;
            }
            boolean ok = push.sendPush(s.getEndpoint(), s.getP256dh(), s.getAuth(), payload);
            if (!ok) {
                // Можно пометить подписку как INACTIVE или почистить «мертвые»
                log.warn("Push failed for sub id={}", s.getId());
            }
        }
    }

    public void sendSubscriptionConfirmation(FlightSubscription subscription) {
        if (isBlank(subscription.getEndpoint()) || isBlank(subscription.getP256dh()) || isBlank(subscription.getAuth())) {
            log.warn("❌ Не удалось отправить подтверждение — нет ключей WebPush");
            return;
        }

        String payload = """
    {
      "title": "Подписка активирована",
      "body": "Вы будете получать уведомления об изменениях рейса %s"
    }
    """.formatted(subscription.getFlight().getFlightNumber());

        push.sendPush(subscription.getEndpoint(), subscription.getP256dh(), subscription.getAuth(), payload);
    }


    private boolean isBlank(String v) { return v == null || v.isBlank(); }
}
