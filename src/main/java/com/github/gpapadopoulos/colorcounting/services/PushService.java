package com.github.gpapadopoulos.colorcounting.services;

import java.util.Collection;

public interface PushService {

    void push(String color);

    void pushAll(Collection<String> colorMessages);
}
