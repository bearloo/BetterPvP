package me.mykindos.betterpvp.core.combat.weapon.types;

import me.mykindos.betterpvp.core.combat.weapon.types.InteractWeapon;
import me.mykindos.betterpvp.core.combat.weapon.types.TrackableWeapon;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import org.bukkit.entity.Player;

@BPvPListener
public interface ChargeableWeapon extends TrackableWeapon, InteractWeapon {

    protected abstract void useCharge(Player player);
    protected abstract String getChargeableName();

    public abstract void trackCharges(Player player, Gamer gamer);
    public abstract void abandonCharges(Player player, Gamer gamer);

    default void notifyCharges(Player player, int charges) {
        UtilMessage.simpleMessage(player, getSimpleName(), String.format("%s Charges: <yellow>%d", getChargeableName(), charges));
    }

    default int getInitialCharges() {
        return 0;
    }
}