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