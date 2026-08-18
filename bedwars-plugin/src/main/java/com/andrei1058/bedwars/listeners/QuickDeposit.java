package com.example.bedwarsdeposit;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ChestDepositListener implements Listener {

    @EventHandler
    public void onChestPunch(PlayerInteractEvent event) {
        // Check if the player left-clicked (punched) a block
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Do nothing if the player's hand is empty
        if (itemInHand.getType() == Material.AIR) {
            return;
        }

        Inventory targetInventory = null;

        // Check if the punched block is a regular Chest or an Ender Chest
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                targetInventory = chest.getInventory();
            }
        } else if (block.getType() == Material.ENDER_CHEST) {
            targetInventory = player.getEnderChest();
        }

        // If a valid inventory was found, attempt to deposit the item
        if (targetInventory != null) {
            // Cancel the event so the player doesn't accidentally break the block in creative mode
            event.setCancelled(true);

            // Try to add the item to the inventory
            HashMap<Integer, ItemStack> leftover = targetInventory.addItem(itemInHand.clone());

            if (leftover.isEmpty()) {
                // The entire stack was successfully deposited
                player.getInventory().setItemInMainHand(null);
                player.updateInventory();
            } else {
                // Only part of the stack (or none) fit into the inventory
                ItemStack remaining = leftover.get(0);
                player.getInventory().setItemInMainHand(remaining);
                player.updateInventory();
            }
        }
    }
}
