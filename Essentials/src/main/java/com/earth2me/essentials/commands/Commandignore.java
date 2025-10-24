package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.ChatColor;
import org.bukkit.Server;

import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.JohnyPermsAPI;
import com.johnymuffin.jperms.core.models.PermissionsUser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.UUID;

public class Commandignore extends EssentialsCommand {

    public Commandignore() {
        super("ignore");
    }

    private UUID getUUIDFromCache(String playerName) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray arr = (JSONArray) parser.parse(new FileReader("uuidcache.json"));
            for (Object obj : arr) {
                JSONObject entry = (JSONObject) obj;
                String name = (String) entry.get("name");
                if (name != null && name.equalsIgnoreCase(playerName)) {
                    String uuidStr = (String) entry.get("uuid");
                    return UUID.fromString(uuidStr);
                }
            }
        } catch (Exception e) {
            // Sil
        }
        return null;
    }

    @Override
    protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }
        User u;
        try {
            u = getPlayer(server, args, 0);
        } catch (NoSuchFieldException ex) {
            u = ess.getOfflineUser(args[0]);
        }
        if (u == null) {
            throw new Exception(Util.i18n("playerNotFound"));
        }
        String name = u.getName();

        if(u.hasPermission("essentials.ignore.exempt") || u.isOp()) {
            user.setIgnoredPlayer(name, false); // Ensure they are not ignored. This is useful if an op/exempt player was ignored before gaining exempt status.
            user.sendMessage(Util.format("ignoreExempt", u.getName()));
            return;
        }

        if (user.isIgnoredPlayer(name)) {
            user.setIgnoredPlayer(name, false);
            user.sendMessage(Util.format("unignorePlayer", u.getName()));
        } else {
            user.setIgnoredPlayer(name, true);
            user.sendMessage(Util.format("ignorePlayer", u.getName()));
        }
    }
}
