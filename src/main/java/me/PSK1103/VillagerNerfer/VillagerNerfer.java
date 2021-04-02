package me.PSK1103.VillagerNerfer;

import me.PSK1103.VillagerNerfer.helpers.VillagerListener;
import me.PSK1103.VillagerNerfer.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VillagerNerfer extends JavaPlugin {

    private VillagerStorage storage;
    private Config config;
    private Metrics metrics;

    private static final int pluginId = 10893;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        config = new Config(this);

        if(config.bstatsEnabled())
            metrics = new Metrics(this, pluginId);

        storage = new VillagerStorage(this);

        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getCommand("VNerfer").setExecutor(new VillagerNerferCommands(this));
    }

    @Override
    public void onDisable() {
        if(storage!=null)
            storage.clearStorage();
    }

    public VillagerStorage getStorage() {
        return storage;
    }

    public void reloadCustomConfig() {
        config.reloadConfig();
        storage.reloadCustomConfig();
    }

    public Config getCustomConfig() {
        return config;
    }

    public Metrics getMetrics(){
        return metrics;
    }
}
