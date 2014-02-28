package nl.rutgerkok.bo3tools.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.bo3tools.BO2Converter;
import nl.rutgerkok.bo3tools.BO3Tools;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo2.BO2;
import com.khorn.terraincontrol.customobjects.bo3.BO3;

/**
 * 
 * Like the BO2 convert command, but this converts all BO2s in either the global
 * or the world folder.
 * 
 * @author Casper van Battum (Creator13)
 * 
 */
public class BO2ConvertFolderCommand implements TabExecutor {
    private static final String GLOBAL = "global";
    private final BO3Tools plugin;

    public BO2ConvertFolderCommand(BO3Tools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalWorld world = null;
        boolean globalObjects = false;

        if (args.length > 1) {
            // Someone hasn't read the instructions
            return false;
        }

        // Fetch world
        if (sender instanceof Player) {
            String worldName = ((Player) sender).getWorld().getName();
            world = TerrainControl.getWorld(worldName);
        }

        // Parse arguments
        if (args.length == 1) {
            String arg = args[0];
            if (arg.equalsIgnoreCase(GLOBAL)) {
                globalObjects = true;
            } else {
                world = TerrainControl.getWorld(arg);
                if (world == null) {
                    sender.sendMessage(BaseCommand.ERROR_COLOR + "World '" + arg + "' not found.");
                }
            }
        }

        // Check if world is set
        if (world == null && globalObjects == false) {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Please provide the name of a world with Terrain Control enabled.");
            if (sender instanceof Player) {
                sender.sendMessage(BaseCommand.ERROR_COLOR + "The world you are currently in is not loaded by Terrain Control.");
            }
            return true;
        }

        // Get and convert objects
        Collection<CustomObject> objects;
        if (globalObjects) {
            objects = TerrainControl.getCustomObjectManager().globalObjects.values();
        } else {
            objects = world.getSettings().worldConfig.customObjects;
        }
        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converting BO2s, hang on...");
        int count = convertBO2s(BO3Tools.getAuthorName(sender), objects);

        // Messages
        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done! Converted " + count + " BO2s.");
        if (!(sender instanceof ConsoleCommandSender)) {
            plugin.log(sender.getName() + " converted " + count + " BO2s to BO3s.");
        }

        return true;

    }

    /**
     * Converts all BO2s in the CustomObject collection. Ignores all other
     * objects in the list.
     * 
     * @param author
     *            The author that should be used for the objects.
     * @param objects
     *            The objects to convert.
     * @return The number of objects that were converted.
     */
    protected int convertBO2s(String author, Collection<CustomObject> objects) {
        int count = 0;

        for (CustomObject object : objects) {
            if (object instanceof BO2) {
                // Convert BO2
                BO2 bo2 = (BO2) object;
                BO3 bo3 = BO2Converter.convertBO2(author, bo2);

                // Save BO3
                bo3.getSettings().writeSettingsFile(true);

                // Move old BO2
                bo2.file.renameTo(new File(bo2.file.getAbsolutePath() + ".old"));

                // Increment count
                count++;
            }
        }

        return count;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Don't suggest anything if too much arguments are provided
        if (args.length > 1) {
            return Collections.emptyList();
        }

        String currentlyTyping = args[0];
        List<String> options = new ArrayList<String>();

        // Suggest worlds
        for (World world : Bukkit.getWorlds()) {
            if (StringUtil.startsWithIgnoreCase(world.getName(), currentlyTyping)) {
                options.add(world.getName());
            }
        }

        // Suggest "global"
        if (StringUtil.startsWithIgnoreCase(GLOBAL, currentlyTyping)) {
            options.add(GLOBAL);
        }

        return options;
    }

}
