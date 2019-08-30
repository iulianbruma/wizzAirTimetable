package com.iulian.wizzair.model.client;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public @Data class FlightResult {
    private String departureStation;
    private String arrivalStation;
    private LocalDate departureDate;
    private FlightPrice price;
    private String priceType;
    private List<LocalDateTime> departureDates;
    private String classOfService;
    private String hasMacFlight;

    @Override
    public String toString() {
        return "departure='" + departureStation + '\'' +
                ", arrival='" + arrivalStation + '\'' +
                ", departureDate=" + departureDate.toString() +
                ", day=" + departureDate.getDayOfWeek().name() +
                ", price=" + price.getAmount();
    }
}
