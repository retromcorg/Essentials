package com.earth2me.essentials.perm;

import com.earth2me.essentials.retrobridge.RetroBridgeAccess;
import org.retromc.retrobridge.bridge.permission.PermissionBridge;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RetroBridgeHandler implements IPermissionsHandler {
    private final transient RetroBridgeAccess access;

    public RetroBridgeHandler() {
        access = new RetroBridgeAccess();
    }

    @Override
    public String getGroup(Player base) {
        PermissionBridge bridge = getPermissionBridge();
        String group = bridge == null ? "" : bridge.getPrimaryGroup(base.getUniqueId());
        return group == null ? "" : group;
    }

    @Override
    public List<String> getGroups(Player base) {
        List<String> groups = new ArrayList<String>();
        PermissionBridge bridge = getPermissionBridge();
        if (bridge != null) {
            String[] array = bridge.getGroups(base.getUniqueId());
            if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null && array[i].length() > 0) {
                    groups.add(array[i]);
                }
            }
            }
        }
        return groups;
    }

    @Override
    public boolean canBuild(Player base, String group) {
        return base.hasPermission("essentials.build") || base.isOp();
    }

    @Override
    public boolean inGroup(Player base, String group) {
        if (group == null || group.length() == 0) {
            return false;
        }
        PermissionBridge bridge = getPermissionBridge();
        return bridge != null && bridge.isInGroup(base.getUniqueId(), group);
    }

    @Override
    public boolean hasPermission(Player base, String node) {
        if (node == null || node.length() == 0) {
            return false;
        }
        PermissionBridge bridge = getPermissionBridge();
        return bridge != null && bridge.has(base.getUniqueId(), node);
    }

    @Override
    public String getPrefix(Player base) {
        PermissionBridge bridge = getPermissionBridge();
        String prefix = bridge == null ? "" : bridge.getPrefix(base.getUniqueId());
        return prefix == null ? "" : prefix;
    }

    @Override
    public String getSuffix(Player base) {
        PermissionBridge bridge = getPermissionBridge();
        String suffix = bridge == null ? "" : bridge.getSuffix(base.getUniqueId());
        return suffix == null ? "" : suffix;
    }

    private PermissionBridge getPermissionBridge() {
        if (!access.isAvailable()) {
            return null;
        }
        return access.getPermissionBridge();
    }
}
