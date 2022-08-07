package com.github.gpapadopoulos.colorcounting.controllers;

import com.github.gpapadopoulos.colorcounting.services.AggregateStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/statistics")
public class AggregateColorsController {

    private final AggregateStatisticsService aggregateService;

    public AggregateColorsController(AggregateStatisticsService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @GetMapping(value = "/aggregate-messages")
    public String custom() {
        Map<String, Long> collect = aggregateService.getColorCounts();
        return collect.toString();
    }
}
