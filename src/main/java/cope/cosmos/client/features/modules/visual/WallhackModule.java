package cope.cosmos.client.features.modules.visual;

import com.google.common.collect.Lists;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aesthetical
 * @since 04/16/2021
 */
public class WallhackModule extends Module {
    public static WallhackModule INSTANCE;

    public WallhackModule() {
        super("Wallhack", new String[] {"XRay"}, Category.VISUAL, "Allows you to see desired blocks through walls");
        INSTANCE = this;
    }

//    // **************************** general ****************************
//
//    public static final Setting<Double> opacity = new Setting<>("Opacity", 0.0, 120.0, 255.0, 0)
//            .setDescription("The opacity of non-whitelisted blocks");
//
//    public static final Setting<Boolean> caveCulling = new Setting<>("NoCaveCulling", false)
//            .setDescription("Allows you to be able to see cave systems as if you were in spectator mode");

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

    private boolean forgeLightPipelineEnabled;

    @Override
    public void onEnable() {
        super.onEnable();

        // disable forge's light pipeline
        forgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;

        // reload renderers, or mark to reload later
        if (nullCheck()) {
            reloadRenderers();
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

//    @SubscribeEvent
//    public void onColorMultiplier(ColorMultiplierEvent event) {
//
//        // update block opacity color
//        event.setOpacity(opacity.getValue().intValue());
//        event.setCanceled(true);
//    }

//    @SubscribeEvent
//    public void onCaveCulling(CaveCullingEvent event) {
//
//        // if we should cancel cave culling
//        event.setCanceled(caveCulling.getValue());
//    }

    /**
     * Reloads minecraft renders
     */
    private void reloadRenderers() {

        // disable many chunk rendering
        mc.renderChunksMany = false;

        Vec3d pos = mc.player.getPositionVector();
        int dist = mc.gameSettings.renderDistanceChunks * 16;

        // mark blocks within our render distance to be reloaded
        mc.renderGlobal.markBlockRangeForRenderUpdate(
                (int) (pos.x) - dist, (int) (pos.y) - dist, (int) (pos.z) - dist,
                (int) (pos.x) + dist, (int) (pos.y) + dist, (int) (pos.z) + dist);
    }
}