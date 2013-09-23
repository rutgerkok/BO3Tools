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

        //TODO: may only look for bo2, not bo3's
        // Get and check the object
        CustomObject object = getObject(world, args[0]);
        
        if (object == null) {
        	//check if a bo3 version of this file exists
        	if (new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), args[0] + ".bo3").exists()) {
        		sender.sendMessage(BaseCommand.ERROR_COLOR + "Object " + args[0] + " is already a BO3.");
        		
        	}
        	else {
        		sender.sendMessage(BaseCommand.ERROR_COLOR + "The BO2 " + args[0] + " was not found.");
        	
        	}
            return true;
        }
        if (!(object instanceof BO2)) {
        	//check if the object is a bo3 instead
        	if (object instanceof BO3) {
        		sender.sendMessage(BaseCommand.ERROR_COLOR + "Object " + args[0] + " is already a BO3.");
        		
        	}
        	else {
        		sender.sendMessage(BaseCommand.ERROR_COLOR + "The object " + args[0] + " is not a BO2.");
        		
        	}
            return true;
        }

        // Create the BO3
        BO2 bo2 = (BO2) object;
        File parentDirectory = TerrainControl.getEngine().getGlobalObjectsDirectory();
        BO3 bo3 = convertBO2(sender, bo2, parentDirectory);

        // Save the BO3
        bo3.getSettings().writeSettingsFile(true);

        // Send message
        //Had to remove the block count here as the block list was moved to the convertBO2() method
        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted " + bo2.getName()  + ".bo2 to the BO3 " + bo3.getName() + "!");

        return true;
    }
    
    /**
     * Converts the given BO2 into a new BO3 object, with the author set to the
     * senders name, or <code>"Unknown"</code> if the sender is not a player.
     * 
     * @param sender
     * 			Sender of the command
     * @param bo2
     * 			The BO2 object to convert
     * @param parentdirectory
     * 			The directory of the bo2 file
     * @return 	
     * 			A new BO3 object
     */
    protected BO3 convertBO2(CommandSender sender, BO2 bo2, File parentdirectory) {
    	if (! parentdirectory.isDirectory()) {
    		throw new IllegalArgumentException("given parentDirectory is not a directory");
    		
    	}  
    	
        File bo3File = new File(parentdirectory, bo2.getName() + ".bo3");
    	BO3 bo3 = new BO3(bo2.getName() + ".bo3", bo3File);
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
        
		if (sender instanceof Player) {
			plugin.log(sender.getName() + " converted the BO2 " + bo2.getName() + " to the BO3 " + bo3.getName());
			
		}
        
        return bo3;
    	
    }
    
    /**
     * Looks for the object with the given name in the given world's 
     * WorldObjects folder or in Global objects if no world is specified.
     * 
     * @param world
     * @param name
     * @return
     */
    protected CustomObject getObject(LocalWorld world, String name) {
    	CustomObject object = null;
    	if (world != null) {
            object = TerrainControl.getCustomObjectManager().getCustomObject(name, world);
        } else {
            // Player isn't in a TC world, or command is sent from the console.
            // Search the global objects.
            object = TerrainControl.getCustomObjectManager().getCustomObject(name);
            
        }
    	
    	return object;
    	
    }
    
}
