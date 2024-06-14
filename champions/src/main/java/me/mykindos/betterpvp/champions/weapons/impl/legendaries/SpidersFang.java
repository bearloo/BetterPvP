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
import me.mykindos.betterpvp.core.combat.weapon.types.impl.ChargeableChannelWeaponImpl;
import me.mykindos.betterpvp.core.combat.weapon.types.LegendaryWeapon;
import me.mykindos.betterpvp.core.combat.weapon.data.WeaponChargeData;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.Set;

@Singleton
@BPvPListener
public class SpidersFang extends ChargeableChannelWeaponImpl implements LegendaryWeapon, Listener {

    private static final String SHIFT_ABILITY_NAME = "Wall Cling";
    private static final String RIGHT_CLICK_ABILITY_NAME = "Web Pounce";

    private double webRadius;
    private double webPounceStrength;
    private double fallDamageLimit;

    private final Champions champions;
    private final ChampionsManager championsManager;
    private final EnergyHandler energyHandler;

    @Inject
    public SpidersFang(Champions champions, CooldownManager cooldownManager, ClientManager clientManager, ChampionsManager championsManager, EnergyHandler energyHandler) {
        super(champions, cooldownManager, clientManager, "spiders_fang");
        this.champions = champions;
        this.championsManager = championsManager;
        this.energyHandler = energyHandler;
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
    public int getInitialCharges() {
        return maxCharges;
    }

    @Override
    protected void useCharge(Player player) {
        VelocityData velocityData = new VelocityData(player.getLocation().getDirection(), webPounceStrength, false, 0.0D, 0.2D, 1.0D, true);
        UtilVelocity.velocity(player, null, velocityData, VelocityType.CUSTOM);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_DEATH, 0.5F, 2.0F); // TODO Change

        UtilServer.runTaskLater(champions, () -> {
            championsManager.getEffects().addEffect(player, player, EffectTypes.NO_FALL, getChargeableName(), (int)fallDamageReduction,
                    100L, true, true, UtilBlock::isGrounded);
        }, 3L);

        // TODO Task for web particles upon landing
        // TODO Web nearby players in radius (slowness, mp jagged effect?)
    }

    @UpdateEvent
    public void doSpidersFang() {
        if (!isEnabled()) {
            return;
        }

        final Iterator<UUID> iterator = active.iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer(iterator.next());
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }

            if (!isUsable()) {
                iterator.remove();
                continue;
            }

            if (!player.isSneaking()) {
                iterator.remove();
                continue;
            }

            if (UtilBlock.isInLiquid(player)) {
                UtilMessage.simpleMessage(player, getSimpleName(), "You cannot use <green>%s <gray>while in water", SHIFT_ABILITY_NAME);
                iterator.remove();
                continue;
            }

            if (!isHoldingWeapon(player)) {
                continue;
            }

            if (!canCling(player)) {
                continue;
            }

            if (!energyHandler.use(player, SHIFT_ABILITY_NAME, energyPerTick, true)) {
                iterator.remove;
                continue;
            }

            doWallCling(player);
        }
    }

    public void doWallCling(Player player) {
        Vector hoverVector = player.getLocation().getDirection();
        hoverVector.setY(0); // TODO Required?

        VelocityData hoverVelocity = new VelocityData(hoverVector, 0, false, 0.0D, 0.0D, 0.0D, true);
        UtilVelocity.velocity(player, null, hoverVelocity, VelocityType.CUSTOM);
    }

    public boolean canCling(Player player) {
        ArrayList<Block> surrounding = UtilBlock.getBlocksSurroundingPlayer(player, false);
        return surrounding.stream().anyMatch(block -> UtilBlock.solid(block));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (event.isSneaking() && isHoldingWeapon(player)) {
            active.add(player.getUniqueId());
        } else {
            active.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(PreCustomDamageEvent event) {
        if (!isEnabled()) {
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
            UtilMessage.simpleMessage(player, getSimpleName(), "You cannot use <green>%s <gray>while in water", RIGHT_CLICK_ABILITY_NAME);
            return false;
        }
        return true;
    }

    @Override
    public void loadWeaponConfig() {
        webRadius = getConfig("webRadius", 3.0, Double.class);
        webPounceStrength = getConfig("webPounceStrength", 2.0, Double.class);
        fallDamageReduction = getConfig("fallDamageReduction", 15.0, Double.class);
    }
}
