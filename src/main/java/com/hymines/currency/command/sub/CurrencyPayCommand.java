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

package com.hymines.currency.command.sub;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CurrencyPayCommand extends AbstractCommand {

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<PlayerRef> targetPlayerArg;
    private final RequiredArg<String> currencyArg;
    private final RequiredArg<Double> amountArg;

    public CurrencyPayCommand(HyCurrencyPlugin plugin) {
        super("pay", "Pay another player");
        this.plugin = plugin;
        this.targetPlayerArg = withRequiredArg("player", "Player to pay", ArgTypes.PLAYER_REF);
        this.currencyArg = withRequiredArg("currency", "Currency type", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount to pay", ArgTypes.DOUBLE);
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        // Check if sender is a player (not console)
        if (!(commandContext.sender() instanceof Player)) {
            commandContext.sendMessage(Message.raw("This command can only be used by players."));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef selfRef = Universe.get().getPlayer(commandContext.sender().getUuid());
        if (selfRef == null) {
            commandContext.sendMessage(Message.raw("Could not find your player account."));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef target = targetPlayerArg.get(commandContext);
        String currency = currencyArg.get(commandContext);
        BigDecimal amount = BigDecimal.valueOf(amountArg.get(commandContext));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            commandContext.sendMessage(Message.raw("Amount must be positive."));
            return CompletableFuture.completedFuture(null);
        }

        // Compare UUIDs to check if paying self
        if (selfRef.getUuid().equals(target.getUuid())) {
            commandContext.sendMessage(Message.raw("You cannot pay yourself."));
            return CompletableFuture.completedFuture(null);
        }

        Map<String, CurrencyModel> currencyDataMap = plugin.getCurrencyDataMap();

        CurrencyModel senderModel = currencyDataMap.computeIfAbsent(
                selfRef.getUuid().toString(), k -> new CurrencyModel()
        );
        CurrencyModel targetModel = currencyDataMap.computeIfAbsent(
                target.getUuid().toString(), k -> new CurrencyModel()
        );

        BigDecimal senderBalance = senderModel.getCurrency(currency);

        if (senderBalance.compareTo(amount) < 0) {
            commandContext.sendMessage(Message.raw(
                    "Insufficient funds. You have " + senderBalance + " " + currency + "."
            ));
            return CompletableFuture.completedFuture(null);
        }

        senderModel.addAmount(currency, amount.negate());
        targetModel.addAmount(currency, amount);

        commandContext.sendMessage(Message.raw(
                "Paid " + amount + " " + currency + " to " + target.getUsername() + "."
        ));
        return CompletableFuture.completedFuture(null);
    }

}

