package me.mykindos.betterpvp.core.combat.weapon.types.impl;

import me.mykindos.betterpvp.core.client.repository.ClientManager;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.cooldowns.CooldownManager;
import me.mykindos.betterpvp.core.combat.weapon.Weapon;
import me.mykindos.betterpvp.core.combat.weapon.data.WeaponChargeData;
import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import me.mykindos.betterpvp.core.components.champions.weapons.types.ChargeableWeapon;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.model.display.PermanentComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Iterator;
import java.util.WeakHashMap;

@BPvPListener
public abstract class ChargeableWeaponImpl extends Weapon implements ChargeableWeapon, Listener {

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

    public ChargeableWeaponImpl(BPvPPlugin plugin, CooldownManager cooldownManager, ClientManager clientManager, String key) {
        super(plugin, key);
        this.cooldownManager = cooldownManager;
        this.clientManager = clientManager;
    }

    @Override
    public void track(Player player) {
        trackCharges(player, clientManager.search().online(player).getGamer());
    }

    @Override
    public void trackCharges(Player player, Gamer gamer) {
        charges.computeIfAbsent(player, k -> new WeaponChargeData(getInitialCharges()));
        gamer.getActionBar().add(900, actionBarComponent);
    }

    @Override
    public void abandon(Player player) {
        abandonCharges(player, clientManager.search().online(player).getGamer());
    }

    @Override
    public void abandonCharges(Player player, Gamer gamer) {
        charges.remove(player);
        gamer.getActionBar().remove(actionBarComponent);
    }

    @Override
    public void activate(Player player) {
        if (!isEnabled() || !isUsable(player)) {
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
                useCharge(player);
            }
            else {
                UtilMessage.simpleMessage(player, getSimpleName(), "You don't have any <green>%s <gray>charges.", getChargeableName(), charges);
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

            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }

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
}
