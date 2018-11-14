package nl.rutgerkok.bo3tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pg85.otg.configuration.CustomObjectConfigFunction;
import com.pg85.otg.configuration.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo2.BO2;
import com.pg85.otg.customobjects.bo2.ObjectCoordinate;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.bo3.BlockFunction;

public class BO2Converter {

    /**
     * Converts the given BO2 into a new BO3 object. All blocks and most
     * settings are converted. The BO3 isn't saved.
     * 
     * @param authorName Name that is used in the author setting of the BO3.
     * @param bo2 The BO2 object to convert
     * @return A new BO3 object
     */
    public static BO3 convertBO2(String authorName, BO2 bo2) {
        File bo3File = new File(bo2.getFile().getParentFile(), bo2.getName() + ".bo3");
        BO3 bo3 = new BO3(bo2.getName(), bo3File);
        bo3.onEnable(Collections.<String, CustomObject> emptyMap());
        BO3Config bo3Config = bo3.getSettings();

        // Convert all blocks
        List<BlockFunction> newBlocks = new ArrayList<BlockFunction>();
        for (ObjectCoordinate oldBlock : bo2.data[0]) {
            BlockFunction newBlock = (BlockFunction) CustomObjectConfigFunction.create(bo3Config, BlockFunction.class);
            newBlock.x = oldBlock.x;
            newBlock.y = oldBlock.y;
            newBlock.z = oldBlock.z;
            newBlock.material = oldBlock.material;
            newBlocks.add(newBlock);
        }
        bo3Config.blocks[0] = newBlocks.toArray(new BlockFunction[newBlocks.size()]);
        bo3Config.rotateBlocksAndChecks();

        // Convert SpawnHeight
        if (bo2.spawnAboveGround && !bo2.spawnUnderGround) {
            bo3Config.spawnHeight = SpawnHeightEnum.highestSolidBlock;
        } else if (bo2.spawnUnderGround) {
            // Not exactly the same, but there isn't anything better
            bo3Config.spawnHeight = SpawnHeightEnum.randomY;
        }

        // Convert misc settings
        bo3Config.author = authorName;
        bo3Config.description = "Converted version of the BO2 " + bo2.getName();
        bo3Config.tree = bo2.tree;
        bo3Config.minHeight = bo2.spawnElevationMin;
        bo3Config.maxHeight = bo2.spawnElevationMax - 1;
        bo3Config.maxPercentageOutsideSourceBlock = (int) bo2.collisionPercentage;
        bo3Config.rotateRandomly = bo2.randomRotation;
        bo3Config.settingsMode = ConfigMode.WriteDisable;
        bo3Config.excludedBiomes = new ArrayList<>();

        return bo3;
    }
}
