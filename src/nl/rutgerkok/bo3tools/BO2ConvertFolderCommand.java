package nl.rutgerkok.bo3tools;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo2.BO2;
import com.khorn.terraincontrol.customobjects.bo3.BO3;

/**
 * 
 * An implementation of the BO2ConvertCommand. Instead of converting only one
 * BO2 at a time, it converts all the BO2's in the given folder.
 * 
 * @author Casper van Battum (Creator13)
 * @see BO2ConvertCommand
 *
 */
public class BO2ConvertFolderCommand extends BO2ConvertCommand 
									 implements CommandExecutor {
	
	public static final String NOT_BO2 = "Object was not a bo2";
	public static final String NOT_FOUND = "BO2 was not found";
	public static final String ERR_UNKNOWN = "Reason unknown";
	private static final String WORLD_OBJECTS = "WorldObjects";
	private static final String GLOBAL_OBJECTS = "GlobalObjects";

	private BO3Tools plugin;
	private ArrayList<String> convertingErrors;
	
	public BO2ConvertFolderCommand(BO3Tools plugin) {
		super(plugin);
		this.plugin = plugin;
		this.convertingErrors = new ArrayList<String>();
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		LocalWorld world = null;
		String customObjectFolder = "Unkown";
		
		//checking the args and defining the (eventual) world
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase(WORLD_OBJECTS)) {
				if (sender instanceof Player) {
					//Using player's world
					world = TerrainControl.getWorld(((Player) sender).getWorld().getName());
					customObjectFolder = WORLD_OBJECTS;
					
				}
				else {
					//World can't be found since the sender is not a player in a world
					sender.sendMessage(BaseCommand.ERROR_COLOR + "Define a world!");
					return true;
					
				}
				
			}
			else if (args[0].equalsIgnoreCase(GLOBAL_OBJECTS)) {
				customObjectFolder = GLOBAL_OBJECTS;
				
			}
			
		}
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase(WORLD_OBJECTS)) {
				world = TerrainControl.getWorld(args[1]);
				if (world == null) {
					//World was not found, or not a TerrainControl world
					sender.sendMessage(BaseCommand.ERROR_COLOR + "Couldn't find world \"" + 
							BaseCommand.VALUE_COLOR + args[1] + BaseCommand.ERROR_COLOR + "\"!");
					return true;
					
				}
				customObjectFolder = WORLD_OBJECTS;
				
			}
			else {
				//Not a valid arg
				return false;
				
			}
			
		}
		else {
			//less than one or more than two args
			return false;
			
		}
		
		//Get all the BO2's
		File customObjectDirectory = getObjectDirectory(customObjectFolder, world);
		CustomObject[] objects = getCustomObjects(customObjectDirectory, world);
		
		//Convert all BO2's
		BO3[] convertedObjects = convertAllObjects(objects, sender, customObjectDirectory);
		
		//Save all BO3's
		saveBO3s(convertedObjects);
		
		//Send message & errors
		if (! convertingErrors.isEmpty()) {
			for (String s : convertingErrors) {
				sender.sendMessage(s);
				
			}
			
		}
		if (convertedObjects.length == 0) {
			sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted no objects");
			
		}
		else if (convertedObjects.length == 1) {
			sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted one BO2 object successfully");
			
		}
		else {
			sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Converted " + convertedObjects.length + " BO2 objects successfully");
			
		}
		
		return true;
		
	}
	
	private void saveBO3s(BO3[] convertedObjects) {
		for (BO3 bo3 : convertedObjects) {
			bo3.getSettings().writeSettingsFile(true);
			
		}
		
	}

	private BO3[] convertAllObjects(CustomObject[] objects, CommandSender sender, File objectDirectory) {
		ArrayList<BO3> bo3List = new ArrayList<>();
		
		for (CustomObject obj : objects) {
			bo3List.add(convertBO2(sender, (BO2) obj, objectDirectory));
			
		}
		
		return bo3List.toArray(new BO3[bo3List.size()]);
		
	}

	private CustomObject[] getCustomObjects(File customObjectsDirectory, LocalWorld world) {
		File[] files = customObjectsDirectory.listFiles();
		ArrayList<String> filenames = new ArrayList<>();
		
		//Get all the bo2 filenames (without extension)
		for (File f : files) {
			if (f.isFile()) {
				//if ends on .bo2 --> means it's a bo2 file. Ignoring case.
				if (f.getName().endsWith(".bo2") 
						|| f.getName().endsWith(".Bo2") 
						|| f.getName().endsWith(".bO2") 
						|| f.getName().endsWith(".BO2")) {
					//add the filename without extension.
					filenames.add(removeExtension(f.getName()));
					
				}
				
			}
			
		}
		
		//Load all the files as CustomObjects
		ArrayList<CustomObject> objects = new ArrayList<>();
		for (String f : filenames) {
			//Will look in both WorldObjects and GlobalObjects, but it should 
			//find everything in the WorldObjects and never come to the GlobalObjects.
			CustomObject o = TerrainControl.getCustomObjectManager().getCustomObject(f, world);
			if (! (o instanceof BO2)) {
				//Object is not a BO2 object, so it won't be added to the list 
				//with items that will be converted. Instead, an error will be 
				//added to the error list.
				addConvertingError(f, NOT_BO2);
				
			}
			else {
				//Object is bo2, add to list.
				objects.add(o);
				
			}
			
		}
		
		return objects.toArray(new CustomObject[objects.size()]);
		
	}
	
	/**
	 * Adds an error to the error list in the following format: <br>
	 * <code>Error converting [object name]: [reason]</code>
	 * 
	 * @param f			object name
	 * @param errType	The reason of this error: 
	 * 				    <code>BO2ConvertFolderCommand.NOT_FOUND</code>,
	 * 				    <code>BO2ConvertFolderCommand.NOT_BO2</code> or
	 * 					<code>BO2ConvertFolderCommand.ERR_UNKNOWN</code>
	 */
	private void addConvertingError(String f, String errType) {
		convertingErrors.add(String.format("Error while converting %s: %s", f, errType));
		
	}

	/**
	 * Defines the abstract path of the directory with the CustomObjects.
	 * 
	 * @param customObjectsFolder
	 * @param world
	 * @return The abstract path of the custom objects directory
	 */
	private File getObjectDirectory(String customObjectsFolder, LocalWorld world) {
		if (customObjectsFolder.equalsIgnoreCase(GLOBAL_OBJECTS)) {
			return TerrainControl.getEngine().getGlobalObjectsDirectory();
			
		}
		else if (customObjectsFolder.equalsIgnoreCase(WORLD_OBJECTS)) {
			String fs = System.getProperty("file.separator");
			return new File("plugins" + fs + "TerrainControl" + fs + "worlds" + fs + world.getName() + fs + "WorldObjects");
			
		}
		else {
			return null;
			
		}
		
	}
	
	/**
	 * Simple method that removes the extension from a filename.
	 * 
	 * @param filename
	 * @return <code>filename</code> without the extension.
	 */
	private String removeExtension(String filename) {
		if (! filename.contains(".")) {
			throw new IllegalArgumentException("Not a filename");
			
		}
		
		StringBuilder sb = new StringBuilder();
		char[] chars = filename.toCharArray();
		
		for (char c : chars) {
			if (c == '.') {
				break;
				
			}
			else {
				sb.append(c);
				
			}
			
		}
		
		return sb.toString();
		
	}

}
