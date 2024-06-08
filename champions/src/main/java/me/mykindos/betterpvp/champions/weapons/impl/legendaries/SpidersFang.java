package me.mykindos.betterpvp.champions.weapons.impl.legendaries;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.cooldowns.CooldownManager;
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
import me.mykindos.betterpvp.core.combat.events.VelocityType;
import me.mykindos.betterpvp.core.effects.EffectTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Effect;
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

    private double webPounceStrength;
    private double fallDamageLimit;

    private final Champions champions;
    private final ChampionsManager championsManager;

    @Inject
    public SpidersFang(Champions champions, CooldownManager cooldownManager, ChampionsManager championsManager, ClientManager clientManager) {
        super(champions, cooldownManager, clientManager, "spiders_fang");
        this.champions = champions;
        this.championsManager = championsManager;
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
    protected String getChargeableName() {
        return RIGHT_CLICK_ABILITY_NAME;
    }

    @Override
    protected void doChargeAbility(Player player) {
        VelocityData velocityData = new VelocityData(player.getLocation().getDirection(), webPounceStrength, false, 0.0D, 0.2D, 1.0D, true);
        UtilVelocity.velocity(player, null, velocityData, VelocityType.CUSTOM);
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 30);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_DEATH, 0.5F, 2.0F);

        UtilServer.runTaskLater(champions, () -> {
            championsManager.getEffects().addEffect(player, player, EffectTypes.NO_FALL, getChargeableName(), (int)fallDamageLimit,
                    50L, true, true, UtilBlock::isGrounded);
        }, 3L);

        // To-Do: Task for web upon landing
    }

    @UpdateEvent(delay = 100)
    public void SpidersFang() {
        if (!enabled) {
            return;
        }

        // Process Wall Cling
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
            UtilMessage.simpleMessage(player, getSimpleName(), String.format("You cannot use <green>%s <gray>while in water", RIGHT_CLICK_ABILITY_NAME));
            return false;
        }
        return true;
    }

    @Override
    public void loadWeaponConfig() {
        webPounceStrength = getConfig("webPounceStrength", 2.0, Double.class);
        fallDamageLimit = getConfig("fallDamageLimit", 15.0, Double.class);
    }
}
