package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.render.gui.tooltip.RenderTooltipEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 08/17/2022
 */
public class TooltipsModule extends Module {
    public static TooltipsModule INSTANCE;

    public TooltipsModule() {
        super("Tooltips", Category.VISUAL, "Displays detailed tooltips");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Boolean> shulkers = new Setting<>("Shulkers", true)
            .setDescription("Renders detailed tooltip for shulkers");

    public static Setting<Boolean> maps = new Setting<>("Maps", true)
            .setDescription("Renders detailed tooltip for maps");

    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent event) {

        // ignore empty items
        if (!event.getItemStack().isEmpty()) {

            // check if the item is a shulker
            if (event.getItemStack().getTagCompound() != null && event.getItemStack().getTagCompound().hasKey("BlockEntityTag", 10)) {
                if (event.getItemStack().getTagCompound().getCompoundTag("BlockEntityTag").hasKey("Items", 9)) {

                    // custom shulker tooltip
                    if (shulkers.getValue()) {

                        // overwrite vanilla tooltip
                        event.setCanceled(true);

                        // shulker item list
                        NonNullList<ItemStack> itemList = NonNullList.withSize(27, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(event.getItemStack().getTagCompound().getCompoundTag("BlockEntityTag"), itemList);

                        // start render
                        GlStateManager.enableBlend();
                        GlStateManager.disableRescaleNormal();
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();

                        // draw container
                        RenderUtil.drawRect(event.getX() + 8, event.getY() - 21, event.getX() + 10, event.getY() - 7, new Color(23, 23, 29).getRGB());
                        RenderUtil.drawRect(event.getX() + 8, event.getY() - 7, event.getX() + 10, event.getY() - 5, ColorUtil.getPrimaryColor());
                        RenderUtil.drawRect(event.getX() + 8, event.getY() - 5, event.getX() + 10, event.getY() + 5, new Color(23, 23, 29).getRGB());

                        // draw shulker title
                        FontUtil.drawStringWithShadow(event.getItemStack().getDisplayName(), event.getX() + 10, event.getY() - 18, -1);

                        // start render
                        mc.getRenderItem().zLevel = 300;
                        GlStateManager.enableBlend();
                        GlStateManager.enableAlpha();
                        GlStateManager.enableTexture2D();
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        RenderHelper.enableGUIStandardItemLighting();

                        // draw each item in the shulker
                        for (int i = 0; i < itemList.size(); i++) {

                            // draw item
                            mc.getRenderItem().renderItemAndEffectIntoGUI(itemList.get(i), event.getX() + (i % 9) * 16 + 11, event.getY() + (i / 9) * 16 - 11 + 8);
                            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemList.get(i), event.getX() + (i % 9) * 16 + 11, event.getY() + (i / 9) * 16 - 11 + 8, null);
                        }

                        // end render
                        RenderHelper.disableStandardItemLighting();
                        mc.getRenderItem().zLevel = 0;
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        RenderHelper.enableStandardItemLighting();
                        GlStateManager.enableRescaleNormal();
                    }
                }
            }

            // check if item is a map
            if (event.getItemStack().getItem() instanceof ItemMap) {

                // get item's map data
                MapData mapData = ((ItemMap) event.getItemStack().getItem()).getMapData(event.getItemStack(), mc.world);

                // check if the map date is valid
                if (mapData != null) {

                    // custom map tooltip
                    if (maps.getValue()) {

                        // overwrite vanilla tooltip
                        event.setCanceled(true);

                        // draw container
                        RenderUtil.drawRect(event.getX() + 6, event.getY() - 3, event.getX() + 89.6F, event.getY() + 4, new Color(23, 23, 29).getRGB());
                        RenderUtil.drawRect(event.getX() + 6, event.getY() + 4, event.getX() + 89.6F, event.getY() + 6, ColorUtil.getPrimaryColor());

                        // start render
                        GlStateManager.pushMatrix();
                        GlStateManager.disableDepth();

                        // render map
                        GlStateManager.translate(event.getX() + 6, event.getY() + 6, 0);
                        GlStateManager.scale(0.7F, 0.7F, 0);
                        GL11.glDepthRange(0, 0.01);
                        mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
                        GL11.glDepthRange(0, 1.0);

                        // end render
                        GlStateManager.enableDepth();
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
}
