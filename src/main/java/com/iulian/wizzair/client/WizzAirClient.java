package com.iulian.wizzair.client;

import com.iulian.wizzair.model.client.WizzAirRequest;
import com.iulian.wizzair.model.client.WizzAirResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class WizzAirClient {
    private static final Logger LOG = LoggerFactory.getLogger(WizzAirClient.class);

    @Value("${wizz-air.url}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    public WizzAirResponse getWizzAirTimeTable(WizzAirRequest request) {
        ResponseEntity<WizzAirResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, getDefaultHttpHeaders()), WizzAirResponse.class);
        } catch (Exception e) {
            LOG.error("Could not get data from WizzAir {}", e.getMessage());
            return null;
        }
        LOG.info("Data was retrieved successfully for departureStation={} and arrivalStation={}", request.getFlightList().get(0).getDepartureStation(), request.getFlightList().get(1).getDepartureStation());
        return responseEntity.getBody();
    }

    private HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setOrigin("https://wizzair.com");

        return headers;
    }
}
