package com.earth2me.essentials.commands;

import com.earth2me.essentials.*;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;


public abstract class EssentialsCommand implements IEssentialsCommand {
    protected final static Logger logger = Logger.getLogger("Minecraft");
    private final transient String name;
    protected transient IEssentials ess;

    protected EssentialsCommand(final String name) {
        this.name = name;
    }

    public static String getFinalArg(final String[] args, final int start) {
        final StringBuilder bldr = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i != start) {
                bldr.append(" ");
            }
            bldr.append(args[i]);
        }
        return bldr.toString();
    }

    public void setEssentials(final IEssentials ess) {
        this.ess = ess;
    }

    public String getName() {
        return name;
    }

    protected User getPlayer(final Server server, final String[] args, final int pos) throws NoSuchFieldException, NotEnoughArgumentsException {
        return getPlayer(server, args, pos, false);
    }

    protected User getPlayer(final Server server, final String[] args, final int pos, final boolean getOffline) throws NoSuchFieldException, NotEnoughArgumentsException {
        if (args.length <= pos) {
            throw new NotEnoughArgumentsException();
        }
        final User user = ess.getUser(args[pos]);
        if (user != null) {
            if (!getOffline && (user.getBase() instanceof OfflinePlayer || user.isHidden())) {
                throw new NoSuchFieldException(Util.i18n("playerNotFound"));
            }
            return user;
        }
        final List<Player> matches = server.matchPlayer(args[pos]);

        if (!matches.isEmpty()) {
            for (Player player : matches) {
                final User userMatch = ess.getUser(player);
                if (userMatch.getDisplayName().startsWith(args[pos]) && (getOffline || !userMatch.isHidden())) {
                    return userMatch;
                }
            }
            final User userMatch = ess.getUser(matches.get(0));
            if (getOffline || !userMatch.isHidden()) {
                return userMatch;
            }
        }
        throw new NoSuchFieldException(Util.i18n("playerNotFound"));
    }

    @Override
    public final void run(final Server server, final User user, final String commandLabel, final Command cmd, final String[] args) throws Exception {
        final Trade charge = new Trade(this.getName(), ess);
        charge.isAffordableFor(user);
        run(server, user, commandLabel, args);
        charge.charge(user);
    }

    protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        run(server, user.getBase(), commandLabel, args);
    }

    @Override
    public final void run(final Server server, final CommandSender sender, final String commandLabel, final Command cmd, final String[] args) throws Exception {
        run(server, sender, commandLabel, args);
    }

    protected void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception {
        throw new Exception(Util.format("onlyPlayers", commandLabel));
    }
}
