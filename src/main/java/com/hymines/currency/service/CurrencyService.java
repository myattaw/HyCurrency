package com.hymines.currency.service;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.api.Economy;
import com.hymines.currency.api.EconomyResponse;
import com.hymines.currency.config.CurrencyConfig;
import com.hymines.currency.model.CurrencyManager;
import com.hymines.currency.model.CurrencyMetadata;
import com.hymines.currency.model.CurrencyModel;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CurrencyService implements Economy {

    private final HyCurrencyPlugin plugin;
    private final CurrencyManager currencyManager;

    public CurrencyService(@Nonnull HyCurrencyPlugin plugin, @Nonnull CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
    }

    @Override
    public boolean currencyExists(@Nonnull String currency) {
        CurrencyConfig config = plugin.getCurrencyConfig();
        return config.getCurrencies() != null && config.getCurrencies().containsKey(currency);
    }

    @Nonnull
    @Override
    public String getCurrencyDisplayName(@Nonnull String currency) {
        CurrencyMetadata metadata = plugin.getCurrencyConfig().getCurrency(currency);
        return metadata != null && metadata.getName() != null ? metadata.getName() : currency;
    }

    @Nonnull
    @Override
    public String format(@Nonnull BigDecimal amount, @Nonnull String currency) {
        CurrencyMetadata metadata = plugin.getCurrencyConfig().getCurrency(currency);
        if (metadata != null) {
            return metadata.formatAmount(amount.toPlainString());
        }
        return amount.toPlainString() + " " + currency;
    }

    @Nonnull
    @Override
    public CompletableFuture<Boolean> hasAccount(@Nonnull UUID playerId) {
        // If online, they have an account
        if (isPlayerOnline(playerId)) {
            return CompletableFuture.completedFuture(true);
        }
        return currencyManager.getStorage().loadAsync(playerId.toString()).thenApply(Objects::nonNull);
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> createAccountAsync(@Nonnull UUID playerId, @Nonnull String playerName) {
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenCompose(existing -> {
                    if (existing != null) {
                        return CompletableFuture.completedFuture(
                                EconomyResponse.success(BigDecimal.ZERO, existing.getCurrency(getDefaultCurrency()))
                        );
                    }
                    CurrencyModel model = createDefaultModel();
                    return currencyManager.getStorage().saveAsync(playerId.toString(), model)
                            .thenApply(v -> EconomyResponse.success(BigDecimal.ZERO, model.getCurrency(getDefaultCurrency())));
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    private CurrencyModel createDefaultModel() {
        CurrencyModel model = new CurrencyModel();
        CurrencyConfig config = plugin.getCurrencyConfig();
        if (config.getCurrencies() != null) {
            for (var entry : config.getCurrencies().entrySet()) {
                CurrencyMetadata metadata = entry.getValue();
                if (metadata.isAutoGrant()) {
                    model.setCurrency(entry.getKey(), metadata.getDefaultAmount());
                }
            }
        }
        return model;
    }

    @Override
    public boolean isPlayerOnline(@Nonnull UUID playerId) {
        return plugin.getCurrencyDataMap().containsKey(playerId.toString());
    }

    private CurrencyModel getOnlinePlayerModel(@Nonnull UUID playerId) {
        return plugin.getCurrencyDataMap().get(playerId.toString());
    }

    @Nonnull
    @Override
    public EconomyResponse getBalance(@Nonnull UUID playerId, @Nonnull String currency) {
        if (!isPlayerOnline(playerId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        CurrencyModel model = getOnlinePlayerModel(playerId);
        BigDecimal balance = model.getCurrency(currency);
        return EconomyResponse.success(balance, balance);
    }

    @Nonnull
    @Override
    public EconomyResponse has(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (!isPlayerOnline(playerId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return EconomyResponse.invalidAmount();
        }
        CurrencyModel model = getOnlinePlayerModel(playerId);
        BigDecimal balance = model.getCurrency(currency);
        if (balance.compareTo(amount) >= 0) {
            return EconomyResponse.success(amount, balance);
        }
        return EconomyResponse.insufficientFunds(balance);
    }

    @Nonnull
    @Override
    public EconomyResponse withdraw(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (!isPlayerOnline(playerId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return EconomyResponse.invalidAmount();
        }
        CurrencyModel model = getOnlinePlayerModel(playerId);
        BigDecimal balance = model.getCurrency(currency);
        if (balance.compareTo(amount) < 0) {
            return EconomyResponse.insufficientFunds(balance);
        }
        BigDecimal newBalance = balance.subtract(amount);
        model.setCurrency(currency, newBalance);
        return EconomyResponse.success(amount, newBalance);
    }

    @Nonnull
    @Override
    public EconomyResponse deposit(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (!isPlayerOnline(playerId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return EconomyResponse.invalidAmount();
        }
        CurrencyModel model = getOnlinePlayerModel(playerId);
        BigDecimal balance = model.getCurrency(currency);
        BigDecimal newBalance = balance.add(amount);
        model.setCurrency(currency, newBalance);
        return EconomyResponse.success(amount, newBalance);
    }

    @Nonnull
    @Override
    public EconomyResponse setBalance(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (!isPlayerOnline(playerId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return EconomyResponse.invalidAmount();
        }
        CurrencyModel model = getOnlinePlayerModel(playerId);
        model.setCurrency(currency, amount);
        return EconomyResponse.success(amount, amount);
    }

    @Nonnull
    @Override
    public EconomyResponse transfer(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (!isPlayerOnline(fromId) || !isPlayerOnline(toId)) {
            return EconomyResponse.playerNotOnline();
        }
        if (!currencyExists(currency)) {
            return EconomyResponse.invalidCurrency(currency);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return EconomyResponse.invalidAmount();
        }

        CurrencyModel fromModel = getOnlinePlayerModel(fromId);
        CurrencyModel toModel = getOnlinePlayerModel(toId);

        BigDecimal fromBalance = fromModel.getCurrency(currency);
        if (fromBalance.compareTo(amount) < 0) {
            return EconomyResponse.insufficientFunds(fromBalance);
        }

        fromModel.setCurrency(currency, fromBalance.subtract(amount));
        toModel.setCurrency(currency, toModel.getCurrency(currency).add(amount));

        return EconomyResponse.success(amount, fromModel.getCurrency(currency));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> getBalanceAsync(@Nonnull UUID playerId, @Nonnull String currency) {
        if (isPlayerOnline(playerId)) {
            return CompletableFuture.completedFuture(getBalance(playerId, currency));
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenApply(model -> {
                    if (model == null) {
                        return EconomyResponse.accountNotFound();
                    }
                    BigDecimal balance = model.getCurrency(currency);
                    return EconomyResponse.success(balance, balance);
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> hasAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (isPlayerOnline(playerId)) {
            return CompletableFuture.completedFuture(has(playerId, currency, amount));
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidAmount());
        }
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenApply(model -> {
                    if (model == null) {
                        return EconomyResponse.accountNotFound();
                    }
                    BigDecimal balance = model.getCurrency(currency);
                    if (balance.compareTo(amount) >= 0) {
                        return EconomyResponse.success(amount, balance);
                    }
                    return EconomyResponse.insufficientFunds(balance);
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> withdrawAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (isPlayerOnline(playerId)) {
            EconomyResponse response = withdraw(playerId, currency, amount);
            if (response.isSuccess()) {
                currencyManager.savePlayer(playerId.toString());
            }
            return CompletableFuture.completedFuture(response);
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidAmount());
        }
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenCompose(model -> {
                    if (model == null) {
                        return CompletableFuture.completedFuture(EconomyResponse.accountNotFound());
                    }
                    BigDecimal balance = model.getCurrency(currency);
                    if (balance.compareTo(amount) < 0) {
                        return CompletableFuture.completedFuture(EconomyResponse.insufficientFunds(balance));
                    }
                    BigDecimal newBalance = balance.subtract(amount);
                    model.setCurrency(currency, newBalance);
                    return currencyManager.getStorage().saveAsync(playerId.toString(), model)
                            .thenApply(v -> EconomyResponse.success(amount, newBalance));
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> depositAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (isPlayerOnline(playerId)) {
            EconomyResponse response = deposit(playerId, currency, amount);
            if (response.isSuccess()) {
                currencyManager.savePlayer(playerId.toString());
            }
            return CompletableFuture.completedFuture(response);
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidAmount());
        }
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenCompose(model -> {
                    if (model == null) {
                        return CompletableFuture.completedFuture(EconomyResponse.accountNotFound());
                    }
                    BigDecimal balance = model.getCurrency(currency);
                    BigDecimal newBalance = balance.add(amount);
                    model.setCurrency(currency, newBalance);
                    return currencyManager.getStorage().saveAsync(playerId.toString(), model)
                            .thenApply(v -> EconomyResponse.success(amount, newBalance));
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> setBalanceAsync(@Nonnull UUID playerId, @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (isPlayerOnline(playerId)) {
            EconomyResponse response = setBalance(playerId, currency, amount);
            if (response.isSuccess()) {
                currencyManager.savePlayer(playerId.toString());
            }
            return CompletableFuture.completedFuture(response);
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidAmount());
        }
        return currencyManager.getStorage().loadAsync(playerId.toString())
                .thenCompose(model -> {
                    if (model == null) {
                        return CompletableFuture.completedFuture(EconomyResponse.accountNotFound());
                    }
                    model.setCurrency(currency, amount);
                    return currencyManager.getStorage().saveAsync(playerId.toString(), model)
                            .thenApply(v -> EconomyResponse.success(amount, amount));
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

    @Nonnull
    @Override
    public CompletableFuture<EconomyResponse> transferAsync(@Nonnull UUID fromId, @Nonnull UUID toId,
                                                            @Nonnull String currency, @Nonnull BigDecimal amount) {
        if (isPlayerOnline(fromId) && isPlayerOnline(toId)) {
            EconomyResponse response = transfer(fromId, toId, currency, amount);
            if (response.isSuccess()) {
                currencyManager.savePlayer(fromId.toString());
                currencyManager.savePlayer(toId.toString());
            }
            return CompletableFuture.completedFuture(response);
        }
        if (!currencyExists(currency)) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidCurrency(currency));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CompletableFuture.completedFuture(EconomyResponse.invalidAmount());
        }

        // Load both models
        CompletableFuture<CurrencyModel> fromFuture = isPlayerOnline(fromId)
                ? CompletableFuture.completedFuture(getOnlinePlayerModel(fromId))
                : currencyManager.getStorage().loadAsync(fromId.toString());
        CompletableFuture<CurrencyModel> toFuture = isPlayerOnline(toId)
                ? CompletableFuture.completedFuture(getOnlinePlayerModel(toId))
                : currencyManager.getStorage().loadAsync(toId.toString());

        return fromFuture.thenCombine(toFuture, (fromModel, toModel) -> new CurrencyModel[]{fromModel, toModel})
                .thenCompose(models -> {
                    CurrencyModel fromModel = models[0];
                    CurrencyModel toModel = models[1];

                    if (fromModel == null || toModel == null) {
                        return CompletableFuture.completedFuture(EconomyResponse.accountNotFound());
                    }

                    BigDecimal fromBalance = fromModel.getCurrency(currency);
                    if (fromBalance.compareTo(amount) < 0) {
                        return CompletableFuture.completedFuture(EconomyResponse.insufficientFunds(fromBalance));
                    }

                    fromModel.setCurrency(currency, fromBalance.subtract(amount));
                    toModel.setCurrency(currency, toModel.getCurrency(currency).add(amount));

                    // Save both (only if not online - online players auto-save)
                    CompletableFuture<Void> saveFrom = isPlayerOnline(fromId)
                            ? CompletableFuture.completedFuture(null)
                            : currencyManager.getStorage().saveAsync(fromId.toString(), fromModel);
                    CompletableFuture<Void> saveTo = isPlayerOnline(toId)
                            ? CompletableFuture.completedFuture(null)
                            : currencyManager.getStorage().saveAsync(toId.toString(), toModel);

                    return saveFrom.thenCombine(saveTo, (v1, v2) ->
                            EconomyResponse.success(amount, fromModel.getCurrency(currency)));
                })
                .exceptionally(ex -> EconomyResponse.internalError(ex.getMessage()));
    }

}
