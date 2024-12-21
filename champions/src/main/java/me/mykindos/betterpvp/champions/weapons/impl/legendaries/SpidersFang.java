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
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import me.mykindos.betterpvp.core.utilities.UtilVelocity;
import me.mykindos.betterpvp.core.utilities.math.VelocityData;
import me.mykindos.betterpvp.core.combat.events.VelocityType;
import me.mykindos.betterpvp.core.effects.EffectTypes;
import me.mykindos.betterpvp.core.particles.effects.Cobweb;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

    private int webRadius;
    private int webSlowness;
    private double webDuration;
    private double webPounceStrength;
    private double fallDamageReduction;

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
    public String getChargeableName() {
        return RIGHT_CLICK_ABILITY_NAME;
    }

    @Override
    public double getEnergy() {
        return 0; // Right click does not require energy
    }

    @Override
    public void useCharge(Player player) {
        if (isChannelling(player)) {
            cancel(player); // Prevent wall cling velocity from stopping the pounce
        }

        doWebPounce(player);
    }

    @UpdateEvent
    public void doSpidersFang() {
        if (!isEnabled()) {
            return;
        }

        final Iterator<UUID> iterator = channelling.iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer(iterator.next());
            if (player == null) {
                iterator.remove();
                continue;
            }

            if (!isUsable(player)) {
                iterator.remove();
                continue;
            }

            if (!player.isSneaking()) {
                iterator.remove();
                continue;
            }

            if (UtilBlock.isGrounded(player)) {
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
                iterator.remove();
                continue;
            }

            doWallCling(player);
        }
    }

    private void doWallCling(Player player) {
        // Apply a small amount of upwards velocity to stop players from sliding down the wall
        VelocityData hoverVelocity = new VelocityData(player.getLocation().getDirection(), 0, true, 0.01D, 0.0D, 0.01D, false);
        UtilVelocity.velocity(player, null, hoverVelocity, VelocityType.CUSTOM);
    }

    private void doWebPounce(Player player) {
        VelocityData velocityData = new VelocityData(player.getLocation().getDirection(), webPounceStrength, false, 0.0D, 0.8D, 1.0D, true);
        UtilVelocity.velocity(player, null, velocityData, VelocityType.CUSTOM);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, SoundCategory.PLAYERS, 2.0F, 0.6F);
        new Cobweb(player.getLocation())
                .setRadius(webRadius)
                .setViewDistance(60)
                .draw();

        UtilServer.runTaskLater(champions, () -> {
            championsManager.getEffects().addEffect(player, player, EffectTypes.NO_FALL, getChargeableName(), (int)fallDamageReduction,
                    100L, true, true, UtilBlock::isGrounded);
        }, 3L);

        final List<Player> enemies = UtilPlayer.getNearbyEnemies(player, player.getLocation(), webRadius);
        for (Player enemy : enemies) {
            web(player, enemy);
        }
    }

    private void web(Player caster, Player target) {
        long duration = (long) (webDuration * 1000L);

        UtilMessage.simpleMessage(caster, getSimpleName(), "You hit <alt>" + target.getName() + "</alt> with <alt>" + RIGHT_CLICK_ABILITY_NAME);
        UtilMessage.simpleMessage(target, getSimpleName(), "<alt>" + caster.getName() + "</alt> hit you with <alt>" + RIGHT_CLICK_ABILITY_NAME);
        championsManager.getEffects().addEffect(target, EffectTypes.SLOWNESS, webSlowness, duration);
    }

    private boolean canCling(Player player) {
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
            channel(player);
        } else {
            cancel(player);
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
        webRadius = getConfig("webRadius", 3, Integer.class);
        webSlowness = getConfig("webbedSlowness", 2, Integer.class);
        webDuration = getConfig("webbedDuration", 5.0, Double.class);
        webPounceStrength = getConfig("webPounceStrength", 2.0, Double.class);
        fallDamageReduction = getConfig("fallDamageReduction", 15.0, Double.class);
    }
}