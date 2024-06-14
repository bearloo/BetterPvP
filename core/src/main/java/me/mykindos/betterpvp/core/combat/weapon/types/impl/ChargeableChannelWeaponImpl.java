package me.mykindos.betterpvp.core.combat.weapon.types.impl;

import me.mykindos.betterpvp.core.client.repository.ClientManager;
import me.mykindos.betterpvp.core.cooldowns.CooldownManager;
import me.mykindos.betterpvp.core.components.champions.weapons.types.ChannelWeapon;
import me.mykindos.betterpvp.core.components.champions.weapons.types.impl.ChargeableWeaponImpl;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@BPvPListener
public abstract class ChargeableChannelWeaponImpl extends ChargeableWeaponImpl implements ChannelWeapon, Listener {

    protected final Set<UUID> channellers = new HashSet<>();

    public ChargeableChannelWeaponImpl(BPvPPlugin plugin, CooldownManager cooldownManager, ClientManager clientManager, String key) {
        super(plugin, key);
        this.cooldownManager = cooldownManager;
        this.clientManager = clientManager;
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