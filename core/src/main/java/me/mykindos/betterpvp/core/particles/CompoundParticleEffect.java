package me.mykindos.betterpvp.core.particles;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CompoundParticleEffect {
    protected final List<Location> points = new ArrayList<>();
    protected final Location origin;

    protected CompoundParticleEffect(Location origin) {
        this.origin = origin;
    }

    public void draw() {
        populate();

        ParticleBuilder builder = getBuilder();

        for (Location point : points) {
            builder.location(point)
                    .spawn();
        }
    }

    protected abstract void populate();
    protected abstract ParticleBuilder getBuilder();
}