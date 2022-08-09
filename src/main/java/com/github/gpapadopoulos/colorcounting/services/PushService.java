package com.github.gpapadopoulos.colorcounting.services;

import java.util.List;

public interface PushService {

    void push(String color);

    void pushAll(List<String> colorMessages);
}
