package nl.rutgerkok.bo3tools;

import nl.rutgerkok.bo3tools.util.BlockLocation;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.khorn.terraincontrol.bukkit.commands.BaseCommand;

public class BO3ClickHandler implements Listener {
    protected BO3Tools plugin;

    public BO3ClickHandler(BO3Tools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("bo3tools.exportbo3")) {
            return;
        }

        ItemStack inHand = event.getItem();
        if (inHand == null) {
            return;
        }

        if (inHand.getType().equals(Material.WOOD_HOE)) {
            // Set center
            handleSetCenter(player, event.getClickedBlock());
            event.setCancelled(true);
        } else if (inHand.getType().equals(Material.GLOWSTONE_DUST)) {
            // Add/remove block check
            handleBlockCheck(player, event.getClickedBlock(), event.getAction());
            event.setCancelled(true);
        }
    }

    private void handleSetCenter(Player player, Block newCenter) {
        NextBO3Data data = plugin.getNextBO3Data(player);
        data.setCenter(BlockLocation.toBlockLocation(newCenter));
        player.sendMessage(BaseCommand.MESSAGE_COLOR + "Selected this block as the center of the next BO3 object created using /exportbo3.");
        if (data.removeAllBlockChecks()) {
            player.sendMessage(BaseCommand.MESSAGE_COLOR + "All previously selected block checks have been removed.");
        }
    }

    private void handleBlockCheck(Player player, Block clicked, Action action) {
        NextBO3Data data = plugin.getNextBO3Data(player);
        if (action == Action.RIGHT_CLICK_BLOCK) {
            // Place block check
            if (data.addBlockCheck(BlockLocation.toBlockLocation(clicked))) {
                player.sendMessage(BaseCommand.MESSAGE_COLOR + "Block is selected as BlockCheck for the next BO3.");
            } else {
                player.sendMessage(BaseCommand.ERROR_COLOR + "Clicked block is already a BlockCheck.");
            }
        } else {
            // Remove block check
            if (data.removeBlockCheck(BlockLocation.toBlockLocation(clicked))) {
                player.sendMessage(BaseCommand.MESSAGE_COLOR + "BlockCheck is removed!");
            }
        }
    }
}
