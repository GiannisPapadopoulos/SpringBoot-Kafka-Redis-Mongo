package com.github.gpapadopoulos.colorcounting.controllers;

import com.github.gpapadopoulos.colorcounting.services.AggregateStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/statistics")
public class AggregateColorsController {

    private final AggregateStatisticsService statisticsService;

    public AggregateColorsController(AggregateStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping(value = "/aggregate-messages")
    public String custom() {
        Map<String, Long> collect = statisticsService.getColorCounts();
        return collect.toString();
    }
}
