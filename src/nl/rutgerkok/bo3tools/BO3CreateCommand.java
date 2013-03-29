package nl.rutgerkok.bo3tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.BlockFunction;
import com.khorn.terraincontrol.util.MathHelper;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class BO3CreateCommand implements TabExecutor {
	public static final List<String> AUTO_COMPLETE_OPTIONS = Arrays.asList(new String[] { "--includeair", "--includetileentities" });

	public static final String INCLUDE_AIR = AUTO_COMPLETE_OPTIONS.get(0);
	public static final String INCLUDE_TILE_ENTITIES = AUTO_COMPLETE_OPTIONS.get(1);

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
		int tileEntityCount = 1;

		// Get the selection
		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		Selection selection = worldEdit.getSelection(player);
		if (selection == null) {
			player.sendMessage(ChatColor.RED + "No WorldEdit selection found.");
			return true;
		}
		Location start = selection.getMinimumPoint();
		Location end = selection.getMaximumPoint();
		List<String> argsList = Arrays.asList(args);

		// Some parameters
		boolean includeAir = argsList.contains(INCLUDE_AIR);
		boolean includeTileEntities = argsList.contains(INCLUDE_TILE_ENTITIES);
		if (includeTileEntities && worldTC == null) {
			player.sendMessage(ChatColor.RED + "Terrain Control needs to be enabled for this world to save TileEntities.");
			return true;
		}

		// Check for size
		if (MathHelper.abs(start.getBlockX() - end.getBlockX()) > 32 || MathHelper.abs(start.getBlockZ() - end.getBlockZ()) > 32) {
			player.sendMessage(BaseCommand.ERROR_COLOR
					+ "BO3s cannot be that large. Try to split object into smaller parts of 16x16x16 max.");
			return true;
		}

		// Create the BO3
		File bo3File = new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), bo3Name + ".bo3");
		File tileEntitiesFolder = new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), bo3Name);
		if(includeTileEntities) {
			tileEntitiesFolder.mkdirs();
		}

		BO3 bo3 = new BO3(bo3Name, bo3File);
		bo3.onEnable(Collections.<String, CustomObject> emptyMap());

		// Get the BO3 center
		int bo3CenterX = (start.getBlockX() + end.getBlockX()) / 2;
		int bo3CenterY = start.getBlockY();
		int bo3CenterZ = (start.getBlockZ() + end.getBlockZ()) / 2;
		if (plugin.getMetadata(player, BO3Tools.BO3_CENTER_X) != null) {
			int selectedBo3CenterX = plugin.getMetadata(player, BO3Tools.BO3_CENTER_X);
			int selectedBo3CenterY = plugin.getMetadata(player, BO3Tools.BO3_CENTER_Y);
			int selectedBo3CenterZ = plugin.getMetadata(player, BO3Tools.BO3_CENTER_Z);
			if (MathHelper.abs(bo3CenterX - selectedBo3CenterX) > 32 || MathHelper.abs(bo3CenterZ - selectedBo3CenterZ) > 32) {
				player.sendMessage(BaseCommand.ERROR_COLOR + "Selected BO3 center is too far away.");
				return true;
			}
			bo3CenterX = selectedBo3CenterX;
			bo3CenterY = selectedBo3CenterY;
			bo3CenterZ = selectedBo3CenterZ;
			player.removeMetadata(BO3Tools.BO3_CENTER_X, plugin);
		}

		// Make a list of all the blocks
		List<BlockFunction> blocks = new ArrayList<BlockFunction>(selection.getWidth() * selection.getHeight() * selection.getLength());
		for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
			for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
				for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
					Block block = world.getBlockAt(x, y, z);
					int id = block.getTypeId();
					byte data = block.getData();
					if (includeAir || id != 0) {
						BlockFunction blockFunction = new BlockFunction();
						blockFunction.blockId = id;
						blockFunction.blockData = data;
						blockFunction.x = x - bo3CenterX;
						blockFunction.y = y - bo3CenterY;
						blockFunction.z = z - bo3CenterZ;

						if (includeTileEntities) {
							// Look for tile entities
							Tag tag = worldTC.getMetadata(x, y, z);
							if (tag != null) {
								String tileEntityName = tileEntityCount + "-" + getTileEntityName(tag) + ".nbt";
								File tileEntityFile = new File(tileEntitiesFolder, tileEntityName);
								
								tileEntityCount++;
								try {
									tileEntityFile.createNewFile();
									FileOutputStream fos = new FileOutputStream(tileEntityFile);
									tag.writeTo(fos);
									fos.flush();
									fos.close();
									blockFunction.hasMetaData = true;
									blockFunction.metaDataTag = tag;
									blockFunction.metaDataName = bo3Name + "/" + tileEntityName;
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}

						}

						blockFunction.setValid(true);
						blocks.add(blockFunction);
					}
				}
			}
		}

		// Add the blocks to the BO3
		bo3.getSettings().blocks[0] = blocks.toArray(new BlockFunction[0]);

		// Add player name to the BO3
		bo3.getSettings().author = player.getName();

		// Don't save it every TC startup
		bo3.getSettings().settingsMode = ConfigMode.WriteDisable;

		// Save the BO3
		bo3.getSettings().writeSettingsFile(bo3File, true);

		// Send message
		player.sendMessage(BaseCommand.MESSAGE_COLOR + "Created a new BO3 file with " + blocks.size() + " blocks");
		plugin.log(sender.getName() + " created the BO3 " + bo3.getName() + " consisting of " + blocks.size() + " blocks");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length > 1) {
			return AUTO_COMPLETE_OPTIONS;
		}
		return Collections.emptyList();
	}

	private String getTileEntityName(Tag tag) {
		Tag[] values = (Tag[]) tag.getValue();
		for (Tag childTag : values) {
			if (childTag.getName().equals("id")) {
				return (String) childTag.getValue();
			}
		}
		return "Unknown";
	}
}
