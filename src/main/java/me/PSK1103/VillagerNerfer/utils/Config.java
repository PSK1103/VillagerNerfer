package me.PSK1103.VillagerNerfer.utils;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Config
{

    private boolean disablePlugin = false;

    private final VillagerNerfer plugin;
    private final Logger logger;

    private long activeCheckInterval;
    private long inactiveCheckInterval;

    private int cyclesTillNextInterval;

    private int maxDailyRestocks;

    private boolean skipNameTaggedVillagers;

    private boolean showNerfedNametag;

    private List<String> nerfedNametags;

    private boolean enableTimings;

    private boolean enableBstats;

    private boolean holeDetectionCheck;

    private boolean nerfBlockCheck;

    private boolean nerfedNametagCheck;

    private int dangerRadiusXZ;

    private int dangerRadiusY;

    private boolean instantDezombification;

    private Material bottomBlock;

    public Config(VillagerNerfer plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

         if(!checkVersion()) {
             disablePlugin = true;
             return;
         }

        if (new File(plugin.getDataFolder(), "config.yml").exists()) loadCustomConfig();
        else loadDefaultConfig();

    }

    private boolean checkVersion() {
        final FileConfiguration configFile = new YamlConfiguration();
        final FileConfiguration defaultConfig = new YamlConfiguration();
        try {
            defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));
            configFile.load(new File(plugin.getDataFolder(), "config.yml"));
            if(configFile.getInt("config-version",-1) == defaultConfig.getInt("config-version"))
                return true;

            logger.severe("Config file version mismatch");
            logger.warning("Disabling VNerfer. Save your old config and delete it from the directory");
            plugin.getPluginLoader().disablePlugin(plugin);
            return false;

        }
        catch (IOException | InvalidConfigurationException | ClassCastException e) {
            logger.severe(e.toString());
            return false;
        }
    }

    private void loadCustomConfig() {
        logger.info("Loading custom config");
        final FileConfiguration configFile = new YamlConfiguration();
        final FileConfiguration defaultConfig = new YamlConfiguration();
        try {
            defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));
            configFile.load(new File(plugin.getDataFolder(), "config.yml"));

            activeCheckInterval = configFile.getLong("active-check-interval",defaultConfig.getLong("active-check-interval",200));
            inactiveCheckInterval = configFile.getLong("inactive-check-interval",defaultConfig.getLong("inactive-check-interval",200));

            cyclesTillNextInterval = configFile.getInt("cycles-till-next-interval",defaultConfig.getInt("cycles-till-next-interval",1));

            maxDailyRestocks = configFile.getInt("max-daily-restocks",defaultConfig.getInt("max-daily-restocks",3));

            skipNameTaggedVillagers = configFile.getBoolean("skip-nametagged-villagers",defaultConfig.getBoolean("skip-nametagged-villagers",true));

            nerfedNametags = configFile.getStringList("nerfed-nametags");

            showNerfedNametag = configFile.getBoolean("show-nerfed-nametag",defaultConfig.getBoolean("show-nerfed-nametag",false));

            holeDetectionCheck = configFile.getBoolean("hole-detection",defaultConfig.getBoolean("hole-detection",true));
            nerfBlockCheck = configFile.getBoolean("nerf-block",defaultConfig.getBoolean("nerf-block",false));
            nerfedNametagCheck = configFile.getBoolean("nerfed-nametag",defaultConfig.getBoolean("nerfed-nametag",false));

            bottomBlock = Material.matchMaterial(configFile.getString("bottom-block",defaultConfig.getString("bottom-block","EMERALD_BLOCK"))) != null ? Material.matchMaterial(configFile.getString("bottom-block",defaultConfig.getString("bottom-block","EMERALD_BLOCK"))) : Material.EMERALD_BLOCK;

            dangerRadiusXZ = configFile.getInt("danger-radius-xz", defaultConfig.getInt("danger-radius-xz",3));
            dangerRadiusY = configFile.getInt("danger-radius-y", defaultConfig.getInt("danger-radius-y",1));

            instantDezombification = configFile.getBoolean("instant-dezombification", defaultConfig.getBoolean("instant-dezombification",false));

            enableTimings = configFile.getBoolean("enable-timings",defaultConfig.getBoolean("enable-timings",true));

            enableBstats = configFile.getBoolean("enable-bstats",defaultConfig.getBoolean("enable-bstats",true));

        } catch (IOException | InvalidConfigurationException e) {
            logger.severe("Failed to parse custom config");
            logger.warning("Reverting to default config");
            loadDefaultConfig();
        }
    }

    private void loadDefaultConfig() {
        logger.info("Loading default config");
        final FileConfiguration defaultConfig = new YamlConfiguration();

        try {
            defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));

            activeCheckInterval = defaultConfig.getLong("active-check-interval",200);
            inactiveCheckInterval = defaultConfig.getLong("inactive-check-interval",200);

            cyclesTillNextInterval = defaultConfig.getInt("cycles-till-next-interval",1);

            maxDailyRestocks = defaultConfig.getInt("max-daily-restocks",3);

            skipNameTaggedVillagers = defaultConfig.getBoolean("skip-nametagged-villagers",true);

            nerfedNametags = defaultConfig.getStringList("nerfed-nametags");

            showNerfedNametag = defaultConfig.getBoolean("show-nerfed-nametag",false);

            holeDetectionCheck = defaultConfig.getBoolean("hole-detection",true);
            nerfBlockCheck = defaultConfig.getBoolean("nerf-block",false);
            nerfedNametagCheck = defaultConfig.getBoolean("nerfed-nametag",false);

            bottomBlock = Material.matchMaterial(defaultConfig.getString("bottom-block","EMERALD_BLOCK")) != null ? Material.matchMaterial(defaultConfig.getString("bottom-block","EMERALD_BLOCK")) : Material.EMERALD_BLOCK;

            dangerRadiusXZ = defaultConfig.getInt("danger-radius-xz",3);
            dangerRadiusY = defaultConfig.getInt("danger-radius-y",1);

            instantDezombification = defaultConfig.getBoolean("instant-dezombification",false);

            enableTimings = defaultConfig.getBoolean("enable-timings",true);

            enableBstats = defaultConfig.getBoolean("enable-bstats",true);

        } catch (IOException | InvalidConfigurationException e) {
            logger.severe("Failed to parse custom config");
            logger.warning("Reverting to default config");
            loadDefaultConfig();
        }
    }

    public void reloadConfig() {
        loadCustomConfig();
    }

    public boolean bstatsEnabled() {
        return enableBstats;
    }

    public boolean skipNameTaggedVillagers() {
        return skipNameTaggedVillagers;
    }

    public boolean showNerfedNametag() {
        return showNerfedNametag;
    }

    public long getActiveCheckInterval() {
        return activeCheckInterval;
    }

    public long getInactiveCheckInterval() {
        return inactiveCheckInterval;
    }

    public int getCyclesTillNextInterval() {
        return cyclesTillNextInterval;
    }

    public int getMaxDailyRestocks() {
        return maxDailyRestocks;
    }

    public List<String> getNerfedNametags() {
        return nerfedNametags;
    }

    public boolean timingsEnabled() {
        return enableTimings;
    }

    public int getCheckingMethod() {
        int checkingMethod = 0;
        if (holeDetectionCheck) {
            checkingMethod += 1;
        }
        if (nerfBlockCheck) {
            checkingMethod += 2;
        }
        if (nerfedNametagCheck) {
            checkingMethod += 4;
        }
        return checkingMethod;
    }

    public Material getBottomBlock() {
        return bottomBlock;
    }

    public int getDangerRadiusXZ() {
        return dangerRadiusXZ;
    }

    public int getDangerRadiusY() {
        return dangerRadiusY;
    }

    public boolean instantDezombification() {
        return instantDezombification;
    }

    public boolean disablePlugin() {
        return disablePlugin;
    }

    @Override
    public String toString() {
        return "Config{" +
                "activeCheckInterval=" + activeCheckInterval +
                ", inactiveCheckInterval=" + inactiveCheckInterval +
                ", cyclesTillNextInterval=" + cyclesTillNextInterval +
                ", maxDailyRestocks=" + maxDailyRestocks +
                ", skipNameTaggedVillagers=" + skipNameTaggedVillagers +
                ", showNerfedNametag=" + showNerfedNametag +
                ", nerfedNametags=" + nerfedNametags +
                ", enableTimings=" + enableTimings +
                ", enableBstats=" + enableBstats +
                ", checkingMethod=" + getCheckingMethod() +
                ", bottomBlock=" + bottomBlock +
                ", instantDezombification=" + instantDezombification +
                '}';
    }
}
