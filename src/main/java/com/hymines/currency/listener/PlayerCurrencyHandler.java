package com.hymines.currency.listener;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.config.currency.CurrencyConfig;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PlayerCurrencyHandler {

    private final HyCurrencyPlugin plugin;

    private PlayerCurrencyHandler(HyCurrencyPlugin plugin) {
        this.plugin = plugin;
    }

    public static void register(HyCurrencyPlugin plugin) {
        PlayerCurrencyHandler listener = new PlayerCurrencyHandler(plugin);
        plugin.getEventRegistry().registerGlobal(PlayerConnectEvent.class, listener::onPlayerConnect);
        plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, listener::onPlayerDisconnect);
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();

        // Get or create the player's currency model
        CurrencyModel model = plugin.getCurrencyDataMap().computeIfAbsent(
                playerRef.getUuid().toString(), k -> new CurrencyModel()
        );

        // Auto-grant currencies that are configured with autoGrant=true
        CurrencyConfig currencyConfig = plugin.getCurrencyConfig();
        if (currencyConfig != null && currencyConfig.getCurrencies() != null) {
            currencyConfig.getCurrencies().forEach((currencyId, currencyEntry) -> {
                if (currencyEntry.isAutoGrant() && !model.hasCurrency(currencyId)) {
                    model.setCurrency(currencyId, currencyEntry.getDefaultAmount());
                    plugin.getLogger().atInfo().log("Auto-granted currency '" + currencyId + "' with default amount " + currencyEntry.getDefaultAmount() + " to player " + playerRef.getUsername());
                }
            });
        }
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        String playerUuid = playerRef.getUuid().toString();

        // Save player data to database, then remove from cache
        plugin.getCurrencyManager().savePlayer(playerUuid)
                .thenRun(() -> {
                    plugin.getCurrencyDataMap().remove(playerUuid);
                    plugin.getLogger().atInfo().log("Saved and unloaded currency data for player " + playerRef.getUsername());
                })
                .exceptionally(ex -> {
                    plugin.getLogger().atSevere().log("Failed to save currency data for player " + playerRef.getUsername() + ": " + ex.getMessage());
                    // Still remove from cache to prevent memory leaks
                    plugin.getCurrencyDataMap().remove(playerUuid);
                    return null;
                });
    }

}
