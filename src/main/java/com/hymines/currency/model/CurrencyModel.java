package com.hymines.currency.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CurrencyModel {

    // Map of currency ID to amount
    private final Map<String, BigDecimal> currencies = new HashMap<>();

    public void addCurrency(String currency) {
        currencies.putIfAbsent(currency, BigDecimal.ZERO);
    }

    public void setCurrency(String currency, BigDecimal amount) {
        currencies.put(currency, amount);
    }

    public void addAmount(String currency, BigDecimal amount) {
        currencies.put(currency, getCurrency(currency).add(amount));
    }

    public boolean hasCurrency(String currency) {
        return currencies.containsKey(currency);
    }

    public BigDecimal getCurrency(String currency) {
        return currencies.getOrDefault(currency, BigDecimal.ZERO);
    }

    public Map<String, BigDecimal> getCurrencies() {
        return Collections.unmodifiableMap(currencies);
    }

    public void removeCurrency(String currency) {
        currencies.remove(currency);
    }

}
