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

package com.reliableplugins.currency.command.sub;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.reliableplugins.currency.config.CurrencyConfig;
import com.reliableplugins.currency.model.CurrencyMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CurrencyListCommand extends AbstractCommand {

    private final HyCurrencyPlugin plugin;

    public CurrencyListCommand(HyCurrencyPlugin plugin) {
        super("list", "List available currencies");
        this.addAliases("currencies");
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        CurrencyConfig cfg = plugin.getCurrencyConfig();
        if (cfg == null || cfg.getCurrencies() == null || cfg.getCurrencies().isEmpty()) {
            ctx.sendMessage(Message.raw("No currencies defined."));
            return CompletableFuture.completedFuture(null);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available currencies:\n");
        for (Map.Entry<String, CurrencyMetadata> e : cfg.getCurrencies().entrySet()) {
            String id = e.getKey();
            CurrencyMetadata entry = e.getValue();
            sb.append("  ").append(id)
                    .append(": ").append(entry.getName())
                    .append(" (").append(entry.getSymbol()).append(")")
                    .append(" leaderboard=").append(entry.isLeaderboard())
                    .append("\n");
        }

        ctx.sendMessage(Message.raw(sb.toString().trim()));
        return CompletableFuture.completedFuture(null);
    }
}

