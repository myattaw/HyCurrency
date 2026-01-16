# HyCurrency

A flexible currency management plugin for Hytale servers. Supports multiple currencies, player balances, payments, and leaderboards.

## Features

- **Multiple Currencies** - Define unlimited custom currencies with unique names and symbols
- **Player Balances** - Track and manage player balances with BigDecimal precision
- **Payments** - Allow players to pay each other
- **Leaderboards** - View top balances for any currency
- **Persistent Storage** - Data saved to database with automatic load/save on join/disconnect
- **Async Operations** - Non-blocking database operations for optimal performance

## Installation

1. Download the latest release of HyCurrency
2. Place the JAR file in your server's `mods` folder
3. Start the server to generate default configuration files
4. Configure currencies in `currency.json`
5. Restart the server

## Configuration

### currencies.yml

Define your custom currencies:

```json
{
  "currencies": {
    "money": {
      "name": "Money",
      "symbol": "$",
      "format": "%symbol%%amount%",
      "leaderboard": true,
      "autoGrant": true,
      "defaultAmount": 2500
    },
    "vote_points": {
      "name": "Vote Points",
      "symbol": "VP",
      "format": "%amount% %symbol%",
      "leaderboard": false,
      "autoGrant": false,
      "defaultAmount": 0
    }
  }
}
```

| Option | Description |
|--------|-------------|
| `name` | Display name of the currency |
| `symbol` | Symbol shown before/after amounts |
| `leaderboard` | Whether this currency appears in leaderboards |

## Commands

| Command | Description | Usage |
|---------|-------------|-------|
| `/currency balance` | Check your own balance | `/currency bal` |
| `/currency balance <player>` | Check another player's balance | `/currency bal Steve` |
| `/currency pay <player> <currency> <amount>` | Pay another player | `/currency pay Steve coins 100` |
| `/currency add <player> <currency> <amount>` | Add currency to a player | `/currency add Steve coins 500` |
| `/currency remove <player> <currency> <amount>` | Remove currency from a player | `/currency remove Steve coins 50` |
| `/currency set <player> <currency> <amount>` | Set a player's balance | `/currency set Steve coins 1000` |
| `/currency top <currency>` | View leaderboard for a currency | `/currency top coins` |
| `/currency list` | List all available currencies | `/currency currencies` |

### Command Aliases

- `balance` → `bal`
- `remove` → `take`
- `list` → `currencies`

## API Usage

### Getting the Plugin Instance

```java
HyCurrencyPlugin plugin = HyCurrencyPlugin.getInstance();
```

### Working with Balances

```java
CurrencyManager manager = plugin.getCurrencyManager();
String playerUuid = player.getUuid().toString();

// Get balance
BigDecimal balance = manager.getBalance(playerUuid, "coins");

// Set balance
manager.setBalance(playerUuid, "coins", BigDecimal.valueOf(1000));

// Add to balance
manager.addBalance(playerUuid, "coins", BigDecimal.valueOf(100));

// Check if player has enough
boolean canAfford = manager.hasBalance(playerUuid, "coins", BigDecimal.valueOf(50));
```

### Direct Model Access

```java
Map<String, CurrencyModel> dataMap = plugin.getCurrencyDataMap();
CurrencyModel model = dataMap.get(playerUuid);

// Get all currencies for a player
Map<String, BigDecimal> currencies = model.getCurrencies();

// Modify directly
model.addAmount("coins", BigDecimal.valueOf(100));
model.setCurrency("gems", BigDecimal.valueOf(50));
```

### Leaderboards

```java
// Get top balances (async)
manager.getTopBalances("coins", 10).thenAccept(topBalances -> {
    topBalances.forEach((uuid, rank) -> {
        System.out.println(rank + ": " + uuid);
    });
});

// Get cached leaderboard (sync, may be null)
Map<String, Integer> cached = manager.getCachedLeaderboard("coins");
```

### Loading/Saving Player Data

```java
// Load player data
manager.loadPlayer(playerUuid).thenAccept(model -> {
    plugin.getCurrencyDataMap().put(playerUuid, model);
});

// Save player data
manager.savePlayer(playerUuid).thenRun(() -> {
    System.out.println("Player data saved!");
});
```

## Data Storage

Player currency data is automatically:
- **Loaded** when a player joins the server
- **Saved** when a player disconnects
- **Cached** in memory for fast access during gameplay

## Support

For issues and feature requests, please open an issue on GitHub.

