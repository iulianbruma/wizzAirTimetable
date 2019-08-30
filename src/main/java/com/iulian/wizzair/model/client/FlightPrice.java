package com.iulian.wizzair.model.client;

import lombok.Data;
import lombok.ToString;

public @Data @ToString class FlightPrice {
    private int amount;
    private String currencyCode;
}
