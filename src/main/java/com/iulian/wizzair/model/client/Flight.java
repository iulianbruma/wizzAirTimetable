package com.iulian.wizzair.model.client;

import lombok.Data;

public @Data class Flight {
    private String departureStation;
    private String arrivalStation;
    private String from;
    private String to;

    public Flight() {
    }

    public Flight(String departureStation, String arrivalStation, String from, String to) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.from = from;
        this.to = to;
    }
}
