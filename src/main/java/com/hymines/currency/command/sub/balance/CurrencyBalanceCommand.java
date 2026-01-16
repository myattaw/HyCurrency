package com.hymines.currency.command.sub.balance;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CurrencyBalanceCommand extends AbstractCommand {

    private final HyCurrencyPlugin plugin;

    public CurrencyBalanceCommand(HyCurrencyPlugin plugin) {
        super("balance", "Check a player's currency balance");
        this.addAliases("bal");
        this.plugin = plugin;

        // Variant: /currency bal <player>
        this.addUsageVariant(new BalanceOtherVariant(plugin));
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        // Base usage: /currency bal  (self only)
        if (!(ctx.sender() instanceof Player player)) {
            ctx.sendMessage(Message.raw("Console must specify a player: /currency bal <player>"));
            return CompletableFuture.completedFuture(null);
        }

        return sendBalance(ctx, player.getPlayerRef(), true);
    }

    private CompletableFuture<Void> sendBalance(CommandContext ctx, PlayerRef target, boolean isSelf) {
        UUID targetUuid = target.getUuid();
        String targetName = target.getUsername();

        CurrencyModel model = plugin.getCurrencyDataMap().get(targetUuid.toString());

        if (model == null || model.getCurrencies().isEmpty()) {
            ctx.sendMessage(Message.raw(isSelf ? "You have no currencies." : targetName + " has no currencies."));
            return CompletableFuture.completedFuture(null);
        }

        ctx.sendMessage(Message.raw(formatBalances(targetName, model.getCurrencies(), isSelf)));
        return CompletableFuture.completedFuture(null);
    }

    private String formatBalances(String name, Map<String, BigDecimal> currencies, boolean isSelf) {
        String header = isSelf ? "Your balances:" : name + "'s balances:";
        String body = currencies.entrySet().stream()
                .map(e -> "  " + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

}
