package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import com.johnymuffin.discordcore.DiscordCore;
import com.johnymuffin.essentials.ESSAdvConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Commandmail extends EssentialsCommand {
    private final String CAN_SEND_MAIL_PERMISSION_NODE = "essentials.mail.send";

    private Server server;

    public Commandmail() {
        super("mail");
    }

    @Override
    public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
        this.server = server;
        User user = ess.getUser(sender);

        if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
            List<String> mail = user.getMails();
            if (mail.isEmpty()) {
                user.sendMessage(Util.i18n("noMail"));
                return;
            }
            for (String s : mail) {
                user.sendMessage(s);
            }
            user.sendMessage(Util.i18n("mailClear"));
            return;
        }
        if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
            if (!user.isAuthorized(CAN_SEND_MAIL_PERMISSION_NODE)) {
                throw new Exception(Util.i18n("noMailSendPerm"));
            }

            Player player = server.getPlayer(args[1]);
            User u;
            if (player != null) {
                u = ess.getUser(player);
            } else {
                u = ess.getOfflineUser(args[1]);
            }
            if (u == null) {
                throw new Exception(Util.format("playerNeverOnServer", args[1]));
            }

            if (!u.isIgnoredPlayer(user.getName())) {
                u.addMail(ChatColor.stripColor(user.getDisplayName()) + ": " + getFinalArg(args, 2));

                boolean forwardToDiscord = ESSAdvConfig.getInstance().getConfigBoolean("settings.mail.send-to-discord.enabled");
                try {
                    forwardMailToDiscord(user, u, args);
                } catch (Exception e) {
                    System.out.println("Failed to forward mail to Discord for " + u.getName() + ".");
                    e.printStackTrace();
                }

            }
            user.sendMessage(Util.i18n("mailSent"));
            return;
        }
        if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
            user.setMails(null);
            user.sendMessage(Util.i18n("mailCleared"));
            return;
        }
        if(args.length == 1 && "discord".equalsIgnoreCase(args[0])) {
            if (!user.isAuthorized("essentials.mail.discord")) {
                throw new Exception(Util.i18n("noMailDiscordPerm"));
            }
            user.setReceiveMailOnDiscord(!user.getReceiveMailOnDiscord());
            boolean receiveMailOnDiscord = user.getReceiveMailOnDiscord();
            user.sendMessage(Util.i18n(receiveMailOnDiscord ? "mailDiscordEnabled" : "mailDiscordDisabled"));
            return;
        }
        throw new NotEnoughArgumentsException();
    }

    private void forwardMailToDiscord(User sender, User recipient, String[] args) {

        // Check if both DiscordAuthentication and DiscordCore plugins are enabled
        if (!(Bukkit.getPluginManager().isPluginEnabled("DiscordAuthentication") && Bukkit.getPluginManager().isPluginEnabled("DiscordCore"))) {
            return;
        }

        // Check if recipient is online or has disabled mail forwarding
        if (recipient.isOnline()) {
            return;
        }

        // Check if recipient has disabled mail forwarding
        if (!recipient.getReceiveMailOnDiscord()) {
            return;
        }


        DiscordAuthentication plugin = (DiscordAuthentication) Bukkit.getPluginManager().getPlugin("DiscordAuthentication");
        String targetUsername = recipient.getName();
        UUID targetUUID = recipient.getUUID();

        if (targetUUID != null) {
            String discordID = plugin.getData().getDiscordIDFromUUID(String.valueOf(targetUUID));

            if (discordID != null) {
                log(Level.INFO, "Mail sent from " + sender.getName() + " to " + targetUsername + " forwarding to Discord ID: " + discordID);
                DiscordCore discordCore = (DiscordCore) Bukkit.getPluginManager().getPlugin("DiscordCore");
                JDA jda = discordCore.getDiscordBot().getJda();

                jda.retrieveUserById(discordID).queue(discordUser -> {
                    if (discordUser != null) {
                        discordUser.openPrivateChannel().queue(privateChannel -> {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(ChatColor.stripColor(sender.getDisplayName()), null, "https://skins.legacyminecraft.com/avatars/" + sender.getBase().getUniqueId());
                            embedBuilder.setTitle("Forwarded Mail from Minecraft Server");
                            embedBuilder.setDescription("You have received a new mail message on the Minecraft server from " + sender.getName() + ".\n\n**Message:**\n" + getFinalArg(args, 2) + "\n\n*Note: You can use `/mail discord` in-game to toggle receiving mail via Discord.*");
                            embedBuilder.setFooter("Essentials (RetroMC fork) - Mail Command", "https://wiki.retromc.org/images/1/1a/Retromcnew.png");
                            embedBuilder.setColor(0x00FF00);
                            privateChannel.sendMessage(embedBuilder.build()).queue();

                            Bukkit.getScheduler().callSyncMethod(ess, () -> {
                                sender.sendMessage(Util.format("mailSentDiscord", discordUser.getAsTag()));
                                return null;
                            });
                        }, throwable -> {
                            log(Level.WARNING, "Failed to send a message to user with Discord ID: " + discordID + ". Reason: " + throwable.getMessage());
                        });
                    } else {
                        log(Level.WARNING, "User with Discord ID " + discordID + " could not be found in any mutual guild.");
                    }
                }, throwable -> {
                    log(Level.WARNING, "Failed to retrieve user with Discord ID: " + discordID + ". Reason: " + throwable.getMessage());
                });
            } else {
                log(Level.INFO, "No Discord ID found for UUID: " + targetUUID + " when attempting to forward mail to Discord.");
            }
        } else {
            log(Level.WARNING, "Failed to send mail to " + targetUsername + " on Discord: No UUID found in Essentials data.");
        }
    }

    private void log(Level level, String message) {
        ess.getServer().getLogger().log(level, "[" + ess.getDescription().getName() + "] " + message);
    }
}
