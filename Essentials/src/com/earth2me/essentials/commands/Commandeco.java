package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.johnymuffin.essentials.ESSAdv;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commandeco extends EssentialsCommand {
    public Commandeco() {
        super("eco");
    }

    private void ecoDiscord(CommandSender user, User u, Double amount, String type) {
        //Discord Addon Start
        String issuedby = (user instanceof Player) ? ((Player)user).getName() : "Console";
        String message = String.valueOf(issuedby + " Has " + type + " " + amount + " to " + u.getName());
        try {
            ESSAdv.sendDiscordEmbed("Essentials - Eco Command", message, "Essentials Log By Rhys B");
        } catch (Exception e) {
            user.sendMessage(e + ": " + e.getMessage());
        }
        //Discord Addon End
    }


    @Override
    public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
        if (args.length < 2) {
            throw new NotEnoughArgumentsException();
        }
        EcoCommands cmd;
        double amount;
        try {
            cmd = EcoCommands.valueOf(args[0].toUpperCase());
            amount = Double.parseDouble(args[2].replaceAll("[^0-9\\.]", ""));
        } catch (Exception ex) {
            throw new NotEnoughArgumentsException(ex);
        }

        if (args[1].contentEquals("*")) {
            for (Player p : server.getOnlinePlayers()) {
                User u = ess.getUser(p);
                switch (cmd) {
                    case GIVE:
                        u.giveMoney(amount);
                        break;

                    case TAKE:
                        u.takeMoney(amount);
                        break;

                    case RESET:
                        u.setMoney(amount == 0 ? ess.getSettings().getStartingBalance() : amount);
                        break;
                }
            }
        } else {
            User u = getPlayer(server, args, 1, true);
            switch (cmd) {
                case GIVE:
                    u.giveMoney(amount, sender);
                    break;

                case TAKE:
                    u.takeMoney(amount, sender);
                    break;

                case RESET:
                    u.setMoney(amount == 0 ? ess.getSettings().getStartingBalance() : amount);
                    break;
            }
        }
    }

    private enum EcoCommands {
        GIVE, TAKE, RESET
    }
}
