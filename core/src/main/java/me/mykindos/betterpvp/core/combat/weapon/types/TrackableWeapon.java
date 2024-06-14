package me.mykindos.betterpvp.core.combat.weapon.types;

import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

@BPvPListener
public interface TrackableWeapon extends IWeapon, Listener {

    public abstract void track(Player player);
    public abstract void abandon(Player player);

    //PlayerDropItemEvent
    //PlayerPickupItemEvent
    //PlayerClickInventoryEvent

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        abandon(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        abandon(event.getPlayer());
    }
}
