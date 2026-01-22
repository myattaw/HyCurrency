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

package com.reliableplugins.currency.command.sub.leaderboard;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class CurrencyTopVariant extends AbstractCommand {

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<String> currencyArg;

    public CurrencyTopVariant(HyCurrencyPlugin plugin) {
        super("View the currency leaderboard (page 1)");
        this.plugin = plugin;
        this.currencyArg = withRequiredArg("currency", "Currency type", ArgTypes.STRING);
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        String currency = currencyArg.get(ctx);
        return CurrencyTopCommand.executeTop(ctx, plugin, currency, 1);
    }

}
