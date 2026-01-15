package com.hymines.currency;

import com.hymines.currency.command.CurrencyCommand;
import com.hymines.currency.config.CurrencyConfig;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class HyCurrencyPlugin extends JavaPlugin {

    private final Map<String, CurrencyModel> currencyDataMap = new LinkedHashMap<>();
    private ExecutorService dbExecutor;
    private CurrencyConfig currencyConfig;

    public HyCurrencyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        loadConfig();
        getCommandRegistry().registerCommand(new CurrencyCommand(this));
    }

    private void loadConfig() {
        try {
            Path dataFolder = getDataDirectory();
            this.currencyConfig = CurrencyConfig.load(dataFolder);
            getLogger().atInfo().log("Loaded " + (currencyConfig.getCurrencies() == null ? 0 : currencyConfig.getCurrencies().size()) + " currencies from currency.json");
        } catch (IOException e) {
            getLogger().atSevere().log("Failed to load currency config: " + e.getMessage());
            this.currencyConfig = new CurrencyConfig();
        }
    }

    public CurrencyConfig getCurrencyConfig() {
        return currencyConfig;
    }

    public ExecutorService getDbExecutor() {
        return dbExecutor;
    }

    public Map<String, CurrencyModel> getCurrencyDataMap() {
        return currencyDataMap;
    }

}
