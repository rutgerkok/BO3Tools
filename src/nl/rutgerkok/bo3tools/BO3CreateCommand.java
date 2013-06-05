package nl.rutgerkok.bo3tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.bo3tools.util.InvalidBO3Exception;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class BO3CreateCommand implements TabExecutor {
    public static final List<String> AUTO_COMPLETE_OPTIONS = Lists.newArrayList("--includeair", "--includetileentities" , "--noleavesfix");

    public static final String INCLUDE_AIR = AUTO_COMPLETE_OPTIONS.get(0);
    public static final String INCLUDE_TILE_ENTITIES = AUTO_COMPLETE_OPTIONS.get(1);
    public static final String NO_LEAVES_FIX = AUTO_COMPLETE_OPTIONS.get(2);

    private BO3Tools plugin;

    public BO3CreateCommand(BO3Tools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Player-only command
            sender.sendMessage("This is a player-only command!");
            return true;
        }

        if (args.length == 0) {
            // Someone hasn't read the help files ;)
            return false;
        }

        // Some variables
        Player player = (Player) sender;
        World world = player.getWorld();
        LocalWorld worldTC = TerrainControl.getWorld(world.getName());
        String bo3Name = "we-" + args[0];

        // Get the selection
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        Selection selection = worldEdit.getSelection(player);
        if (selection == null) {
            player.sendMessage(BaseCommand.ERROR_COLOR + "No WorldEdit selection found.");
            return true;
        }

        List<String> argsList = Arrays.asList(args);

        // Some command parameters
        boolean includeAir = argsList.contains(INCLUDE_AIR);
        boolean includeTileEntities = argsList.contains(INCLUDE_TILE_ENTITIES);
        boolean noLeavesFix = argsList.contains(NO_LEAVES_FIX);
        if (includeTileEntities && worldTC == null) {
            player.sendMessage(ChatColor.RED + "Terrain Control needs to be enabled for this world to save TileEntities.");
            return true;
        }

        // Get some parameters given earlier
        NextBO3Data nextBO3data = plugin.getNextBO3Data(player);

        // Check whether they are valid
        try {
            nextBO3data.checkBO3Valid(selection);
        } catch (InvalidBO3Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid BO3: " + e.getMessage());
            return true;
        }

        // Create the BO3 file
        BO3Creator creator = BO3Creator.name(bo3Name).author(player).center(nextBO3data.getCenter(selection)).selection(selection);
        creator.blockChecks(nextBO3data.getBlockChecks());
        if (includeAir) {
            creator.includeAir();
        }
        if (includeTileEntities) {
            creator.includeTileEntities(worldTC);
        }
        if(noLeavesFix) {
            creator.noLeavesFix();
        }
        BO3 bo3 = creator.create();

        // Send message
        int size = bo3.getSettings().blocks[0].length;
        player.sendMessage(BaseCommand.MESSAGE_COLOR + "Created a new BO3 file with " + size + " blocks");
        plugin.log(sender.getName() + " created the BO3 " + bo3.getName() + " consisting of " + size + " blocks");

        // Remove NextBO3Data
        plugin.removeNextBO3Data(player);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 1) {
            return AUTO_COMPLETE_OPTIONS;
        }
        return Collections.emptyList();
    }
}
