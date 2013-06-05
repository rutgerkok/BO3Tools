package nl.rutgerkok.bo3tools;

import java.util.HashSet;
import java.util.Set;

import nl.rutgerkok.bo3tools.util.BlockLocation;
import nl.rutgerkok.bo3tools.util.InvalidBO3Exception;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class NextBO3Data {
    private Set<BlockLocation> blockChecks;
    private BlockLocation center;

    public NextBO3Data() {
        blockChecks = new HashSet<BlockLocation>();
    }

    /**
     * Adds a block check to the next created BO3. It will check for the block
     * at the specified location.
     * 
     * @param blockLocation
     *            The location of the block check.
     * @return True if the block check was added, false if the block was already
     *         selected before.
     */
    public boolean addBlockCheck(BlockLocation blockLocation) {
        return blockChecks.add(blockLocation);
    }

    /**
     * Checks if the BO3 created with these parameters will be valid. It will
     * throw an {@link InvalidBO3Exception} if the BO3 will be too big, or if
     * not all points are in the same world.
     * 
     * @param worldEditSelection
     *            The selection of the user.
     * @throws InvalidBO3Exception
     *             If the BO3 will be too big, or if not all points are in the
     *             same world.
     */
    public void checkBO3Valid(Selection worldEditSelection) throws InvalidBO3Exception {
        // Check size
        Vector startPos = worldEditSelection.getNativeMinimumPoint();
        Vector endPos = worldEditSelection.getNativeMaximumPoint();
        if (endPos.getBlockX() - startPos.getBlockX() > BO3Tools.MAX_DISTANCE) {
            throw new InvalidBO3Exception("The BO3 is too big.");
        }
        if (endPos.getBlockZ() - startPos.getBlockZ() > BO3Tools.MAX_DISTANCE) {
            throw new InvalidBO3Exception("The BO3 is too big.");
        }

        // Check distance and world of center
        BlockLocation centerOfSelection = getCenterOfSelection(worldEditSelection);
        if (center != null) {
            if (center.distanceSquared(centerOfSelection) > BO3Tools.MAX_DISTANCE_SQUARED) {
                throw new InvalidBO3Exception("The BO3 center is too far away.");
            }
        }

        // Check distance and world of blockchecks
        for (BlockLocation blockCheck : this.blockChecks) {
            if (blockCheck.distanceSquared(centerOfSelection) > BO3Tools.MAX_DISTANCE_SQUARED) {
                throw new InvalidBO3Exception("One of the BlockChecks is too far away.");
            }
        }
    }

    /**
     * Gets the center of the BO3 object. If the user has selected a center
     * using {@link #setCenter(BlockLocation)}, that center will be returned,
     * otherwise the center of the bottom of the selection will be returned.
     * 
     * @param worldEditSelection
     *            The WorldEdit selection of the player.
     * @return The center of the BO3 object.
     */
    public BlockLocation getCenter(Selection worldEditSelection) {
        if (center == null) {
            // Get center of bottom of selection
            return getCenterOfSelection(worldEditSelection);
        } else {
            // Get selected center
            return center;
        }
    }

    /**
     * Gets the center of the botton of the selection.
     * 
     * @param worldEditSelection
     *            The selection.
     * @return The center of the bottom.
     */
    private BlockLocation getCenterOfSelection(Selection worldEditSelection) {
        Vector startPos = worldEditSelection.getNativeMinimumPoint();
        Vector endPos = worldEditSelection.getNativeMaximumPoint();
        int x = (startPos.getBlockX() + endPos.getBlockX()) / 2;
        int y = startPos.getBlockY();
        int z = (startPos.getBlockZ() + endPos.getBlockZ()) / 2;
        return new BlockLocation(worldEditSelection.getWorld(), x, y, z);
    }

    /**
     * Removes a block check of the next created BO3.
     * 
     * @param blockLocation
     *            The location of the block check.
     * @return True if the block check was removed, false if the block wasn't
     *         selected before.
     */
    public boolean removeBlockCheck(BlockLocation blockLocation) {
        return blockChecks.remove(blockLocation);
    }

    /**
     * Removes all block checks, if any.
     * 
     * @return If there were previously no block checks, this will return false.
     */
    public boolean removeAllBlockChecks() {
        if (blockChecks.size() > 0) {
            blockChecks.clear();
            return true;
        }
        return false;
    }

    /**
     * Sets the center of the next BO3.
     * 
     * @param center
     *            The center of the next BO3.
     */
    public void setCenter(BlockLocation center) {
        this.center = center;
    }
}
