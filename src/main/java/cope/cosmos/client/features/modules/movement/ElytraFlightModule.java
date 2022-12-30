package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.*;
import cope.cosmos.client.events.entity.player.UpdateWalkingPlayerEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.motion.movement.TravelEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * @author linustouchtips
 * @since 09/26/2022
 */
public class ElytraFlightModule extends Module {
    public static ElytraFlightModule INSTANCE;

    public ElytraFlightModule() {
        super("ElytraFlight", new String[] {"ElytraFly", "EFly", "ElytraPlus", "Elytra+"}, Category.MOVEMENT, "Allows you to fly infinitely on an elytra", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.CONTROL)
            .setDescription("Mode for ElytraFlight");

    public static Setting<Strict> strict = new Setting<>("Strict", Strict.NEW)
            .setAlias("NCPStrict")
            .setDescription("Strict mode for packets")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Vertical> vertical = new Setting<>("Vertical", Vertical.CONTROL)
            .setAlias("Up", "UpMode", "VMode")
            .setDescription("Vertical movement")
            .setVisible(() -> mode.getValue().equals(Mode.CONTROL));

    public static Setting<Friction> friction = new Setting<>("Friction", Friction.CUTOFF)
            .setAlias("Pause")
            .setDescription("Mode for speed friction factor")
            .setVisible(() -> !mode.getValue().equals(Mode.PACKET));

    public static Setting<Boolean> instantFly = new Setting<>("InstantFly", true)
            .setAlias("AutoTakeoff", "Takeoff", "InstantTakeoff")
            .setDescription("Attempts to takeoff instantly")
            .setVisible(() -> !mode.getValue().equals(Mode.PACKET) && !mode.getValue().equals(Mode.CONTROL_STRICT));

    public static Setting<Double> speed = new Setting<>("Speed", 0.1D, 2.5D, 10.0D, 1)
            .setAlias("Glide", "GlideSpeed")
            .setDescription("Speed for horizontal movement")
            .setVisible(() -> !mode.getValue().equals(Mode.FACTORIZE) && !mode.getValue().equals(Mode.CONTROL_STRICT));

    public static Setting<Double> baseSpeed = new Setting<>("BaseSpeed", 0.1D, 15.0D, 50.0D, 1)
            .setDescription("Base Speed for horizontal movement")
            .setVisible(() -> mode.getValue().equals(Mode.CONTROL_STRICT));

    public static Setting<Double> verticalSpeed = new Setting<>("VerticalSpeed", 0.1D, 1.0D, 5.0D, 1)
            .setAlias("UpSpeed", "DownSpeed", "Ascend", "Descend", "VSpeed")
            .setDescription("Speed for vertical movement")
            .setVisible(() -> !vertical.getValue().equals(Vertical.NONE) && mode.getValue().equals(Mode.CONTROL));

    public static Setting<Double> factorizeBoost = new Setting<>("FactorizeBoost", 0.1D, 0.5D, 1.0D, 1)
            .setAlias("FactorizeSpeed", "BoostSpeed")
            .setDescription("Boost speed")
            .setVisible(() -> mode.getValue().equals(Mode.FACTORIZE));

    public static Setting<Double> factorizeTick = new Setting<>("FactorizeTicks", 1.0D, 25.0D, 150.0D, 0)
            .setAlias("MaxBoost", "BoostTicks")
            .setDescription("Max ticks to boost")
            .setVisible(() -> mode.getValue().equals(Mode.FACTORIZE));

    public static Setting<Boolean> accelerate = new Setting<>("Accelerate", true)
            .setDescription("Automatically accelerates")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET) || mode.getValue().equals(Mode.CONTROL_STRICT));

    public static Setting<Double> accelerateSpeed = new Setting<>("AccelerateSpeed", 0.1D, 15.0D, 50.0D, 1)
            .setDescription("Maximum speed")
            .setVisible(() -> mode.getValue().equals(Mode.CONTROL_STRICT) && accelerate.getValue());

    public static Setting<Boolean> infiniteDurability = new Setting<>("InfiniteDurability", false)
            .setAlias("InfDurability", "InfDura", "InfiniteDura")
            .setDescription("Infinite durability exploit")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Boolean> firework = new Setting<>("Fireworks", false)
            .setDescription("Attempts to use fireworks while flying")
            .setVisible(() -> !mode.getValue().equals(Mode.PACKET));

    // movement speed
    private double moveSpeed;
    private double strictSpeed;

    // spoofed yaw and pitch
    private float yaw;
    private float pitch;

    // flight ticks
    private int verticalTicks;
    private int factorizeTicks;

    // fall timer
    private final Timer fallFlyTimer = new Timer();

    // random factor
    private final Random random = new Random();

    @Override
    public void onEnable() {
        super.onEnable();

        // restart
        factorizeTicks = 0;
        verticalTicks = 0;
        moveSpeed = 0;
        strictSpeed = baseSpeed.getValue();
    }

    @Override
    public void onUpdate() {

        // attempt to takeoff automatically
        if (instantFly.getValue()) {

            // do not attempt in packet mode or strict mode
            if (!mode.getValue().equals(Mode.PACKET) && !mode.getValue().equals(Mode.CONTROL_STRICT)) {

                // can takeoff
                if (!mc.player.onGround && !mc.player.isElytraFlying() && !mc.player.capabilities.isFlying && mc.player.motionY < 0) {

                    // takeoff
                    if (fallFlyTimer.passedTime(500, Format.MILLISECONDS)) {

                        // fall packet
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                        fallFlyTimer.resetTime();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onTravel(TravelEvent event) {

        // IDK WHY RAHHHHHH
        if (nullCheck()) {

            // check if player is elytra flying
            if (mc.player.isElytraFlying()) {

                // stop in water and liquids
                if (friction.getValue().equals(Friction.CUTOFF) || mode.getValue().equals(Mode.PACKET)) {

                    // make sure the player is not in a liquid
                    if (PlayerUtil.isInLiquid()) {
                        return;
                    }

                    // make sure the player is not in a web
                    if (((IEntity) mc.player).getInWeb()) {
                        return;
                    }
                }

                // full creative flight
                if (mode.getValue().equals(Mode.CONTROL)) {

                    // prevent vanilla travel
                    event.setCanceled(true);

                    // the current movement input values of the user
                    float forward = mc.player.movementInput.moveForward;
                    float strafe = mc.player.movementInput.moveStrafe;
                    float yaw = mc.player.rotationYaw;

                    // our facing values, according to movement not rotations
                    double cos = Math.cos(Math.toRadians(yaw + 90));
                    double sin = Math.sin(Math.toRadians(yaw + 90));

                    // if we're not inputting any movements, then we shouldn't be adding any motion
                    if (!MotionUtil.isMoving()) {
                        mc.player.motionX = 0;
                        mc.player.motionZ = 0;
                    }

                    else {

                        // look down to move forward
                        pitch = 12;

                        // update the movements
                        mc.player.motionX = (forward * speed.getValue() * cos) + (strafe * speed.getValue() * sin);
                        mc.player.motionZ = (forward * speed.getValue() * sin) - (strafe * speed.getValue() * cos);
                    }

                    // prevent falling
                    mc.player.motionY = 0;
                    pitch = 0;

                    // upward motion
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {

                        // allow vertical motion
                        if (!vertical.getValue().equals(Vertical.NONE)) {

                            // look up to move up
                            pitch = -51;

                            // full upward control
                            if (vertical.getValue().equals(Vertical.CONTROL)) {

                                // straight up
                                mc.player.motionY = verticalSpeed.getValue();
                            }

                            // vanilla elytra movement
                            else if (vertical.getValue().equals(Vertical.GLIDE)) {

                                // idk
                                if (mc.player.motionY > -0.5) {
                                    mc.player.fallDistance = 1;
                                }

                                // must be moving for vertical movement
                                if (mc.player.motionX != 0 && mc.player.motionZ != 0) {

                                    // vector from rotations
                                    Vec3d rotations = AngleUtil.getVectorForRotation(new Rotation(mc.player.rotationYaw, pitch));

                                    // motion based on rotations
                                    float pitchScaled = pitch * 0.017453292F;
                                    double rotationDistance = Math.sqrt(rotations.x * rotations.x + rotations.z * rotations.z);
                                    double distance = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                                    double length = rotations.lengthVector();
                                    float pitchCos = MathHelper.cos(pitchScaled);
                                    pitchCos = (float) ((double) pitchCos * (double) pitchCos * Math.min(1, length / 0.4D));

                                    // vertical motion
                                    mc.player.motionY += -0.08D + (double) pitchCos * 0.06D;

                                    // fall speed
                                    /*
                                    if (mc.player.motionY < 0 && rotationDistance > 0) {
                                        double verticalSpeed = mc.player.motionY * -0.1D * (double) pitchCos;
                                        mc.player.motionY += verticalSpeed;
                                        mc.player.motionX += rotations.x * verticalSpeed / rotationDistance;
                                        mc.player.motionZ += rotations.z * verticalSpeed / rotationDistance;
                                    }
                                    */

                                    // up speed
                                    if (pitchScaled < 0) {
                                        double distanceSpeed = distance * (double) (-MathHelper.sin(pitchScaled)) * 0.04;
                                        mc.player.motionY += distanceSpeed * 3.2;
                                        mc.player.motionX -= rotations.x * distanceSpeed / rotationDistance;
                                        mc.player.motionZ -= rotations.z * distanceSpeed / rotationDistance;
                                    }

                                    // horizontal speed
                                    /*
                                    if (rotationDistance > 0) {
                                        mc.player.motionX += (rotations.x / rotationDistance * distance - mc.player.motionX) * 0.1;
                                        mc.player.motionZ += (rotations.z / rotationDistance * distance - mc.player.motionZ) * 0.1;
                                    }
                                    */

                                    // drag
                                    mc.player.motionX *= 0.9900000095367432D;
                                    mc.player.motionY *= 0.9800000190734863D;
                                    mc.player.motionZ *= 0.9900000095367432D;
                                }
                            }

                            verticalTicks++;
                        }
                    }

                    // downward motion
                    else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                        // straight down, should pretty much always work
                        mc.player.motionY = -verticalSpeed.getValue();
                    }

                    // lock limbs
                    mc.player.prevLimbSwingAmount = 0;
                    mc.player.limbSwingAmount = 0;
                    mc.player.limbSwing = 0;
                }

                // strict creative flight
                else if (mode.getValue().equals(Mode.CONTROL_STRICT)) {

                    // prevent vanilla travel
                    event.setCanceled(true);

                    // accelerate to speed
                    if (accelerate.getValue()) {

                        // increment
                        if (strictSpeed < accelerateSpeed.getValue()) {

                            // acceleration factor: 0.001 (might change in the future)
                            strictSpeed += 0.001;
                        }

                        // clamp
                        if (strictSpeed > accelerateSpeed.getValue()) {
                            strictSpeed = accelerateSpeed.getValue();
                        }
                    }

                    // instant motion
                    else {
                        strictSpeed = accelerateSpeed.getValue();
                    }

                    // the current movement input values of the user
                    float forward = mc.player.movementInput.moveForward;

                    // motion based on rotations
                    float yawScaled = mc.player.rotationYaw * 0.017453292F;

                    // move
                    double sin = -Math.sin(yawScaled);
                    double cos = Math.cos(yawScaled);

                    // if we're not inputting any movements, then we shouldn't be adding any motion
                    if (!MotionUtil.isMoving()) {
                        mc.player.motionX = 0;
                        mc.player.motionZ = 0;
                    }

                    else if (forward > 0) {
                        mc.player.motionX += (sin * strictSpeed) / 50F;
                        mc.player.motionZ += (cos * strictSpeed) / 50F;
                    }

                    // lock limbs
                    mc.player.prevLimbSwingAmount = 0;
                    mc.player.limbSwingAmount = 0;
                    mc.player.limbSwing = 0;
                }

                // boosted vanilla flight
                else if (mode.getValue().equals(Mode.BOOST)) {

                    // idk
                    if (mc.player.motionY > -0.5) {
                        mc.player.fallDistance = 1;
                    }

                    // vector from rotations
                    Vec3d rotations = AngleUtil.getVectorForRotation(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch));

                    // motion based on rotations
                    float pitchScaled = mc.player.rotationPitch * 0.017453292F;
                    double rotationDistance = Math.sqrt(rotations.x * rotations.x + rotations.z * rotations.z);
                    double distance = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                    double length = rotations.lengthVector();
                    float pitchCos = MathHelper.cos(pitchScaled);
                    pitchCos = (float) ((double) pitchCos * (double) pitchCos * Math.min(1, length / 0.4D));

                    // vertical motion
                    mc.player.motionY += -0.08D + (double) pitchCos * 0.06D;

                    // fall speed
                    if (mc.player.motionY < 0 && rotationDistance > 0) {
                        double verticalSpeed = mc.player.motionY * -0.1D * (double) pitchCos;
                        mc.player.motionY += verticalSpeed;
                        mc.player.motionX += rotations.x * verticalSpeed / rotationDistance;
                        mc.player.motionZ += rotations.z * verticalSpeed / rotationDistance;
                    }

                    // up speed
                    if (pitchScaled < 0) {
                        double distanceSpeed = distance * (double) (-MathHelper.sin(pitchScaled)) * 0.04;
                        mc.player.motionY += distanceSpeed * 3.2;
                        mc.player.motionX -= rotations.x * distanceSpeed / rotationDistance;
                        mc.player.motionZ -= rotations.z * distanceSpeed / rotationDistance;
                    }

                    // horizontal speed
                    if (rotationDistance > 0) {
                        mc.player.motionX += (rotations.x / rotationDistance * distance - mc.player.motionX) * 0.1;
                        mc.player.motionZ += (rotations.z / rotationDistance * distance - mc.player.motionZ) * 0.1;
                    }

                    // drag
                    mc.player.motionX *= 0.9900000095367432D;
                    mc.player.motionY *= 0.9800000190734863D;
                    mc.player.motionZ *= 0.9900000095367432D;

                    // the current movement input values of the user
                    boolean forward = mc.player.movementInput.jump;

                    // motion based on rotations
                    float yawScaled = mc.player.rotationYaw * 0.017453292F;

                    // boost motion
                    if (forward) {
                        double sin = -Math.sin(yawScaled);
                        double cos = Math.cos(yawScaled);
                        mc.player.motionX += sin * speed.getValue() / 20F;
                        mc.player.motionZ += cos * speed.getValue() / 20F;
                    }

                    // lock limbs
                    mc.player.prevLimbSwingAmount = 0;
                    mc.player.limbSwingAmount = 0;
                    mc.player.limbSwing = 0;
                }

                // boosted vanilla flight (more vanilla)
                else if (mode.getValue().equals(Mode.FACTORIZE)) {

                    // idk
                    if (mc.player.motionY > -0.5) {
                        mc.player.fallDistance = 1;
                    }

                    // vector from rotations
                    Vec3d rotations = AngleUtil.getVectorForRotation(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch));

                    // motion based on rotations
                    float pitchScaled = mc.player.rotationPitch * 0.017453292F;
                    double rotationDistance = Math.sqrt(rotations.x * rotations.x + rotations.z * rotations.z);
                    double distance = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                    double length = rotations.lengthVector();
                    float pitchCos = MathHelper.cos(pitchScaled);
                    pitchCos = (float) ((double) pitchCos * (double) pitchCos * Math.min(1, length / 0.4D));

                    // vertical motion
                    mc.player.motionY += -0.08D + (double) pitchCos * 0.06D;

                    // fall speed
                    if (mc.player.motionY < 0 && rotationDistance > 0) {
                        double verticalSpeed = mc.player.motionY * -0.1D * (double) pitchCos;
                        mc.player.motionY += verticalSpeed;
                        mc.player.motionX += rotations.x * verticalSpeed / rotationDistance;
                        mc.player.motionZ += rotations.z * verticalSpeed / rotationDistance;
                    }

                    // up speed
                    if (pitchScaled < 0) {
                        double distanceSpeed = distance * (double) (-MathHelper.sin(pitchScaled)) * 0.04;
                        mc.player.motionY += distanceSpeed * 3.2;
                        mc.player.motionX -= rotations.x * distanceSpeed / rotationDistance;
                        mc.player.motionZ -= rotations.z * distanceSpeed / rotationDistance;
                    }

                    // horizontal speed
                    if (rotationDistance > 0) {
                        mc.player.motionX += (rotations.x / rotationDistance * distance - mc.player.motionX) * 0.1;
                        mc.player.motionZ += (rotations.z / rotationDistance * distance - mc.player.motionZ) * 0.1;
                    }

                    // drag
                    mc.player.motionX *= 0.9900000095367432D;
                    mc.player.motionY *= 0.9800000190734863D;
                    mc.player.motionZ *= 0.9900000095367432D;

                    // the current movement input values of the user
                    boolean forward = mc.player.movementInput.jump;

                    // motion based on rotations
                    float yawScaled = mc.player.rotationYaw * 0.017453292F;

                    // boost motion
                    if (forward && pitchScaled > 0 && mc.player.motionY <= 0) {

                        // reset
                        verticalTicks = 0;

                        // only boost for certain number of ticks
                        if (factorizeTicks <= factorizeTick.getValue()) {

                            // move
                            double sin = -Math.sin(yawScaled);
                            double cos = Math.cos(yawScaled);
                            mc.player.motionX += sin * factorizeBoost.getValue() / 20F;
                            mc.player.motionZ += cos * factorizeBoost.getValue() / 20F;
                            factorizeTicks++;
                        }
                    }

                    // update vertical ticks
                    else if (pitchScaled < 0) {
                        verticalTicks++;
                        factorizeTicks--;
                    }

                    // clear ticks
                    else {
                        factorizeTicks--;
                    }

                    // clamp
                    if (factorizeTicks < 0) {
                        factorizeTicks = 0;
                    }

                    // lock limbs
                    mc.player.prevLimbSwingAmount = 0;
                    mc.player.limbSwingAmount = 0;
                    mc.player.limbSwing = 0;
                }

                // factor in slowdown
                if (friction.getValue().equals(Friction.FACTOR)) {

                    // slowdown vector
                    Vec3d slowdown = getSlowdown(event.getStrafe(), event.getVertical(), event.getForward());

                    // total slowdown factor
                    double netSlowdown = slowdown.lengthSquared();
                    double netSlowdownSqrt = slowdown.lengthVector();

                    // update motion
                    if (netSlowdown >= 1.0E-4F || netSlowdownSqrt == 1) {
                        mc.player.motionX *= slowdown.x;
                        mc.player.motionY *= slowdown.y;
                        mc.player.motionZ *= slowdown.z;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // attempt to "re-fly"
        if (mode.getValue().equals(Mode.CONTROL_STRICT)) {

            // falling
            if (!mc.player.onGround && !mc.player.isElytraFlying() && !mc.player.capabilities.isFlying) {

                // stop in water and liquids
                if (friction.getValue().equals(Friction.CUTOFF)) {

                    // make sure the player is not in a liquid
                    if (PlayerUtil.isInLiquid()) {
                        return;
                    }

                    // make sure the player is not in a web
                    if (((IEntity) mc.player).getInWeb()) {
                        return;
                    }
                }

                // slowdown and send packet
                event.setCanceled(true);
                event.setY(0);
                event.setX(0);
                event.setZ(0);
                mc.player.motionY = 0;

                // takeoff
                if (fallFlyTimer.passedTime(500, Format.MILLISECONDS)) {

                    // fall packet
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    fallFlyTimer.resetTime();
                }
            }
        }

        // fly with packets
        else if (mode.getValue().equals(Mode.PACKET)) {

            // make sure the player is not in a liquid
            if (PlayerUtil.isInLiquid()) {
                return;
            }

            // make sure the player is not in a web
            if (((IEntity) mc.player).getInWeb()) {
                return;
            }

            // wearing elytra & off ground, we don't need to actually be elytra flying for this
            if (!mc.player.onGround && isWearingElytra()) {

                // override motion
                event.setCanceled(true);
                event.setY(0);
                mc.player.motionY = 0;

                // make sure the player is moving
                if (MotionUtil.isMoving()) {

                    // accelerate to speed
                    if (accelerate.getValue()) {

                        // increment
                        if (moveSpeed < speed.getValue()) {

                            // acceleration factor: 0.1 (might change in the future)
                            moveSpeed += 0.1;
                        }

                        // clamp
                        if (moveSpeed > speed.getValue()) {
                            moveSpeed = speed.getValue();
                        }
                    }

                    // instant motion
                    else {
                        moveSpeed = speed.getValue();
                    }

                    // old 2b bypass
                    if (strict.getValue().equals(Strict.OLD)) {

                        // fall for anti kick
                        mc.player.motionY = -2.0E-4;

                        // scale y position
                        if (mc.player.ticksExisted % 32 == 0) {
                            event.setY(0.006200000000000001);
                        }

                        else {
                            event.setY(-2.0E-4);
                        }
                    }

                    // horizontal motion
                    mc.player.motionX = event.getX() * moveSpeed;
                    mc.player.motionZ = event.getZ() * moveSpeed;
                    event.setX(event.getX() * moveSpeed);
                    event.setZ(event.getZ() * moveSpeed);
                }

                // no motion if we are not inputting
                else {
                    event.setX(0);
                    event.setZ(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {

        // fly with packets
        if (mode.getValue().equals(Mode.PACKET)) {

            // wearing elytra & off ground, we don't need to actually be elytra flying for this
            if (!mc.player.onGround && isWearingElytra()) {

                // manual
                if (!mc.player.movementInput.jump) {

                    // ??? idk
                    if (infiniteDurability.getValue()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for moving and rotations
        if (event.getPacket() instanceof CPacketPlayer) {

            // packet has motion
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                // fly with packets
                if (mode.getValue().equals(Mode.PACKET)) {

                    // wearing elytra & off ground, we don't need to actually be elytra flying for this
                    if (!mc.player.onGround && isWearingElytra()) {

                        // bypass for 2b
                        if (strict.getValue().equals(Strict.NEW)) {

                            // only apply if moving
                            if (MotionUtil.isMoving()) {

                                // random factor
                                double factor = 1.0E-8 + 1.0E-8 * (1 + random.nextInt(1 + (random.nextBoolean() ? random.nextInt(34) : random.nextInt(43))));

                                // y position of the packet
                                double packetY = ((CPacketPlayer) event.getPacket()).getY(mc.player.posY);

                                // direction
                                factor *= (mc.player.onGround || mc.player.ticksExisted % 2 == 0) ? 1 : -1;

                                // update y
                                ((ICPacketPlayer) event.getPacket()).setY(packetY + factor);
                            }
                        }
                    }
                }
            }

            // packet has rotations
            if (((ICPacketPlayer) event.getPacket()).isRotating()) {

                // check if the player is elytra flying
                if (mc.player.isElytraFlying()) {

                    // check mode
                    if (mode.getValue().equals(Mode.CONTROL)) {

                        // stop in water and liquids
                        if (friction.getValue().equals(Friction.CUTOFF)) {

                            // make sure the player is not in a liquid
                            if (PlayerUtil.isInLiquid()) {
                                return;
                            }

                            // make sure the player is not in a web
                            if (((IEntity) mc.player).getInWeb()) {
                                return;
                            }
                        }

                        // spoof pitch
                        ((ICPacketPlayer) event.getPacket()).setPitch(pitch);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
            if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
                return;
            }

            // rubberband packet
            if (event.getPacket() instanceof SPacketPlayerPosLook) {

                // no rotate
                ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);

                // must account for rubberbands
                // reset
                factorizeTicks = 0;
                moveSpeed = 0;
                strictSpeed = baseSpeed.getValue();

                // check if the player is elytra flying
                if (mc.player.isElytraFlying()) {

                    // stop in water and liquids
                    if (friction.getValue().equals(Friction.CUTOFF) || mode.getValue().equals(Mode.PACKET)) {

                        // make sure the player is not in a liquid
                        if (PlayerUtil.isInLiquid()) {
                            return;
                        }

                        // make sure the player is not in a web
                        if (((IEntity) mc.player).getInWeb()) {
                            return;
                        }
                    }

                    // use firework when we rubberband
                    if (firework.getValue()) {

                        // log previous slot, we'll switch back to this item
                        int previousSlot = mc.player.inventory.currentItem;

                        // firework slot
                        int fireworkSlot = getCosmos().getInventoryManager().searchSlot(Items.FIREWORKS, InventoryRegion.HOTBAR);

                        // found firework
                        if (fireworkSlot != -1) {

                            // switch to firework
                            getCosmos().getInventoryManager().switchToSlot(fireworkSlot, Switch.NORMAL);

                            // use firework
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

                            // switch back
                            if (previousSlot != -1) {
                                getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.NORMAL);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {

        // cancel rotation renders when flying
        if (mc.player.isElytraFlying()) {

            // check mode
            if (mode.getValue().equals(Mode.CONTROL)) {

                // show real pitch
                event.setCanceled(true);
                event.setPitch(pitch);
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && mc.player.isElytraFlying();
    }

    /**
     * Gets the frictional slowdown for flight
     * @param strafe Strafe movement
     * @param vertical Vertical movement
     * @param forward Forward movement
     * @return The slowdown of flight
     */
    public Vec3d getSlowdown(float strafe, float vertical, float forward) {

        // water slowdown
        if (mc.player.isInWater()) {

            // previous y position
            double previousY = mc.player.posY;
            
            // slowdown factors
            float waterSlowdown = ((IEntityLivingBase) mc.player).hookGetWaterSlowDown();
            float depthStriderModifier = (float) EnchantmentHelper.getDepthStriderModifier(mc.player);

            // water friction
            float friction = 0.02F;

            // clamp
            if (depthStriderModifier > 3) {
                depthStriderModifier = 3;
            }

            // factor changes in air
            if (!mc.player.onGround) {
                depthStriderModifier *= 0.5F;
            }

            // scale by depth strider
            if (depthStriderModifier > 0) {
                waterSlowdown += (0.54600006F - waterSlowdown) * depthStriderModifier / 3;
                friction += (mc.player.getAIMoveSpeed() - friction) * depthStriderModifier / 3;
            }

            // update motion 
            Vec3d movement = moveRelative(strafe, vertical, forward, friction);
            // mc.player.move(MoverType.SELF, movement.x, movement.y, movement.z);
            // mc.player.motionX *= waterSlowdown;
            // mc.player.motionY *= 0.800000011920929;
            // mc.player.motionZ *= waterSlowdown;

            // fall/drag
            if (!mc.player.hasNoGravity()) {
                mc.player.motionY -= 0.02;
            }

            // if (mc.player.collidedHorizontally && mc.player.isOffsetPositionInLiquid(movement.x, movement.y + 0.6000000238418579 - mc.player.posY + previousY, movement.z)) {
            //    mc.player.motionY = 0.30000001192092896;
            // }
            
            // slowdown
            return new Vec3d(waterSlowdown, 0.800000011920929, waterSlowdown);
        }

        // lava slowdown
        else if (mc.player.isInLava()) {

            // previous y position
            double previousY = mc.player.posY;

            // update motion
            Vec3d movement = moveRelative(strafe, vertical, forward, 0.02F);
            // mc.player.move(MoverType.SELF, movement.x, movement.y, movement.z);
            // mc.player.motionX *= 0.5;
            // mc.player.motionY *= 0.5;
            // mc.player.motionZ *= 0.5;

            // fall/drag
            if (!mc.player.hasNoGravity()) {
                mc.player.motionY -= 0.02;
            }

            // if (mc.player.collidedHorizontally && mc.player.isOffsetPositionInLiquid(movement.x, movement.y + 0.6000000238418579 - mc.player.posY + previousY, movement.z)) {
            //    mc.player.motionY = 0.30000001192092896;
            // }

            // slowdown
            return new Vec3d(0.5, 0.5, 0.5);
        }

        // no slowdown
        return new Vec3d(1, 1, 1);
    }

    /**
     * Recreation of vanilla "moveRelative" function
     * @param strafe Strafe movement
     * @param up Vertical movement
     * @param forward Forward movement
     * @param friction Friction slowing movement
     */
    public Vec3d moveRelative(float strafe, float up, float forward, float friction) {

        // total movement
        float movement = strafe * strafe + up * up + forward * forward;

        // check if player is moving
        if (movement >= 1.0E-4F) {

            // movement distance
            movement = MathHelper.sqrt(movement);

            // clamp
            if (movement < 1) {
                movement = 1;
            }

            // apply friction
            movement = friction / movement;
            strafe = strafe * movement;
            up = up * movement;
            forward = forward * movement;

            // apply swim modifiers
            if (mc.player.isInWater() || mc.player.isInLava()) {
                strafe = strafe * (float) mc.player.getEntityAttribute(EntityLivingBase.SWIM_SPEED).getAttributeValue();
                up = up * (float) mc.player.getEntityAttribute(EntityLivingBase.SWIM_SPEED).getAttributeValue();
                forward = forward * (float) mc.player.getEntityAttribute(EntityLivingBase.SWIM_SPEED).getAttributeValue();
            }

            // yaw radians
            float yawScaled = mc.player.rotationYaw * 0.017453292F;

            // rotations
            float sin = MathHelper.sin(yawScaled);
            float cos = MathHelper.cos(yawScaled);

            // move relative
            double motionX = mc.player.motionX + (strafe * cos - forward * sin);
            double motionY = mc.player.motionY + up;
            double motionZ = mc.player.motionZ + (forward * cos + strafe * sin);

            // updated motion
            return new Vec3d(motionX, motionY, motionZ);
        }

        // player motion
        return new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
    }

    /**
     * Checks whether the player is wearing an elytra
     * @return Whether the player is wearing an elytra
     */
    public boolean isWearingElytra() {
        return mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem().equals(Items.ELYTRA);
    }

    public enum Mode {

        /**
         * Allows full creative flight
         */
        CONTROL,

        /**
         * Control flight with remount to bypass NCP Updated (2b bypass as of 10/02/2022)
         */
        CONTROL_STRICT,

        /**
         * Boosted vanilla flight
         */
        BOOST,

        /**
         * Boosted vanilla with movements closer to vanilla elytra flying
         */
        FACTORIZE,

        /**
         * Flight with packets
         */
        PACKET
    }

    public enum Strict {

        /**
         * New 2b2t bypass
         */
        NEW,

        /**
         * Old 2b2t bypass
         */
        OLD,

        /**
         * Doesn't attempt to bypass
         */
        NONE
    }

    public enum Vertical {

        /**
         * Allows full control when moving vertically
         */
        CONTROL,

        /**
         * Allows upward motion when moving horizontally
         */
        GLIDE,

        /**
         * Does not allow vertical movement
         */
        NONE
    }

    public enum Friction {

        /**
         * Silently scales speed for various slowdowns
         */
        FACTOR,

        /**
         * Pause flight
         */
        CUTOFF,

        /**
         * No friction
         */
        NONE
    }
}
