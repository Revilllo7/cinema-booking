package com.cinema.cart;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@SessionScope
@Data
public class SessionCart implements Serializable {

    private Long screeningId;
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void clear() {
        screeningId = null;
        items.clear();
    }
}
