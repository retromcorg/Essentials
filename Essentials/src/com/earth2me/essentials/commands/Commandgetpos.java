package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;


public class Commandgetpos extends EssentialsCommand {
    public Commandgetpos() {
        super("getpos");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        Location coords = user.getLocation();
        user.sendMessage("§7X: " + coords.getBlockX() + " (-North <-> +South)");
        user.sendMessage("§7Y: " + coords.getBlockY() + " (+Up <-> -Down)");
        user.sendMessage("§7Z: " + coords.getBlockZ() + " (+East <-> -West)");
        user.sendMessage("§7Yaw: " + user.getCorrectedYaw() + " (Rotation)");
        user.sendMessage("§7Pitch: " + coords.getPitch() + " (Head angle)");
    }
}
