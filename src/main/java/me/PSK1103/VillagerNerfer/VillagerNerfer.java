package me.PSK1103.VillagerNerfer;

import me.PSK1103.VillagerNerfer.helpers.VillagerListener;
import me.PSK1103.VillagerNerfer.utils.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class VillagerNerfer extends JavaPlugin {

    private VillagerStorage storage;
    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        storage = new VillagerStorage(this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this),this);
        getCommand("VNerfer").setExecutor(new VillagerNerferCommands(this));
    }

    public VillagerStorage getStorage() {
        return storage;
    }

    public FileConfiguration getCustomConfig() {

        customConfigFile = new File(getDataFolder(), "config.yml");
        if (!customConfigFile.exists()) {
            return getConfig();
        }
        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return customConfig;
    }

}
