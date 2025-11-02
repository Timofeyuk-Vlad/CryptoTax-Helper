package com.example.lowflightzone.config;

import com.example.lowflightzone.entity.Airport;
import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.repositories.AirportRepository;
import com.example.lowflightzone.repositories.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 Запуск инициализации тестовых данных...");
        initializeAirports();
        generateTestFlightsIfEmpty();
    }

    /**
     * ✅ Добавляем аэропорты, только если их ещё нет в базе
     */
    private void initializeAirports() {
        Map<String, Airport> predefinedAirports = Map.ofEntries(
                Map.entry("SVO", createAirport("SVO", "Шереметьево", "Москва", "Россия", 55.972642, 37.414589, "Europe/Moscow")),
                Map.entry("DME", createAirport("DME", "Домодедово", "Москва", "Россия", 55.408611, 37.906111, "Europe/Moscow")),
                Map.entry("VKO", createAirport("VKO", "Внуково", "Москва", "Россия", 55.591531, 37.261486, "Europe/Moscow")),
                Map.entry("LED", createAirport("LED", "Пулково", "Санкт-Петербург", "Россия", 59.800292, 30.262503, "Europe/Moscow")),
                Map.entry("AER", createAirport("AER", "Сочи", "Сочи", "Россия", 43.449928, 39.956589, "Europe/Moscow")),
                Map.entry("JFK", createAirport("JFK", "John F. Kennedy", "Нью-Йорк", "США", 40.6413, -73.7781, "America/New_York")),
                Map.entry("LHR", createAirport("LHR", "Heathrow", "Лондон", "Великобритания", 51.4700, -0.4543, "Europe/London")),
                Map.entry("CDG", createAirport("CDG", "Шарль-де-Голль", "Париж", "Франция", 49.0097, 2.5479, "Europe/Paris")),
                Map.entry("DXB", createAirport("DXB", "Dubai International", "Дубай", "ОАЭ", 25.2532, 55.3657, "Asia/Dubai")),
                Map.entry("HND", createAirport("HND", "Haneda", "Токио", "Япония", 35.5494, 139.7798, "Asia/Tokyo")),
                Map.entry("IST", createAirport("IST", "Istanbul", "Стамбул", "Турция", 41.2753, 28.7519, "Europe/Istanbul")),
                Map.entry("SIN", createAirport("SIN", "Changi", "Сингапур", "Сингапур", 1.3644, 103.9915, "Asia/Singapore")),
                Map.entry("LAX", createAirport("LAX", "Los Angeles", "Лос-Анджелес", "США", 33.9416, -118.4085, "America/Los_Angeles"))
        );

        int added = 0;
        for (var entry : predefinedAirports.entrySet()) {
            if (airportRepository.findByIataCode(entry.getKey()).isEmpty()) {
                airportRepository.save(entry.getValue());
                added++;
            }
        }
        log.info("✅ Аэропорты готовы. Новых добавлено: {}", added);
    }

    /**
     * ✅ Генерация рейсов только если их в базе нет
     */
    private void generateTestFlightsIfEmpty() {
        long existingFlights = flightRepository.count();
        if (existingFlights > 0) {
            log.info("✈️ Рейсы уже существуют ({} шт.) — генерация пропущена.", existingFlights);
            return;
        }

        List<Airport> airports = airportRepository.findAll();
        if (airports.size() < 2) {
            log.warn("⚠️ Недостаточно аэропортов для генерации рейсов");
            return;
        }

        Random random = new Random();
        List<Flight> newFlights = new ArrayList<>();
        String[] airlines = {"Аэрофлот", "S7 Airlines", "Уральские авиалинии", "Emirates", "Turkish Airlines",
                "Air France", "Lufthansa", "British Airways", "Japan Airlines", "Delta", "United Airlines"};

        Flight.FlightStatus[] statuses = Flight.FlightStatus.values();

        for (int i = 0; i < 50; i++) {
            Airport departure = airports.get(random.nextInt(airports.size()));
            Airport arrival;
            do {
                arrival = airports.get(random.nextInt(airports.size()));
            } while (arrival.equals(departure));

            int delay = random.nextInt(90); // задержка от 0 до 90 мин
            LocalDateTime scheduledDeparture = LocalDateTime.now().plusHours(random.nextInt(72));
            LocalDateTime scheduledArrival = scheduledDeparture.plusHours(2 + random.nextInt(4));

            Flight flight = new Flight();
            flight.setFlightNumber(generateFlightNumber(random));
            flight.setAirline(airlines[random.nextInt(airlines.length)]);
            flight.setDepartureAirport(departure);
            flight.setArrivalAirport(arrival);
            flight.setScheduledDeparture(scheduledDeparture);
            flight.setScheduledArrival(scheduledArrival);
            flight.setDelayMinutes(delay);
            flight.setStatus(statuses[random.nextInt(statuses.length)]);
            flight.setTerminal("T" + (1 + random.nextInt(3)));
            flight.setGate("G" + (10 + random.nextInt(20)));
            flight.setLastUpdated(LocalDateTime.now());

            // ✅ Если есть задержка — рассчитываем estimated
            if (delay > 0) {
                flight.setEstimatedDeparture(scheduledDeparture.plusMinutes(delay));
                flight.setEstimatedArrival(scheduledArrival.plusMinutes(delay));
            }

            newFlights.add(flight);
        }

        flightRepository.saveAll(newFlights);
        log.info("✅ Сгенерировано {} рейсов", newFlights.size());
    }

    private Airport createAirport(String code, String name, String city, String country,
                                  double lat, double lon, String timezone) {
        Airport a = new Airport();
        a.setIataCode(code);
        a.setName(name);
        a.setCity(city);
        a.setCountry(country);
        a.setLatitude(lat);
        a.setLongitude(lon);
        a.setTimezone(timezone);
        return a;
    }

    private String generateFlightNumber(Random random) {
        String[] prefixes = {"SU", "AF", "BA", "LH", "EK", "JL", "UA", "DL", "TK", "U6", "S7"};
        return prefixes[random.nextInt(prefixes.length)] + (1000 + random.nextInt(9000));
    }
}
