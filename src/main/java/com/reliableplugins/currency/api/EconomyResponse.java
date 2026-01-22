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
import javax.annotation.Nullable;
import java.math.BigDecimal;

public class EconomyResponse {

    private final EconomyResponseType type;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final String errorMessage;

    /**
     * Creates a new EconomyResponse.
     *
     * @param type         The response type
     * @param amount       The amount involved in the transaction
     * @param balance      The new balance after the transaction
     * @param errorMessage An optional error message
     */
    public EconomyResponse(@Nonnull EconomyResponseType type, @Nonnull BigDecimal amount,
                           @Nonnull BigDecimal balance, @Nullable String errorMessage) {
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.errorMessage = errorMessage;
    }

    /**
     * @return The response type indicating success or failure reason
     */
    @Nonnull
    public EconomyResponseType getType() {
        return type;
    }

    /**
     * @return The amount involved in the transaction
     */
    @Nonnull
    public BigDecimal getAmountExact() {
        return amount;
    }

    /**
     * @return The amount as a double
     */
    public double getAmount() {
        return toSafeDouble(amount);
    }

    /**
     * @return The balance after the transaction (or current balance for queries)
     */
    @Nonnull
    public BigDecimal getBalanceExact() {
        return balance;
    }

    /**
     * @return The balance as a double
     */
    public double getBalance() {
        return toSafeDouble(balance);
    }

    /**
     * @return An error message if the operation failed, null otherwise
     */
    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return true if the operation was successful
     */
    public boolean isSuccess() {
        return type == EconomyResponseType.SUCCESS;
    }


    private static double toSafeDouble(BigDecimal value) {
        double d = value.doubleValue();
        if (BigDecimal.valueOf(d).compareTo(value) > 0) {
            d = Math.nextAfter(d, Double.NEGATIVE_INFINITY);
        }
        return d;
    }

    // Static factory methods for convenience

    public static EconomyResponse success(@Nonnull BigDecimal amount, @Nonnull BigDecimal balance) {
        return new EconomyResponse(EconomyResponseType.SUCCESS, amount, balance, null);
    }

    public static EconomyResponse failure(@Nonnull EconomyResponseType type, @Nullable String errorMessage) {
        return new EconomyResponse(type, BigDecimal.ZERO, BigDecimal.ZERO, errorMessage);
    }

    public static EconomyResponse failure(@Nonnull EconomyResponseType type, @Nonnull BigDecimal balance, @Nullable String errorMessage) {
        return new EconomyResponse(type, BigDecimal.ZERO, balance, errorMessage);
    }

    public static EconomyResponse insufficientFunds(@Nonnull BigDecimal balance) {
        return new EconomyResponse(EconomyResponseType.INSUFFICIENT_FUNDS, BigDecimal.ZERO, balance, "Insufficient funds");
    }

    public static EconomyResponse playerNotOnline() {
        return failure(EconomyResponseType.PLAYER_NOT_ONLINE, "Player is not online");
    }

    public static EconomyResponse accountNotFound() {
        return failure(EconomyResponseType.ACCOUNT_NOT_FOUND, "Account does not exist");
    }

    public static EconomyResponse invalidCurrency(@Nonnull String currency) {
        return failure(EconomyResponseType.INVALID_CURRENCY, "Invalid currency: " + currency);
    }

    public static EconomyResponse invalidAmount() {
        return failure(EconomyResponseType.INVALID_AMOUNT, "Amount must be positive");
    }

    public static EconomyResponse internalError(@Nullable String message) {
        return failure(EconomyResponseType.INTERNAL_ERROR, message);
    }

    @Override
    public String toString() {
        return "EconomyResponse{" +
                "type=" + type +
                ", amount=" + amount +
                ", balance=" + balance +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}

