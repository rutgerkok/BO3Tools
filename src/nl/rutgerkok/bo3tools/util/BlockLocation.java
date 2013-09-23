package nl.rutgerkok.bo3tools.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

/**
 * An alternative location object to Bukkit's location. This class only stores 3
 * integers and one string, so it costs less memory. It is immutable, so it can
 * be used as a key in hashmaps.
 * 
 * The static methods are not threadsafe, only call them from the main thread.
 * 
 */
public class BlockLocation {
    private static Location workLocation;

    private static Location getWorkLocation() {
        if (workLocation == null) {
            workLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        }
        return workLocation;
    }

    /**
     * Gets the BlockLocation of the block. Can only be used from the main
     * thread.
     * 
     * @param block
     *            The block.
     * @return The BlockLocation of the block.
     */
    public static BlockLocation toBlockLocation(Block block) {
        return new BlockLocation(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    /**
     * Gets the BlockLocation of the entity. Doesn't create additional Location
     * instances. Can only be used from the main thread.
     * 
     * @param entity
     *            The entity.
     * @return The BlockLocation of the entity.
     */
    public static BlockLocation toBlockLocation(Entity entity) {
        Location location = entity.getLocation(getWorkLocation());
        return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private final String world;

    private final int x;

    private final int y;

    private final int z;

    /**
     * Creates a new BlockLocation.
     * 
     * @param world
     *            World of the location.
     * @param x
     *            X position of the location.
     * @param y
     *            Y position of the location.
     * @param z
     *            Z position of the location.
     * @throws IllegalArgumentException
     *             If the world is null.
     */
    public BlockLocation(World world, int x, int y, int z) throws IllegalArgumentException {
        Validate.notNull(world, "World cannot be null");
        this.world = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the distance between two locations. Returns {@link Double#NaN} if
     * the other location is null, or if the other location is in a different
     * world.
     * <p>
     * A note about performance: this function is slower than
     * {@link #distanceSquared(BlockLocation)} because it has to use a costly
     * square root.. If you are calling this function in a loop, you can better
     * get the squared distance, and compared that with the squared
     * minimum/maximum distance.
     * 
     * @param that
     *            The other location.
     * @return The distance between the locations, or NaN.
     */
    public double distance(BlockLocation that) {
        return Math.sqrt(distanceSquared(that));
    }

    /**
     * Returns the squared distance between two locations. Returns
     * {@link Double#NaN} if the other location is null, or if the other
     * location is in a different world.
     * 
     * @param that
     *            The other location.
     * @return The squared distance between the locations, or NaN.
     */
    public double distanceSquared(BlockLocation that) {
        if (that == null) {
            return Double.NaN;
        }
        if (!that.world.equals(this.world)) {
            return Double.NaN;
        }
        int deltaX = Math.abs(this.x - that.x);
        int deltaY = Math.abs(this.y - that.y);
        int deltaZ = Math.abs(this.z - that.z);
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BlockLocation) {
            BlockLocation other = (BlockLocation) obj;
            if (!world.equals(other.world)) {
                return false;
            }
            if (x != other.x) {
                return false;
            }
            if (y != other.y) {
                return false;
            }
            if (z != other.z) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the world of this location. Warning: may return null if the world
     * has been unloaded.
     * 
     * @return The world, or null if unloaded.
     */
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * Gets the world name of this location.
     * 
     * @return The world name.
     */
    public String getWorldName() {
        return world;
    }

    /**
     * Gets the x position of this location.
     * 
     * @return The x position of this location.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y position of this location.
     * 
     * @return The y position of this location.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the z position of this location.
     * 
     * @return The z position of this location.
     */
    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + world.hashCode();
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "BlockLocation(" + world + ", " + x + ", " + y + ", " + z + ")";
    }
}
