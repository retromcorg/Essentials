package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.net.InetSocketAddress;


public interface IUser {
    int getHealth();

    Location getLocation();

    boolean isOnline();

    void sendMessage(String string);

    long getLastTeleportTimestamp();

    void setLastTeleportTimestamp(long time);

    boolean isAuthorized(String node);

    boolean isAuthorized(IEssentialsCommand cmd);

    boolean isAuthorized(IEssentialsCommand cmd, String permissionPrefix);

    Location getLastLocation();

    Player getBase();

    double getMoney();

    void takeMoney(double value);

    void giveMoney(double value);

    PlayerInventory getInventory();

    void updateInventory();

    String getGroup();

    void setLastLocation();

    Location getHome(String name) throws Exception;

    Location getHome(Location loc) throws Exception;

    String getName();

    InetSocketAddress getAddress();

    String getDisplayName();

    boolean isHidden();
}
