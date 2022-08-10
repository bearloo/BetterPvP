package me.mykindos.betterpvp.core.utilities;

import me.mykindos.betterpvp.core.utilities.events.FetchNearbyEntityEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class UtilEntity {

    public static List<LivingEntity> getNearbyEntities(Player player, double radius) {
        return getNearbyEntities(player, player.getLocation(), radius);
    }

    public static List<LivingEntity> getNearbyEntities(LivingEntity source, Location location, double radius) {
        List<LivingEntity> livingEntities = source.getWorld().getLivingEntities().stream()
                .filter(livingEntity -> livingEntity.getLocation().distance(location) <= radius && !livingEntity.equals(source))
                .collect(Collectors.toList());
        FetchNearbyEntityEvent<LivingEntity> fetchNearbyEntityEvent = new FetchNearbyEntityEvent<>(source, location, livingEntities);
        UtilServer.callEvent(fetchNearbyEntityEvent);

        return fetchNearbyEntityEvent.getEntities();
    }

    public static void setHealth(LivingEntity entity, double health){
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if(maxHealthAttribute != null) {
            entity.setHealth(Math.min(maxHealthAttribute.getValue(), health));
        }
    }

}