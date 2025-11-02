package com.cryptotax.helper.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final ReportService reportService;

    public byte[] generateTaxReportPdf(Long userId, int year) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Map<String, Object> reportData = reportService.generateTaxReport(userId, year);

            // Создаем PDF документ
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Добавляем содержимое
            addHeader(document, year);
            addTaxpayerInfo(document, reportData);
            addTaxSummary(document, reportData);
            addTransactionsSummary(document, reportData);
            addRecommendations(document, reportData);
            addFooter(document);

            document.close();

            log.info("PDF отчет успешно сгенерирован для пользователя {} за {} год", userId, year);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Ошибка при генерации PDF отчета: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка генерации PDF отчета", e);
        }
    }

    private void addHeader(Document document, int year) {
        // Заголовок документа
        Paragraph header = new Paragraph("Налоговый отчет по операциям с цифровыми активами")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16);
        document.add(header);

        Paragraph subHeader = new Paragraph("За " + year + " налоговый год")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginBottom(20);
        document.add(subHeader);

        Paragraph generationDate = new Paragraph(
                "Сгенерировано: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(15);
        document.add(generationDate);
    }

    @SuppressWarnings("unchecked")
    private void addTaxpayerInfo(Document document, Map<String, Object> reportData) {
        Paragraph sectionHeader = new Paragraph("1. Информация о налогоплательщике")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(sectionHeader);

        Map<String, Object> taxpayerInfo = (Map<String, Object>) reportData.get("taxpayerInfo");

        document.add(new Paragraph("ФИО: " + taxpayerInfo.get("firstName") + " " + taxpayerInfo.get("lastName")));
        document.add(new Paragraph("Email: " + taxpayerInfo.get("email")));
        document.add(new Paragraph("ИНН: " + taxpayerInfo.get("taxIdentificationNumber")));

        document.add(new Paragraph("\n"));
    }

    @SuppressWarnings("unchecked")
    private void addTaxSummary(Document document, Map<String, Object> reportData) {
        Paragraph sectionHeader = new Paragraph("2. Налоговая сводка")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(sectionHeader);

        Map<String, Object> taxData = (Map<String, Object>) reportData.get("taxData");

        document.add(new Paragraph("Общий доход: " + formatCurrency(taxData.get("totalIncome")) + " RUB"));
        document.add(new Paragraph("Общие расходы: " + formatCurrency(taxData.get("totalExpenses")) + " RUB"));
        document.add(new Paragraph("Налогооблагаемая прибыль: " + formatCurrency(taxData.get("taxableProfit")) + " RUB"));

        Paragraph taxAmount = new Paragraph("Сумма налога к уплате: " + formatCurrency(taxData.get("taxAmount")) + " RUB")
                .setBold()
                .setFontSize(11);
        document.add(taxAmount);

        document.add(new Paragraph("\n"));
    }

    @SuppressWarnings("unchecked")
    private void addTransactionsSummary(Document document, Map<String, Object> reportData) {
        Paragraph sectionHeader = new Paragraph("3. Сводка по операциям")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(sectionHeader);

        Map<String, Object> taxData = (Map<String, Object>) reportData.get("taxData");
        Map<String, Object> taxCalculation = (Map<String, Object>) reportData.get("taxCalculation");

        document.add(new Paragraph("Количество операций: " + taxData.get("transactionCount")));
        document.add(new Paragraph("Метод расчета: " + taxCalculation.get("calculationMethod")));
        document.add(new Paragraph("Налоговая ставка: " + taxCalculation.get("taxRate")));
        document.add(new Paragraph("Отчетный период: " + reportData.get("reportYear") + " год"));

        document.add(new Paragraph("\n"));
    }

    @SuppressWarnings("unchecked")
    private void addRecommendations(Document document, Map<String, Object> reportData) {
        Paragraph sectionHeader = new Paragraph("4. Рекомендации")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(sectionHeader);

        Map<String, Object> recommendations = (Map<String, Object>) reportData.get("recommendations");

        document.add(new Paragraph(recommendations.get("message").toString()));

        if (Boolean.TRUE.equals(recommendations.get("actionRequired"))) {
            document.add(new Paragraph("Срок подачи декларации: " + recommendations.get("deadline")));

            Paragraph docsHeader = new Paragraph("Необходимые документы:")
                    .setMarginTop(5f);
            document.add(docsHeader);

            java.util.List<String> documents = (java.util.List<String>) recommendations.get("documents");
            for (String doc : documents) {
                document.add(new Paragraph("• " + doc));
            }
        }

        document.add(new Paragraph("\n"));
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph(
                "Данный отчет сгенерирован автоматически системой CryptoTax Helper. " +
                        "Рекомендуется проконсультироваться с налоговым специалистом перед подачей документов в ФНС.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9)
                .setItalic()
                .setMarginTop(20f);
        document.add(footer);
    }

    private String formatCurrency(Object amount) {
        if (amount instanceof java.math.BigDecimal) {
            java.math.BigDecimal decimal = (java.math.BigDecimal) amount;
            return String.format("%,.2f", decimal);
        }
        return amount.toString();
    }

    // Альтернативный метод с HTML шаблоном (более продвинутый)
    public byte[] generateHtmlTaxReportPdf(Long userId, int year) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Map<String, Object> reportData = reportService.generateTaxReport(userId, year);

            String htmlContent = generateHtmlTemplate(reportData);
            HtmlConverter.convertToPdf(htmlContent, outputStream);

            log.info("HTML PDF отчет успешно сгенерирован для пользователя {} за {} год", userId, year);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Ошибка при генерации HTML PDF отчета: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка генерации HTML PDF отчета", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String generateHtmlTemplate(Map<String, Object> reportData) {
        Map<String, Object> taxpayerInfo = (Map<String, Object>) reportData.get("taxpayerInfo");
        Map<String, Object> taxData = (Map<String, Object>) reportData.get("taxData");
        Map<String, Object> recommendations = (Map<String, Object>) reportData.get("recommendations");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .section { margin-bottom: 25px; }
                    .section-title { font-weight: bold; font-size: 14px; margin-bottom: 10px; border-bottom: 1px solid #ccc; padding-bottom: 5px; }
                    .tax-amount { font-weight: bold; color: #d32f2f; font-size: 13px; }
                    .footer { margin-top: 40px; font-style: italic; font-size: 10px; text-align: center; color: #666; }
                    table { width: 100%; border-collapse: collapse; margin: 10px 0; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f5f5f5; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Налоговый отчет по операциям с цифровыми активами</h1>
                    <h2>За %d налоговый год</h2>
                    <p><em>Сгенерировано: %s</em></p>
                </div>
                
                <div class="section">
                    <div class="section-title">1. Информация о налогоплательщике</div>
                    <table>
                        <tr><td><strong>ФИО:</strong></td><td>%s %s</td></tr>
                        <tr><td><strong>Email:</strong></td><td>%s</td></tr>
                        <tr><td><strong>ИНН:</strong></td><td>%s</td></tr>
                    </table>
                </div>
                
                <div class="section">
                    <div class="section-title">2. Налоговая сводка</div>
                    <table>
                        <tr><td>Общий доход:</td><td>%s RUB</td></tr>
                        <tr><td>Общие расходы:</td><td>%s RUB</td></tr>
                        <tr><td>Налогооблагаемая прибыль:</td><td>%s RUB</td></tr>
                        <tr class="tax-amount"><td><strong>Сумма налога к уплате:</strong></td><td><strong>%s RUB</strong></td></tr>
                    </table>
                </div>
                
                <div class="section">
                    <div class="section-title">3. Сводка по операциям</div>
                    <p>Количество операций: %d</p>
                    <p>Метод расчета: FIFO</p>
                    <p>Налоговая ставка: 13%%</p>
                </div>
                
                <div class="section">
                    <div class="section-title">4. Рекомендации</div>
                    <p>%s</p>
                    %s
                </div>
                
                <div class="footer">
                    Данный отчет сгенерирован автоматически системой CryptoTax Helper.<br>
                    Рекомендуется проконсультироваться с налоговым специалистом перед подачей документов в ФНС.
                </div>
            </body>
            </html>
            """.formatted(
                reportData.get("reportYear"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                taxpayerInfo.get("firstName"),
                taxpayerInfo.get("lastName"),
                taxpayerInfo.get("email"),
                taxpayerInfo.get("taxIdentificationNumber"),
                formatCurrency(taxData.get("totalIncome")),
                formatCurrency(taxData.get("totalExpenses")),
                formatCurrency(taxData.get("taxableProfit")),
                formatCurrency(taxData.get("taxAmount")),
                taxData.get("transactionCount"),
                recommendations.get("message"),
                Boolean.TRUE.equals(recommendations.get("actionRequired")) ?
                        "<p><strong>Срок подачи декларации:</strong> " + recommendations.get("deadline") + "</p>" : ""
        );
    }
}