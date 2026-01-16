package com.hymines.currency.command.sub.balance;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyMetadata;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

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
        this.addUsageVariant(new CurrencyBalanceOtherVariant(plugin));
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {

        // Base usage: /currency bal  (self only)
        if (!(ctx.sender() instanceof Player)) {
            ctx.sendMessage(Message.raw("Console must specify a player: /currency bal <player>"));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef selfRef = Universe.get().getPlayer(ctx.sender().getUuid());
        if (selfRef == null) {
            ctx.sendMessage(Message.raw("Could not find your player account."));
            return CompletableFuture.completedFuture(null);
        }

        return sendBalance(ctx, selfRef, true);
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
                .map(e -> {
                    String currencyId = e.getKey();
                    BigDecimal amount = e.getValue();
                    CurrencyMetadata meta = plugin.getCurrencyConfig() == null ? null : plugin.getCurrencyConfig().getCurrency(currencyId);
                    if (meta != null) {
                        return "  " + meta.getName() + ": " + meta.formatAmount(amount.toPlainString());
                    }
                    // fallback to id + raw amount
                    return "  " + currencyId + ": " + amount.toPlainString();
                })
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

}
