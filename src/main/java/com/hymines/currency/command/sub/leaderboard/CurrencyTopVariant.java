package com.hymines.currency.command.sub.leaderboard;

import com.hymines.currency.HyCurrencyPlugin;
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