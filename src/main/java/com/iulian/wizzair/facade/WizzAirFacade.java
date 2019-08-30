package com.iulian.wizzair.facade;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.iulian.wizzair.client.WizzAirClient;
import com.iulian.wizzair.config.CountriesConfig;
import com.iulian.wizzair.model.client.FlightRequest;
import com.iulian.wizzair.model.client.FlightResult;
import com.iulian.wizzair.model.client.WizzAirRequest;
import com.iulian.wizzair.model.client.WizzAirResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WizzAirFacade {
    private static final int MAX_PRICE = 111;

    @Autowired
    private WizzAirClient wizzAirClient;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private CountriesConfig countriesConfig;

    @Autowired
    private ExecutorService executorService;

    public void generateEmailWithPrices(FlightRequest request) {
        WizzAirRequest wizzAirRequest = conversionService.convert(request, WizzAirRequest.class);

        System.out.println(countriesConfig.getCountries().values().stream().collect(Collectors.joining(",")));

        List<CompletableFuture<WizzAirResponse>> futures = countriesConfig.getCountries().values().stream()
                .map(e -> CompletableFuture.supplyAsync(() -> {
                    wizzAirRequest.getFlightList().get(0).setArrivalStation(e);
                    wizzAirRequest.getFlightList().get(1).setDepartureStation(e);
                    return wizzAirClient.getWizzAirTimeTable(wizzAirRequest);
                }, executorService))
                .collect(Collectors.toList());

        List<WizzAirResponse> responses = futures.stream().map(CompletableFuture::join).distinct().collect(Collectors.toList());

        List<WizzAirResponse> filteredFlights = responses.stream().map(e -> {
            List<FlightResult> outbound = e.getOutboundFlights().stream().filter(getFlightPricePredicate()).collect(Collectors.toList());
            List<FlightResult> returns;
            if (CollectionUtils.isEmpty(outbound)) {
                returns = e.getReturnFlights().stream().filter(getFlightPricePredicate()).collect(Collectors.toList());
            } else {
                Optional<FlightResult> firstFlight = outbound.stream().filter(f -> f.getPrice().getAmount() > 0).findFirst();
                if (firstFlight.isPresent()) {
                    LocalDate firstAvailableDate = firstFlight.get().getDepartureDate();
                    returns = e.getReturnFlights().stream()
                            .filter(getFlightPricePredicate())
                            .filter(p -> p.getDepartureDate().isAfter(firstAvailableDate) || p.getDepartureDate().isEqual(firstAvailableDate)).collect(Collectors.toList());
                } else {
                    returns = e.getReturnFlights().stream().filter(getFlightPricePredicate()).collect(Collectors.toList());
                }
            }

            return new WizzAirResponse(outbound, returns);
        }).collect(Collectors.toList());

        printlnList(filteredFlights);
    }

    private void printlnList(List<WizzAirResponse> filteredFlights) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("WizzAirData.pdf"));
            document.open();

            Paragraph titleParagraph = new Paragraph();
            Font font = FontFactory.getFont(FontFactory.COURIER, 20, BaseColor.BLACK);
            Chunk chunk2 = new Chunk("Wizz Air Flights", font);
            titleParagraph.setAlignment(Element.ALIGN_LEFT);
            titleParagraph.add(chunk2);

            Paragraph logoParagraph = new Paragraph();
            Path path = Paths.get("wizz-logo.png");
            Image img = Image.getInstance(path.toAbsolutePath().toString());
            img.scalePercent(10);

            logoParagraph.setAlignment(Element.ALIGN_RIGHT);
            logoParagraph.add(img);
            document.add(chunk2);
            document.add(img);

            Paragraph paragraphMain = new Paragraph();
            addEmptyLine(paragraphMain, 1);
            document.add(paragraphMain);

            Map<String, String> countries = countriesConfig.getCountries();
            filteredFlights.forEach(e -> {
                try {
                    if (CollectionUtils.isNotEmpty(e.getOutboundFlights()) && CollectionUtils.isNotEmpty(e.getReturnFlights())) {
                        Font font2 = FontFactory.getFont(FontFactory.COURIER, 8, BaseColor.BLACK);
                        final Chunk[] chunk = {new Chunk("Flight:", font2)};
                        document.add(chunk[0]);
                        final Paragraph[] paragraph = {new Paragraph()};
                        addEmptyLine(paragraph[0], 0);
                        document.add(paragraph[0]);
                        System.out.println("Flight: ");
                        chunk[0] = new Chunk("OUTBOUND:", font2);
                        document.add(chunk[0]);
                        paragraph[0] = new Paragraph();
                        addEmptyLine(paragraph[0], 0);
                        document.add(paragraph[0]);
                        System.out.println("OUTBOUND: ");
                        e.getOutboundFlights().forEach(o -> {
                            Optional<String> name = keys(countries, o.getArrivalStation()).findFirst();
                            try {
                                if (name.isPresent()) {
                                    o.setArrivalStation(name.get());
                                } else {
                                    chunk[0] = new Chunk("Error for countryCode=" + o.getArrivalStation(), font2);
                                    document.add(chunk[0]);
                                    paragraph[0] = new Paragraph();
                                    addEmptyLine(paragraph[0], 0);
                                    document.add(paragraph[0]);
                                    System.out.println("Error for countryCode=" + o.getArrivalStation());
                                }
                                System.out.println(o);
                                chunk[0] = new Chunk(o.toString(), font2);
                                document.add(chunk[0]);
                                paragraph[0] = new Paragraph();
                                addEmptyLine(paragraph[0], 0);
                                document.add(paragraph[0]);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        });
                        chunk[0] = new Chunk("RETURN:", font2);
                        document.add(chunk[0]);
                        paragraph[0] = new Paragraph();
                        addEmptyLine(paragraph[0], 0);
                        document.add(paragraph[0]);
                        System.out.println("RETURN: ");
                        e.getReturnFlights().forEach(o -> {
                            Optional<String> name = keys(countries, o.getDepartureStation()).findFirst();
                            try {
                                if (name.isPresent()) {
                                    o.setDepartureStation(name.get());
                                } else {
                                    chunk[0] = new Chunk("Error for countryCode=" + o.getDepartureStation(), font2);
                                    document.add(chunk[0]);
                                    paragraph[0] = new Paragraph();
                                    addEmptyLine(paragraph[0], 0);
                                    document.add(paragraph[0]);
                                    System.out.println("Error for countryCode=" + o.getDepartureStation());
                                }
                                System.out.println(o);
                                chunk[0] = new Chunk(o.toString(), font2);
                                document.add(chunk[0]);
                                paragraph[0] = new Paragraph();
                                addEmptyLine(paragraph[0], 0);
                                document.add(paragraph[0]);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        });
                        System.out.println();
                        paragraph[0] = new Paragraph();
                        addEmptyLine(paragraph[0], 1);
                        document.add(paragraph[0]);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

            });
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finished!");
       /* Map<String, String> countries = countriesConfig.getCountries();
        StringBuilder builder = new StringBuilder();
        filteredFlights.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getOutboundFlights()) && CollectionUtils.isNotEmpty(e.getReturnFlights())) {
                Font font2 = FontFactory.getFont(FontFactory.COURIER, 13, BaseColor.BLACK);
                Chunk chunk = new Chunk("Flight:", font2);
                document.add(chunk);
                builder.append("Flight:");
                builder.append("\n");
                builder.append("OUTBOUND:");
                builder.append("\n");
                System.out.println("Flight: ");
                System.out.println("OUTBOUND: ");
                e.getOutboundFlights().forEach(o -> {
                    Optional<String> name = keys(countries, o.getArrivalStation()).findFirst();
                    if (name.isPresent()) {
                        o.setArrivalStation(name.get());
                    } else {
                        System.out.println("Error for countryCode=" + o.getArrivalStation());
                        builder.append("Error for countryCode=" + o.getArrivalStation());
                        builder.append("\n");
                    }
                    System.out.println(o);
                    builder.append(o);
                    builder.append("\n");
                });
                builder.append("RETURN:");
                builder.append("\n");
                System.out.println("RETURN: ");
                e.getReturnFlights().forEach(o -> {
                    Optional<String> name = keys(countries, o.getDepartureStation()).findFirst();
                    if (name.isPresent()) {
                        o.setDepartureStation(name.get());
                    } else {
                        System.out.println("Error for countryCode=" + o.getDepartureStation());
                        builder.append("Error for countryCode=" + o.getDepartureStation());
                        builder.append("\n");
                    }
                    System.out.println(o);
                    builder.append(o);
                    builder.append("\n");
                });
                System.out.println();
                builder.append("\n");
            }
        });
        generatePdfFile(builder.toString());*/
    }

    private Consumer<FlightResult> printFlightsConsumer(Map<String, String> countries) {
        return o -> {
            o.setArrivalStation(keys(countries, o.getArrivalStation()).findFirst().get());
            o.setDepartureStation(keys(countries, o.getDepartureStation()).findFirst().get());
            System.out.println(o);
        };
    }

    public <K, V> Stream<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private Predicate<FlightResult> getFlightPricePredicate() {
        return p -> p.getPrice().getAmount() <= MAX_PRICE && p.getPrice().getAmount() > 0;
    }

    private void generatePdfFile(String data) {
        System.out.println(data);
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("WizzAirData.pdf"));
            document.open();

            Paragraph titleParagraph = new Paragraph();
            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            Chunk chunk = new Chunk("Wizz Air Flights", font);
            titleParagraph.setAlignment(Element.ALIGN_LEFT);
            titleParagraph.add(chunk);

            Paragraph logoParagraph = new Paragraph();
            Path path = Paths.get("wizz-logo.png");
            Image img = Image.getInstance(path.toAbsolutePath().toString());
            img.scalePercent(10);

            logoParagraph.setAlignment(Element.ALIGN_RIGHT);
            logoParagraph.add(img);
            document.add(chunk);
            document.add(img);

            Chunk info = new Chunk(data, font);
            document.add(info);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finished!");
    }
}
