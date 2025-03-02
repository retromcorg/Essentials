package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Commandlist extends EssentialsCommand {
    private final String showHiddenPlayers_PermissionNode = "essentials.list.hidden";

    public Commandlist() {
        super("list");
    }

    @Override
    public void run(
        Server server,
        CommandSender sender,
        String commandLabel,
        String[] args
    ) {
        Player[] allPlayers = server.getOnlinePlayers();

        List<Player> onlinePlayers = getOnlinePlayers(allPlayers);
        int onlinePlayerCount = onlinePlayers.size();

        List<Player> hiddenPlayers = getHiddenPlayers(allPlayers);
        int hiddenPlayerCount = hiddenPlayers.size();

        boolean showHiddenPlayers = canListHiddenPlayers(sender);
        if(showHiddenPlayers)
            onlinePlayerCount += hiddenPlayerCount;

        String chatHeader = getCommandChatHeader(onlinePlayerCount, hiddenPlayerCount, showHiddenPlayers);
        sender.sendMessage(chatHeader);

        addOnlinePlayers(sender, onlinePlayers);
        if(showHiddenPlayers)
            addHiddenPlayers(sender, hiddenPlayers);
    }
    
    private String getCommandChatHeader(
        int playerCount,
        int hiddenPlayerCount,
        boolean showHiddenPlayers
    ) {
        String output = "";

        if (playerCount == 1)
            output = Util.format("playersOnlineSingle");
        else
            output = Util.format("playersOnlineMultiple", playerCount);

        if (showHiddenPlayers && hiddenPlayerCount > 0)
            output += " " + Util.format("playersOnlineHiddenTag", hiddenPlayerCount);

        output += ":\n";
        return output;
    }

    private boolean canListHiddenPlayers(CommandSender sender) {
        // console check
        if (!(sender instanceof Player))
            return true;

        User commandUser = ess.getUser(sender);
        if (commandUser.isAuthorized(showHiddenPlayers_PermissionNode))
            return true;

        return false;
    }

    private boolean isHiddenPlayer(Player player) {
        return ess.getUser(player).isHidden();
    }

    private List<Player> getOnlinePlayers(Player[] allPlayers) {
        List<Player> output = new ArrayList<>();
        
        for (Player player : allPlayers) {
            if(isHiddenPlayer(player))
                continue;

            output.add(player);
        }
    
        return output;
    }

    private List<Player> getHiddenPlayers(Player[] allPlayers) {
        List<Player> output = new ArrayList<>();
        
        for (Player player : allPlayers) {
            if(isHiddenPlayer(player))
                output.add(player);
        }

        return output;
    }

    private String getPlayerString(User user, boolean hidden) {
        String output = "";

        if(hidden)
            output = "§7[§dHIDDEN§7]";
        if(user.isAfk())
            output += "§7[§8AFK§7]";
        
        output += user.getDisplayName();
        return output;
    }

    private void addOnlinePlayers(CommandSender sender, List<Player> players) {
        addPlayers(sender, players, false);
    }
    private void addHiddenPlayers(CommandSender sender, List<Player> players) {
        addPlayers(sender, players, true);
    }

    private void addPlayers(CommandSender sender, List<Player> players, boolean hidden) {
        if(players.size() == 0)
            return;
        
        List<User> onlineUsers = new ArrayList<>();
        for (Player player : players) {
            User user = ess.getUser(player);
            onlineUsers.add(user);
        }

        for (User user : onlineUsers) {
            String playerName = getPlayerString(user, hidden);
            playerName += "\n";

            sender.sendMessage(playerName);
        }
    }
}
