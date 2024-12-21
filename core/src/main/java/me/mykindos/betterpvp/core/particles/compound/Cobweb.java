package me.mykindos.betterpvp.core.particles.effects;

import me.mykindos.betterpvp.core.particles.CompoundParticleEffect;
import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

public class Cobweb extends CompoundParticleEffect {

    static final int PARTICLES_PER_BLOCK = 10;

    private int radius;
    private int viewDistance;

    public Cobweb(Location location) {
        super(location);
    }

    public Cobweb setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public Cobweb setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        return this;
    }

    @Override
    protected void populate() {
        final int particles = radius * PARTICLES_PER_BLOCK;

//        final Location lineStart = origin.add(0.0, player.getHeight() / 2, 0.0);
//        final Location lineEnd = player.getLocation().clone().add(0.0, player.getHeight() / 2, 0.0);
//        final VectorLine line = VectorLine.withStepSize(lineStart, lineEnd, 0.25f);
//        for (Location point : line.toLocations()) {
//            Particle.FIREWORKS_SPARK.builder().location(point).count(2).receivers(100).extra(0).spawn();
//        }

        for (int x = -particles; x <= particles; x++) {
            double blockOffsetX = (double)x / PARTICLES_PER_BLOCK;

            for (int z = -particles; z <= particles; z++) {
                double blockOffsetZ = (double)z / PARTICLES_PER_BLOCK;

                Location point = origin.clone();
                point.setX(point.getX() + blockOffsetX);
                point.setZ(point.getZ() + blockOffsetZ);
                point.setY(point.getY() + 0.1);

                if (isAxis(x, z)) {
                    points.add(point);
                }
            }
        }
    }

    protected boolean isAxis(int x, int z) {
        int dx = Math.abs(x);
        int dz = Math.abs(z);
        double radiusSquared = Math.pow((radius * PARTICLES_PER_BLOCK), 2);

        boolean pointInCircle = (Math.pow(dx, 2) + Math.pow(dz, 2) <= radiusSquared);

        boolean perpendicularAxis = (x == 0 || z == 0);
        boolean diagonalAxis = (dx == dz && pointInCircle);

        return perpendicularAxis || diagonalAxis;
    }

    @Override
    protected ParticleBuilder getBuilder() {
        var receivers = origin.getNearbyPlayers(viewDistance);

        return new ParticleBuilder(Particle.FALLING_DUST)
                .data(Material.COBWEB.createBlockData())
                .count(1)
                .receivers(receivers);
    }
}