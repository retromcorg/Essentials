package com.johnymuffin.essentials;

import org.bukkit.util.config.Configuration;

import java.io.File;


public class ESSAdvConfig extends Configuration {
    private ESSAdv essAdv;
    private static ESSAdvConfig singleton;


    private ESSAdvConfig(ESSAdv essAdv) {
        super(new File(essAdv.getEssentials().getDataFolder(), "EssentialsAdvanced" + File.separator + "config.yml"));
        this.essAdv = essAdv;
        this.reload();

    }

    public static ESSAdvConfig getInstance() {
        if (ESSAdvConfig.singleton == null) {
            throw new RuntimeException("A instance of Essentials needs to be passed into Essentials");
        }
        return ESSAdvConfig.singleton;
    }

    public static ESSAdvConfig getInstance(ESSAdv essAdv) {
        if (ESSAdvConfig.singleton == null) {
            ESSAdvConfig.singleton = new ESSAdvConfig(essAdv);
        }
        return ESSAdvConfig.singleton;
    }


    private void write() {
        generateConfigOption("use-fundamentals-economy", true);
        generateConfigOption("discord.enable", true);
        generateConfigOption("discord.channel-id", "0");


    }

    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }


    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End

    public void setConfigOptionAndSave(String key, Object value) {
        this.setProperty(key, value);
        this.save();
    }


    private boolean convertToNewAddress(String newKey, String oldKey) {
        if (this.getString(newKey) != null) {
            return false;
        }
        if (this.getString(oldKey) == null) {
            return false;
        }
        System.out.println("Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;

    }

    private void reload() {
        this.load();
        this.write();
        this.save();
    }

}
