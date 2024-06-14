package me.mykindos.betterpvp.core.combat.weapon.types;

import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@BPvPListener
public interface ChannelWeapon extends IWeapon, Listener {

    public abstract void channel(Player player);
    public abstract void cancel(Player player);

    public abstract double getEnergy();

    default boolean useShield(Player player) {
        return false;
    }

    @EventHandler
    default void onDeath(PlayerDeathEvent event) {
        cancel(event.getEntity());
    }

    @EventHandler
    default void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }
}
