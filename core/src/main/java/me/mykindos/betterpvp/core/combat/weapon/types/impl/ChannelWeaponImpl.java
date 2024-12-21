package me.mykindos.betterpvp.core.combat.weapon.types.impl;

import me.mykindos.betterpvp.core.combat.weapon.Weapon;
import me.mykindos.betterpvp.core.combat.weapon.types.ChannelWeapon;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class ChannelWeaponImpl extends Weapon implements ChannelWeapon {

    protected final Set<UUID> channelling = new HashSet<>();
    protected final Set<UUID> activeUsageNotifications = new HashSet<>();

    public ChannelWeaponImpl(BPvPPlugin plugin, String key) {
        super(plugin, key);
    }

    @Override
    public boolean channel(Player player) {
        return channelling.add(player.getUniqueId());
    }

    @Override
    public boolean cancel(Player player) {
        activeUsageNotifications.remove(player.getUniqueId());
        return channelling.remove(player.getUniqueId());
    }

    @Override
    public boolean isChannelling(Player player) {
        return channelling.contains(player.getUniqueId());
    }

    @Override
    public double getEnergy() {
        return initialEnergyCost;
    }
}