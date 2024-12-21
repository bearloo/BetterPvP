package me.mykindos.betterpvp.core.combat.weapon.types;

import me.mykindos.betterpvp.core.components.champions.weapons.IWeapon;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;

@BPvPListener
public interface TrackableWeapon extends IWeapon, Listener {

    public abstract void track(Player player);
    public abstract void abandon(Player player);

    @EventHandler(priority = EventPriority.LOWEST)
    default void onPickupWeapon(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;

        if (event.getEntity() instanceof Player player) {
           if (matches(event.getItem().getItemStack())) {
               track(player);
           }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onDropWeapon(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        if (UtilPlayer.isDead(event.getPlayer())) return;

        if (matches(event.getItemDrop().getItemStack())) {
            abandon(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onStoreWeapon(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getAction().name().contains("PLACE") && !event.getSlotType().equals(InventoryType.SlotType.FUEL) && event.getClickedInventory() != null) {
                placeItemLogic(player, Objects.requireNonNull(event.getClickedInventory()), event.getCursor());
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onRetrieveWeapon(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getWhoClicked() instanceof Player player) {
            final Inventory clickedInventory = event.getClickedInventory();
            final Inventory inventory = event.getInventory();

            switch(event.getAction()) {
                case InventoryAction.PLACE_ALL:
                case InventoryAction.PLACE_ONE:
                case InventoryAction.PLACE_SOME:

                    break;

                case InventoryAction.MOVE_TO_OTHER_INVENTORY:

                    break;

                case InventoryAction.HOTBAR_SWAP:

                    break;

                case InventoryAction.DROP_ALL_CURSOR:
                case InventoryAction.DROP_ONE_CURSOR:
                case InventoryAction.DROP_ALL_SLOT:
                case InventoryAction.DROP_ONE_SLOT:

                    break;
                default:
                    break;
            }



            if (event.isCancelled()) return;
            if (event.getWhoClicked() instanceof Player player) {
                if (event.getAction().equals()) {

                    if (INVENTORY_FURNACE_TYPES.contains(event.getInventory().getType()) && !event.getInventory().equals(event.getClickedInventory())) {
                        //this is a furnace, UUIDItems cannot be shift clicked in, but can be shift clicked out
                        return;
                    }
                    if (inventory.getType().equals(InventoryType.PLAYER)) {
                        processStoreItem(player, event.getInventory(), event.getCurrentItem());
                    } else {
                        processRetrieveItem(player, inventory, event.getCurrentItem());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onDeath(PlayerDeathEvent event) {
        abandon(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onQuit(PlayerQuitEvent event) {
        abandon(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    default void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (hasWeapon(player)) {
            track(player);
        }
    }
}