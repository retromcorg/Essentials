package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Commandlist extends EssentialsCommand {
    private final String SHOW_HIDDEN_PLAYERS_PERMISSION_NODE = "essentials.list.hidden";
    private final int PLAYERS_PER_PAGE = 15;

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
        boolean showHiddenPlayers = canListHiddenPlayers(sender);
        Player[] players = server.getOnlinePlayers();

        List<Player> sortedPlayers = new ArrayList<>();

        int hiddenPlayerCount = 0;
        if(showHiddenPlayers) {
            List<Player> hiddenPlayers = getHiddenPlayers(players);

            hiddenPlayerCount = hiddenPlayers.size();
            sortedPlayers.addAll(hiddenPlayers);
        }
        sortedPlayers.addAll(getOnlinePlayers(players));

        int playerCount = sortedPlayers.size();

        int pageToView;
        int pageCount;
        try {
            int[] pageDetails = getPageDetails(playerCount, args);
            pageToView = pageDetails[0];
            pageCount = pageDetails[1];
        } catch (NumberFormatException e) {
            String errorMessage = Util.i18n("pageIsNotAnInteger");
            sender.sendMessage(errorMessage);

            return;
        }

        sendChatMessage(sender, sortedPlayers, playerCount, hiddenPlayerCount, pageToView, pageCount);
    }

    private void sendChatMessage(
        CommandSender sender,
        List<Player> players,
        int playerCount,
        int hiddenPlayerCount,
        int pageToView,
        int pageCount
    ) {
        String chatHeader = getChatHeader(playerCount, hiddenPlayerCount);
        sender.sendMessage(chatHeader);

        int startIndex = (pageToView - 1) * PLAYERS_PER_PAGE;

        addPlayers(sender, players, startIndex);

        String chatFooter = getChatFooter(pageToView, pageCount);
        sender.sendMessage(chatFooter);
    }

    private int[] getPageDetails(int playerCount, String[] args) throws NumberFormatException {
        int pagesAllowed = playerCount / PLAYERS_PER_PAGE + 1;
        int[] output = new int[] {1, pagesAllowed};

        if (args.length == 0)
            return output;

        String pageString = args[0];
        int pageNumber = Integer.parseInt(pageString);

        if (pageNumber < 1)
            return output;

        if (pageNumber > pagesAllowed) {
            output[0] = pagesAllowed;
            return output;
        }

        output[0] = pageNumber;
        return output;
    }

    private String getChatHeader(int playerCount, int hiddenPlayerCount) {
        String output = "";

        if (playerCount == 1)
            output = Util.format("playersOnlineSingle");
        else
            output = Util.format("playersOnlineMultiple", playerCount);

        if (hiddenPlayerCount > 0)
            output += " " + Util.format("playersOnlineHiddenTag", hiddenPlayerCount);

        output += ":\n";
        return output;
    }

    private String getChatFooter(int page, int totalPages) {
        return Util.format("pageNumberDisplay", page, totalPages);
    }

    private boolean canListHiddenPlayers(CommandSender sender) {
        // console check
        if (!(sender instanceof Player))
            return true;

        User commandUser = ess.getUser(sender);
        if (commandUser.isAuthorized(SHOW_HIDDEN_PLAYERS_PERMISSION_NODE))
            return true;

        return false;
    }

    private boolean isHiddenPlayer(Player player) {
        return ess.getUser(player).isHidden();
    }

    private List<Player> getOnlinePlayers(Player[] allPlayers) {
        List<Player> output = new ArrayList<>();

        for (Player player : allPlayers) {
            if (isHiddenPlayer(player))
                continue;

            output.add(player);
        }

        return output;
    }

    private List<Player> getHiddenPlayers(Player[] allPlayers) {
        List<Player> output = new ArrayList<>();

        for (Player player : allPlayers) {
            if (isHiddenPlayer(player))
                output.add(player);
        }

        return output;
    }

    private String getPlayerString(User user) {
        String output = "";

        if (user.isHidden())
            output = "§7[§dHIDDEN§7]";
        if (user.isAfk())
            output += "§7[§8AFK§7]";

        output += user.getDisplayName();
        return output;
    }

    private int addPlayers(
        CommandSender sender,
        List<Player> players,
        int startIndex
    ) {
        int playerCount = players.size();

        if (playerCount == 0)
            return 0;

        /**
         * this looks complicated, but it just generates a segment of the player list starting at the 'startIndex'.
         * it tries to get PLAYERS_PER_PAGE names, but till prevent IOOBE if there arent enough players present
         */
        int namesAbleToPrint = Integer.min(playerCount - startIndex, PLAYERS_PER_PAGE);
        players = players.subList(
            startIndex,
            namesAbleToPrint + startIndex
        );

        List<User> onlineUsers = new ArrayList<>();
        for (Player player : players) {
            User user = ess.getUser(player);
            onlineUsers.add(user);
        }

        for (User user : onlineUsers) {
            String playerName = getPlayerString(user);
            playerName += "\n";

            sender.sendMessage(playerName);
        }

        return namesAbleToPrint;
    }
}
