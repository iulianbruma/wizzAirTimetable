package com.iulian.wizzair.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

public @Data @AllArgsConstructor @NoArgsConstructor @ToString class WizzAirResponse {
    private List<FlightResult> outboundFlights;
    private List<FlightResult> returnFlights;

}
