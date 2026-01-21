/*
 * MIT License
 *
 * Copyright (c) 2026 Michael Yattaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * See the LICENSE file in the project root for full license information.
 */

package com.hymines.currency.listener;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.config.CurrencyConfig;
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
        String playerUuid = playerRef.getUuid().toString();
        String playerName = playerRef.getUsername();
        // Load player currency data from database
        plugin.getCurrencyManager().loadPlayer(playerUuid)
                .thenAccept(model -> {
                    // Store the player's name
                    model.setPlayerName(playerName.toLowerCase());
                    // Auto-grant currencies that are configured with autoGrant=true
                    CurrencyConfig currencyConfig = plugin.getCurrencyConfig();
                    if (currencyConfig != null && currencyConfig.getCurrencies() != null) {
                        currencyConfig.getCurrencies().forEach((currencyId, currencyEntry) -> {
                            if (currencyEntry.isAutoGrant() && !model.hasCurrency(currencyId)) {
                                model.setCurrency(currencyId, currencyEntry.getDefaultAmount());
                                plugin.getLogger().atInfo().log("Auto-granted currency '" + currencyId + "' with default amount " + currencyEntry.getDefaultAmount() + " to player " + playerName);
                            }
                        });
                    }
                    plugin.getLogger().atInfo().log("Loaded currency data for player " + playerName);
                })
                .exceptionally(ex -> {
                    plugin.getLogger().atSevere().log("Failed to load currency data for player " + playerName + ": " + ex.getMessage());
                    return null;
                });
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

