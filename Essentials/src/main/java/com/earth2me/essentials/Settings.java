package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Settings implements ISettings {
    private final static Logger logger = Logger.getLogger("Minecraft");
    private final static double MAXMONEY = 10000000000000.0;
    private final transient EssentialsConf config;
    private final transient IEssentials ess;

    public Settings(IEssentials ess) {
        this.ess = ess;
        config = new EssentialsConf(new File(ess.getDataFolder(), "config.yml"));
        config.setTemplateName("/config.yml");
        reloadConfig();
    }

    @Override
    public boolean getRespawnAtHome() {
        return config.getBoolean("respawn-at-home", false);
    }

    @Override
    public int getMultipleHomes() {
        return config.getInt("multiple-homes", 5);
    }

    @Override
    public boolean getBedSetsHome() {
        return config.getBoolean("bed-sethome", false);
    }

    @Override
    public int getChatRadius() {
        return config.getInt("chat.radius", config.getInt("chat-radius", 0));
    }

    @Override
    public double getTeleportDelay() {
        return config.getDouble("teleport-delay", 0);
    }

    @Override
    public int getDefaultStackSize() {
        return config.getInt("default-stack-size", 64);
    }

    @Override
    public int getStartingBalance() {
        return config.getInt("starting-balance", 0);
    }

    @Override
    public boolean getNetherPortalsEnabled() {
        return isNetherEnabled() && config.getBoolean("nether.portals-enabled", false);
    }

    @Override
    public boolean isCommandDisabled(final IEssentialsCommand cmd) {
        return isCommandDisabled(cmd.getName());
    }

    @Override
    public boolean isCommandDisabled(String label) {
        for (String c : config.getStringList("disabled-commands", new ArrayList<String>(0))) {
            if (!c.equalsIgnoreCase(label)) continue;
            return true;
        }
        return config.getBoolean("disable-" + label.toLowerCase(), false);
    }

    @Override
    public boolean isCommandRestricted(IEssentialsCommand cmd) {
        return isCommandRestricted(cmd.getName());
    }

    @Override
    public boolean isCommandRestricted(String label) {
        for (String c : config.getStringList("restricted-commands", new ArrayList<String>(0))) {
            if (!c.equalsIgnoreCase(label)) continue;
            return true;
        }
        return config.getBoolean("restrict-" + label.toLowerCase(), false);
    }

    @Override
    public boolean isPlayerCommand(String label) {
        for (String c : config.getStringList("player-commands", new ArrayList<String>(0))) {
            if (!c.equalsIgnoreCase(label)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isCommandOverridden(String name) {
        List<String> defaultList = new ArrayList<String>(1);
        defaultList.add("god");
        for (String c : config.getStringList("overridden-commands", defaultList)) {
            if (!c.equalsIgnoreCase(name))
                continue;
            return true;
        }
        return config.getBoolean("override-" + name.toLowerCase(), false);
    }

    @Override
    public double getCommandCost(IEssentialsCommand cmd) {
        return getCommandCost(cmd.getName());
    }

    @Override
    public double getCommandCost(String label) {
        double cost = config.getDouble("command-costs." + label, 0.0);
        if (cost == 0.0)
            cost = config.getDouble("cost-" + label, 0.0);
        return cost;
    }

    @Override
    public String getNicknamePrefix() {
        return config.getString("nickname-prefix", "~");
    }

    @Override
    public double getTeleportCooldown() {
        return config.getDouble("teleport-cooldown", 60);
    }

    @Override
    public double getHealCooldown() {
        return config.getDouble("heal-cooldown", 60);
    }

    @Override
    public Object getKit(String name) {
        Map<String, Object> kits = (Map<String, Object>) config.getProperty("kits");
        for (Map.Entry<String, Object> entry : kits.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name.replace('.', '_').replace('/', '_'))) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getKits() {
        return (Map<String, Object>) config.getProperty("kits");
    }

    @Override
    public ChatColor getOperatorColor() throws Exception {
        String colorName = config.getString("ops-name-color", null);

        if (colorName == null)
            return ChatColor.RED;
        if ("none".equalsIgnoreCase(colorName) || colorName.isEmpty())
            throw new Exception();

        try {
            return ChatColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException ex) {
        }

        return ChatColor.getByCode(Integer.parseInt(colorName, 16));
    }

    @Override
    public boolean getReclaimSetting() {
        return config.getBoolean("reclaim-onlogout", true);
    }

    @Override
    public String getNetherName() {
        return config.getString("nether.folder", "nether");
    }

    @Override
    public boolean isNetherEnabled() {
        return config.getBoolean("nether.enabled", true);
    }

    @Override
    public int getSpawnMobLimit() {
        return config.getInt("spawnmob-limit", 10);
    }

    @Override
    public boolean showNonEssCommandsInHelp() {
        return config.getBoolean("non-ess-in-help", true);
    }

    @Override
    public boolean hidePermissionlessHelp() {
        return config.getBoolean("hide-permissionless-help", true);
    }

    @Override
    public int getProtectCreeperMaxHeight() {
        return config.getInt("protect.creeper.max-height", -1);
    }

    @Override
    public boolean areSignsDisabled() {
        return config.getBoolean("signs-disabled", false);
    }

    @Override
    public long getBackupInterval() {
        return config.getInt("backup.interval", 1440); // 1440 = 24 * 60
    }

    @Override
    public String getBackupCommand() {
        return config.getString("backup.command", null);
    }

    @Override
    public String getChatFormat(String group) {
        return config.getString("chat.group-formats." + (group == null ? "Default" : group),
                config.getString("chat.format", "&7[{GROUP}]&f {DISPLAYNAME}&7:&f {MESSAGE}"));
    }

    @Override
    public boolean getGenerateExitPortals() {
        return config.getBoolean("nether.generate-exit-portals", true);
    }

    @Override
    public boolean getAnnounceNewPlayers() {
        return !config.getString("newbies.announce-format", "-").isEmpty();
    }

    @Override
    public String getAnnounceNewPlayerFormat(IUser user) {
        return format(config.getString("newbies.announce-format", "&dWelcome {DISPLAYNAME} to the server!"), user);
    }

    @Override
    public String format(String format, IUser user) {
        return format.replace('&', '§').replace("§§", "&").replace("{PLAYER}", user.getDisplayName()).replace("{DISPLAYNAME}", user.getDisplayName()).replace("{GROUP}", user.getGroup()).replace("{USERNAME}", user.getName()).replace("{ADDRESS}", user.getAddress().toString());
    }

    @Override
    public String getNewbieSpawn() {
        return config.getString("newbies.spawnpoint", "default");
    }

    @Override
    public boolean getPerWarpPermission() {
        return config.getBoolean("per-warp-permission", false);
    }

    @Override
    public void reloadConfig() {
        config.load();
    }

    @Override
    public List<Integer> itemSpawnBlacklist() {
        final List<Integer> epItemSpwn = new ArrayList<Integer>();
        for (String itemName : config.getString("item-spawn-blacklist", "").split(",")) {
            itemName = itemName.trim();
            if (itemName.isEmpty()) {
                continue;
            }
            ItemStack is;
            try {
                is = ess.getItemDb().get(itemName);
                epItemSpwn.add(is.getTypeId());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, Util.format("unknownItemInList", itemName, "item-spawn-blacklist"));
            }
        }
        return epItemSpwn;
    }

    @Override
    public boolean spawnIfNoHome() {
        return config.getBoolean("spawn-if-no-home", false);
    }

    @Override
    public boolean warnOnBuildDisallow() {
        return config.getBoolean("protect.disable.warn-on-build-disallow", false);
    }

    @Override
    public boolean use1to1RatioInNether() {
        return config.getBoolean("nether.use-1to1-ratio", false);
    }

    @Override
    public double getNetherRatio() {
        if (config.getBoolean("nether.use-1to1-ratio", false)) {
            return 1.0;
        }
        return config.getDouble("nether.ratio", 16.0);
    }

    @Override
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    @Override
    public boolean warnOnSmite() {
        return config.getBoolean("warn-on-smite", true);
    }

    @Override
    public boolean permissionBasedItemSpawn() {
        return config.getBoolean("permission-based-item-spawn", false);
    }

    @Override
    public String getLocale() {
        return config.getString("locale", "");
    }

    @Override
    public String getCurrencySymbol() {
        return config.getString("currency-symbol", "$").substring(0, 1).replaceAll("[0-9]", "$");
    }

    @Override
    public boolean isTradeInStacks(int id) {
        return config.getBoolean("trade-in-stacks-" + id, false);
    }

    @Override
    public boolean isEcoDisabled() {
        return config.getBoolean("disable-eco", false);
    }

    @Override
    public boolean getProtectPreventSpawn(final String creatureName) {
        return config.getBoolean("protect.prevent.spawn." + creatureName, false);
    }

    @Override
    public List<Integer> getProtectList(final String configName) {
        final List<Integer> list = new ArrayList<Integer>();
        for (String itemName : config.getString(configName, "").split(",")) {
            itemName = itemName.trim();
            if (itemName.isEmpty()) {
                continue;
            }
            ItemStack itemStack;
            try {
                itemStack = ess.getItemDb().get(itemName);
                list.add(itemStack.getTypeId());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, Util.format("unknownItemInList", itemName, configName));
            }
        }
        return list;
    }

    @Override
    public String getProtectString(final String configName) {
        return config.getString(configName, null);
    }

    @Override
    public boolean getProtectBoolean(final String configName, boolean def) {
        return config.getBoolean(configName, def);
    }

    public double getMaxMoney() {
        double max = config.getDouble("max-money", MAXMONEY);
        if (Math.abs(max) > MAXMONEY) {
            max = max < 0 ? -MAXMONEY : MAXMONEY;
        }
        return max;
    }

    public boolean isEcoLogEnabled() {
        return config.getBoolean("economy-log-enabled", false);
    }

    public boolean removeGodOnDisconnect() {
        return config.getBoolean("remove-god-on-disconnect", false);
    }

    public boolean changeDisplayName() {
        return config.getBoolean("change-displayname", true);
    }

    public boolean useBukkitPermissions() {
        return config.getBoolean("use-bukkit-permissions", false);
    }

    public boolean addPrefixSuffix() {
        return config.getBoolean("add-prefix-suffix", ess.getServer().getPluginManager().isPluginEnabled("EssentialsChat"));
    }

    @Override
    public boolean isUpdateEnabled() {
        return config.getBoolean("update-check", false);
    }

    @Override
    public long getAutoAfk() {
        return config.getLong("auto-afk", 300);
    }

    @Override
    public long getAutoAfkKick() {
        return config.getLong("auto-afk-kick", -1);
    }

    @Override
    public boolean getFreezeAfkPlayers() {
        return config.getBoolean("freeze-afk-players", false);
    }
}
