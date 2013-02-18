package nl.rutgerkok.bo3tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo2.BO2;
import com.khorn.terraincontrol.customobjects.bo2.ObjectCoordinate;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.BO3Config;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightSetting;
import com.khorn.terraincontrol.customobjects.bo3.BlockFunction;

public class BO2ConvertCommand implements CommandExecutor {
    private BO3Tools plugin;

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
        CustomObject object = null;
        if (world != null) {
            object = TerrainControl.getCustomObjectManager().getCustomObject(args[0], world);
        } else {
            // Player isn't in a TC world, or command is sent from the console.
            // Search
            // the global objects.
            object = TerrainControl.getCustomObjectManager().getCustomObject(args[0]);
        }

        if (object == null) {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "The BO2 " + args[0] + " was not found.");
            return true;
        }
        if (!(object instanceof BO2)) {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "The object " + args[0] + " is not a BO2.");
            return true;
        }

        // Create the BO3
        BO2 bo2 = (BO2) object;
        File bo3File = new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), bo2.getName() + "-converted.bo3");
        BO3 bo3 = new BO3(bo2.getName() + "-converted.bo3", bo3File);
        bo3.onEnable(Collections.<String, CustomObject> emptyMap());
        BO3Config bo3Config = bo3.getSettings();

        // Convert all blocks
        List<BlockFunction> newBlocks = new ArrayList<BlockFunction>();
        for (ObjectCoordinate oldBlock : bo2.data[0]) {
            BlockFunction newBlock = new BlockFunction();
            newBlock.blockId = oldBlock.blockId;
            newBlock.blockData = oldBlock.blockData;
            newBlock.x = oldBlock.x;
            newBlock.y = oldBlock.y;
            newBlock.z = oldBlock.z;
            newBlock.setValid(true);
            newBlocks.add(newBlock);
        }
        bo3Config.blocks[0] = newBlocks.toArray(new BlockFunction[0]);
        bo3Config.rotateBlocksAndChecks();

        // Convert some settings
        if (sender instanceof Player) {
            bo3Config.author = sender.getName();
        }
        bo3Config.description = "Converted version of the BO2 " + bo2.name;
        bo3Config.tree = bo2.tree;
        bo3Config.minHeight = bo2.spawnElevationMin;
        bo3Config.maxHeight = bo2.spawnElevationMax - 1;
        bo3Config.maxPercentageOutsideSourceBlock = (int) bo2.collisionPercentage;
        bo3Config.rotateRandomly = bo2.randomRotation;
        bo3Config.settingsMode = ConfigMode.WriteDisable;
        if (bo2.spawnAboveGround && !bo2.spawnUnderGround) {
            bo3Config.spawnHeight = SpawnHeightSetting.highestSolidBlock;
        } else if (bo2.spawnUnderGround) {
            bo3Config.spawnHeight = SpawnHeightSetting.randomY; // Not exactly
                                                                // the same, but
                                                                // there isn't
                                                                // anything
                                                                // better
        }

        // Add player name to the BO3
        if (sender instanceof Player) {
            bo3Config.author = sender.getName();
        }

        // Save the BO3
        bo3.getSettings().writeSettingsFile(bo3File, true);

        // Send message
        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted to a new BO3 file with " + newBlocks.size() + " blocks");
        if (sender instanceof Player) {
            plugin.log(sender.getName() + " converted the BO2 " + bo2.getName() + " to the BO3 " + bo3.getName());
        }

        return true;
    }
}
