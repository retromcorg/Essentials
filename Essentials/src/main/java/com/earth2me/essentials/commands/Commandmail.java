package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Commandmail extends EssentialsCommand {
    private final String CAN_SEND_MAIL_PERMISSION_NODE = "essentials.mail.send";

    private Server server;

    public Commandmail() {
        super("mail");
    }

    @Override
    public void run(
        Server server,
        CommandSender sender,
        String commandLabel,
        String[] args
    ) throws NotEnoughArgumentsException {
        this.server = server;

        User user = ess.getUser(sender);
        int argCount = args.length;

        boolean success = false;
        if(argCount == 1)
            success = oneParameter(args[0], user);
        else if(argCount >= 3)
            success = threeParameter(args, user);

        if(!success)
            throw new NotEnoughArgumentsException();
    }

    private boolean oneParameter(String arg, User user) {
        switch(arg) {
            case "read":
                return read(user);
            case "clear":
                return clear(user);
        }

        return false;
    }

    private boolean threeParameter(String[] args, User user) {
        String subCommand = args[0];
        String to = args[1];
        String message = getFinalArg(args, 2);

        // yes this is stupid, but its done for consistancy with the oneParameter() function
        switch(subCommand) {
            case "send":
                return send(to, message, user);
        }

        return false;
    }

    private boolean read(User user) {
        List<String> mails = user.getMails();

        if (mails.isEmpty()) {
            String noMailMessage = Util.i18n("noMail");
            user.sendMessage(noMailMessage);

            return true;
        }

        for (String mail : mails) {
            user.sendMessage(mail);
        }

        String clearMessage = Util.i18n("mailClear");
        user.sendMessage(clearMessage);

        return true;
    }

    private boolean clear(User user) {
        user.setMails(null);

        String clearedMailMessage = Util.i18n("mailCleared");
        user.sendMessage(clearedMailMessage);

        return true;
    }

    private boolean send(String to, String message, User user) {
        if (!canSendMail(user)) {
            String noPermissionMessage = Util.i18n("noMailSendPerm");
            user.sendMessage(noPermissionMessage);

            return true;
        }

        User toUser = findUser(to);
        if (toUser == null) {
            String neverSeenMessage = Util.format("playerNeverOnServer", to);
            user.sendMessage(neverSeenMessage);

            return true;
        }

        String mailSentMessage = Util.i18n("mailSent");
        user.sendMessage(mailSentMessage);

        if (hasIgnoredPlayer(toUser, user))
            return true;
        
        String mailMessage = ChatColor.stripColor(user.getDisplayName()) + ": " + message;
        toUser.addMail(mailMessage);

        return true;
    }

    private User findUser(String playerName) {
        Player player = server.getPlayer(playerName);
        if (player == null)
            return ess.getOfflineUser(playerName);

        return ess.getUser(player);
    }

    private boolean hasIgnoredPlayer(User recipient, User sender) {
        return recipient.isIgnoredPlayer(sender.getName());
    }

    private boolean canSendMail(User user) {
        return user.isAuthorized(CAN_SEND_MAIL_PERMISSION_NODE);
    }
}
