package nl.rutgerkok.bo3tools;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class BO3Tools extends JavaPlugin {
    public static final String BO3_CENTER_X = "bo3toolscenterx";
    public static final String BO3_CENTER_Y = "bo3toolscentery";
    public static final String BO3_CENTER_Z = "bo3toolscenterz";

    public void onEnable() {
        this.getCommand("exportbo3").setExecutor(new BO3CreateCommand(this));
        this.getCommand("convertbo2").setExecutor(new BO2ConvertCommand(this));
    }

    public void log(String string) {
        Logger.getLogger("Minecraft").info("[" + getDescription().getName() + "] " + string);
    }
}
