package com.github.gpapadopoulos.colorcounting.kafka.testBeans;

import com.github.gpapadopoulos.colorcounting.services.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DoNothingPushService implements PushService {
    private static Logger logger = LoggerFactory.getLogger(DoNothingPushService.class);;

    @Override
    public void push(String color) {
    }

    @Override
    public void pushAll(Collection<String> colorMessages) {
    }
}
