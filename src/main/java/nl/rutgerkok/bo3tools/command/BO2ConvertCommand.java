package nl.rutgerkok.bo3tools.command;

import java.io.File;

import nl.rutgerkok.bo3tools.BO2Converter;
import nl.rutgerkok.bo3tools.BO3Tools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo2.BO2;
import com.khorn.terraincontrol.customobjects.bo3.BO3;

public class BO2ConvertCommand implements CommandExecutor {

    private final BO3Tools plugin;

    public BO2ConvertCommand(BO3Tools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalWorld world = (sender instanceof Player) ? TerrainControl.getWorld(((Player) sender).getWorld().getName()) : null;

        if (args.length != 1) {
            // Someone hasn't read the help files ;)
            return false;
        }

        // Get and check the object
        CustomObject object = getObject(world, args[0]);

        // Validate returned object
        if (object == null) {
            // check if a bo3 version of this file exists
            sender.sendMessage(BaseCommand.ERROR_COLOR + "No BO2 with the name " + args[0] + " exists.");
            return true;
        }
        if (!(object instanceof BO2)) {
            // check if the object is a bo3 instead
            if (object instanceof BO3) {
                sender.sendMessage(BaseCommand.ERROR_COLOR + "Object " + args[0] + " is already a BO3.");
            } else {
                sender.sendMessage(BaseCommand.ERROR_COLOR + "The object " + args[0] + " is not a BO2.");
            }
            return true;
        }

        // Get "author" of BO2
        String authorName = BO3Tools.getAuthorName(sender);

        // Create the BO3
        BO2 bo2 = (BO2) object;
        BO3 bo3 = BO2Converter.convertBO2(authorName, bo2);

        // Save the BO3
        FileSettingsWriter.writeToFile(bo3.getSettings(), ConfigMode.WriteAll);

        // Move old BO2
        bo2.getFile().renameTo(new File(bo2.getFile().getAbsolutePath() + ".old"));

        // Send message
        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted " + bo2.getName() + ".bo2 to the BO3 " + bo3.getName());
        if (!(sender instanceof ConsoleCommandSender)) {
            plugin.log(sender.getName() + " converted the BO2 " + bo2.getName() + " to the BO3 " + bo3.getName());
        }

        return true;
    }

    /**
     * Looks for the object with the given name in the given world's
     * WorldObjects folder or in Global objects if no world is specified.
     *
     * @param world
     *            The world the object is in, or null to search only in the
     *            global objects directory.
     * @param name
     *            The name of the object.
     * @return The object, or null if not found.
     */
    protected CustomObject getObject(LocalWorld world, String name) {
        CustomObject object = null;
        if (world != null) {
            object = world.getConfigs().getCustomObjects().getObjectByName(name);
        } else {
            // Player isn't in a TC world, or command is sent from the console.
            // Search the global objects.
            object = TerrainControl.getCustomObjectManager().getGlobalObjects().getObjectByName(name);
        }

        return object;

    }

}
