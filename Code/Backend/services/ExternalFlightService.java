package com.example.lowflightzone.services;

import com.example.lowflightzone.dto.FlightDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j // ✅ подключает логгер log.info()
@Service
public class ExternalFlightService {

    @Value("${aviationstack.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public FlightDto fetchFlightByNumber(String flightNumber) {
        String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&flight_iata=" + flightNumber;

        // ✅ Заголовки, чтобы Cloudflare не блокировал
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/json");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Connection", "keep-alive");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("🌍 Отправляем запрос к AviationStack: {}", url);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.info("📩 Статус ответа: {}", response.getStatusCode());
        log.info("📦 Тело ответа: {}", response.getBody());

        String body = response.getBody();
        if (body != null && body.trim().startsWith("<")) {
            throw new RuntimeException("❌ AviationStack вернул HTML — Cloudflare заблокировал запрос.");
        }

        JSONObject root = new JSONObject(body);
        JSONArray data = root.getJSONArray("data");

        if (data.isEmpty()) {
            throw new RuntimeException("Рейс не найден: " + flightNumber);
        }

        JSONObject flightData = data.getJSONObject(0);

        FlightDto dto = new FlightDto();
        dto.setFlightNumber(flightData.getJSONObject("flight").optString("iata"));
        dto.setAirline(flightData.getJSONObject("airline").optString("name"));
        dto.setStatus(flightData.optString("flight_status", "UNKNOWN"));

        JSONObject departure = flightData.optJSONObject("departure");
        if (departure != null) {
            String scheduled = departure.optString("scheduled");
            if (!scheduled.isEmpty()) {
                dto.setScheduledDeparture(LocalDateTime.parse(scheduled, DateTimeFormatter.ISO_DATE_TIME));
            }
        }

        JSONObject arrival = flightData.optJSONObject("arrival");
        if (arrival != null) {
            String scheduled = arrival.optString("scheduled");
            if (!scheduled.isEmpty()) {
                dto.setScheduledArrival(LocalDateTime.parse(scheduled, DateTimeFormatter.ISO_DATE_TIME));
            }
        }

        log.info("✅ DTO успешно сформирован: {}", dto);
        return dto;
    }
}
