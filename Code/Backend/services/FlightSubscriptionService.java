package com.example.lowflightzone.services;

import com.example.lowflightzone.dao.FlightDao;
import com.example.lowflightzone.dao.FlightSubscriptionDao;
import com.example.lowflightzone.dao.UserDao;
import com.example.lowflightzone.dto.FlightDto;
import com.example.lowflightzone.dto.FlightSubscriptionDto;
import com.example.lowflightzone.dto.UserDto;
import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.entity.FlightSubscription;
import com.example.lowflightzone.entity.User;
import com.example.lowflightzone.exceptions.FlightException;
import com.example.lowflightzone.exceptions.SubscriptionException;
import com.example.lowflightzone.repositories.FlightSubscriptionRepository;
import com.example.lowflightzone.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightSubscriptionService {

    private final FlightSubscriptionDao subscriptionDao;
    private final FlightDao flightDao;
    private final NotificationService notificationService;
    private final UserDao userDao;
    private final SecurityUtils securityUtils;
    private final FlightSubscriptionRepository flightSubscriptionRepository;

    @Autowired
    public FlightSubscriptionService(FlightSubscriptionDao subscriptionDao,
                                     FlightDao flightDao,
                                     NotificationService notificationService,
                                     UserDao userDao,
                                     SecurityUtils securityUtils, FlightSubscriptionRepository flightSubscriptionRepository) {
        this.subscriptionDao = subscriptionDao;
        this.flightDao = flightDao;
        this.notificationService = notificationService;
        this.userDao = userDao;
        this.securityUtils = securityUtils;
        this.flightSubscriptionRepository = flightSubscriptionRepository;
    }

    /**
     * Универсальная подписка: по flightId или flightNumber.
     * ✅ Поведение: если была подписка со статусом CANCELLED — меняем её на ACTIVE (реактивация),
     *               если активной нет и прошлых не было — создаём новую.
     */
    public FlightSubscriptionDto subscribeFlexible(
            Integer flightId,
            String flightNumber,
            String endpoint,
            String p256dh,
            String auth
    ) {
        // 1️⃣ Определяем flightNumber
        String resolvedFlightNumber = flightNumber;
        if (resolvedFlightNumber == null && flightId != null) {
            Flight flight = flightDao.findById(flightId)
                    .orElseThrow(() -> new FlightException("Рейс не найден: id=" + flightId));
            resolvedFlightNumber = flight.getFlightNumber();
        }
        if (resolvedFlightNumber == null || resolvedFlightNumber.isBlank()) {
            throw new SubscriptionException("Не передан flightId или flightNumber");
        }
        final String finalFlightNumber = resolvedFlightNumber;

        // 2️⃣ Текущий пользователь
        User current = securityUtils.getCurrentUserOrThrow();
        String userEmail = current.getEmail();

        // 3️⃣ Проверка на уже существующую активную подписку
        if (subscriptionDao.existsActiveByFlightAndUser(finalFlightNumber, userEmail)) {
            throw new SubscriptionException("Активная подписка уже существует для пользователя: " + userEmail);
        }

        // 4️⃣ Получаем сущности рейса и пользователя
        Flight flight = flightDao.findByFlightNumber(finalFlightNumber)
                .orElseThrow(() -> new FlightException("Рейс не найден: " + finalFlightNumber));

        User user = userDao.findByEmail(userEmail)
                .orElseThrow(() -> new SubscriptionException("Пользователь не найден: " + userEmail));

        // 5️⃣ Реактивация или новая подписка
        Optional<FlightSubscription> latestOpt =
                subscriptionDao.findLatestByFlightNumberAndUserEmail(finalFlightNumber, userEmail);

        FlightSubscription saved;

        if (latestOpt.isPresent()) {
            // ✅ Реактивация старой подписки
            FlightSubscription existing = latestOpt.get();
            existing.setStatus(FlightSubscription.SubscriptionStatus.ACTIVE);
            existing.setNotificationTypes("DELAY,CANCELLATION,STATUS_CHANGE");

            // 💾 Сохраняем Web Push ключи
            if (endpoint != null && !endpoint.isBlank()) existing.setEndpoint(endpoint);
            if (p256dh != null && !p256dh.isBlank()) existing.setP256dh(p256dh);
            if (auth != null && !auth.isBlank()) existing.setAuth(auth);

            saved = subscriptionDao.save(existing);

        } else {
            // ✨ Создание новой подписки
            FlightSubscription sub = new FlightSubscription();
            sub.setFlight(flight);
            sub.setUser(user);
            sub.setStatus(FlightSubscription.SubscriptionStatus.ACTIVE);
            sub.setNotificationTypes("DELAY,CANCELLATION,STATUS_CHANGE");

            // 💾 Сохраняем Web Push ключи
            if (endpoint != null && !endpoint.isBlank()) sub.setEndpoint(endpoint);
            if (p256dh != null && !p256dh.isBlank()) sub.setP256dh(p256dh);
            if (auth != null && !auth.isBlank()) sub.setAuth(auth);

            saved = subscriptionDao.save(sub);
        }

        // 📬 Отправляем подтверждение подписки
        notificationService.sendSubscriptionConfirmation(saved);

        return convertToDto(saved);
    }

    @Transactional
    public void updateWebPushSubscription(Integer userId, String endpoint, String p256dh, String auth) {
        // Ищем активную подписку пользователя
        FlightSubscription subscription = flightSubscriptionRepository
                .findFirstByUserIdAndStatus(userId, FlightSubscription.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionException("Активная подписка не найдена для пользователя id=" + userId));

        // Обновляем пуш-данные
        if (endpoint != null && !endpoint.isBlank()) subscription.setEndpoint(endpoint);
        if (p256dh != null && !p256dh.isBlank()) subscription.setP256dh(p256dh);
        if (auth != null && !auth.isBlank()) subscription.setAuth(auth);

        subscriptionDao.save(subscription);
    }

    /** Универсальная отписка: subscriptionId или (flightId / flightNumber) для текущего пользователя */
    @Transactional
    public void unsubscribeFlexible(Integer subscriptionId, Integer flightId, String flightNumber) {
        String resolvedFlightNumber = flightNumber;

        if ((resolvedFlightNumber == null || resolvedFlightNumber.isBlank()) && flightId != null) {
            Flight flight = flightDao.findById(flightId)
                    .orElseThrow(() -> new FlightException("Рейс не найден: id=" + flightId));
            resolvedFlightNumber = flight.getFlightNumber();
        }

        if (subscriptionId != null) {
            FlightSubscription sub = subscriptionDao.findAll().stream()
                    .filter(s -> s.getId().equals(subscriptionId))
                    .findFirst()
                    .orElseThrow(() -> new SubscriptionException("Подписка не найдена: id=" + subscriptionId));
            sub.setStatus(FlightSubscription.SubscriptionStatus.CANCELLED);
            subscriptionDao.save(sub);
            return;
        }

        if (resolvedFlightNumber == null || resolvedFlightNumber.isBlank()) {
            throw new SubscriptionException("Не передан subscriptionId или flightId/flightNumber");
        }

        String userEmail = securityUtils.getCurrentUserOrThrow().getEmail();

        final String finalFlightNumber = resolvedFlightNumber;

        FlightSubscription sub = subscriptionDao
                .findActiveByUserEmailAndFlightNumber(userEmail, finalFlightNumber)
                .orElseThrow(() -> new SubscriptionException(
                        "Активная подписка не найдена: " + userEmail + " / " + finalFlightNumber));


        sub.setStatus(FlightSubscription.SubscriptionStatus.CANCELLED);
        subscriptionDao.save(sub);
    }



    // ----- Прочие методы (без изменений по сути) -----

    public List<FlightSubscriptionDto> getUserSubscriptions(String userEmail) {
        return subscriptionDao.findByUserEmail(userEmail).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FlightSubscriptionDto> getSubscriptionsByUserId(Integer userId) {
        return subscriptionDao.findActiveByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FlightSubscriptionDto> getSubscriptionsForFlight(String flightNumber) {
        return subscriptionDao.findByFlightNumberAndStatus(
                        flightNumber, FlightSubscription.SubscriptionStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FlightSubscriptionDto convertToDto(FlightSubscription subscription) {
        FlightSubscriptionDto dto = new FlightSubscriptionDto();
        dto.setId(subscription.getId());

        Flight flight = subscription.getFlight();
        FlightDto flightDto = new FlightDto();
        flightDto.setId(flight.getId());
        flightDto.setFlightNumber(flight.getFlightNumber());
        flightDto.setAirline(flight.getAirline());
        flightDto.setStatus(flight.getStatus() != null ? flight.getStatus().toString() : null);
        flightDto.setScheduledDeparture(flight.getScheduledDeparture());
        flightDto.setScheduledArrival(flight.getScheduledArrival());
        flightDto.setEstimatedDeparture(flight.getEstimatedDeparture());
        flightDto.setEstimatedArrival(flight.getEstimatedArrival());
        dto.setFlight(flightDto);

        User user = subscription.getUser();
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        dto.setUser(userDto);

        dto.setStatus(subscription.getStatus() != null ? subscription.getStatus().toString() : null);
        dto.setNotificationTypes(subscription.getNotificationTypes());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setLastNotifiedAt(subscription.getLastNotifiedAt());
        dto.setNotifyBeforeHours(subscription.getNotifyBeforeHours());
        dto.setMinDelayMinutes(subscription.getMinDelayMinutes());

        return dto;
    }
}
