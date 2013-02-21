package nl.rutgerkok.bo3tools;

import java.util.logging.Logger;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

public class BO3Tools extends JavaPlugin {
    public static final String BO3_CENTER_X = "bo3toolscenterx";
    public static final String BO3_CENTER_Y = "bo3toolscentery";
    public static final String BO3_CENTER_Z = "bo3toolscenterz";

    public void onEnable() {
        getCommand("exportbo3").setExecutor(new BO3CreateCommand(this));
        getCommand("convertbo2").setExecutor(new BO2ConvertCommand(this));
        getServer().getPluginManager().registerEvents(new BO3CenterCreator(this), this);
    }

    public void log(String string) {
        Logger.getLogger("Minecraft").info("[" + getDescription().getName() + "] " + string);
    }

    // Metadata helpers

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(Metadatable lookup, String key) {
        for (MetadataValue value : lookup.getMetadata(key)) {
            if (value.getOwningPlugin().equals(this)) {
                return (T) value.value();
            }
        }
        return null;
    }

    public void setMetadata(Metadatable lookup, String key, Object value) {
        lookup.removeMetadata(key, this);
        lookup.setMetadata(key, new FixedMetadataValue(this, value));
    }

    public void removeMetadata(Metadatable lookup, String key) {
        lookup.removeMetadata(key, this);
    }
}
