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

public class CurrencySetCommand extends AbstractCommand {

    private final HyCurrencyPlugin plugin;

    private final RequiredArg<PlayerRef> targetPlayerArg;
    private final RequiredArg<String> currencyArg;
    private final RequiredArg<Double> amountArg;

    public CurrencySetCommand(HyCurrencyPlugin plugin) {
        super("set", "Set a player's currency balance");
        this.plugin = plugin;
        this.targetPlayerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        this.currencyArg = withRequiredArg("currency", "Currency type", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount to set", ArgTypes.DOUBLE);
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef target = targetPlayerArg.get(commandContext);
        String currency = currencyArg.get(commandContext);
        BigDecimal amount = BigDecimal.valueOf(amountArg.get(commandContext));

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            commandContext.sendMessage(Message.raw("Amount cannot be negative."));
            return CompletableFuture.completedFuture(null);
        }

        Map<String, CurrencyModel> currencyDataMap = plugin.getCurrencyDataMap();

        CurrencyModel targetModel = currencyDataMap.computeIfAbsent(
            target.getUuid().toString(), k -> new CurrencyModel()
        );

        targetModel.setCurrency(currency, amount);

        commandContext.sendMessage(Message.raw("Set " + target.getUsername() + "'s " + currency + " balance to " + amount + "."));
        return CompletableFuture.completedFuture(null);
    }
}
