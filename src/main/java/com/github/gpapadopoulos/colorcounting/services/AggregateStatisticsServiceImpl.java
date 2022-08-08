package com.github.gpapadopoulos.colorcounting.services;

import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AggregateStatisticsServiceImpl implements  AggregateStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    private final ColorRepository colorRepository;

    public AggregateStatisticsServiceImpl(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    public Map<String, Long> getColorCounts() {
        ArrayList<Color> repoColors = new ArrayList<>();
        colorRepository.findAll().forEach(repoColors::add);

        Map<String, Long> frequencyMap = repoColors.stream().collect(Collectors.groupingBy(Color::getColor, Collectors.counting()));
        logger.info("Returning counts: " + frequencyMap );
        return frequencyMap;
    }
}
