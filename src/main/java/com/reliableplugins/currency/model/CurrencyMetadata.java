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

import java.math.BigDecimal;

public class CurrencyMetadata {

    private String id;
    private String name;
    private String symbol;
    private String format;
    private boolean leaderboard;
    private boolean autoGrant = false;
    private BigDecimal defaultAmount = BigDecimal.ZERO;

    // Required by Gson
    public CurrencyMetadata() {
    }

    public CurrencyMetadata(String id, String name, String symbol, String format, boolean leaderboard) {
        this(id, name, symbol, format, leaderboard, false, BigDecimal.ZERO);
    }

    public CurrencyMetadata(String id, String name, String symbol, String format, boolean leaderboard, boolean autoGrant, BigDecimal defaultAmount) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.format = format;
        this.leaderboard = leaderboard;
        this.autoGrant = autoGrant;
        this.defaultAmount = defaultAmount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFormat() {
        return format;
    }

    public boolean isLeaderboard() {
        return leaderboard;
    }

    public boolean isAutoGrant() {
        return autoGrant;
    }

    public BigDecimal getDefaultAmount() {
        return defaultAmount != null ? defaultAmount : BigDecimal.ZERO;
    }

    public String formatAmount(String amount) {
        return format.replace("%symbol%", symbol).replace("%amount%", amount);
    }

}
