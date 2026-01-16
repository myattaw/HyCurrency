package com.hymines.currency.command.sub.leaderboard;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CurrencyTopCommand extends AbstractCommand {

    private static final int ENTRIES_PER_PAGE = 10;

    private final HyCurrencyPlugin plugin;
    private final RequiredArg<String> currencyArg;
    private final RequiredArg<Integer> pageArg;

    public CurrencyTopCommand(HyCurrencyPlugin plugin) {
        super("top", "View the currency leaderboard");
        this.addAliases("leaderboard", "lb");
        this.plugin = plugin;
        this.currencyArg = withRequiredArg("currency", "Currency type", ArgTypes.STRING);
        this.pageArg = withRequiredArg("page", "Page number", ArgTypes.INTEGER);

        // Add variant for just currency (no page)
        this.addUsageVariant(new CurrencyTopVariant(plugin));
    }

    @Nullable
    @Override
    public CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
        String currency = currencyArg.get(ctx);
        int page = pageArg.get(ctx);
        return executeTop(ctx, plugin, currency, page);
    }

    // Shared execution logic - package-private for variant access
    static CompletableFuture<Void> executeTop(CommandContext ctx, HyCurrencyPlugin plugin, String currency, int page) {
        if (page < 1) {
            ctx.sendMessage(Message.raw("Page number must be at least 1."));
            return CompletableFuture.completedFuture(null);
        }

        Map<String, CurrencyModel> currencyDataMap = plugin.getCurrencyDataMap();

        // Build sorted list of entries
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map.Entry<String, CurrencyModel> entry : currencyDataMap.entrySet()) {
            BigDecimal amount = entry.getValue().getCurrency(currency);
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                entries.add(new LeaderboardEntry(entry.getKey(), amount));
            }
        }

        // Sort by amount descending
        entries.sort(Comparator.comparing(LeaderboardEntry::amount).reversed());

        if (entries.isEmpty()) {
            ctx.sendMessage(Message.raw("No entries found for currency: " + currency));
            return CompletableFuture.completedFuture(null);
        }

        int totalPages = (int) Math.ceil((double) entries.size() / ENTRIES_PER_PAGE);

        if (page > totalPages) {
            ctx.sendMessage(Message.raw("Page " + page + " does not exist. Total pages: " + totalPages));
            return CompletableFuture.completedFuture(null);
        }

        int startIndex = (page - 1) * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, entries.size());

        StringBuilder message = new StringBuilder();
        message.append("=== ").append(currency).append(" Leaderboard (Page ").append(page).append("/").append(totalPages).append(") ===\n");

        for (int i = startIndex; i < endIndex; i++) {
            LeaderboardEntry entry = entries.get(i);
            String username = Universe.get().getPlayer(UUID.fromString(entry.playerId())).getUsername();
            message.append(i + 1).append(". ").append(username).append(": ").append(entry.amount()).append("\n");
        }

        ctx.sendMessage(Message.raw(message.toString().trim()));
        return CompletableFuture.completedFuture(null);
    }

}
