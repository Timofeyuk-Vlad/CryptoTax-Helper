package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TaxCalculationService taxCalculationService;

    public byte[] generateTaxReportExcel(Long userId, int year) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            TaxCalculationResultDto taxResult = taxCalculationService.calculateTaxes(userId, year);
            List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(user);

            // Создаем листы
            createSummarySheet(workbook, user, taxResult, year);
            createTransactionsSheet(workbook, transactions);
            createTaxCalculationSheet(workbook, taxResult, year);

            workbook.write(outputStream);
            log.info("Excel отчет успешно сгенерирован для пользователя {} за {} год", userId, year);

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при генерации Excel отчета: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка генерации Excel отчета", e);
        }
    }

    private void createSummarySheet(Workbook workbook, User user, TaxCalculationResultDto taxResult, int year) {
        Sheet sheet = workbook.createSheet("Налоговая сводка");

        // Стили
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = createBoldStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Заголовок
        Row headerRow = sheet.createRow(rowNum++);
        createCell(headerRow, 0, "Налоговый отчет по операциям с цифровыми активами", headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

        rowNum++; // Пропуск строки

        // Информация о пользователе
        Row userInfoRow = sheet.createRow(rowNum++);
        createCell(userInfoRow, 0, "Информация о налогоплательщике:", boldStyle);

        Row userNameRow = sheet.createRow(rowNum++);
        createCell(userNameRow, 0, "ФИО:");
        createCell(userNameRow, 1, user.getFirstName() + " " + user.getLastName());

        Row userEmailRow = sheet.createRow(rowNum++);
        createCell(userEmailRow, 0, "Email:");
        createCell(userEmailRow, 1, user.getEmail());

        Row yearRow = sheet.createRow(rowNum++);
        createCell(yearRow, 0, "Отчетный год:");
        createCell(yearRow, 1, String.valueOf(year));

        rowNum++; // Пропуск строки

        // Налоговая сводка
        Row taxHeaderRow = sheet.createRow(rowNum++);
        createCell(taxHeaderRow, 0, "Налоговая сводка:", boldStyle);

        Row incomeRow = sheet.createRow(rowNum++);
        createCell(incomeRow, 0, "Общий доход:");
        createCell(incomeRow, 1, taxResult.getTotalIncome(), currencyStyle);
        createCell(incomeRow, 2, "RUB");

        Row expensesRow = sheet.createRow(rowNum++);
        createCell(expensesRow, 0, "Общие расходы:");
        createCell(expensesRow, 1, taxResult.getTotalExpenses(), currencyStyle);
        createCell(expensesRow, 2, "RUB");

        Row profitRow = sheet.createRow(rowNum++);
        createCell(profitRow, 0, "Налогооблагаемая прибыль:");
        createCell(profitRow, 1, taxResult.getTaxableProfit(), currencyStyle);
        createCell(profitRow, 2, "RUB");

        Row taxRow = sheet.createRow(rowNum++);
        createCell(taxRow, 0, "Сумма налога к уплате:", boldStyle);
        createCell(taxRow, 1, taxResult.getTaxAmount(), currencyStyle);
        createCell(taxRow, 2, "RUB");

        Row countRow = sheet.createRow(rowNum);
        createCell(countRow, 0, "Количество операций:");
        createCell(countRow, 1, String.valueOf(taxResult.getTransactionCount()));

        // Автоподбор ширины колонок
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTransactionsSheet(Workbook workbook, List<Transaction> transactions) {
        Sheet sheet = workbook.createSheet("Операции");

        // Стили
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Заголовки таблицы
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Дата", "Тип операции", "Базовая валюта", "Котируемая валюта",
                "Количество", "Цена", "Общая сумма", "Комиссия", "Валюта комиссии"
        };

        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }

        // Данные операций
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Transaction transaction : transactions) {
            Row row = sheet.createRow(rowNum++);

            createCell(row, 0, transaction.getTimestamp().format(formatter), dateStyle);
            createCell(row, 1, transaction.getType().getDisplayName());
            createCell(row, 2, transaction.getBaseAsset());
            createCell(row, 3, transaction.getQuoteAsset());
            createCell(row, 4, transaction.getAmount(), currencyStyle);
            createCell(row, 5, transaction.getPrice(), currencyStyle);
            createCell(row, 6, transaction.getTotal(), currencyStyle);
            createCell(row, 7, transaction.getFee(), currencyStyle);
            createCell(row, 8, transaction.getFeeAsset());
        }

        // Автоподбор ширины колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTaxCalculationSheet(Workbook workbook, TaxCalculationResultDto taxResult, int year) {
        Sheet sheet = workbook.createSheet("Расчет налога");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = createBoldStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Заголовок
        Row headerRow = sheet.createRow(rowNum++);
        createCell(headerRow, 0, "Детализация расчета налога", headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

        rowNum++; // Пропуск строки

        // Метод расчета
        Row methodRow = sheet.createRow(rowNum++);
        createCell(methodRow, 0, "Метод расчета:", boldStyle);
        createCell(methodRow, 1, "FIFO (First-In, First-Out)");

        // Налоговая ставка
        Row rateRow = sheet.createRow(rowNum++);
        createCell(rateRow, 0, "Налоговая ставка:", boldStyle);

        if (taxResult.getTaxableProfit().compareTo(new BigDecimal("2400000")) <= 0) {
            createCell(rateRow, 1, "13% (до 2,400,000 RUB)");
        } else {
            createCell(rateRow, 1, "13% до 2,400,000 RUB + 15% свыше");
        }

        rowNum++; // Пропуск строки

        // Формулы расчета
        Row formulaHeader = sheet.createRow(rowNum++);
        createCell(formulaHeader, 0, "Формулы расчета:", boldStyle);

        Row formula1 = sheet.createRow(rowNum++);
        createCell(formula1, 0, "Налогооблагаемая прибыль =");
        createCell(formula1, 1, "Общий доход - Общие расходы");

        Row formula2 = sheet.createRow(rowNum++);
        createCell(formula2, 0, "Сумма налога =");
        createCell(formula2, 1, "Налогооблагаемая прибыль × Налоговая ставка");

        rowNum++; // Пропуск строки

        // Рекомендации
        Row recommendationsHeader = sheet.createRow(rowNum++);
        createCell(recommendationsHeader, 0, "Рекомендации:", boldStyle);

        Row rec1 = sheet.createRow(rowNum++);
        createCell(rec1, 0, "• Подать налоговую декларацию до 30 апреля " + (year + 1));

        Row rec2 = sheet.createRow(rowNum++);
        createCell(rec2, 0, "• Сохранить этот отчет для налоговых органов");

        Row rec3 = sheet.createRow(rowNum);
        createCell(rec3, 0, "• Проконсультироваться с налоговым специалистом");

        // Автоподбор ширины колонок
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Вспомогательные методы для создания стилей
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy hh:mm"));
        return style;
    }

    // Вспомогательные методы для создания ячеек
    private void createCell(Row row, int column, String value) {
        createCell(row, column, value, null);
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void createCell(Row row, int column, BigDecimal value) {
        createCell(row, column, value, null);
    }

    private void createCell(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue(0);
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void createCell(Row row, int column, int value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
    }
}