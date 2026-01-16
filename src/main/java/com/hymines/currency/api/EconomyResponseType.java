package com.hymines.currency.api;

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
