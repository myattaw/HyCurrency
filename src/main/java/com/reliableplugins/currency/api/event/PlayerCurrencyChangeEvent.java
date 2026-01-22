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

package com.reliableplugins.currency.api.event;

import com.hypixel.hytale.event.IEvent;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.UUID;

public class PlayerCurrencyChangeEvent implements IEvent<Void> {

    @Nonnull
    private final UUID playerUuid;
    @Nonnull
    private final String currency;

    @Nonnull
    private final BigDecimal oldAmount;
    @Nonnull
    private final BigDecimal newAmount;

    public PlayerCurrencyChangeEvent(
            @Nonnull UUID playerUuid,
            @Nonnull String currency,
            @Nonnull BigDecimal oldAmount,
            @Nonnull BigDecimal newAmount
    ) {
        this.playerUuid = playerUuid;
        this.currency = currency;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    @Nonnull
    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    @Nonnull
    public String getCurrency() {
        return this.currency;
    }

    @Nonnull
    public BigDecimal getOldAmount() {
        return this.oldAmount;
    }

    @Nonnull
    public BigDecimal getNewAmount() {
        return this.newAmount;
    }

    @Override
    @Nonnull
    public String toString() {
        return "PlayerCurrencyChangeEvent{playerUuid=" + this.playerUuid
                + ", currency=" + this.currency
                + ", oldAmount=" + this.oldAmount
                + ", newAmount=" + this.newAmount
                + "}";
    }

}
