package nl.rutgerkok.bo3tools;

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
                    plugin.setMetadata(player, BO3Tools.BO3_CENTER_X, clicked.getX());
                    plugin.setMetadata(player, BO3Tools.BO3_CENTER_Y, clicked.getY());
                    plugin.setMetadata(player, BO3Tools.BO3_CENTER_Z, clicked.getZ());
                    player.sendMessage(BaseCommand.MESSAGE_COLOR + "Selected this block as the center of the next BO3 object created using /exportbo3.");
                    event.setCancelled(true);
                }
            }
        }
    }
}
