package com.example.lowflightzone.services;

import com.example.lowflightzone.dao.AirportDao;
import com.example.lowflightzone.dao.FlightDao;
import com.example.lowflightzone.dto.AirportDto;
import com.example.lowflightzone.dto.FlightDto;
import com.example.lowflightzone.entity.Airport;
import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.entity.FlightSubscription;
import com.example.lowflightzone.exceptions.AirportException;
import com.example.lowflightzone.exceptions.FlightException;
import com.example.lowflightzone.exceptions.ValidationException;
import com.example.lowflightzone.repositories.FlightRepository;
import com.example.lowflightzone.services.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FlightService {

    private static final String FLIGHT_NOT_FOUND_MESSAGE = "Рейс не найден: ";
    private static final String FLIGHT_ALREADY_EXISTS_MESSAGE = "Рейс с таким номером уже существует: ";

    private final FlightDao flightDao;
    private final AirportDao airportDao;
    private final FlightRepository flightRepository;
    private final NotificationService notificationService;

    @Autowired
    public FlightService(FlightDao flightDao, AirportDao airportDao, FlightRepository flightRepository, NotificationService notificationService) {
        this.flightDao = flightDao;
        this.airportDao = airportDao;
        this.flightRepository = flightRepository;
        this.notificationService = notificationService;
    }

    public List<FlightDto> getFlights(String departureAirport, String arrivalAirport, String status) {
        List<FlightDto> flights = flightDao.findAll().stream()
                .filter(flight -> departureAirport == null ||
                        flight.getDepartureAirport().getIataCode().equals(departureAirport))
                .filter(flight -> arrivalAirport == null ||
                        flight.getArrivalAirport().getIataCode().equals(arrivalAirport))
                .filter(flight -> status == null || flight.getStatus().toString().equals(status))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        if (flights.isEmpty()) {
            throw new FlightException("Рейсы с такими параметрами не найдены");
        }
        return flights;
    }

    public FlightDto getFlightByNumber(String flightNumber) {
        Flight flight = flightDao.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightException(FLIGHT_NOT_FOUND_MESSAGE + flightNumber));
        return convertToDto(flight);
    }

    public List<FlightDto> searchFlights(String query, String userEmail) {
        return flightRepository
                .findByFlightNumberContainingIgnoreCaseOrDepartureAirport_CityContainingIgnoreCaseOrArrivalAirport_CityContainingIgnoreCaseOrDepartureAirport_IataCodeContainingIgnoreCaseOrArrivalAirport_IataCodeContainingIgnoreCaseOrDepartureAirport_NameContainingIgnoreCaseOrArrivalAirport_NameContainingIgnoreCase(
                        query, query, query, query, query, query, query
                )
                .stream()
                .map(flight -> convertToDtoWithSubscription(flight, userEmail))
                .collect(Collectors.toList());
    }

    private FlightDto convertToDtoWithSubscription(Flight flight, String userEmail) {
        FlightDto dto = convertToDto(flight);

        if (userEmail != null && flight.getSubscriptions() != null) {
            boolean isSubscribed = flight.getSubscriptions().stream()
                    .anyMatch(sub -> sub.getUser() != null
                            && sub.getUser().getEmail().equalsIgnoreCase(userEmail)
                            && sub.getStatus() == FlightSubscription.SubscriptionStatus.ACTIVE);

            dto.setSubscribed(isSubscribed);
        } else {
            dto.setSubscribed(false);
        }

        return dto;
    }

    @Transactional
    public void recalculateDelayedFlights() {
        final LocalDateTime now = LocalDateTime.now();
        List<Flight> flights = flightRepository.findAll();

        for (Flight f : flights) {
            boolean updated = false;

            // ❌ Пропускаем отменённые рейсы
            if (f.getStatus() == Flight.FlightStatus.CANCELLED) {
                continue;
            }

            int delay = (f.getDelayMinutes() != null) ? f.getDelayMinutes() : 0;

            // ✅ 1. Пересчёт estimated на основе расписания + задержки
            LocalDateTime expectedEstDep = (f.getScheduledDeparture() != null)
                    ? f.getScheduledDeparture().plusMinutes(delay)
                    : null;
            LocalDateTime expectedEstArr = (f.getScheduledArrival() != null)
                    ? f.getScheduledArrival().plusMinutes(delay)
                    : null;

            if (!Objects.equals(f.getEstimatedDeparture(), expectedEstDep)) {
                f.setEstimatedDeparture(expectedEstDep);
                updated = true;
            }
            if (!Objects.equals(f.getEstimatedArrival(), expectedEstArr)) {
                f.setEstimatedArrival(expectedEstArr);
                updated = true;
            }

            // ✅ 2. Сбрасываем actual, если вылет или прилёт ещё не наступили
            if (expectedEstDep != null && now.isBefore(expectedEstDep)) {
                if (f.getActualDeparture() != null) {
                    f.setActualDeparture(null);
                    updated = true;
                }
            }
            if (expectedEstArr != null && now.isBefore(expectedEstArr)) {
                if (f.getActualArrival() != null) {
                    f.setActualArrival(null);
                    updated = true;
                }
            }

            // ✅ 3. Устанавливаем actualDeparture корректно
            if (f.getScheduledDeparture() != null) {
                // 📅 Без задержки — сравниваем с расписанием
                if (delay == 0 && now.isAfter(f.getScheduledDeparture())) {
                    if (!Objects.equals(f.getActualDeparture(), f.getScheduledDeparture())) {
                        f.setActualDeparture(f.getScheduledDeparture());
                        updated = true;
                    }
                }
                // ⏱ С задержкой — сравниваем с estimated
                if (delay > 0 && expectedEstDep != null && now.isAfter(expectedEstDep)) {
                    if (!Objects.equals(f.getActualDeparture(), expectedEstDep)) {
                        f.setActualDeparture(expectedEstDep);
                        updated = true;
                    }
                }
            }

            // ✅ 4. Устанавливаем actualArrival корректно
            if (f.getScheduledArrival() != null) {
                // 📅 Без задержки — сравниваем с расписанием
                if (delay == 0 && now.isAfter(f.getScheduledArrival())) {
                    if (!Objects.equals(f.getActualArrival(), f.getScheduledArrival())) {
                        f.setActualArrival(f.getScheduledArrival());
                        updated = true;
                    }
                }
                // ⏱ С задержкой — сравниваем с estimated
                if (delay > 0 && expectedEstArr != null && now.isAfter(expectedEstArr)) {
                    if (!Objects.equals(f.getActualArrival(), expectedEstArr)) {
                        f.setActualArrival(expectedEstArr);
                        updated = true;
                    }
                }
            }

            // ✅ 5. Определяем статус
            Flight.FlightStatus newStatus;
            if (delay > 0) {
                newStatus = Flight.FlightStatus.DELAYED;
            } else {
                newStatus = Flight.FlightStatus.SCHEDULED;
            }

            if (f.getActualDeparture() != null && now.isAfter(f.getActualDeparture())) {
                newStatus = Flight.FlightStatus.DEPARTED;
            }

            if (f.getActualArrival() != null && now.isAfter(f.getActualArrival())) {
                newStatus = Flight.FlightStatus.ARRIVED;
            }

            if (f.getStatus() != newStatus) {
                f.setStatus(newStatus);
                updated = true;
            }

            // ✅ 6. Финализация
            if (updated) {
                f.setLastUpdated(now);
                flightRepository.save(f);
                log.info("✈️ Пересчитан рейс {} — status={} | delay={} | estDep={} estArr={} | actDep={} actArr={}",
                        f.getFlightNumber(), f.getStatus(), f.getDelayMinutes(),
                        f.getEstimatedDeparture(), f.getEstimatedArrival(),
                        f.getActualDeparture(), f.getActualArrival());
            }
        }
    }

    /** Запуск при старте */
    @PostConstruct
    public void runRecalculationOnStartup() {
        log.info("🚀 Пересчёт рейсов при старте…");
        recalculateDelayedFlights();
    }

    /** (опционально) Периодический пересчёт каждые 5 минут */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void recalcScheduler() {
        recalculateDelayedFlights();
    }

    public FlightDto addFlight(FlightDto flightDto) {
        validateFlightDto(flightDto);

        if (flightDao.existsByFlightNumber(flightDto.getFlightNumber())) {
            throw new FlightException(FLIGHT_ALREADY_EXISTS_MESSAGE + flightDto.getFlightNumber());
        }

        Flight flight = convertToEntity(flightDto);
        Flight savedFlight = flightDao.save(flight);

        return convertToDto(savedFlight);
    }

    public void deleteFlightById(Integer id) {
        flightDao.findById(id)
                .orElseThrow(() -> new FlightException(FLIGHT_NOT_FOUND_MESSAGE + id));

        flightDao.deleteById(id);
    }

    @Transactional
    public FlightDto updateFlight(Integer id, FlightDto updatedFlightDto) {
        validateFlightDto(updatedFlightDto);

        Flight flight = flightDao.findById(id)
                .orElseThrow(() -> new FlightException(FLIGHT_NOT_FOUND_MESSAGE + id));

        // 🔎 Сохраняем старые значения, чтобы потом сравнить
        Flight.FlightStatus oldStatus = flight.getStatus();
        LocalDateTime oldDeparture = flight.getScheduledDeparture();

        // ✈️ Обновляем аэропорты через их коды
        if (updatedFlightDto.getDepartureAirport() != null) {
            Airport departureAirport = airportDao.findByIataCode(
                    updatedFlightDto.getDepartureAirport().getIataCode()
            ).orElseThrow(() -> new AirportException("Аэропорт вылета не найден"));
            flight.setDepartureAirport(departureAirport);
        }

        if (updatedFlightDto.getArrivalAirport() != null) {
            Airport arrivalAirport = airportDao.findByIataCode(
                    updatedFlightDto.getArrivalAirport().getIataCode()
            ).orElseThrow(() -> new AirportException("Аэропорт прибытия не найден"));
            flight.setArrivalAirport(arrivalAirport);
        }

        // ✏️ Обновляем остальные поля
        flight.setFlightNumber(updatedFlightDto.getFlightNumber());
        flight.setAirline(updatedFlightDto.getAirline());
        flight.setScheduledDeparture(updatedFlightDto.getScheduledDeparture());
        flight.setStatus(Flight.FlightStatus.valueOf(updatedFlightDto.getStatus()));
        flight.setDelayMinutes(updatedFlightDto.getDelayMinutes());

        Flight updatedFlight = flightDao.save(flight);

        // 📢 Проверяем: если статус или время изменились — отправляем уведомление
        boolean statusChanged = !Objects.equals(oldStatus, updatedFlight.getStatus());
        boolean departureChanged = !Objects.equals(oldDeparture, updatedFlight.getScheduledDeparture());

        if (statusChanged || departureChanged) {
            notificationService.notifySubscribersAboutFlightUpdate(updatedFlight);
        }

        return convertToDto(updatedFlight);
    }

    public FlightDto patchFlight(Integer id, FlightDto partialFlightDto) {
        Flight flight = flightDao.findById(id)
                .orElseThrow(() -> new FlightException(FLIGHT_NOT_FOUND_MESSAGE + id));

        if (partialFlightDto.getFlightNumber() != null) {
            flight.setFlightNumber(partialFlightDto.getFlightNumber());
        }
        if (partialFlightDto.getAirline() != null) {
            flight.setAirline(partialFlightDto.getAirline());
        }
        if (partialFlightDto.getDepartureAirport() != null) {
            Airport departureAirport = airportDao.findByIataCode(partialFlightDto.getDepartureAirport().getIataCode())
                    .orElseThrow(() -> new AirportException("Аэропорт вылета не найден"));
            flight.setDepartureAirport(departureAirport);
        }
        if (partialFlightDto.getArrivalAirport() != null) {
            Airport arrivalAirport = airportDao.findByIataCode(partialFlightDto.getArrivalAirport().getIataCode())
                    .orElseThrow(() -> new AirportException("Аэропорт прибытия не найден"));
            flight.setArrivalAirport(arrivalAirport);
        }
        if (partialFlightDto.getScheduledDeparture() != null) {
            flight.setScheduledDeparture(partialFlightDto.getScheduledDeparture());
        }
        if (partialFlightDto.getStatus() != null) {
            flight.setStatus(Flight.FlightStatus.valueOf(partialFlightDto.getStatus()));
        }
        if (partialFlightDto.getDelayMinutes() != null) {
            flight.setDelayMinutes(partialFlightDto.getDelayMinutes());
        }

        Flight updatedFlight = flightDao.save(flight);
        return convertToDto(updatedFlight);
    }

    private void validateFlightDto(FlightDto flightDto) {
        if (flightDto.getFlightNumber() == null || flightDto.getFlightNumber().trim().isEmpty()) {
            throw new ValidationException("Номер рейса не может быть пустым");
        }
        if (flightDto.getAirline() == null || flightDto.getAirline().trim().isEmpty()) {
            throw new ValidationException("Авиакомпания не может быть пустой");
        }
        if (flightDto.getDepartureAirport() == null || flightDto.getDepartureAirport().getIataCode() == null) {
            throw new ValidationException("Аэропорт вылета не может быть пустым");
        }
        if (flightDto.getArrivalAirport() == null || flightDto.getArrivalAirport().getIataCode() == null) {
            throw new ValidationException("Аэропорт прибытия не может быть пустым");
        }
        if (flightDto.getScheduledDeparture() == null) {
            throw new ValidationException("Время вылета не может быть пустым");
        }
    }

    private FlightDto convertToDto(Flight flight) {
        FlightDto flightDto = new FlightDto();
        flightDto.setId(flight.getId());
        flightDto.setFlightNumber(flight.getFlightNumber());
        flightDto.setAirline(flight.getAirline());

        // ✈️ Конвертируем Airport entity в AirportDto
        if (flight.getDepartureAirport() != null) {
            AirportDto departureDto = new AirportDto();
            departureDto.setIataCode(flight.getDepartureAirport().getIataCode());
            departureDto.setName(flight.getDepartureAirport().getName());
            departureDto.setCity(flight.getDepartureAirport().getCity());
            departureDto.setCountry(flight.getDepartureAirport().getCountry());
            departureDto.setAltitude(flight.getDepartureAirport().getAltitude());
            departureDto.setLatitude(flight.getDepartureAirport().getLatitude());
            departureDto.setLongitude(flight.getDepartureAirport().getLongitude());
            flightDto.setDepartureAirport(departureDto);
        }

        if (flight.getArrivalAirport() != null) {
            AirportDto arrivalDto = new AirportDto();
            arrivalDto.setIataCode(flight.getArrivalAirport().getIataCode());
            arrivalDto.setName(flight.getArrivalAirport().getName());
            arrivalDto.setCity(flight.getArrivalAirport().getCity());
            arrivalDto.setCountry(flight.getArrivalAirport().getCountry());
            arrivalDto.setAltitude(flight.getDepartureAirport().getAltitude());
            arrivalDto.setLatitude(flight.getArrivalAirport().getLatitude());
            arrivalDto.setLongitude(flight.getArrivalAirport().getLongitude());
            flightDto.setArrivalAirport(arrivalDto);
        }

        flightDto.setScheduledDeparture(flight.getScheduledDeparture());
        flightDto.setScheduledArrival(flight.getScheduledArrival());
        flightDto.setEstimatedDeparture(flight.getEstimatedDeparture());
        flightDto.setEstimatedArrival(flight.getEstimatedArrival());
        flightDto.setActualDeparture(flight.getActualDeparture());
        flightDto.setActualArrival(flight.getActualArrival());
        flightDto.setStatus(flight.getStatus() != null ? flight.getStatus().toString() : null);
        flightDto.setDelayMinutes(flight.getDelayMinutes());
        flightDto.setTerminal(flight.getTerminal());
        flightDto.setGate(flight.getGate());
        flightDto.setLastUpdated(flight.getLastUpdated());

        // ✅ Считаем только активные подписки
        if (flight.getSubscriptions() != null) {
            long activeSubscriptions = flight.getSubscriptions().stream()
                    .filter(sub -> sub.getStatus() == FlightSubscription.SubscriptionStatus.ACTIVE)
                    .count();
            flightDto.setSubscriptionCount((int) activeSubscriptions);
        } else {
            flightDto.setSubscriptionCount(0);
        }

        return flightDto;
    }

    @Transactional
    public FlightDto getFlightById(Integer id) {
        Flight flight = flightRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new FlightException("Рейс не найден: " + id));

        // 🛠 Безопасная запись просмотра (не прерывает выполнение)
        try {
            viewHistoryService.recordFlightView(id);
        } catch (Exception e) {
            log.warn("⚠️ Не удалось записать просмотр рейса {}: {}", id, e.getMessage());
        }

        return convertToDtoSafe(flight);
    }

    private FlightDto convertToDtoSafe(Flight flight) {
        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setAirline(flight.getAirline());
        dto.setStatus(flight.getStatus() != null ? flight.getStatus().toString() : "UNKNOWN");
        dto.setDelayMinutes(flight.getDelayMinutes());
        dto.setTerminal(flight.getTerminal());
        dto.setGate(flight.getGate());
        dto.setLastUpdated(flight.getLastUpdated());

        // ✈️ Аэропорты
        if (flight.getDepartureAirport() != null) {
            AirportDto dep = new AirportDto();
            dep.setIataCode(flight.getDepartureAirport().getIataCode());
            dep.setName(flight.getDepartureAirport().getName());
            dep.setCity(flight.getDepartureAirport().getCity());
            dep.setCountry(flight.getDepartureAirport().getCountry());
            dep.setAltitude(flight.getDepartureAirport().getAltitude());
            dep.setLongitude(flight.getDepartureAirport().getLongitude());
            dep.setLatitude(flight.getDepartureAirport().getLatitude());
            dto.setDepartureAirport(dep);
        }

        if (flight.getArrivalAirport() != null) {
            AirportDto arr = new AirportDto();
            arr.setIataCode(flight.getArrivalAirport().getIataCode());
            arr.setName(flight.getArrivalAirport().getName());
            arr.setCity(flight.getArrivalAirport().getCity());
            arr.setCountry(flight.getArrivalAirport().getCountry());
            arr.setAltitude(flight.getDepartureAirport().getAltitude());
            arr.setLatitude(flight.getArrivalAirport().getLatitude());
            arr.setLongitude(flight.getArrivalAirport().getLongitude());
            dto.setArrivalAirport(arr);
        }

        dto.setScheduledDeparture(flight.getScheduledDeparture());
        dto.setScheduledArrival(flight.getScheduledArrival());
        dto.setEstimatedDeparture(flight.getEstimatedDeparture());
        dto.setEstimatedArrival(flight.getEstimatedArrival());
        dto.setActualDeparture(flight.getActualDeparture());
        dto.setActualArrival(flight.getActualArrival());

        // 📊 Подписки
        int activeSubs = 0;
        try {
            if (flight.getSubscriptions() != null) {
                activeSubs = (int) flight.getSubscriptions().stream()
                        .filter(sub -> sub.getStatus() == FlightSubscription.SubscriptionStatus.ACTIVE)
                        .count();
            }
        } catch (Exception e) {
            log.warn("⚠️ Ошибка при подсчёте подписок для рейса {}: {}", flight.getId(), e.getMessage());
        }
        dto.setSubscriptionCount(activeSubs);

        return dto;
    }



    @Autowired
    private FlightViewHistoryService viewHistoryService;

    public FlightDto getFlightById(Integer id, Integer userId) {
        Flight flight = flightDao.findById(id)
                .orElseThrow(() -> new FlightException(FLIGHT_NOT_FOUND_MESSAGE + id));

        // Записываем просмотр
        if (userId != null) {
            try {
                viewHistoryService.recordFlightView(userId, id);
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем выполнение
                log.warn("Failed to record flight view for user {} and flight {}", userId, id, e);
            }
        }

        return convertToDto(flight);
    }

    private Flight convertToEntity(FlightDto flightDto) {
        Flight flight = new Flight();
        flight.setId(flightDto.getId());
        flight.setFlightNumber(flightDto.getFlightNumber());
        flight.setAirline(flightDto.getAirline());

        // Находим аэропорты по их кодам
        if (flightDto.getDepartureAirport() != null) {
            Airport departureAirport = airportDao.findByIataCode(flightDto.getDepartureAirport().getIataCode())
                    .orElseThrow(() -> new AirportException("Аэропорт вылета не найден: " + flightDto.getDepartureAirport().getIataCode()));
            flight.setDepartureAirport(departureAirport);
        }

        if (flightDto.getArrivalAirport() != null) {
            Airport arrivalAirport = airportDao.findByIataCode(flightDto.getArrivalAirport().getIataCode())
                    .orElseThrow(() -> new AirportException("Аэропорт прибытия не найден: " + flightDto.getArrivalAirport().getIataCode()));
            flight.setArrivalAirport(arrivalAirport);
        }

        flight.setScheduledDeparture(flightDto.getScheduledDeparture());
        flight.setScheduledArrival(flightDto.getScheduledArrival());
        flight.setEstimatedDeparture(flightDto.getEstimatedDeparture());
        flight.setEstimatedArrival(flightDto.getEstimatedArrival());
        flight.setActualDeparture(flightDto.getActualDeparture());
        flight.setActualArrival(flightDto.getActualArrival());

        if (flightDto.getStatus() != null) {
            flight.setStatus(Flight.FlightStatus.valueOf(flightDto.getStatus()));
        }

        flight.setDelayMinutes(flightDto.getDelayMinutes());
        flight.setTerminal(flightDto.getTerminal());
        flight.setGate(flightDto.getGate());
        return flight;
    }
}