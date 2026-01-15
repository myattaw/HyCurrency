package com.hymines.currency;

import com.hymines.currency.command.CurrencyCommand;
import com.hymines.currency.model.CurrencyModel;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class HyCurrencyPlugin extends JavaPlugin {

    private final Map<String, CurrencyModel> currencyDataMap = new LinkedHashMap<>();
    private ExecutorService dbExecutor;


    public HyCurrencyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        getCommandRegistry().registerCommand(new CurrencyCommand(this));
    }

    public ExecutorService getDbExecutor() {
        return dbExecutor;
    }

    public Map<String, CurrencyModel> getCurrencyDataMap() {
        return currencyDataMap;
    }

}
