package nl.rutgerkok.bo3tools.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents a cache of Player data that keeps data in memory for at least the
 * given number of miliseconds.
 * <p>
 * Every time the {@link #hearthBeat()} is called, all old data is removed. You
 * have to call that method, otherwise old data <strong>will not be
 * removed</strong>. The data is only kept in memory. never saved to disk. Data
 * of offline players will be pruned automatically.
 * <p>
 * Implementation detail: the player instances aren't referenced here, instead
 * their names are used.
 * 
 * @param <T>
 *            The type of data to store.
 */
public class PlayerDataCache<T> {
    private static class DataEntry {
        private final Object data;
        private final long date;

        private DataEntry(Object data) {
            date = Calendar.getInstance().getTimeInMillis();
            this.data = data;
        }
    }
    private final Map<String, DataEntry> data;

    private final long timeToLive;

    /**
     * Constructs a new cache.
     * 
     * @param timeToLive
     *            The time to live for the data, in milliseconds. Setting this
     *            to 0 or a negative value will cause the data to be removed
     *            only when the player logs off.
     */
    public PlayerDataCache(long timeToLive) {
        data = new HashMap<String, DataEntry>();
        this.timeToLive = timeToLive;
    }

    /**
     * Gets a value for that player. Returns null if the value is not found.
     * Expired data will never be returned, even if {@link #hearthBeat()} hasn't
     * been called yet.
     * 
     * @param player
     *            The player to look up.
     * @return The data, or null if not found.
     */
    @SuppressWarnings("unchecked")
    // Safe, because this is the only class that modifies it.
    public T get(Player player) {
        DataEntry dataEntry = data.get(player.getName());
        if (dataEntry == null) {
            return null;
        }
        if (timeToLive <= 0 || Calendar.getInstance().getTimeInMillis() - dataEntry.date <= timeToLive) {
            return (T) dataEntry.data;
        } else {
            // Outdated data that was not yet pruned
            data.remove(player.getName());
        }
        return null;
    }

    /**
     * Gets a value for that player. Returns null if the value is not found.
     * Expired data will never be returned, even if {@link #hearthBeat()} hasn't
     * been called yet. The value for the player will immediately be removed
     * from the cache.
     * 
     * @param player
     *            The player get the value of.
     * @return The value, or null if not found.
     */
    public T getAndRemove(Player player) {
        T value = get(player);
        remove(player);
        return value;
    }

    /**
     * Cleans up old data. You need to call this every now and then.
     */
    public void hearthBeat() {
        // Make a set of all online player names
        Set<String> playerNames = new HashSet<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();
        for (Iterator<Entry<String, DataEntry>> it = data.entrySet().iterator(); it.hasNext();) {
            Entry<String, DataEntry> entry = it.next();
            if (timeToLive > 0 && currentTime - entry.getValue().date > timeToLive) {
                it.remove();
            } else if (!playerNames.contains(entry.getKey())) {
                it.remove();
            }
        }
    }

    /**
     * Removes the value for that player. If the player had nothing stored, this
     * won't do anything.
     * 
     * @param player
     *            The player.
     */
    public void remove(Player player) {
        data.remove(player.getName());
    }

    /**
     * Sets a new value for that player.
     * 
     * @param player
     *            The player.
     * @param value
     *            The value.
     */
    public void set(Player player, T value) {
        data.put(player.getName(), new DataEntry(value));
    }
}
