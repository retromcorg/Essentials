package com.earth2me.essentials.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;

public class Commandnick extends EssentialsCommand {
    private final String PERMISSION_NODE = "essentials.nick.others";

    private Server server;

    public Commandnick() {
        super("nick");
    }

    @Override
    public void run(
        Server server,
        User sender,
        String commandLabel,
        String[] args
    ) {
        if(!isDisplayNamesEnabled()) {
            sender.sendMessage(Util.i18n("nickNotEnabled"));
            return;
        }

        autoUpdateNickname(sender);

        this.server = server;

        switch (args.length) {
            case 1: {
                setSendersNickname(sender, args);

                return;
            }
            case 2: {
                if (canSetOtherPlayerNicknames(sender)) {
                    setTargetPlayersNickname(sender, args);

                    return;
                }
            }
        }

        printUsage(sender);
    }

    private boolean canSetOtherPlayerNicknames(User sender) {
        return (
            sender.isAuthorized(PERMISSION_NODE) ||
            sender.isOp()
        );
    }
    
    private boolean isDisplayNamesEnabled() {
        return ess.getSettings().changeDisplayName();
    }
    
    private boolean isNicknameUsed(User sender, String newNickname) {
        newNickname = newNickname.toLowerCase();

        for (Player p : server.getOnlinePlayers()) {
            if (sender == p)
                continue;

            String playerDisplayName = p.getDisplayName().toLowerCase();
            String playerName = p.getName().toLowerCase();

            if (newNickname.equals(playerDisplayName) || newNickname.equals(playerName))
                return true;
        }

        return false;
    }

    private void printUsage(User sender) {
        sender.sendMessage(Util.i18n("nickUsage"));
        if(canSetOtherPlayerNicknames(sender))
            sender.sendMessage(Util.i18n("nickUsageStaffExtra"));
    }

    private void autoUpdateNickname(User sender) {
        String senderNickname = sender.getNickname();
        if(senderNickname == null)
            return;

        if(!allowedNickname(senderNickname)) {
            clearServerNickname(sender);

            sender.sendMessage(Util.i18n("nickUpgraded"));
            sender.sendMessage(Util.i18n("nickRequirements"));
        }
    }

    private void setSendersNickname(User sender, String[] args) {
        String nickname = args[0];

        setNickname(sender, sender, nickname);
    }

    private void setTargetPlayersNickname(User sender, String[] args) {
        User targetUser = null;

        try {
            targetUser = getPlayer(server, args, 0);
        }
        catch(NoSuchFieldException e) { } // this is handled below in the null check. only here to prevent RuntimeException
        catch(Exception e) {
            /* 
            the check for NotEnoughArgumentsException happens before this entire function is called
            this should never happen, but required anyways
            */
            throw new RuntimeException(e);
        }

        if (targetUser == null) {
            sender.sendMessage(Util.i18n("playerNotFound"));

            return;
        }

        String nickname = args[1];

        setNickname(sender, targetUser, nickname);
    }

    private boolean shouldRemoveNickname(User targetUser, String nickname) {
        return (
            nickname.equalsIgnoreCase("clear") ||
            targetUser.getName().equalsIgnoreCase(nickname)
        );
    }

    private boolean allowedNickname(String nickname) {
        nickname = decolorize(nickname);
    
        return (
            nickname.matches(
                "^(?!" +
                    ".*\\[.*\\[|" +     // no more than 1 '['
                    ".*\\].*\\]" +      // no more than 1 ']'
                ")" +
                "[a-zA-Z0-9_\\[\\]]*$"  // allowed chars
            ) && 
            nickname.length() <= 16
        );
    }

    private String decolorize(String input) {
        // there is a method for this already in ChatColor, but it only removes § and not &
        input = input.replaceAll("(?i)§[0-F]", "");
        input = input.replaceAll("(?i)&[0-F]", "");

        return input;
    }

    private void setNickname(User sender, User targetUser, String nickname) {
        if (shouldRemoveNickname(targetUser, nickname)) {
            clearServerNickname(targetUser);

            sender.sendMessage(Util.i18n("nickCleared"));
            return;
        }

        if (!allowedNickname(nickname)) {
            sender.sendMessage(Util.i18n("nickRequirements"));

            return;
        }

        if (isNicknameUsed(targetUser, nickname)) {
            sender.sendMessage(Util.i18n("nickInUse"));

            return;
        }


        setServerNickname(targetUser, nickname);

        String nickSetMessage = Util.format("nickSet", targetUser.getDisplayName());
        sender.sendMessage(nickSetMessage);
    }

    private void clearServerNickname(User targetUser) {
        targetUser.setDisplayName(targetUser.getName());
        targetUser.setNickname(null);
    }

    private void setServerNickname(User targetUser, String nickname) {
        targetUser.setDisplayName(ess.getSettings().getNicknamePrefix() + nickname);
        targetUser.setNickname(nickname);
    }
}
