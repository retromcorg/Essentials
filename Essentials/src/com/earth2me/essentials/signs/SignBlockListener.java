package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

import java.util.logging.Level;
import java.util.logging.Logger;


public class SignBlockListener extends BlockListener {
    private final static Logger LOGGER = Logger.getLogger("Minecraft");
    private final transient IEssentials ess;

    public SignBlockListener(IEssentials ess) {
        this.ess = ess;
    }

    @Override
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled() || ess.getSettings().areSignsDisabled()) {
            return;
        }

        if (protectSignsAndBlocks(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public boolean protectSignsAndBlocks(final Block block, final Player player) {
        final int mat = block.getTypeId();
        if (mat == Material.SIGN_POST.getId() || mat == Material.WALL_SIGN.getId()) {
            final Sign csign = (Sign) block.getState();
            for (Signs signs : Signs.values()) {
                final EssentialsSign sign = signs.getSign();
                if (csign.getLine(0).equalsIgnoreCase(sign.getSuccessName())
                        && !sign.onSignBreak(block, player, ess)) {
                    return true;
                }
            }
        } else {
            // prevent any signs be broken by destroying the block they are attached to
            if (EssentialsSign.checkIfBlockBreaksSigns(block)) {
                LOGGER.log(Level.INFO, "Prevented that a block was broken next to a sign.");
                return true;
            }
            for (Signs signs : Signs.values()) {
                final EssentialsSign sign = signs.getSign();
                if (sign.getBlocks().contains(block.getType())
                        && !sign.onBlockBreak(block, player, ess)) {
                    LOGGER.log(Level.INFO, "A block was protected by a sign.");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSignChange(final SignChangeEvent event) {
        if (event.isCancelled() || ess.getSettings().areSignsDisabled()) {
            return;
        }
        User user = ess.getUser(event.getPlayer());
        if (user.isAuthorized("essentials.signs.color")) {
            for (int i = 0; i < 4; i++) {
                event.setLine(i, event.getLine(i).replaceAll("&([0-9a-f])", "§$1"));
            }
        }
        for (Signs signs : Signs.values()) {
            final EssentialsSign sign = signs.getSign();
            if (event.getLine(0).equalsIgnoreCase(sign.getSuccessName())) {
                event.setCancelled(true);
                return;
            }
            if (event.getLine(0).equalsIgnoreCase(sign.getTemplateName())
                    && !sign.onSignCreate(event, ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled() || ess.getSettings().areSignsDisabled()) {
            return;
        }

        // If player is muted and they are trying to place a sign, cancel the event. (RetroMC Start)
        User user = ess.getUser(event.getPlayer());
        if (user.isMuted() && (event.getBlockPlaced().getType() == Material.SIGN || event.getBlockPlaced().getType() == Material.SIGN_POST || event.getBlockPlaced().getType() == Material.WALL_SIGN)) {
            event.getPlayer().sendMessage(Util.i18n("voiceSilenced").replace("&", "\u00a7")); //Unsure if the replace is needed, but it's there just in case.
            event.setCancelled(true);
            return;
        }
        // (RetroMC End)

        final Block against = event.getBlockAgainst();
        if ((against.getType() == Material.WALL_SIGN
                || against.getType() == Material.SIGN_POST)
                && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(against))) {
            event.setCancelled(true);
            return;
        }
        final Block block = event.getBlock();
        if (block.getType() == Material.WALL_SIGN
                || block.getType() == Material.SIGN_POST) {
            return;
        }
        for (Signs signs : Signs.values()) {
            final EssentialsSign sign = signs.getSign();
            if (sign.getBlocks().contains(block.getType())
                    && !sign.onBlockPlace(block, event.getPlayer(), ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockBurn(final BlockBurnEvent event) {
        if (event.isCancelled() || ess.getSettings().areSignsDisabled()) {
            return;
        }

        final Block block = event.getBlock();
        if (((block.getType() == Material.WALL_SIGN
                || block.getType() == Material.SIGN_POST)
                && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)))
                || EssentialsSign.checkIfBlockBreaksSigns(block)) {
            event.setCancelled(true);
            return;
        }
        for (Signs signs : Signs.values()) {
            final EssentialsSign sign = signs.getSign();
            if (sign.getBlocks().contains(block.getType())
                    && !sign.onBlockBurn(block, ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (event.isCancelled() || ess.getSettings().areSignsDisabled()) {
            return;
        }

        final Block block = event.getBlock();
        if (((block.getType() == Material.WALL_SIGN
                || block.getType() == Material.SIGN_POST)
                && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)))
                || EssentialsSign.checkIfBlockBreaksSigns(block)) {
            event.setCancelled(true);
            return;
        }
        for (Signs signs : Signs.values()) {
            final EssentialsSign sign = signs.getSign();
            if (sign.getBlocks().contains(block.getType())
                    && !sign.onBlockIgnite(block, ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (((block.getType() == Material.WALL_SIGN
                    || block.getType() == Material.SIGN_POST)
                    && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)))
                    || EssentialsSign.checkIfBlockBreaksSigns(block)) {
                event.setCancelled(true);
                return;
            }
            for (Signs signs : Signs.values()) {
                final EssentialsSign sign = signs.getSign();
                if (sign.getBlocks().contains(block.getType())
                        && !sign.onBlockPush(block, ess)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        if (event.isSticky()) {
            final Block block = event.getBlock();
            if (((block.getType() == Material.WALL_SIGN
                    || block.getType() == Material.SIGN_POST)
                    && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)))
                    || EssentialsSign.checkIfBlockBreaksSigns(block)) {
                event.setCancelled(true);
                return;
            }
            for (Signs signs : Signs.values()) {
                final EssentialsSign sign = signs.getSign();
                if (sign.getBlocks().contains(block.getType())
                        && !sign.onBlockPush(block, ess)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
