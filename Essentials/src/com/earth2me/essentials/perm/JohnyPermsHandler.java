package com.earth2me.essentials.perm;

import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.JohnyPermsAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JohnyPermsHandler implements IPermissionsHandler {
    private final transient JohnyPermsAPI api;

    public JohnyPermsHandler() {
        this.api = JohnyPerms.getJohnyPermsAPI();
    }


    @Override
    public String getGroup(Player base) {
        return this.api.getUser(base.getUniqueId()).getGroup().getName();
    }

    @Override
    public List<String> getGroups(Player base) {
        List<String> myList = new ArrayList<>();
        myList.add(this.api.getUser(base.getUniqueId()).getGroup().getName());
        return myList;
    }

    @Override
    public boolean canBuild(Player base, String group) {
        return base.hasPermission("essentials.build");
    }

    @Override
    public boolean inGroup(Player base, String group) {
        return this.api.getUser(base.getUniqueId()).getGroup().getName().equalsIgnoreCase(group);
    }

    @Override
    public boolean hasPermission(Player base, String node) {
        return base.hasPermission(node);
    }

    @Override
    public String getPrefix(Player base) {
        String prefix = this.api.getUser(base.getUniqueId()).getPrefix();
        if (prefix == null) {
            prefix = this.api.getUser(base.getUniqueId()).getGroup().getPrefix();
        }
        return prefix;
    }

    @Override
    public String getSuffix(Player base) {
        String prefix = this.api.getUser(base.getUniqueId()).getSuffix();
        if (prefix == null) {
            prefix = this.api.getUser(base.getUniqueId()).getGroup().getSuffix();
        }
        return prefix;
    }
}
