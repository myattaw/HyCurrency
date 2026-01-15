package com.hymines.currency.command.sub;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
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
    private final OptionalArg<PlayerRef> targetPlayerArg;

    public CurrencyBalanceCommand(HyCurrencyPlugin plugin) {
        super("balance", "Check your currency balance");
        this.addAliases("bal");
        this.plugin = plugin;
        this.targetPlayerArg = withOptionalArg("player", "Player to check balance for", ArgTypes.PLAYER_REF);
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        PlayerRef specified = targetPlayerArg.get(ctx);

        UUID targetUuid;
        String targetName;
        boolean isSelf;

        if (specified != null) {
            targetUuid = specified.getUuid();
            targetName = specified.getUsername();
            isSelf = targetUuid.equals(ctx.sender().getUuid());
        } else {
            // No target specified, use sender
            if (!(ctx.sender() instanceof Player player)) {
                ctx.sendMessage(Message.raw("Console must specify a player. Usage: /currency balance <player>"));
                return CompletableFuture.completedFuture(null);
            }
            targetUuid = player.getUuid();
            targetName = player.getDisplayName();
            isSelf = true;
        }

        CurrencyModel model = plugin.getCurrencyDataMap().get(targetUuid.toString());

        if (model == null || model.getCurrencies().isEmpty()) {
            ctx.sendMessage(Message.raw(formatNoData(isSelf, targetName)));
            return CompletableFuture.completedFuture(null);
        }

        ctx.sendMessage(Message.raw(formatBalances(isSelf, targetName, model.getCurrencies())));
        return CompletableFuture.completedFuture(null);
    }

    private String formatNoData(boolean isSelf, String name) {
        return isSelf ? "You have no currencies." : name + " has no currencies.";
    }

    private String formatBalances(boolean isSelf, String name, Map<String, BigDecimal> currencies) {
        String header = isSelf ? "Your balances:" : name + "'s balances:";
        String body = currencies.entrySet().stream()
                .map(e -> "  " + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }
}
