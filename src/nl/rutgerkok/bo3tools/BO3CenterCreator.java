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

public class BO3CenterCreator implements Listener {
    protected BO3Tools plugin;

    public BO3CenterCreator(BO3Tools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            ItemStack inHand = event.getItem();
            if (inHand != null && inHand.getType().equals(Material.WOOD_HOE)) {
                if (player.hasPermission("bo3tools.exportbo3")) {
                    Block clicked = event.getClickedBlock();
                    NextBO3Data data = plugin.getNextBO3Data(player);
                    data.setCenter(BlockLocation.toBlockLocation(clicked));
                    player.sendMessage(BaseCommand.MESSAGE_COLOR + "Selected this block as the center of the next BO3 object created using /exportbo3.");
                    if (data.removeAllBlockChecks()) {
                        player.sendMessage(BaseCommand.MESSAGE_COLOR + "All previously selected block checks have been removed.");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
}
