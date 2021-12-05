package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.combat.TargetUtil.Target;
import cope.cosmos.utility.player.InventoryUtil.Switch;
import cope.cosmos.utility.player.PlayerUtil.Hand;
import cope.cosmos.utility.player.Rotation.Rotate;

public class AutoTrap extends Module {
    public AutoTrap() {
        super("AutoTrap", Category.COMBAT, "Traps enemies in obsidian");
    }

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode to switch to blocks", Switch.NORMAL);
    public static Setting<Double> blocks = new Setting<>("Blocks", "Allowed block placements per tick", 0.0, 4.0, 10.0, 0);
    public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing when placing blocks", Hand.MAINHAND);
    public static Setting<Boolean> packet = new Setting<>("Packet", "Place with packets", false);
    public static Setting<Boolean> confirm = new Setting<>("Confirm", "Confirm the placement", false);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
    public static Setting<Boolean> rotateCenter = new Setting<>("Center", "Center rotations on target", false).setParent(rotate);
    public static Setting<Boolean> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", false).setParent(rotate);

    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Double> targetRange = new Setting<>("Range", "Range to trap players", 0.0, 5.0, 10.0, 0).setParent(target);

    /*

    int previousSlot = -1;
    int trapPlaced = 0;
    EntityPlayer trapTarget = null;
    Rotation trapRotation = new Rotation(Float.NaN, Float.NaN, Rotate.NONE);

    @Override
    public void onUpdate() {
        trapTarget = TargetUtil.getTargetPlayer(targetRange.getValue(), target.getValue());

        if (trapTarget != null) {
            trapPlaced = 0;
            autoTrap(mapTrapPositions());
        }
    }


    public void autoTrap(Iterator<Vec3d> trapPositions) {
        previousSlot = mc.player.inventory.currentItem;

        InventoryUtil.switchToSlot(Item.getItemFromBlock(Blocks.OBSIDIAN), autoSwitch.getValue());

        while (trapPositions.hasNext()) {
            BlockPos trapPosition = new BlockPos(trapTarget.getPositionVector().add(trapPositions.next()));

            if (Objects.equals(BlockUtil.getBlockResistance(trapPosition), BlockResistance.BLANK) && trapPlaced <= blocks.getValue()) {
                if (!rotate.getValue().equals(Rotate.NONE)) {
                    float[] trapAngles = rotateCenter.getValue() ? AngleUtil.calculateCenter(trapPosition) : AngleUtil.calculateAngles(trapPosition);
                    trapRotation = new Rotation((float) (trapAngles[0] + (rotateRandom.getValue() ? ThreadLocalRandom.current().nextDouble(-4, 4) : 0)), (float) (trapAngles[1] + (rotateRandom.getValue() ? ThreadLocalRandom.current().nextDouble(-4, 4) : 0)), rotate.getValue());

                    if (!Float.isNaN(trapRotation.getYaw()) && !Float.isNaN(trapRotation.getPitch()))
                        trapRotation.updateRotations();
                }

                BlockUtil.placeBlock(trapPosition, packet.getValue(), confirm.getValue());
                PlayerUtil.swingArm(swing.getValue());
                trapPlaced++;
            }
        }

        InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && !Float.isNaN(trapRotation.getYaw()) && !Float.isNaN(trapRotation.getPitch())) {
            ((ICPacketPlayer) event.getPacket()).setYaw(trapRotation.getYaw());
            ((ICPacketPlayer) event.getPacket()).setPitch(trapRotation.getPitch());
        }
    }

    public Iterator<Vec3d> mapTrapPositions() {
        if (trapTarget != null && trapTarget.onGround) {
            boolean middleX = Math.abs(Math.round(trapTarget.posX) - trapTarget.posX) <= 0.3;
            boolean middleZ = Math.abs(Math.round(trapTarget.posZ) - trapTarget.posZ) <= 0.3;

            if (middleX && middleZ) {

            }

            else if (middleX) {

            }

            else if (middleZ) {
                if (Math.round(trapTarget.posX) - trapTarget.posX < 0) {
                    return Arrays.asList(
                            new Vec3d(0, -1, -1),
                            new Vec3d(1, -1, 0),
                            new Vec3d(0, -1, 1),
                            new Vec3d(-1, -1, 0),
                            new Vec3d(0, 0, -1),
                            new Vec3d(1, 0, 0),
                            new Vec3d(0, 0, 1),
                            new Vec3d(-1, 0, 0),
                            new Vec3d(0, 1, -1),
                            new Vec3d(1, 1, 0),
                            new Vec3d(0, 1, 1),
                            new Vec3d(-1, 1, 0),
                            new Vec3d(0, 2, -1),
                            new Vec3d(0, 2, 1),
                            new Vec3d(0, 2, 0)
                    ).iterator();
                }

                else if (Math.round(trapTarget.posX) - trapTarget.posX > 0) {
                    return Arrays.asList(
                            new Vec3d(0, -1, -1),
                            new Vec3d(1, -1, -1),
                            new Vec3d(1, -1, -1),
                            new Vec3d(0, -1, -2),
                            new Vec3d(1, -1, 0),
                            new Vec3d(0, -1, 1),
                            new Vec3d(-1, -1, 0),
                            new Vec3d(-1, -1, 0),
                            new Vec3d(1, 0, -1),
                            new Vec3d(1, 0, -1),
                            new Vec3d(0, 0, -2),
                            new Vec3d(1, 0, 0),
                            new Vec3d(0, 0, 1),
                            new Vec3d(-1, 0, 0),
                            new Vec3d(-1, 0, 0),
                            new Vec3d(1, 1, -1),
                            new Vec3d(1, 1, -1),
                            new Vec3d(0, 1, -2),
                            new Vec3d(1, 1, 0),
                            new Vec3d(0, 1, 1),
                            new Vec3d(-1, 1, 0),
                            new Vec3d(-1, 1, 0),
                            new Vec3d(0, 2, -1),
                            new Vec3d(0, 2, -2),
                            new Vec3d(0, 2, 1),
                            new Vec3d(0, 2, 0)
                    ).iterator();
                }
            }

            else {
                return Arrays.asList(
                        new Vec3d(0, -1, -1),
                        new Vec3d(1, -1, 0),
                        new Vec3d(0, -1, 1),
                        new Vec3d(-1, -1, 0),
                        new Vec3d(0, 0, -1),
                        new Vec3d(1, 0, 0),
                        new Vec3d(0, 0, 1),
                        new Vec3d(-1, 0, 0),
                        new Vec3d(0, 1, -1),
                        new Vec3d(1, 1, 0),
                        new Vec3d(0, 1, 1),
                        new Vec3d(-1, 1, 0),
                        new Vec3d(0, 2, -1),
                        new Vec3d(0, 2, 1),
                        new Vec3d(0, 2, 0)
                ).iterator();
            }
        }

        return null;
    }

     */
}
