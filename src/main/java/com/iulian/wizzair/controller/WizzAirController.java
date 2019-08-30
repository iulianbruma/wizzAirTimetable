package com.iulian.wizzair.controller;

import com.iulian.wizzair.facade.WizzAirFacade;
import com.iulian.wizzair.model.client.FlightRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
//@RequestMapping("/time-table")
public class WizzAirController {

    @Autowired
    private WizzAirFacade wizzAirFacade;

    @GetMapping("/")
    public String welcome(@ModelAttribute("flightRequest") FlightRequest batchCommand, Model model) {
        model.addAttribute("started", false);
        model.addAttribute("stopped", true);
        model.addAttribute("statusMsg", " were not processed yet");
        model.addAttribute("error", null);
        return "index";
    }

    @PostMapping("/flights")
    @Async
    public String generateTimeTableData(@ModelAttribute("flightRequest") FlightRequest flightRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
//            batchData.setErrorMessage(result.getFieldErrors().stream().map(e -> "Field " + e.getField() + " was rejected with error code: " + e.getCode() + " and value: " + e.getRejectedValue()).collect(Collectors.joining("; ")));
            return "redirect:/";
        }
        CompletableFuture.runAsync(() -> wizzAirFacade.generateEmailWithPrices(flightRequest));
        return "redirect:/";
    }
}
