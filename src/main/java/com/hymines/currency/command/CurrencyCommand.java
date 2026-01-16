package com.hymines.currency.command;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.command.sub.*;
import com.hymines.currency.command.sub.balance.CurrencyBalanceCommand;
import com.hymines.currency.command.sub.leaderboard.CurrencyTopCommand;
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
