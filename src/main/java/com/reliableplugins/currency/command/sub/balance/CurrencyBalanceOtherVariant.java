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

package com.reliableplugins.currency.command.sub.balance;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.reliableplugins.currency.model.CurrencyMetadata;
import com.reliableplugins.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CurrencyBalanceOtherVariant extends AbstractCommand {

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<String> playerArg;

    public CurrencyBalanceOtherVariant(HyCurrencyPlugin plugin) {
        super("Check another player's currency balance");
        this.plugin = plugin;
        this.playerArg = withRequiredArg("player", "Player to check balance for", ArgTypes.STRING);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        String targetName = playerArg.get(ctx);
        PlayerRef target = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);

        PlayerRef selfRef = Universe.get().getPlayer(ctx.sender().getUuid());

        // If player is online, use cached data
        if (target != null) {
            boolean isSelf = selfRef != null && target.getUuid().equals(selfRef.getUuid());
            CurrencyModel model = plugin.getCurrencyDataMap().get(target.getUuid().toString());

            if (model == null || model.getCurrencies().isEmpty()) {
                ctx.sendMessage(Message.raw(isSelf ? "You have no currencies." : target.getUsername() + " has no currencies."));
                return CompletableFuture.completedFuture(null);
            }

            sendBalanceMessage(ctx, target.getUsername(), model, isSelf);
            return CompletableFuture.completedFuture(null);
        }

        // Player is offline - load from database by name
        return plugin.getCurrencyManager().getStorage().loadByNameAsync(targetName)
                .thenAccept(model -> {
                    if (model == null) {
                        ctx.sendMessage(Message.raw("Player not found."));
                        return;
                    }

                    if (model.getCurrencies().isEmpty()) {
                        String displayName = model.getPlayerName() != null ? model.getPlayerName() : targetName;
                        ctx.sendMessage(Message.raw(displayName + " has no currencies."));
                        return;
                    }

                    String displayName = model.getPlayerName() != null ? model.getPlayerName() : targetName;
                    sendBalanceMessage(ctx, displayName, model, false);
                });
    }

    private void sendBalanceMessage(CommandContext ctx, String playerName, CurrencyModel model, boolean isSelf) {
        String header = isSelf ? "Your balances:" : playerName + "'s balances:";
        String body = model.getCurrencies().entrySet().stream()
                .map(e -> {
                    String currencyId = e.getKey();
                    BigDecimal amount = e.getValue();
                    CurrencyMetadata meta = plugin.getCurrencyConfig() == null ? null : plugin.getCurrencyConfig().getCurrency(currencyId);
                    if (meta != null) {
                        return "  " + meta.getName() + ": " + meta.formatAmount(amount.toPlainString());
                    }
                    return "  " + currencyId + ": " + amount.toPlainString();
                })
                .collect(Collectors.joining("\n"));

        ctx.sendMessage(Message.raw(header + "\n" + body));
    }

}
