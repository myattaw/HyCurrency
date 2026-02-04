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

package com.reliableplugins.currency.api;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Economy API for currency management.
 * <p>
 * This API provides two types of methods:
 * <ul>
 *     <li><b>Sync methods</b> - For online players only. These methods will return
 *     {@link EconomyResponseType#PLAYER_NOT_ONLINE} if the player is offline.</li>
 *     <li><b>Async methods</b> - For any player (online or offline). These return
 *     {@link CompletableFuture} and may involve database operations.</li>
 * </ul>
 */
public interface Economy {

    /**
     * @return The default currency identifier
     */
    @Nonnull
    default String getDefaultCurrency() {
        return "money";
    }

    /**
     * Checks if a currency exists.
     *
     * @param currency The currency identifier
     * @return true if the currency exists
     */
    boolean currencyExists(@Nonnull String currency);

    /**
     * Gets the display name of a currency.
     *
     * @param currency The currency identifier
     * @return The display name, or the identifier if not found
     */
    @Nonnull
    String getCurrencyDisplayName(@Nonnull String currency);

    /**
     * Formats an amount for display.
     *
     * @param amount   The amount to format
     * @param currency The currency identifier
     * @return The formatted amount string
     */
    @Nonnull
    String format(@Nonnull BigDecimal amount, @Nonnull String currency);

    /**
     * Formats an amount for display.
     *
     * @param amount   The amount to format as double
     * @param currency The currency identifier
     * @return The formatted amount string
     */
    @Nonnull
    default String format(double amount, @Nonnull String currency) {
        return format(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Formats an amount using the default currency.
     *
     * @param amount The amount to format
     * @return The formatted amount string
     */
    @Nonnull
    default String format(@Nonnull BigDecimal amount) {
        return format(amount, getDefaultCurrency());
    }

    /**
     * Formats an amount using the default currency.
     *
     * @param amount The amount to format as double
     * @return The formatted amount string
     */
    @Nonnull
    default String format(double amount) {
        return format(amount, getDefaultCurrency());
    }

    /**
     * Checks if an account exists for the given player.
     *
     * @param playerId The player's UUID
     * @return CompletableFuture resolving to true if the account exists
     */
    @Nonnull
    CompletableFuture<Boolean> hasAccount(@Nonnull UUID playerId);

    /**
     * Creates an account for the given player.
     *
     * @param playerId   The player's UUID
     * @param playerName The player's name (for logging/display purposes)
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> createAccountAsync(@Nonnull UUID playerId, @Nonnull String playerName);

    /**
     * Checks if the player is online and has cached data available.
     *
     * @param playerId The player's UUID
     * @return true if sync operations can be performed
     */
    boolean isPlayerOnline(@Nonnull UUID playerId);

    /**
     * Gets the balance of an online player.
     * <p>
     * <b>Note:</b> This method only works for online players with cached data.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @return The response containing the balance, or PLAYER_NOT_ONLINE if offline
     */
    @Nonnull
    EconomyResponse getBalance(@Nonnull UUID playerId, @Nonnull String currency);

    /**
     * Gets the balance of an online player using the default currency.
     *
     * @param playerId The player's UUID
     * @return The response containing the balance
     */
    @Nonnull
    default EconomyResponse getBalance(@Nonnull UUID playerId) {
        return getBalance(playerId, getDefaultCurrency());
    }

    /**
     * Checks if an online player has at least the specified amount.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to check
     * @return The response (SUCCESS if they have enough, INSUFFICIENT_FUNDS otherwise)
     */
    @Nonnull
    EconomyResponse has(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Checks if an online player has at least the specified amount.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to check as double
     * @return The response (SUCCESS if they have enough, INSUFFICIENT_FUNDS otherwise)
     */
    @Nonnull
    default EconomyResponse has(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return has(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Checks if an online player has at least the specified amount using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to check
     * @return The response
     */
    @Nonnull
    default EconomyResponse has(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return has(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Checks if an online player has at least the specified amount using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to check as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse has(@Nonnull UUID playerId, double amount) {
        return has(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Withdraws an amount from an online player's balance.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to withdraw
     * @return The response containing the new balance
     */
    @Nonnull
    EconomyResponse withdraw(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Withdraws an amount from an online player's balance.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to withdraw as double
     * @return The response containing the new balance
     */
    @Nonnull
    default EconomyResponse withdraw(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return withdraw(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Withdraws an amount from an online player using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to withdraw
     * @return The response
     */
    @Nonnull
    default EconomyResponse withdraw(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return withdraw(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Withdraws an amount from an online player using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to withdraw as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse withdraw(@Nonnull UUID playerId, double amount) {
        return withdraw(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Deposits an amount to an online player's balance.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to deposit
     * @return The response containing the new balance
     */
    @Nonnull
    EconomyResponse deposit(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Deposits an amount to an online player's balance.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to deposit as double
     * @return The response containing the new balance
     */
    @Nonnull
    default EconomyResponse deposit(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return deposit(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Deposits an amount to an online player using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to deposit
     * @return The response
     */
    @Nonnull
    default EconomyResponse deposit(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return deposit(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Deposits an amount to an online player using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to deposit as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse deposit(@Nonnull UUID playerId, double amount) {
        return deposit(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Sets an online player's balance to a specific amount.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The new balance
     * @return The response
     */
    @Nonnull
    EconomyResponse setBalance(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Sets an online player's balance to a specific amount.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The new balance as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse setBalance(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return setBalance(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Sets an online player's balance using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The new balance
     * @return The response
     */
    @Nonnull
    default EconomyResponse setBalance(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return setBalance(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Sets an online player's balance using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The new balance as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse setBalance(@Nonnull UUID playerId, double amount) {
        return setBalance(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Gets the balance of any player asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> getBalanceAsync(@Nonnull UUID playerId, @Nonnull String currency);

    /**
     * Gets the balance of any player using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> getBalanceAsync(@Nonnull UUID playerId) {
        return getBalanceAsync(playerId, getDefaultCurrency());
    }

    /**
     * Checks if any player has at least the specified amount asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to check
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> hasAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Checks if any player has at least the specified amount asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to check as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> hasAsync(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return hasAsync(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Checks if any player has at least the specified amount using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to check
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> hasAsync(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return hasAsync(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Checks if any player has at least the specified amount using default currency.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to check as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> hasAsync(@Nonnull UUID playerId, double amount) {
        return hasAsync(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Withdraws an amount from any player's balance asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to withdraw
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> withdrawAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Withdraws an amount from any player's balance asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to withdraw as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> withdrawAsync(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return withdrawAsync(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Withdraws an amount from any player using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to withdraw
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> withdrawAsync(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return withdrawAsync(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Withdraws an amount from any player using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to withdraw as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> withdrawAsync(@Nonnull UUID playerId, double amount) {
        return withdrawAsync(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Deposits an amount to any player's balance asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to deposit
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> depositAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Deposits an amount to any player's balance asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to deposit as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> depositAsync(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return depositAsync(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Deposits an amount to any player using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to deposit
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> depositAsync(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return depositAsync(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Deposits an amount to any player using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The amount to deposit as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> depositAsync(@Nonnull UUID playerId, double amount) {
        return depositAsync(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Sets any player's balance to a specific amount asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The new balance
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> setBalanceAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Sets any player's balance to a specific amount asynchronously.
     *
     * @param playerId The player's UUID
     * @param currency The currency identifier
     * @param amount   The new balance as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> setBalanceAsync(@Nonnull UUID playerId, @Nonnull String currency, double amount) {
        return setBalanceAsync(playerId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Sets any player's balance using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The new balance
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> setBalanceAsync(@Nonnull UUID playerId, @Nonnull BigDecimal amount) {
        return setBalanceAsync(playerId, getDefaultCurrency(), amount);
    }

    /**
     * Sets any player's balance using default currency asynchronously.
     *
     * @param playerId The player's UUID
     * @param amount   The new balance as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> setBalanceAsync(@Nonnull UUID playerId, double amount) {
        return setBalanceAsync(playerId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Transfers an amount between two online players synchronously.
     *
     * @param fromId   The source player's UUID
     * @param toId     The destination player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to transfer
     * @return The response
     */
    @Nonnull
    EconomyResponse transfer(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Transfers an amount between two online players synchronously.
     *
     * @param fromId   The source player's UUID
     * @param toId     The destination player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to transfer as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse transfer(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull String currency, double amount) {
        return transfer(fromId, toId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Transfers an amount between two online players using default currency.
     *
     * @param fromId The source player's UUID
     * @param toId   The destination player's UUID
     * @param amount The amount to transfer
     * @return The response
     */
    @Nonnull
    default EconomyResponse transfer(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull BigDecimal amount) {
        return transfer(fromId, toId, getDefaultCurrency(), amount);
    }

    /**
     * Transfers an amount between two online players using default currency.
     *
     * @param fromId The source player's UUID
     * @param toId   The destination player's UUID
     * @param amount The amount to transfer as double
     * @return The response
     */
    @Nonnull
    default EconomyResponse transfer(@Nonnull UUID fromId, @Nonnull UUID toId, double amount) {
        return transfer(fromId, toId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Transfers an amount between any two players asynchronously.
     *
     * @param fromId   The source player's UUID
     * @param toId     The destination player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to transfer
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    CompletableFuture<EconomyResponse> transferAsync(@Nonnull UUID fromId, @Nonnull UUID toId,
                                                     @Nonnull String currency, @Nonnull BigDecimal amount);

    /**
     * Transfers an amount between any two players asynchronously.
     *
     * @param fromId   The source player's UUID
     * @param toId     The destination player's UUID
     * @param currency The currency identifier
     * @param amount   The amount to transfer as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> transferAsync(@Nonnull UUID fromId, @Nonnull UUID toId,
                                                             @Nonnull String currency, double amount) {
        return transferAsync(fromId, toId, currency, BigDecimal.valueOf(amount));
    }

    /**
     * Transfers an amount between any two players using default currency asynchronously.
     *
     * @param fromId The source player's UUID
     * @param toId   The destination player's UUID
     * @param amount The amount to transfer
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> transferAsync(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull BigDecimal amount) {
        return transferAsync(fromId, toId, getDefaultCurrency(), amount);
    }

    /**
     * Transfers an amount between any two players using default currency asynchronously.
     *
     * @param fromId The source player's UUID
     * @param toId   The destination player's UUID
     * @param amount The amount to transfer as double
     * @return CompletableFuture resolving to the response
     */
    @Nonnull
    default CompletableFuture<EconomyResponse> transferAsync(@Nonnull UUID fromId, @Nonnull UUID toId, double amount) {
        return transferAsync(fromId, toId, getDefaultCurrency(), BigDecimal.valueOf(amount));
    }

    /**
     * Gets the top balances for a currency from currently cached players.
     * <p>
     * This only includes online players.
     *
     * @param currency The currency identifier
     * @return Map of player UUID to balance, sorted descending
     */
    @Nonnull
    Map<UUID, BigDecimal> getTopBalances(@Nonnull String currency);

    @Nonnull
    default Map<UUID, BigDecimal> getTopBalances() {
        return getTopBalances(getDefaultCurrency());
    }


}

