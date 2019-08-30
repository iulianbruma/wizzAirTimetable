package com.iulian.wizzair.model.client;

import lombok.Data;

public @Data class FlightRequest {
    private int departureMonth;
    private int arrivalMonth;
    private int departureYear;
    private int arrivalYear;

}
