package me.mykindos.betterpvp.clans.champions.skills.skills.assassin.bow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.config.SkillConfigFactory;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.PrepareArrowSkill;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Singleton
@BPvPListener
public class ToxicArrow extends PrepareArrowSkill {


    @Inject
    public ToxicArrow(Clans clans, ChampionsManager championsManager, SkillConfigFactory configFactory) {
        super(clans, championsManager, configFactory);
    }

    @Override
    public String getName() {
        return "Toxic Arrow";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Your next arrow will poison your target, and give them",
                "nausea for " + ChatColor.GREEN + (6 + level) + ChatColor.GRAY + " seconds.",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)

        };
    }

    @Override
    public Role getClassType() {
        return Role.ASSASSIN;
    }

    @Override
    public SkillType getType() {
        return SkillType.BOW;
    }

    @Override
    public double getCooldown(int level) {

        return 14 - ((level - 1));
    }


    @Override
    public void activate(Player player, int level) {
        if (!active.contains(player.getUniqueId())) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.5F, 2.0F);
            active.add(player.getUniqueId());
        }
    }

    @Override
    public Action[] getActions() {
        return SkillActions.LEFT_CLICK;
    }

    @Override
    public void onHit(Player damager, LivingEntity target, int level) {
        if (target.hasPotionEffect(PotionEffectType.CONFUSION)) {
            target.removePotionEffect(PotionEffectType.CONFUSION);
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (6 + level) * 20, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (6 + level) * 20, 0));
    }

    @Override
    public void displayTrail(Location location) {
        Particle.REDSTONE.builder().location(location).color(0, 255, 0).count(3).extra(0).receivers(60, true).spawn();
    }
}
