package me.mykindos.betterpvp.core.combat.weapon.data;

import lombok.Data;

@Data
public class WeaponChargeData {

    private int charges = 0;

    public void useCharge() {
        charges = Math.max(0, charges - 1);
    }

    public void addCharge(){
        charges++;
    }
}
