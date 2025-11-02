package com.cryptotax.helper.entity;

public enum NotificationType {
    TAX_DEADLINE_REMINDER("Напоминание о налоговом сроке"),
    TRANSACTION_IMPORT_SUCCESS("Успешный импорт транзакций"),
    TRANSACTION_IMPORT_ERROR("Ошибка импорта транзакций"),
    TAX_CALCULATION_READY("Расчет налогов готов"),
    SECURITY_ALERT("Оповещение безопасности"),
    SYSTEM_ANNOUNCEMENT("Системное уведомление"),
    EXCHANGE_CONNECTION_ISSUE("Проблема с подключением к бирже"),
    PORTFOLIO_UPDATE("Обновление портфеля");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}