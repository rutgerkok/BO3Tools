package nl.rutgerkok.bo3tools;

import java.util.logging.Logger;

import nl.rutgerkok.bo3tools.util.PlayerDataCache;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BO3Tools extends JavaPlugin {
    public static final int MAX_DISTANCE = 32;
    public static final int MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    private PlayerDataCache<NextBO3Data> playerDataCache;

    /**
     * Gets the data for the next BO3 of the player. If there is no data yet, it
     * will be created.
     * 
     * @param player
     *            The player.
     * @return The data for the next BO3.
     */
    public NextBO3Data getNextBO3Data(Player player) {
        NextBO3Data data = playerDataCache.get(player);
        if (data == null) {
            // No data yet, create
            data = new NextBO3Data();
            playerDataCache.set(player, data);
        }
        return data;
    }

    public void log(String string) {
        Logger.getLogger("Minecraft").info("[" + getDescription().getName() + "] " + string);
    }

    public void onEnable() {
        getCommand("exportbo3").setExecutor(new BO3CreateCommand(this));
        getCommand("convertbo2").setExecutor(new BO2ConvertCommand(this));
        getServer().getPluginManager().registerEvents(new BO3ClickHandler(this), this);

        // Initialize data cache
        playerDataCache = new PlayerDataCache<NextBO3Data>(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDataCache.hearthBeat();
            }
        }.runTaskTimer(this, 20 * 60 * 2, 20 * 60 * 2);
    }

    /**
     * Removes the data for the next BO3. Call this after a BO3 has been
     * created.
     * 
     * @param player
     *            The affected player.
     */
    public void removeNextBO3Data(Player player) {
        playerDataCache.remove(player);
    }
}
