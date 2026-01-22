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

package com.reliableplugins.currency.command;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.reliableplugins.currency.command.sub.*;
import com.reliableplugins.currency.command.sub.balance.CurrencyBalanceCommand;
import com.reliableplugins.currency.command.sub.leaderboard.CurrencyTopCommand;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class CurrencyCommand extends AbstractAsyncCommand {

    public CurrencyCommand(HyCurrencyPlugin plugin) {
        super("currency", "Manage currencies");
        this.addSubCommand(new CurrencyAddCommand(plugin));
        this.addSubCommand(new CurrencyBalanceCommand(plugin));
        this.addSubCommand(new CurrencyPayCommand(plugin));
        this.addSubCommand(new CurrencySetCommand(plugin));
        this.addSubCommand(new CurrencyRemoveCommand(plugin));
        this.addSubCommand(new CurrencyTopCommand(plugin));
        this.addSubCommand(new CurrencyListCommand(plugin));
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext) {
        commandContext.sendMessage(Message.raw("Usage: /currency <balance|pay|set|add|remove|top|list> [args...]"));
        return CompletableFuture.completedFuture(null);
    }

}

