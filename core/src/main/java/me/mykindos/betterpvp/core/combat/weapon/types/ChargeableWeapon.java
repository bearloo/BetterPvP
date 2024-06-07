package me.mykindos.betterpvp.core.combat.weapon.types;

import me.mykindos.betterpvp.core.combat.weapon.Weapon;
import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import me.mykindos.betterpvp.core.combat.weapon.data.WeaponChargeData;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Iterator;
import java.util.WeakHashMap;

public abstract class ChargeableWeapon extends Weapon implements InteractWeapon, Listener {

    protected final WeakHashMap<UUID, WeaponCharge> charges = new WeakHashMap<>();

    public ChargeableWeapon(BPvPPlugin plugin, String key) {
        super(plugin, key);
    }

    public ChargeableWeapon(BPvPPlugin plugin, String key, List<Component> lore) {
        super(key, plugin, lore);
    }

    public abstract void useCharge(Player player);
    public abstract void notifyChargeUse(Player player);

    public void trackCharges(Player player)
    {

    }

    public void invalidateCharges(Player player)
    {

    }

    @UpdateEvent
    protected void recharge() {

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        invalidateCharges(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        invalidateCharges(event.getPlayer().getUniqueId());
    }

}
