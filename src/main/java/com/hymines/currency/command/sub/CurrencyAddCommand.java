package com.hymines.currency.command.sub;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CurrencyAddCommand extends AbstractCommand {

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<PlayerRef> targetPlayerArg;
    private final RequiredArg<String> currencyArg;
    private final RequiredArg<Double> amountArg;

    public CurrencyAddCommand(HyCurrencyPlugin plugin) {
        super("add", "Add currency to a player's balance");
        this.plugin = plugin;
        this.targetPlayerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        this.currencyArg = withRequiredArg("currency", "Currency type", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount to add", ArgTypes.DOUBLE);
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef target = targetPlayerArg.get(commandContext);
        String currency = currencyArg.get(commandContext);
        BigDecimal amount = BigDecimal.valueOf(amountArg.get(commandContext));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            commandContext.sendMessage(Message.raw("Amount must be positive."));
            return CompletableFuture.completedFuture(null);
        }

        Map<String, CurrencyModel> currencyDataMap = plugin.getCurrencyDataMap();

        CurrencyModel targetModel = currencyDataMap.computeIfAbsent(
            target.getUuid().toString(), k -> new CurrencyModel()
        );

        targetModel.addAmount(currency, amount);
        BigDecimal newBalance = targetModel.getCurrency(currency);

        commandContext.sendMessage(Message.raw("Added " + amount + " " + currency + " to " + target.getUsername() + ". New balance: " + newBalance));
        return CompletableFuture.completedFuture(null);
    }
}
