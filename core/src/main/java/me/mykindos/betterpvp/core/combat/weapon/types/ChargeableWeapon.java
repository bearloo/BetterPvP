package me.mykindos.betterpvp.core.combat.weapon.types;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.core.client.repository.ClientManager;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.cooldowns.CooldownManager;
import me.mykindos.betterpvp.core.combat.weapon.Weapon;
import me.mykindos.betterpvp.core.combat.weapon.data.WeaponChargeData;
import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import me.mykindos.betterpvp.core.components.champions.events.PlayerUseItemEvent;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import me.mykindos.betterpvp.core.utilities.model.display.PermanentComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Iterator;
import java.util.WeakHashMap;

@Singleton
@BPvPListener
public abstract class ChargeableWeapon extends Weapon implements InteractWeapon, Listener {

    public static boolean x = false;

    protected final ClientManager clientManager;
    protected final CooldownManager cooldownManager;

    protected final WeakHashMap<Player, WeaponChargeData> charges = new WeakHashMap<>();

    private final PermanentComponent actionBarComponent = new PermanentComponent(gamer -> {
        final Player player = gamer.getPlayer();

        if (player == null || !charges.containsKey(player) || !isHoldingWeapon(player)) {
            return null;
        }

        final int currentCharges = charges.get(player).getCharges();

        return Component.text(getChargeableName() + " ").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
                .append(Component.text("\u25A0".repeat(currentCharges)).color(NamedTextColor.GREEN))
                .append(Component.text("\u25A0".repeat(Math.max(0, maxCharges - currentCharges))).color(NamedTextColor.RED));
    });

    public ChargeableWeapon(BPvPPlugin plugin, CooldownManager cooldownManager, ClientManager clientManager, String key) {
        super(plugin, key);
        this.cooldownManager = cooldownManager;
        this.clientManager = clientManager;
    }

    protected abstract String getChargeableName();
    protected abstract void doChargeAbility(Player player);

    private void notifyCharges(Player player, int charges) {
        UtilMessage.simpleMessage(player, getSimpleName(), String.format("%s Charges: <yellow>%d", getChargeableName(), charges));
    }

    protected int getInitialCharges() {
        return maxCharges;
    }

    public void trackCharges(Player player, Gamer gamer) {
        charges.computeIfAbsent(player, k -> new WeaponChargeData());

        WeaponChargeData data = charges.get(player);
        if (data != null)
        {
            data.setCharges(getInitialCharges());
        }

        gamer.getActionBar().add(900, actionBarComponent);
    }

    public void invalidateCharges(Player player, Gamer gamer) {
        charges.remove(player);
        gamer.getActionBar().remove(actionBarComponent);
    }

    protected boolean playerHasWeapon(Player player) {
        return true;
    }

    @Override
    public void activate(Player player) {
        if (!x)
        {
            trackCharges(player, clientManager.search().online(player).getGamer());
            x = true;
        }

        var checkUsageEvent = UtilServer.callEvent(new PlayerUseItemEvent(player, this, true));
        if (checkUsageEvent.isCancelled()) {
            UtilMessage.simpleMessage(player, "Restriction", "You cannot use this weapon here.");
            return;
        }

        if (canUse(player)) {
            WeaponChargeData data = charges.get(player);

            if (data != null && data.getCharges() > 0) {
                if (data.getCharges() >= maxCharges) {
                    // Reset recharge cooldown when using the first charge
                    cooldownManager.use(player, getChargeableName(), rechargeSeconds, false, true, true);
                }

                data.useCharge();
                notifyCharges(player, data.getCharges());
                doChargeAbility(player);
            }
            else {
                UtilMessage.simpleMessage(player, getSimpleName(), String.format("You don't have any <green>%s <gray>charges.", getChargeableName(), charges));
            }
        }
    }

    @UpdateEvent(delay = 100)
    public void recharge() {
        final Iterator<Map.Entry<Player, WeaponChargeData>> iterator = charges.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Player, WeaponChargeData> entry = iterator.next();

            final Player player = entry.getKey();
            final WeaponChargeData data = entry.getValue();

            if (data.getCharges() >= maxCharges) {
                continue; // Skip if at max charges
            }

            if (!cooldownManager.use(player, getChargeableName(), rechargeSeconds, false, true, true)) {
                continue; // Skip if recharge cooldown has not expired
            }

            data.addCharge();
            notifyCharges(player, data.getCharges());
        }
    }

    //PlayerDropItemEvent
    //PlayerPickupItemEvent
    //PlayerClickInventoryEvent

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        //invalidateCharges(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //invalidateCharges(event.getPlayer());
    }
}
