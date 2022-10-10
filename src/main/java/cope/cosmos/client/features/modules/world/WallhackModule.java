package cope.cosmos.client.features.modules.world;

import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

/**
 * @author aesthetical
 * @since 04/16/2021
 */
public class WallhackModule extends Module {
    public static WallhackModule INSTANCE;

    public WallhackModule() {
        super("Wallhack", new String[] {"XRay"}, Category.WORLD, "Allows you to see desired blocks through walls");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Type> type = new Setting<>("Type", Type.BLACKLIST)
            .setAlias("Mode")
            .setDescription("Valid blocks");

    public static Setting<List<Block>> whiteList = new Setting<>("WhiteList", Arrays.asList(
            Blocks.GRASS,
            Blocks.GRAVEL,
            Blocks.STONE,
            Blocks.DIRT
    ))
            .setDescription("Valid block whitelist");

    public static Setting<List<Block>> blackList = new Setting<>("BlackList", Arrays.asList(
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
    ))
            .setDescription("Block blacklist");

//
//    public static final Setting<Double> opacity = new Setting<>("Opacity", 0.0, 120.0, 255.0, 0)
//            .setDescription("The opacity of non-whitelisted blocks");
//
//    public static final Setting<Boolean> caveCulling = new Setting<>("NoCaveCulling", false)
//            .setDescription("Allows you to be able to see cave systems as if you were in spectator mode");

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

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (nullCheck()) {

            // type changed
            if (event.getSetting().equals(type)) {
                reloadRenderers();
            }
        }
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

    /**
     * Checks if the given block is valid
     * @param in The given block
     * @return Whether the given block is valid
     */
    public boolean isValid(Block in) {

        // check if item is in the whitelist
        if (type.getValue().equals(Type.WHITELIST)) {
            return !whiteList.getValue().contains(in);
        }

        // check if item is not in the blacklist
        else if (type.getValue().equals(Type.BLACKLIST)) {
            return blackList.getValue().contains(in);
        }

        // all items
        return true;
    }

    public enum Type {

        /**
         * Only uses whitelist blocks
         */
        WHITELIST,


        /**
         * Only uses blocks not in the blacklist
         */
        BLACKLIST,

        /**
         * Uses all blocks
         */
        ALL
    }
}