package nl.rutgerkok.bo3tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.rutgerkok.bo3tools.util.BlockLocation;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.BlockCheck;
import com.khorn.terraincontrol.customobjects.bo3.BlockFunction;
import com.khorn.terraincontrol.util.BlockHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Creates a BO3 with the given parameters. Doesn't check the BO3 size. so be
 * sure that those aren't too big.
 * 
 */
public class BO3Creator {
    private String name;
    private BlockLocation center;
    private Selection selection;
    private boolean includeAir;
    private String author = "Unknown";
    private boolean includeTileEntities;
    private LocalWorld worldTC;
    private Set<BlockLocation> blockChecks;
    private boolean hasLeaves;
    private boolean noLeavesFix;

    private BO3Creator(String name) {
        this.name = name;
    }

    /**
     * Creates a new BO3 creator.
     * 
     * @param name
     *            Name of the BO3.
     * @return The BO3Creator, for easy linking.
     */
    public static BO3Creator name(String name) {
        Validate.notNull(name, "Name cannot be null");
        return new BO3Creator(name);
    }

    /**
     * Sets the author.
     * 
     * @param player
     *            The author.
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator author(Player player) {
        author = player.getName();
        return this;
    }

    /**
     * Sets the center.
     * 
     * @param location
     *            The location of the center.
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator center(BlockLocation location) {
        Validate.notNull(location, "Center cannot be null");
        center = location;
        return this;
    }

    /**
     * Sets the block checks to include.
     * 
     * @param locations
     *            The locations. Can be immutable.
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator blockChecks(Set<BlockLocation> locations) {
        Validate.notNull(locations, "BlockChecks cannot be null");
        blockChecks = locations;
        return this;
    }

    /**
     * Sets the bounds.
     * 
     * @param selection
     *            The bounds.
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator selection(Selection selection) {
        Validate.notNull(selection, "Selection cannot be null");
        this.selection = selection;
        return this;
    }

    /**
     * Activates tile entities.
     * 
     * @param world
     *            The LocalWorld object, which is needed for tile entities.
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator includeTileEntities(LocalWorld world) {
        Validate.notNull(world, "World cannot be null");
        this.worldTC = world;
        this.includeTileEntities = true;
        return this;
    }

    /**
     * Includes all air blocks in the BO3.
     * 
     * @return The BO3Creator, for easy linking.
     */
    public BO3Creator includeAir() {
        this.includeAir = true;
        return this;
    }

    public BO3Creator noLeavesFix() {
        this.noLeavesFix = true;
        return this;
    }

    /**
     * Creates the BO3 with the given settings. BO3 is automatically saved.
     * 
     * @return The newly created BO3.
     */
    public BO3 create() {
        Validate.notNull(selection, "No selection given");
        Validate.notNull(center, "No center given");

        // Create the BO3 file
        File bo3File = new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), name + ".bo3");

        BO3 bo3 = new BO3(this.name, bo3File);
        bo3.onEnable(Collections.<String, CustomObject> emptyMap());

        // Add the blocks to the BO3
        List<BlockFunction> blocks = createBlocks();
        bo3.getSettings().blocks[0] = blocks.toArray(new BlockFunction[blocks.size()]);

        // Add the block checks to the BO3
        List<BlockCheck> blockChecks = createBlockChecks();
        bo3.getSettings().bo3Checks[0] = blockChecks.toArray(new BlockCheck[blockChecks.size()]);

        // Fill the other three direction arrays
        bo3.getSettings().rotateBlocksAndChecks();

        // Set to tree
        bo3.getSettings().tree = hasLeaves;

        // Add player name to the BO3
        bo3.getSettings().author = author;

        // Don't save it every TC startup
        bo3.getSettings().settingsMode = ConfigMode.WriteDisable;

        // Save the BO3
        bo3.getSettings().writeSettingsFile(true);

        return bo3;
    }

	private List<BlockFunction> createBlocks() {
        File tileEntitiesFolder = new File(TerrainControl.getEngine().getGlobalObjectsDirectory(), name);
        if (includeTileEntities) {
            tileEntitiesFolder.mkdirs();
        }

        int tileEntityCount = 1;
        World world = selection.getWorld();

        Location start = selection.getMinimumPoint();
        Location end = selection.getMaximumPoint();

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
                        blockFunction.blockData = filterData(id, data);
                        blockFunction.x = x - center.getX();
                        blockFunction.y = y - center.getY();
                        blockFunction.z = z - center.getZ();

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
                                    blockFunction.metaDataName = name + "/" + tileEntityName;
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
        return blocks;
    }

    private byte filterData(int blockId, byte blockData) {
        if (blockId == DefaultMaterial.LEAVES.id) {
            // Leaves detected
            hasLeaves = true;
            if (!noLeavesFix) {
                // Clear no-leave-decay flag
                return (byte) (blockData % 4);
            }
        }
        return blockData;
    }

    private List<BlockCheck> createBlockChecks() {
        if (blockChecks != null) {
            World world = selection.getWorld();
            List<BlockCheck> tcBlockChecks = new ArrayList<BlockCheck>(blockChecks.size());
            for (BlockLocation location : blockChecks) {
                Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
                BlockCheck blockCheck = new BlockCheck();
                blockCheck.x = location.getX() - center.getX();
                blockCheck.y = location.getY() - center.getY();
                blockCheck.z = location.getZ() - center.getZ();
                int blockId = block.getTypeId();
                byte blockData = block.getData();
                blockCheck.blockIds = Lists.newArrayList(blockId);
                if (BlockHelper.rotateData(blockId, blockData) == blockData && blockData != 0) {
                    // Data isn't used for rotation, so it's used for subdata
                    // So if the data is not 0, add it
                    blockCheck.blockDatas = Lists.newArrayList(blockData);
                } else {
                    // Data is used for rotation, so don't add it
                    blockCheck.blockDatas = Lists.newArrayList((byte) -1);
                }

                blockCheck.setValid(true);
                tcBlockChecks.add(blockCheck);
            }
            return tcBlockChecks;
        } else {
            return Collections.emptyList();
        }
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
