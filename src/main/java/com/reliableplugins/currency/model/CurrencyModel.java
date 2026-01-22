/*
 * MIT License
 *
 * Copyright (c) 2026 Michael Yattaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * See the LICENSE file in the project root for full license information.
 */

package com.reliableplugins.currency.model;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CurrencyModel {

    private String playerName;

    // Map of currency ID to amount
    private final Map<String, BigDecimal> currencies = new HashMap<>();

    @Nullable
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@Nullable String playerName) {
        this.playerName = playerName;
    }

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

