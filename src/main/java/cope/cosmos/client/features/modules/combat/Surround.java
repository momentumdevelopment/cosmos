package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import cope.cosmos.util.world.*;
import cope.cosmos.util.world.BlockUtil.Resistance;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class Surround extends Module {
    public static Surround INSTANCE;

    public Surround() {
        super("Surround", Category.COMBAT, "Surrounds your feet with obsidian");
        INSTANCE = this;
    }

    public static Setting<SurroundVectors> surround = new Setting<>("Surround", "Block positions for surround", SurroundVectors.BASE);
    public static Setting<Completion> completion = new Setting<>("Completion", "When to toggle surround", Completion.AIR);
    public static Setting<Center> center = new Setting<>("Center", "Mode to center the player position", Center.TELEPORT);
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode to switch to blocks", Switch.NORMAL);

    public static Setting<Double> blocks = new Setting<>("Blocks", "Allowed block placements per tick", 0.0, 4.0, 10.0, 0);

    public static Setting<Boolean> strict = new Setting<>("Strict", "Only places on visible sides", false);
    public static Setting<Boolean> reactive = new Setting<>("Reactive", "Replaces surround blocks when they break", true);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
    public static Setting<Boolean> rotateCenter = new Setting<>("Center", "Center rotations on target", false).setParent(rotate);
    public static Setting<Boolean> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", false).setParent(rotate);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual of the surround", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style of the visual", Box.FILL).setParent(render);
    public static Setting<Color> renderSafe = new Setting<>("SafeColor", "Color for surrounded blocks", new Color(0, 255, 0, 40)).setParent(render);
    public static Setting<Color> renderUnsafe = new Setting<>("UnsafeColor", "Color for unsafe blocks", new Color(255, 0, 0, 40)).setParent(render);

    int previousSlot = -1;
    int surroundPlaced = 0;

    BlockPos previousPosition = BlockPos.ORIGIN;
    BlockPos surroundPosition = BlockPos.ORIGIN;

    @Override
    public void onEnable() {
        super.onEnable();

        previousPosition = new BlockPos(new Vec3d(MathUtil.roundFloat(mc.player.getPositionVector().x, 0), MathUtil.roundFloat(mc.player.getPositionVector().y, 0), MathUtil.roundFloat(mc.player.getPositionVector().z, 0)));

        switch (center.getValue()) {
            case TELEPORT:
                double xPosition = mc.player.getPositionVector().x;
                double zPosition = mc.player.getPositionVector().z;

                if (Math.abs((previousPosition.getX() + 0.5) - mc.player.getPositionVector().x) >= 0.2) {
                    int xDirection = (previousPosition.getX() + 0.5) - mc.player.getPositionVector().x > 0 ? 1 : -1;
                    xPosition += 0.3 * xDirection;
                }

                if (Math.abs((previousPosition.getZ() + 0.5) - mc.player.getPositionVector().z) >= 0.2) {
                    int zDirection = (previousPosition.getZ() + 0.5) - mc.player.getPositionVector().z > 0 ? 1 : -1;
                    zPosition += 0.3 * zDirection;
                }
                
                TeleportUtil.teleportPlayer(xPosition, mc.player.posY, zPosition);
                break;
            case MOTION:
                mc.player.motionX = ((Math.floor(mc.player.posX) + 0.5) - mc.player.posX) / 2;
                mc.player.motionZ = ((Math.floor(mc.player.posZ) + 0.5) - mc.player.posZ) / 2;
                break;
            case NONE:
            	break;
        }
    }

    @Override
    public void onUpdate() {
        surroundPlaced = 0;

        switch (completion.getValue()) {
            case AIR:
                if (!previousPosition.equals(new BlockPos(MathUtil.roundFloat(mc.player.getPositionVector().x, 0), MathUtil.roundFloat(mc.player.getPositionVector().y, 0), MathUtil.roundFloat(mc.player.getPositionVector().z, 0))) || mc.player.posY > previousPosition.getY()) {
                    disable();
                    getAnimation().setState(false);
                    return;
                }

                break;
            case SURROUNDED:
                if (getCosmos().getHoleManager().isHoleEntity(mc.player)) {
                    disable();
                    getAnimation().setState(false);
                    return;
                }

                break;
            case PERSISTENT:
                break;
        }

        handleSurround();
    }

    @Override
    public void onRender3D() {
        if (render.getValue()) {
            for (Vec3d surroundVectors : surround.getValue().getVectors()) {
                RenderUtil.drawBox(new RenderBuilder().position(new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ)))).color((Objects.equals(BlockUtil.getResistance(new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ)))), Resistance.RESISTANT) || Objects.equals(BlockUtil.getResistance(new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ)))), Resistance.UNBREAKABLE)) ? renderSafe.getValue() : renderUnsafe.getValue()).box(renderMode.getValue()).setup().line(1.5F).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
            }
        }
    }

    public void handleSurround() {
        previousSlot = mc.player.inventory.currentItem;

        if (!getCosmos().getHoleManager().isHoleEntity(mc.player)) {
            InventoryUtil.switchToSlot(Item.getItemFromBlock(Blocks.OBSIDIAN), autoSwitch.getValue());

            placeSurround();

            InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
        }
    }

    public void placeSurround() {
        for (Vec3d surroundVectors : surround.getValue().getVectors()) {
            if (Objects.equals(BlockUtil.getResistance(new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ)))), Resistance.REPLACEABLE) && surroundPlaced <= blocks.getValue()) {
                surroundPosition = new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ)));

                getCosmos().getInteractionManager().placeBlock(new BlockPos(surroundVectors.add(new Vec3d(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ))), rotate.getValue(), strict.getValue());

                surroundPlaced++;
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && !getCosmos().getHoleManager().isHoleEntity(mc.player);
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketBlockChange) {
            if (surround.getValue().getVectors().contains(new Vec3d(((SPacketBlockChange) event.getPacket()).getBlockPosition()))) {
                // switch to obsidian
                previousSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchToSlot(Item.getItemFromBlock(Blocks.OBSIDIAN), autoSwitch.getValue());

                // place block
                getCosmos().getInteractionManager().placeBlock(((SPacketBlockChange) event.getPacket()).getBlockPosition(), rotate.getValue(), false);

                // switchback
                InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
            }
        }
    }

    public enum SurroundVectors {
        BASE(Arrays.asList(
                new Vec3d(0, -1, 0),
                new Vec3d(1, -1, 0),
                new Vec3d(0, -1, 1),
                new Vec3d(-1, -1, 0),
                new Vec3d(0, -1, -1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(-1, 0, 0),
                new Vec3d(0, 0, -1)
        )),

        STANDARD(Arrays.asList(
                new Vec3d(0, -1, 0),
                new Vec3d(1, 0, 0),
                new Vec3d(-1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(0, 0, -1)
        )),

        PROTECT(Arrays.asList(
                new Vec3d(0, -1, 0),
                new Vec3d(1, 0, 0),
                new Vec3d(-1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(0, 0, -1),
                new Vec3d(2, 0, 0),
                new Vec3d(-2, 0, 0),
                new Vec3d(0, 0, 2),
                new Vec3d(0, 0, -2),
                new Vec3d(3, 0, 0),
                new Vec3d(-3, 0, 0),
                new Vec3d(0, 0, 3),
                new Vec3d(0, 0, -3)
        ));

        private final List<Vec3d> vectors;

        SurroundVectors(List<Vec3d> vectors) {
            this.vectors = vectors;
        }

        public List<Vec3d> getVectors() {
            return vectors;
        }
    }

    public enum Center {
        TELEPORT, MOTION, NONE
    }

    public enum Completion {
        AIR, SURROUNDED, PERSISTENT
    }
}