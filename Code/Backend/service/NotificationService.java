package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.*;
import com.cryptotax.helper.repository.NotificationRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public Notification createNotification(Long userId, NotificationType type, String title, String message) {
        return createNotification(userId, type, title, message, false, null, null);
    }

    public Notification createNotification(Long userId, NotificationType type, String title, String message,
                                           boolean isImportant, String actionUrl, String actionText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Notification notification = new Notification(user, type, title, message);
        notification.setIsImportant(isImportant);
        notification.setActionUrl(actionUrl);
        notification.setActionText(actionText);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Создано уведомление для пользователя {}: {}", user.getEmail(), title);

        return savedNotification;
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ запрещен к этому уведомлению");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        notificationRepository.markAllAsReadByUser(user);
        log.info("Все уведомления пользователя {} помечены как прочитанные", user.getEmail());
    }

    // Автоматические напоминания о налоговых сроках
    @Scheduled(cron = "0 0 9 * * ?") // Каждый день в 9:00
    public void checkTaxDeadlines() {
        log.info("Проверка налоговых сроков...");

        List<User> users = userRepository.findAll();
        int currentYear = LocalDate.now().getYear();

        for (User user : users) {
            try {
                checkUserTaxDeadlines(user, currentYear);
            } catch (Exception e) {
                log.error("Ошибка при проверке налоговых сроков для пользователя {}: {}",
                        user.getEmail(), e.getMessage());
            }
        }
    }

    private void checkUserTaxDeadlines(User user, int currentYear) {
        LocalDate now = LocalDate.now();
        LocalDate taxDeadline = LocalDate.of(currentYear, 4, 30); // 30 апреля

        long daysUntilDeadline = ChronoUnit.DAYS.between(now, taxDeadline);

        if (daysUntilDeadline == 30) {
            createTaxDeadlineNotification(user.getId(), 30, currentYear);
        } else if (daysUntilDeadline == 7) {
            createTaxDeadlineNotification(user.getId(), 7, currentYear);
        } else if (daysUntilDeadline == 1) {
            createTaxDeadlineNotification(user.getId(), 1, currentYear);
        } else if (daysUntilDeadline < 0) {
            createTaxDeadlineNotification(user.getId(), -1, currentYear);
        }
    }

    private void createTaxDeadlineNotification(Long userId, int daysLeft, int year) {
        String title, message, actionUrl = "/reports/tax", actionText = "Сгенерировать отчет";

        if (daysLeft == 30) {
            title = "Напоминание о налоговом сроке";
            message = String.format("До подачи налоговой декларации за %d год остался 1 месяц. Рекомендуем подготовить документы заранее.", year - 1);
        } else if (daysLeft == 7) {
            title = "Скоро налоговый срок!";
            message = String.format("До подачи налоговой декларации за %d год осталась 1 неделя. Не забудьте подать документы до 30 апреля.", year - 1);
        } else if (daysLeft == 1) {
            title = "Последний день для подачи налоговой декларации!";
            message = String.format("Завтра, 30 апреля, последний день для подачи налоговой декларации за %d год.", year - 1);
            actionText = "Срочно сгенерировать отчет";
        } else if (daysLeft == -1) {
            title = "Просрочка подачи налоговой декларации";
            message = String.format("Вы пропустили срок подачи налоговой декларации за %d год. Рекомендуем подать документы как можно скорее.", year - 1);
            actionText = "Сгенерировать отчет";
        } else {
            return;
        }

        createNotification(userId, NotificationType.TAX_DEADLINE_REMINDER, title, message, true, actionUrl, actionText);
    }

    // Уведомления об успешном импорте транзакций
    public void notifyImportSuccess(Long userId, int importedCount, String exchangeName) {
        String title = "Импорт транзакций завершен";
        String message = String.format("Успешно импортировано %d транзакций с биржи %s", importedCount, exchangeName);

        createNotification(userId, NotificationType.TRANSACTION_IMPORT_SUCCESS, title, message,
                false, "/transactions", "Посмотреть транзакции");
    }

    // Уведомления об ошибках импорта
    public void notifyImportError(Long userId, String exchangeName, String error) {
        String title = "Ошибка импорта транзакций";
        String message = String.format("Не удалось импортировать транзакции с биржи %s: %s", exchangeName, error);

        createNotification(userId, NotificationType.TRANSACTION_IMPORT_ERROR, title, message,
                true, "/exchange/connections", "Проверить подключение");
    }

    // Уведомления о готовности расчета налогов
    public void notifyTaxCalculationReady(Long userId, int year, String taxAmount) {
        String title = "Расчет налогов готов";
        String message = String.format("Расчет налогов за %d год завершен. Сумма к уплате: %s RUB", year, taxAmount);

        createNotification(userId, NotificationType.TAX_CALCULATION_READY, title, message,
                true, "/reports/tax", "Посмотреть отчет");
    }

    // Очистка старых уведомлений (раз в неделю)
    @Scheduled(cron = "0 0 2 * * 0") // Каждое воскресенье в 2:00
    public void cleanupOldNotifications() {
        log.info("Очистка старых уведомлений...");

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                notificationRepository.deleteOldNotifications(user, threeMonthsAgo);
            } catch (Exception e) {
                log.error("Ошибка при очистке уведомлений пользователя {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Очистка старых уведомлений завершена");
    }
}