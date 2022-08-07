package com.github.gpapadopoulos.colorcounting.services;

import java.util.Map;

public interface AggregateStatisticsService {

    Map<String, Long> getColorCounts();
}
