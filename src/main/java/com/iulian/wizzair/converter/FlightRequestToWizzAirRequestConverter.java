package com.iulian.wizzair.converter;

import com.iulian.wizzair.model.client.Flight;
import com.iulian.wizzair.model.client.FlightRequest;
import com.iulian.wizzair.model.client.WizzAirRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlightRequestToWizzAirRequestConverter implements Converter<FlightRequest, WizzAirRequest> {

    private static final int ADULT_COUNT = 2;
    private static final String DISCOUNT_PRICE_TYPE = "wdc";
    private static final String DEPARTURE_CLUJ = "CLJ";

    @Override
    public WizzAirRequest convert(FlightRequest flightRequest) {
        if (flightRequest == null) {
            return null;
        }

        LocalDate fromDepartureDate;
        LocalDate toDepartureDate;
        LocalDate fromArrivalDate;
        LocalDate toArrivalDate;

        WizzAirRequest request = new WizzAirRequest();
        request.setAdultCount(ADULT_COUNT);
        request.setPriceType(DISCOUNT_PRICE_TYPE);
        if (flightRequest.getDepartureMonth() == LocalDate.now().getMonthValue()) {
            fromDepartureDate = LocalDate.now();
            fromArrivalDate = LocalDate.now();
        } else {
            fromDepartureDate = LocalDate.of(flightRequest.getDepartureYear(), flightRequest.getDepartureMonth(), 1);
            fromArrivalDate = LocalDate.of(flightRequest.getArrivalYear(), flightRequest.getArrivalMonth(), 1);
        }

        toDepartureDate = LocalDate.of(flightRequest.getDepartureYear(), flightRequest.getDepartureMonth(), YearMonth.of(flightRequest.getDepartureYear(), flightRequest.getDepartureMonth()).lengthOfMonth());
        toArrivalDate = LocalDate.of(flightRequest.getArrivalYear(), flightRequest.getArrivalMonth(), YearMonth.of(flightRequest.getArrivalYear(), flightRequest.getArrivalMonth()).lengthOfMonth());

        Flight departureFlight = new Flight(DEPARTURE_CLUJ, null, fromDepartureDate.toString(), toDepartureDate.toString());
        Flight arrivalFlight = new Flight(null, DEPARTURE_CLUJ, fromArrivalDate.toString(), toArrivalDate.toString());
        List<Flight> flights = new ArrayList<>();
        flights.add(departureFlight);
        flights.add(arrivalFlight);
        request.setFlightList(flights);

        return request;
    }
}
