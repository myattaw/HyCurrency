package com.hymines.currency.command.sub.balance;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BalanceOtherVariant extends AbstractCommand {

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<PlayerRef> playerArg;

    public BalanceOtherVariant(HyCurrencyPlugin plugin) {
        super("Check another player's currency balance");
        this.plugin = plugin;
        this.playerArg = withRequiredArg("player", "Player to check balance for", ArgTypes.PLAYER_REF);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        PlayerRef target = playerArg.get(ctx);

        boolean isSelf = ctx.sender() instanceof Player p && target.getUuid().equals(p.getUuid());

        // Reuse same logic (copy minimal to avoid making outer method static)
        CurrencyModel model = plugin.getCurrencyDataMap().get(target.getUuid().toString());
        if (model == null || model.getCurrencies().isEmpty()) {
            ctx.sendMessage(Message.raw(isSelf ? "You have no currencies." : target.getUsername() + " has no currencies."));
            return CompletableFuture.completedFuture(null);
        }

        String header = isSelf ? "Your balances:" : target.getUsername() + "'s balances:";
        String body = model.getCurrencies().entrySet().stream()
                .map(e -> "  " + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));

        ctx.sendMessage(Message.raw(header + "\n" + body));
        return CompletableFuture.completedFuture(null);
    }

}