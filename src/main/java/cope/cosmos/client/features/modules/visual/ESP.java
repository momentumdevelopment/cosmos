package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.client.events.ShaderColorEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.client.ColorUtil;
import cope.cosmos.utility.world.EntityUtil;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unused")
public class ESP extends Module {
    public static ESP INSTANCE;

    public ESP() {
        super("ESP", Category.VISUAL, "Allows you to see players through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "The mode for the render style", Mode.SHADER);
    public static Setting<Double> width = new Setting<>(() -> mode.getValue().equals(Mode.SHADER), "Width", "Line width for the visual", 0.0, 1.25, 5.0, 1);

    public static Setting<Boolean> players = new Setting<>("Players", "Highlight players", true);
    public static Setting<Color> playersColor = new Setting<>("Color", "Color to highlight players", ColorUtil.getPrimaryColor()).setParent(players);

    public static Setting<Boolean> passives = new Setting<>("Passives", "Highlight passives", true);
    public static Setting<Color> passivesColor = new Setting<>("Color", "Color to highlight passives", ColorUtil.getPrimaryColor()).setParent(passives);

    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", "Highlight neutrals", true);
    public static Setting<Color> neutralsColor = new Setting<>("Color", "Color to highlight neutrals", ColorUtil.getPrimaryColor()).setParent(neutrals);

    public static Setting<Boolean> hostiles = new Setting<>("Hostiles", "Highlight hostiles", true);
    public static Setting<Color> hostilesColor = new Setting<>("Color", "Color to highlight hostiles", ColorUtil.getPrimaryColor()).setParent(hostiles);

    public static Setting<Boolean> items = new Setting<>("Items", "Highlight items", true);
    public static Setting<Color> itemsColor = new Setting<>("Color", "Color to highlight items", ColorUtil.getPrimaryColor()).setParent(items);

    public static Setting<Boolean> crystals = new Setting<>("Crystals", "Highlight crystals", true);
    public static Setting<Color> crystalsColor = new Setting<>("Color", "Color to highlight crystals", ColorUtil.getPrimaryColor()).setParent(crystals);

    public static Setting<Boolean> vehicles = new Setting<>("Vehicles", "Highlight vehicles", true);
    public static Setting<Color> vehiclesColor = new Setting<>("Color", "Color to highlight vehicles", ColorUtil.getPrimaryColor()).setParent(vehicles);

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.SHADER)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (!entity.equals(mc.player) && hasHighlight(entity)) {
                    entity.setGlowing(true);
                }
            });

            ShaderGroup outlineShaderGroup = ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader();
            List<Shader> shaders = ((IShaderGroup) outlineShaderGroup).getListShaders();

            shaders.forEach(shader -> {
                ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");

                if (outlineRadius != null)
                    outlineRadius.set(width.getValue().floatValue());
            });
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mode.getValue().equals(Mode.SHADER)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }
    }

    @SubscribeEvent
    public void onShaderColor(ShaderColorEvent event) {
        if (mode.getValue().equals(Mode.SHADER)) {
            event.setColor(getColorByEntity(event.getEntity()));
            event.setCanceled(hasHighlight(event.getEntity()));
        }
    }

    public static boolean hasHighlight(Entity entity) {
        return players.getValue() && entity instanceof EntityPlayer || passives.getValue() && EntityUtil.isPassiveMob(entity) || neutrals.getValue() && EntityUtil.isNeutralMob(entity) || hostiles.getValue() && EntityUtil.isHostileMob(entity) || vehicles.getValue() && EntityUtil.isVehicleMob(entity) || items.getValue() && (entity instanceof EntityItem || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb) || crystals.getValue() && entity instanceof EntityEnderCrystal;
    }

    public Color getColorByEntity(Entity entity) {
        if (entity instanceof EntityPlayer)
            return playersColor.getValue();

        else if (EntityUtil.isPassiveMob(entity))
            return passivesColor.getValue();

        else if (EntityUtil.isNeutralMob(entity))
            return neutralsColor.getValue();

        else if (EntityUtil.isHostileMob(entity))
            return hostilesColor.getValue();

        else if (EntityUtil.isVehicleMob(entity))
            return vehiclesColor.getValue();

        else if (entity instanceof EntityItem || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb)
            return itemsColor.getValue();

        else if (entity instanceof EntityEnderCrystal)
            return crystalsColor.getValue();

        return Color.WHITE;
    }

    public enum Mode {
        SHADER,
    }
}
