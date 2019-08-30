package com.iulian.wizzair.model.client;

import lombok.Data;

import java.util.List;

public @Data class WizzAirRequest {
    private List<Flight> flightList;
    private String priceType;
    private int adultCount;
    private int childCount;
    private int infantCount;

}
