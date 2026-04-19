package com.earth2me.essentials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class InvseeManager implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private static final String PENDING_DIR = "invsee-pending";

    private final IEssentials ess;

    // viewerName > targetName
    private final Map<String, String> sessions = new HashMap<String, String>();

    // viewerName > offline target User (strong ref — prevents GC, keeps config object alive)
    private final Map<String, User> offlineTargets = new HashMap<String, User>();

    // viewerName > inventory snapshot at last sync tick (bidirectional change detection)
    private final Map<String, ItemStack[]> lastSyncState = new HashMap<String, ItemStack[]>();

    public InvseeManager(IEssentials ess) {
        this.ess = ess;
    }

    public void startSession(User viewer, User target, boolean offline) {
        sessions.put(viewer.getName(), target.getName());
        if (offline) {
            offlineTargets.put(viewer.getName(), target);
        } else {
            lastSyncState.put(viewer.getName(), cloneContents(target.getInventory().getContents()));
        }
    }

    public boolean hasSession(String viewerName) {
        return sessions.containsKey(viewerName);
    }

    public String getTargetName(String viewerName) {
        return sessions.get(viewerName);
    }

    public boolean isOfflineSession(String viewerName) {
        return offlineTargets.containsKey(viewerName);
    }

    public void endSession(User viewer) {
        String targetName  = sessions.remove(viewer.getName());
        User   offlineTarget = offlineTargets.remove(viewer.getName());
        lastSyncState.remove(viewer.getName());

        if (offlineTarget != null) {
            // Offline session: write viewer's current inventory to a dedicated pending file.
            // Using a fresh EssentialsConf completely bypasses UserData caching.
            savePendingFile(offlineTarget.getName(), viewer.getInventory().getContents());
        } else if (targetName != null) {
            // Online session: final push to the live target
            User target = ess.getUser(targetName);
            if (target != null && !(target.getBase() instanceof OfflinePlayer)) {
                target.getInventory().setContents(viewer.getInventory().getContents());
                target.getBase().updateInventory();
            }
        }
    }

    // Removes session tracking without saving (forced cleanup)
    public void forceEndSession(String viewerName) {
        sessions.remove(viewerName);
        offlineTargets.remove(viewerName);
        lastSyncState.remove(viewerName);
    }


     // Writes items to  <dataFolder>/invsee-pending/<playerName>.yml.
    // Always uses a fresh EssentialsConf, zero caching, guaranteed disk clean.
    private void savePendingFile(String playerName, ItemStack[] contents) {
        try {
            File dir = new File(ess.getDataFolder(), PENDING_DIR);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, Util.sanitizeFileName(playerName) + ".yml");
            EssentialsConf cfg = new EssentialsConf(file);
            cfg.load();

            cfg.setProperty("size", contents.length);
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    cfg.setProperty(String.valueOf(i), contents[i]);
                } else {
                    cfg.removeProperty(String.valueOf(i));
                }
            }

            boolean saved = cfg.save();
            if (!saved) {
                LOGGER.log(Level.WARNING, "[Essentials] invsee: failed to write pending file for " + playerName);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Essentials] invsee: error saving pending inventory for " + playerName, e);
        }
    }

     // Reads items from  <dataFolder>/invsee-pending/<playerName>.yml and deletes the file.
     //Returns null if no pending file exists.
    public ItemStack[] loadAndClearPendingFile(String playerName) {
        File dir  = new File(ess.getDataFolder(), PENDING_DIR);
        File file = new File(dir, Util.sanitizeFileName(playerName) + ".yml");
        if (!file.exists()) return null;

        try {
            EssentialsConf cfg = new EssentialsConf(file);
            cfg.load();

            int size = cfg.getInt("size", 0);
            if (size < 1) {
                file.delete();
                return null;
            }

            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = cfg.getItemStack(String.valueOf(i));
            }

            file.delete();
            return items;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Essentials] invsee: error loading pending inventory for " + playerName, e);
            file.delete(); // delete corrupted file so it doesn't loop forever
            return null;
        }
    }

    @Override
    public void run() {
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, String> entry : sessions.entrySet()) {
            String viewerName = entry.getKey();
            String targetName = entry.getValue();

            if (offlineTargets.containsKey(viewerName)) {
                continue;
            }

            User viewer = ess.getUser(viewerName);
            if (viewer == null || viewer.getBase() instanceof OfflinePlayer) {
                toRemove.add(viewerName);
                continue;
            }

            User target = ess.getUser(targetName);
            if (target == null || target.getBase() instanceof OfflinePlayer) {
                continue;
            }

            syncBidirectional(viewer, target);
        }

        for (String name : toRemove) {
            forceEndSession(name);
        }
    }


      //Per-slot change detection: each side's changes are applied to the other
     // without clobbering changes the other side made in the same tick.
    // Admin (viewer) wins if both change the same slot simultaneously.
    private void syncBidirectional(User viewer, User target) {
        ItemStack[] viewerInv = viewer.getInventory().getContents();
        ItemStack[] targetInv = target.getInventory().getContents();
        ItemStack[] snapshot  = lastSyncState.get(viewer.getName());

        if (snapshot == null || viewerInv.length != snapshot.length) {
            lastSyncState.put(viewer.getName(), cloneContents(targetInv));
            viewer.getInventory().setContents(Arrays.copyOf(targetInv, viewerInv.length));
            viewer.getBase().updateInventory();
            return;
        }

        int size = viewerInv.length;
        ItemStack[] newViewerInv = Arrays.copyOf(viewerInv, size);
        ItemStack[] newTargetInv = Arrays.copyOf(targetInv, Math.max(targetInv.length, size));
        ItemStack[] newSnapshot  = new ItemStack[size];
        boolean viewerChanged = false;
        boolean targetChanged = false;

        for (int i = 0; i < size; i++) {
            ItemStack vSlot = viewerInv[i];
            ItemStack tSlot = (i < targetInv.length) ? targetInv[i] : null;
            ItemStack snap  = snapshot[i];

            boolean viewerDiff = !stackEquals(vSlot, snap);
            boolean targetDiff = !stackEquals(tSlot, snap);

            if (viewerDiff && !targetDiff) {
                newTargetInv[i] = cloneStack(vSlot);
                newSnapshot[i]  = cloneStack(vSlot);
                targetChanged = true;
            } else if (targetDiff && !viewerDiff) {
                newViewerInv[i] = cloneStack(tSlot);
                newSnapshot[i]  = cloneStack(tSlot);
                viewerChanged = true;
            } else if (viewerDiff) { // both changed — admin wins
                newTargetInv[i] = cloneStack(vSlot);
                newSnapshot[i]  = cloneStack(vSlot);
                targetChanged = true;
            } else {
                newSnapshot[i] = cloneStack(snap);
            }
        }

        lastSyncState.put(viewer.getName(), newSnapshot);

        if (targetChanged) {
            target.getInventory().setContents(newTargetInv);
            target.getBase().updateInventory();
        }
        if (viewerChanged) {
            viewer.getInventory().setContents(newViewerInv);
            viewer.getBase().updateInventory();
        }
    }

    private static ItemStack[] cloneContents(ItemStack[] src) {
        if (src == null) return new ItemStack[0];
        ItemStack[] copy = new ItemStack[src.length];
        for (int i = 0; i < src.length; i++) copy[i] = cloneStack(src[i]);
        return copy;
    }

    private static ItemStack cloneStack(ItemStack s) {
        return (s == null) ? null : s.clone();
    }

    private static boolean stackEquals(ItemStack a, ItemStack b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.getType() == Material.AIR && b.getType() == Material.AIR) return true;
        return a.equals(b);
    }
}
