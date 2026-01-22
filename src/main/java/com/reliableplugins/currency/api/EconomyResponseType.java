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

public enum EconomyResponseType {

    /**
     * Operation completed successfully.
     */
    SUCCESS,

    /**
     * Operation failed for a generic reason.
     */
    FAILURE,

    /**
     * The account does not exist.
     */
    ACCOUNT_NOT_FOUND,

    /**
     * The player is not online (for sync operations).
     */
    PLAYER_NOT_ONLINE,

    /**
     * Insufficient funds to complete the operation.
     */
    INSUFFICIENT_FUNDS,

    /**
     * The currency type is not supported or does not exist.
     */
    INVALID_CURRENCY,

    /**
     * The amount provided is invalid (e.g., negative).
     */
    INVALID_AMOUNT,

    /**
     * An internal error occurred during the operation.
     */
    INTERNAL_ERROR

}

