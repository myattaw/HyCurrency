# HyCurrency

A flexible currency management plugin for Hytale servers. Supports multiple currencies, player balances, payments, and leaderboards.

## Features

- **Multiple Currencies** - Define unlimited custom currencies with unique names and symbols
- **Player Balances** - Track and manage player balances with BigDecimal precision
- **Payments** - Allow players to pay each other
- **Leaderboards** - View top balances for any currency
- **Persistent Storage** - Data saved to database with automatic load/save on join/disconnect
- **Async Operations** - Non-blocking database operations for optimal performance
- **Modern Economy API** - Sync operations for online players, async for any player

## Installation

1. Download the latest release of HyCurrency
2. Place the JAR file in your server's `mods` folder
3. Start the server to generate default configuration files
4. Configure currencies in `currency.json`
5. Restart the server

## Configuration

### currency.json

Define your custom currencies:

```json
{
  "currencies": {
    "money": {
      "id": "money",
      "name": "Money",
      "symbol": "$",
      "format": "%symbol%%amount%",
      "leaderboard": true,
      "autoGrant": true,
      "defaultAmount": 2500
    },
    "vote_points": {
      "id": "vote_points",
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
| `id` | Unique identifier for the currency |
| `name` | Display name of the currency |
| `symbol` | Symbol shown before/after amounts |
| `format` | Format string using `%symbol%` and `%amount%` placeholders |
| `leaderboard` | Whether this currency appears in leaderboards |
| `autoGrant` | Whether new players automatically receive this currency |
| `defaultAmount` | Starting balance for new players (if autoGrant is true) |

## Commands

| Command | Description | Usage |
|---------|-------------|-------|
| `/currency balance` | Check your own balance | `/currency bal` |
| `/currency balance <player>` | Check another player's balance | `/currency bal Steve` |
| `/currency pay <player> <currency> <amount>` | Pay another player | `/currency pay Steve money 100` |
| `/currency add <player> <currency> <amount>` | Add currency to a player | `/currency add Steve money 500` |
| `/currency remove <player> <currency> <amount>` | Remove currency from a player | `/currency remove Steve money 50` |
| `/currency set <player> <currency> <amount>` | Set a player's balance | `/currency set Steve money 1000` |
| `/currency top <currency>` | View leaderboard for a currency | `/currency top money` |
| `/currency list` | List all available currencies | `/currency currencies` |

### Command Aliases

- `balance` → `bal`
- `remove` → `take`
- `list` → `currencies`

## API Usage

HyCurrency provides a modern Economy API with two types of operations:
- **Sync methods** - For online players only (instant, uses cached data)
- **Async methods** - For any player, online or offline (returns `CompletableFuture`)

### Getting the Economy API

```java
HyCurrencyPlugin plugin = ...; // Get plugin instance
Economy economy = plugin.getEconomy();
```

### Sync Operations (Online Players Only)

Sync methods are fast and use cached player data. They return an `EconomyResponse` immediately.
If the player is offline, the response will have type `PLAYER_NOT_ONLINE`.

```java
UUID playerId = player.getUuid();

// Check if player is online (has cached data)
if (economy.isPlayerOnline(playerId)) {
    
    // Get balance
    EconomyResponse response = economy.getBalance(playerId, "money");
    if (response.isSuccess()) {
        BigDecimal balance = response.getBalance();
    }
    
    // Check if player has enough
    EconomyResponse hasResponse = economy.has(playerId, "money", BigDecimal.valueOf(100));
    if (hasResponse.isSuccess()) {
        // Player has at least 100
    } else if (hasResponse.getType() == EconomyResponseType.INSUFFICIENT_FUNDS) {
        // Player doesn't have enough
    }
    
    // Withdraw
    EconomyResponse withdrawResponse = economy.withdraw(playerId, "money", BigDecimal.valueOf(50));
    if (withdrawResponse.isSuccess()) {
        BigDecimal newBalance = withdrawResponse.getBalance();
    }
    
    // Deposit
    EconomyResponse depositResponse = economy.deposit(playerId, "money", BigDecimal.valueOf(100));
    
    // Set balance
    EconomyResponse setResponse = economy.setBalance(playerId, "money", BigDecimal.valueOf(1000));
    
    // Transfer between two online players
    UUID targetId = targetPlayer.getUuid();
    EconomyResponse transferResponse = economy.transfer(playerId, targetId, "money", BigDecimal.valueOf(50));
}
```

### Using Default Currency

All methods have overloads that use the default currency ("money"):

```java
// These are equivalent:
economy.getBalance(playerId, "money");
economy.getBalance(playerId);

