package com.johnymuffin.essentials;

import com.earth2me.essentials.Essentials;
import com.johnymuffin.discordcore.DiscordCore;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ESSAdv {
    private Essentials plugin;
    private final Logger logger;
    private ESSAdvConfig config;

    public ESSAdv(Essentials plugin) {
        logger = plugin.getServer().getLogger();
        logInfo(Level.INFO, "Starting Essentials Advanced Module");
        this.plugin = plugin;

        //Generate Config
        logInfo(Level.INFO, "Loading Config");
        config = ESSAdvConfig.getInstance(this);


        if (config.getConfigBoolean("use-fundamentals-economy")) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Fundamentals")) {
                logInfo(Level.WARNING, "Fundamentals economy support is enabled however the Fundamentals plugin couldn't be found. Disabling Fundamentals Economy support");
                config.setConfigOptionAndSave("use-fundamentals-economy", false);
            } else {
                logInfo(Level.INFO, "Hooking into Fundamentals for economy calls.");
            }
        }

        if (config.getConfigBoolean("discord.enable")) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordCore")) {
                logInfo(Level.WARNING, "Discord Core support is enabled however the Discord Core plugin couldn't be found. Disabling Discord Core support");
                config.setConfigOptionAndSave("discord.enable", false);
            } else {
                if (config.getConfigString("discord.channel-id").trim().isEmpty() || config.getConfigString("discord.channel-id").trim().equalsIgnoreCase("0")) {
                    logInfo(Level.INFO, "Disabling Discord Core Support, No Channel ID provided");
                    config.setConfigOptionAndSave("discord.enable", false);
                }
            }
        }


    }

    public static void sendDiscordEmbed(String title, String description, String footer) {
        ESSAdvConfig config = ESSAdvConfig.getInstance();
        if (!config.getConfigBoolean("discord.enable")) {
            return;
        }
        DiscordCore Core = (DiscordCore) Bukkit.getServer().getPluginManager().getPlugin("DiscordCore");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(Color.RED);
        eb.setDescription(description);
        eb.setFooter(footer, null);
        Core.Discord().jda.getTextChannelById(config.getConfigString("discord.channel-id")).sendMessage(eb.build()).queue();
    }


    public Essentials getEssentials() {
        return plugin;
    }

    public void logInfo(Level level, String s) {
        logger.log(level, "[Essentials-Advanced] " + s);
    }


}
