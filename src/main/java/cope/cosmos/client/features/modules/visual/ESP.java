package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.client.events.ShaderColorEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
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

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SHADER).setDescription("The mode for the render style");
    public static Setting<Double> width = new Setting<>("Width", 0.0, 1.25, 5.0, 1).setDescription( "Line width for the visual").setVisible(() -> mode.getValue().equals(Mode.SHADER));

    public static Setting<Boolean> players = new Setting<>("Players", true).setDescription("Highlight players");
    public static Setting<Color> playersColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(players).setDescription("Color to highlight players");

    public static Setting<Boolean> passives = new Setting<>("Passives", true).setDescription("Highlight passives");
    public static Setting<Color> passivesColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(passives).setDescription("Color to highlight passives");

    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", true).setDescription("Highlight neutrals");
    public static Setting<Color> neutralsColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(neutrals).setDescription("Color to highlight neutrals");

    public static Setting<Boolean> hostiles = new Setting<>("Hostiles", true).setDescription("Highlight hostiles");
    public static Setting<Color> hostilesColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(hostiles).setDescription("Color to highlight hostiles");

    public static Setting<Boolean> items = new Setting<>("Items", true).setDescription("Highlight items");
    public static Setting<Color> itemsColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(items).setDescription("Color to highlight items");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true).setDescription("Highlight crystals");
    public static Setting<Color> crystalsColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(crystals).setDescription("Color to highlight crystals");

    public static Setting<Boolean> vehicles = new Setting<>("Vehicles", true).setDescription("Highlight vehicles");
    public static Setting<Color> vehiclesColor = new Setting<>("Color", ColorUtil.getPrimaryColor()).setParent(vehicles).setDescription("Color to highlight vehicles");

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

                if (outlineRadius != null) {
                    outlineRadius.set(width.getValue().floatValue());
                }
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
        if (entity instanceof EntityPlayer) {
            return playersColor.getValue();
        }

        else if (EntityUtil.isPassiveMob(entity)) {
            return passivesColor.getValue();
        }

        else if (EntityUtil.isNeutralMob(entity)) {
            return neutralsColor.getValue();
        }

        else if (EntityUtil.isHostileMob(entity)) {
            return hostilesColor.getValue();
        }

        else if (EntityUtil.isVehicleMob(entity))
            return vehiclesColor.getValue();

        else if (entity instanceof EntityItem || entity instanceof EntityEnderPearl || entity instanceof EntityEnderEye || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb)
            return itemsColor.getValue();

        else if (entity instanceof EntityEnderCrystal) {
            return crystalsColor.getValue();
        }

        return Color.WHITE;
    }

    public enum Mode {
        SHADER,
    }
}