economy.withdraw(playerId, "money", amount);
economy.withdraw(playerId, amount);
```

### Async Operations (Any Player)

Async methods work for both online and offline players. They return `CompletableFuture` and may involve database operations.

```java
UUID playerId = UUID.fromString("...

// Get balance (works for offline players)
economy.getBalanceAsync(playerId, "money").thenAccept(response -> {
    if (response.isSuccess()) {
        BigDecimal balance = response.getBalance();
        System.out.println("Balance: " + balance);
    } else if (response.getType() == EconomyResponseType.ACCOUNT_NOT_FOUND) {
        System.out.println("Player has no account");
    }
});

// Withdraw from offline player
economy.withdrawAsync(playerId, "money", BigDecimal.valueOf(100)).thenAccept(response -> {
    if (response.isSuccess()) {
        System.out.println("Withdrew successfully, new balance: " + response.getBalance());
    } else {
        System.out.println("Failed: " + response.getErrorMessage());
    }
});

// Deposit to offline player
economy.depositAsync(playerId, "money", BigDecimal.valueOf(500)).thenAccept(response -> {
    if (response.isSuccess()) {
        System.out.println("Deposited successfully");
    }
});

// Transfer between any players (online or offline)
economy.transferAsync(fromId, toId, "money", BigDecimal.valueOf(100)).thenAccept(response -> {
    if (response.isSuccess()) {
        System.out.println("Transfer complete");
    }
});

// Check if account exists
economy.hasAccount(playerId).thenAccept(exists -> {
    if (!exists) {
        // Create account
        economy.createAccountAsync(playerId, "PlayerName").thenAccept(response -> {
            System.out.println("Account created");
        });
    }
});
```

### Handling EconomyResponse

```java
EconomyResponse response = economy.withdraw(playerId, "money", amount);

switch (response.getType()) {
    case SUCCESS:
        System.out.println("Success! New balance: " + response.getBalance());
        break;
    case INSUFFICIENT_FUNDS:
        System.out.println("Not enough funds. Current balance: " + response.getBalance());
        break;
    case PLAYER_NOT_ONLINE:
        System.out.println("Player is offline, use async method instead");
        break;
    case ACCOUNT_NOT_FOUND:
        System.out.println("Player has no account");
        break;
    case INVALID_CURRENCY:
        System.out.println("Currency doesn't exist");
        break;
    case INVALID_AMOUNT:
        System.out.println("Amount must be positive");
        break;
    case INTERNAL_ERROR:
        System.out.println("Error: " + response.getErrorMessage());
        break;
}
```

### Currency Information

```java
// Check if currency exists
boolean exists = economy.currencyExists("money");

// Get display name
String name = economy.getCurrencyDisplayName("money"); // "Money"

// Format amount for display
String formatted = economy.format(BigDecimal.valueOf(1234.56), "money"); // "$1,234.56"
```

## Response Types

| Type | Description |
|------|-------------|
| `SUCCESS` | Operation completed successfully |
| `FAILURE` | Generic failure |
| `ACCOUNT_NOT_FOUND` | Player has no account |
| `PLAYER_NOT_ONLINE` | Sync operation attempted on offline player |
| `INSUFFICIENT_FUNDS` | Not enough balance for withdrawal/transfer |
| `INVALID_CURRENCY` | Currency doesn't exist |
| `INVALID_AMOUNT` | Amount is negative or zero (when not allowed) |
| `INTERNAL_ERROR` | Database or internal error |

## Data Storage

Player currency data is automatically:
- **Loaded** when a player joins the server
- **Saved** when a player disconnects
- **Cached** in memory for fast sync access during gameplay
- **Persisted** immediately for async operations on offline players

## Support

For issues and feature requests, please open an issue on GitHub.
