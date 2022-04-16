package cope.cosmos.client.features.modules.visual;

import com.google.common.collect.Lists;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;

import java.util.ArrayList;
import java.util.List;

public class WallhackModule extends Module {
    public static WallhackModule INSTANCE;

    public static final List<Block> WHITELIST = new ArrayList<>();
    public static final List<Block> DEFAULT_BLOCKS = Lists.newArrayList(

            // Normal blocks we'd maybe like to see
            Blocks.OBSIDIAN,
            Blocks.BEDROCK,
            Blocks.PORTAL,
            Blocks.END_PORTAL,
            Blocks.END_PORTAL_FRAME,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.MOB_SPAWNER,
            Blocks.BEACON,
            Blocks.BED,

            // Ores
            Blocks.DIAMOND_ORE,
            Blocks.COAL_ORE,
            Blocks.EMERALD_ORE,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.LAPIS_ORE,
            Blocks.LIT_REDSTONE_ORE,
            Blocks.QUARTZ_ORE,
            Blocks.REDSTONE_ORE,

            // Blocks that can be created from ores
            Blocks.DIAMOND_BLOCK,
            Blocks.COAL_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.REDSTONE_BLOCK
    );

    public WallhackModule() {
        super("Wallhack", Category.VISUAL, "Allows you to see desired blocks through walls");
        INSTANCE = this;
    }

    public static final Setting<Double> opacity = new Setting<>("Opacity", 0.0, 120.0, 255.0, 1)
            .setDescription("The opacity of non-whitelisted blocks");

    public static final Setting<Boolean> softReload = new Setting<>("SoftReload", false)
            .setDescription("If we should soft reload instead of reloading the entire renderer.");

    private boolean forgeLightPipelineEnabled;

    // For if this module is enabled whilst world or player is null, we'll reload once we ge the chance
    private boolean markReload;

    @Override
    public void onEnable() {
        super.onEnable();

        // disable forge's light pipeline
        forgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;

        // reload renderers, or mark to reload later
        if (nullCheck()) {
            reloadRenderers();
        } else {
            markReload = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // set forge's light pipeline back to what it was
        ForgeModContainer.forgeLightPipelineEnabled = forgeLightPipelineEnabled;

        // reload our renderers
        reloadRenderers();
    }

    @Override
    public void onTick() {

        // if the module was enabled while world/player is null, we'll reload now that we're in a world.
        if (markReload) {
            markReload = false;
            reloadRenderers();
        }
    }

    private void reloadRenderers() {

        if (softReload.getValue()) {

            // disable many chunk rendering
            mc.renderChunksMany = false;

            Vec3d pos = mc.player.getPositionVector();
            int dist = mc.gameSettings.renderDistanceChunks * 16;

            // mark blocks within our render distance to be reloaded
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) (pos.x) - dist, (int) (pos.y) - dist, (int) (pos.z) - dist,
                    (int) (pos.x) + dist, (int) (pos.y) + dist, (int) (pos.z) + dist);
        } else {

            // reload our renders
            mc.renderGlobal.loadRenderers();
        }
    }
}
