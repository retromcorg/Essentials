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
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;


public class Commandmail extends EssentialsCommand {
    public Commandmail() {
        super("mail");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
            List<String> mail = user.getMails();
            if (mail.isEmpty()) {
                throw new Exception(Util.i18n("noMail"));
            }
            for (String s : mail) {
                user.sendMessage(s);
            }
            throw new Exception(Util.i18n("mailClear"));
        }
        if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
            if (!user.isAuthorized("essentials.mail.send")) {
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

                // Send to user on Discord if they are linked
                boolean forwardToDiscord = ESSAdvConfig.getInstance().getConfigBoolean("settings.mail.send-to-discord.enabled");
                if (forwardToDiscord && Bukkit.getPluginManager().isPluginEnabled("DiscordAuthentication") && Bukkit.getPluginManager().isPluginEnabled("DiscordCore")) {
                    try {
                        forwardMailToDiscord(user, u, args);
                    } catch (Exception e) {
                        System.out.println("Failed to forward mail to Discord for " + u.getName() + ".");
                        e.printStackTrace();
                    }
                }

            }
            user.sendMessage(Util.i18n("mailSent"));
            return;
        }
        if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
            user.setMails(null);
            throw new Exception(Util.i18n("mailCleared"));
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
        if (recipient.isOnline()) {
            System.out.println("Skipping mail forwarding to Discord for " + recipient.getName() + ": Recipient is online.");
            return;
        }
        // Check recipient has received mail before forwarding
        if (!recipient.getReceiveMailOnDiscord()) {
            System.out.println("Skipping mail forwarding to Discord for " + recipient.getName() + ": Recipient has disabled mail forwarding.");
            return;
        }

        System.out.println("Forwarding mail to Discord for " + recipient.getName());
        DiscordAuthentication plugin = (DiscordAuthentication) Bukkit.getPluginManager().getPlugin("DiscordAuthentication");
        String targetUsername = recipient.getName();
        UUID targetUUID = recipient.getUUID();

        if (targetUUID != null) {
            String discordID = plugin.getData().getDiscordIDFromUUID(String.valueOf(targetUUID));

            if (discordID != null) {
                System.out.println("Sending mail to " + targetUsername + " on Discord with ID: " + discordID);
                DiscordCore discordCore = (DiscordCore) Bukkit.getPluginManager().getPlugin("DiscordCore");
                JDA jda = discordCore.getDiscordBot().getJda();

                jda.retrieveUserById(discordID).queue(discordUser -> {
                    if (discordUser != null) {
                        discordUser.openPrivateChannel().queue(privateChannel -> {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(sender.getDisplayName(), null, "https://skins.legacyminecraft.com/avatars/" + sender.getBase().getUniqueId());
                            embedBuilder.setTitle("Forwarded Mail from Minecraft Server");
                            embedBuilder.setDescription("You have received a new mail message on the Minecraft server from " + sender.getDisplayName() + ".\n\n**Message:**\n" + getFinalArg(args, 2) + "\n\n*Note: You can use `/mail discord` in-game to toggle receiving mail via Discord.*");
                            embedBuilder.setFooter("Essentials (RetroMC fork) - Mail Command", "https://wiki.retromc.org/images/1/1a/Retromcnew.png");
                            embedBuilder.setColor(0x00FF00);
                            privateChannel.sendMessage(embedBuilder.build()).queue();

                            Bukkit.getScheduler().callSyncMethod(ess, () -> {
                                sender.sendMessage(Util.format("mailSentDiscord", discordUser.getAsTag()));
                                return null;
                            });
                        }, throwable -> {
                            System.out.println("Failed to send a message to user with Discord ID: " + discordID + ". Reason: " + throwable.getMessage());
                        });
                    } else {
                        System.out.println("User with Discord ID " + discordID + " could not be found in any mutual guild.");
                    }
                }, throwable -> {
                    System.out.println("Failed to retrieve user with Discord ID: " + discordID + ". Reason: " + throwable.getMessage());
                });
            } else {
                System.out.println("Failed to send mail to " + targetUsername + " on Discord: No Discord ID found.");
            }
        } else {
            System.out.println("Failed to send mail to " + targetUsername + " on Discord: No UUID found in Essentials data.");
        }
    }
}
