package com.hymines.currency.config;

public class CurrencyEntry {

    private String name;
    private String symbol;
    private String format;
    private boolean leaderboard;

    // Required by Gson
    public CurrencyEntry() {
    }

    public CurrencyEntry(String name, String symbol, String format, boolean leaderboard) {
        this.name = name;
        this.symbol = symbol;
        this.format = format;
        this.leaderboard = leaderboard;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFormat() {
        return format;
    }

    public boolean isLeaderboard() {
        return leaderboard;
    }

    public String formatAmount(String amount) {
        return format.replace("%symbol%", symbol).replace("%amount%", amount);
    }

}