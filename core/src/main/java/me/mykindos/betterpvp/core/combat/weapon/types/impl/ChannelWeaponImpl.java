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

    protected final Set<UUID> channellers = new HashSet<>();

    public ChannelWeaponImpl(BPvPPlugin plugin, String key) {
        super(plugin, key);
    }

    @Override
    public void channel(Player player) {
        channellers.add(player.getUniqueId());
    }

    @Override
    public void cancel(Player player) {
        channellers.remove(player.getUniqueId());
    }

    @Override
    public double getEnergy() {
        return initialEnergyCost;
    }
}