package me.mykindos.betterpvp.core.combat.weapon.types.impl;

import me.mykindos.betterpvp.core.client.repository.ClientManager;
import me.mykindos.betterpvp.core.cooldowns.CooldownManager;
import me.mykindos.betterpvp.core.combat.weapon.types.ChannelWeapon;
import me.mykindos.betterpvp.core.combat.weapon.types.impl.ChargeableWeaponImpl;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@BPvPListener
public abstract class ChargeableChannelWeaponImpl extends ChargeableWeaponImpl implements ChannelWeapon, Listener {

    protected final Set<UUID> channelling = new HashSet<>();

    public ChargeableChannelWeaponImpl(BPvPPlugin plugin, CooldownManager cooldownManager, ClientManager clientManager, String key) {
        super(plugin, cooldownManager, clientManager, key);
    }

    @Override
    public boolean channel(Player player) {
        return channelling.add(player.getUniqueId());
    }

    @Override
    public boolean cancel(Player player) {
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

    @Override
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cancel(event.getEntity());
        abandon(event.getEntity());
    }

    @Override
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
        abandon(event.getPlayer());
    }
}