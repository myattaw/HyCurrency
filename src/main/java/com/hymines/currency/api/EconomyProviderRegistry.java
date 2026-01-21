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

package com.hymines.currency.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public final class EconomyProviderRegistry {

    private static final AtomicReference<Economy> ECONOMY = new AtomicReference<>();

    private EconomyProviderRegistry() {}

    public static void register(@Nonnull Economy economy) {
        ECONOMY.set(economy);
    }

    public static void unregister(@Nonnull Economy economy) {
        ECONOMY.compareAndSet(economy, null);
    }

    public static boolean isPresent() {
        return ECONOMY.get() != null;
    }

    @Nonnull
    public static Economy get() {
        Economy eco = ECONOMY.get();
        if (eco == null) {
            throw new IllegalStateException("No Economy provider registered");
        }
        return eco;
    }

    @Nullable
    public static Economy getOrNull() {
        return ECONOMY.get();
    }

}
