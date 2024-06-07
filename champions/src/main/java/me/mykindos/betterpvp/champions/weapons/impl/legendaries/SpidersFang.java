package me.mykindos.betterpvp.champions.weapons.impl.legendaries;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.client.repository.ClientManager;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.combat.events.PreCustomDamageEvent;
import me.mykindos.betterpvp.core.combat.weapon.types.ChargeableWeapon;
import me.mykindos.betterpvp.core.combat.weapon.types.LegendaryWeapon;
import me.mykindos.betterpvp.core.combat.weapon.data.WeaponChargeData;
import me.mykindos.betterpvp.core.components.champions.events.PlayerUseItemEvent;
import me.mykindos.betterpvp.core.energy.EnergyHandler;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilBlock;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import me.mykindos.betterpvp.core.utilities.UtilVelocity;
import me.mykindos.betterpvp.core.utilities.math.VelocityData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Singleton
@BPvPListener
public class SpidersFang extends ChargeableWeapon implements LegendaryWeapon, Listener {

    private static final String SHIFT_ABILITY_NAME = "Wall Cling";
    private static final String RIGHT_CLICK_ABILITY_NAME = "Web Pounce";

    private final ClientManager clientManager;
    private final ChampionsManager championsManager;

    private double webPounceStrength;

    @Inject
    public SpidersFang(Champions champions, ChampionsManager championsManager, ClientManager clientManager) {
        super(champions, "spiders_fang");
        this.championsManager = championsManager;
        this.clientManager = clientManager;
    }

    @Override
    public List<Component> getLore(ItemMeta itemMeta) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("   TO-DO   ", NamedTextColor.WHITE));
        lore.add(Component.text("   TO-DO   ", NamedTextColor.WHITE));
        lore.add(Component.text("   TO-DO   ", NamedTextColor.WHITE));
        lore.add(Component.text("   TO-DO   ", NamedTextColor.WHITE));
        lore.add(Component.text("   TO-DO   ", NamedTextColor.WHITE));
        lore.add(Component.text(""));
        lore.add(UtilMessage.deserialize("<white>Deals <yellow>%.1f Damage <white>with attack", baseDamage));
        lore.add(UtilMessage.deserialize("<yellow>Right-Click <white>to use <green>%s", RIGHT_CLICK_ABILITY_NAME));
        lore.add(UtilMessage.deserialize("<yellow>Shift <white>to use <green>%s", SHIFT_ABILITY_NAME));
        return lore;
    }

    @Override
    public void activate(Player player) {

    }

    @UpdateEvent
    public void SpidersFang() {
        if (!enabled) {
            return;
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(PreCustomDamageEvent event) {
        if (!enabled) {
            return;
        }

        CustomDamageEvent cde = event.getCustomDamageEvent();
        if (cde.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(cde.getDamager() instanceof Player damager)) return;
        if (isHoldingWeapon(damager)) {
            cde.setDamage(baseDamage);
            cde.setRawDamage(baseDamage);
        }
    }

    @Override
    public boolean canUse(Player player) {
        if (UtilBlock.isInLiquid(player)) {
            UtilMessage.simpleMessage(player, getSimpleName(), String.format("You cannot use <green>%s <gray> while in water", ABILITY_NAME));
            return false;
        }

        WeaponCharge data = charges.get(player);
        if (wreathData != null && wreathData.getCharges() > 0) {
            return true;
        }

        UtilMessage.simpleMessage(player, getClassType().getName(), "You don't have any <alt>" + getName() + "</alt> charges.");
        return false;


        return true;
    }

    @Override
    public void loadWeaponConfig() {
        webPounceStrength = getConfig("webPounceStrength", 0.5, Double.class);
        fallDamageLimit = getConfig("fallDamageLimit", 15.0, Double.class);
    }
}
