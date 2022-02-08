package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.ISPacketPlayerPosLook;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.motion.movement.MotionUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.motion.movement.PushOutOfBlocksEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.player.MotionUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class PacketFlight extends Module {
	public static PacketFlight INSTANCE;

	public PacketFlight() {
		super("PacketFlight", Category.MOVEMENT, "Fly with packet exploit.", () -> StringFormatter.formatEnum(mode.getValue()));
		INSTANCE = this;
	}

	public static Setting<Mode> mode = new Setting<>("Mode", Mode.FAST).setDescription("Mode for PacketFlight");
	public static Setting<Direction> direction = new Setting<>("Direction", Direction.DOWN).setDescription("Direction of the bounds packets");
	public static Setting<Double> factor = new Setting<>("Factor", 0.0, 1.0, 5.0, 1).setDescription("Speed factor").setVisible(() -> mode.getValue().equals(Mode.FACTOR));
	public static Setting<Double> subdivisions = new Setting<>("Subdivisions", 0.0, 4.0, 10.0, 0).setDescription("How many rotations packets to send").setVisible(() -> mode.getValue().equals(Mode.STRICT));
	public static Setting<Boolean> antiKick = new Setting<>("AntiKick", true).setDescription("Prevents getting kicked by vanilla anti-cheat");
	public static Setting<Boolean> limitJitter = new Setting<>("LimitJitter", true).setDescription("Proactively confirms packets");
	public static Setting<Boolean> overshoot = new Setting<>("Overshoot", false).setDescription("Slightly overshoots the packet positions");
	public static Setting<Boolean> stabilize = new Setting<>("Stabilize", true).setDescription("Ignores server position and rotation requests");

	private final ConcurrentSet<CPacketPlayer> safePackets = new ConcurrentSet<>();
	private final ConcurrentHashMap<Integer, Vec3d> vectorMap = new ConcurrentHashMap<>();

	int lastTeleportId;

	// client packet data
	float clientYaw;
	float clientPitch;
	double clientX;
	double clientY;
	double clientZ;

	// server packet data
	float serverYaw;
	float serverPitch;
	double serverX;
	double serverY;
	double serverZ;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMotionUpdate(MotionUpdateEvent event) {
		if (nullCheck()) {
			mc.player.setVelocity(0, 0, 0);

			double[] motion = getMotion(isPlayerClipped() ? (mode.getValue().equals(Mode.FACTOR) ? factor.getValue() : 1) : 1);
			mc.player.motionX = motion[0];
			mc.player.motionY = motion[1];
			mc.player.motionZ = motion[2];
			mc.player.setVelocity(motion[0], motion[1], motion[2]);
			mc.player.noClip = true;

			processPackets(new double[] {
					motion[0], motion[1], motion[2]
			});
		}
	}

	@SubscribeEvent
	public void onMove(MotionEvent event) {
		if (nullCheck()) {
			event.setCanceled(true);

			double[] motion = getMotion(isPlayerClipped() ? (mode.getValue().equals(Mode.FACTOR) ? factor.getValue() : 1) : 1);
			event.setX(motion[0]);
			event.setY(motion[1]);
			event.setZ(motion[2]);
			mc.player.setVelocity(motion[0], motion[1], motion[2]);
			mc.player.noClip = true;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPacketSend(PacketEvent.PacketSendEvent event) {
		if (event.getPacket() instanceof CPacketPlayer) {
			CPacketPlayer packet = (CPacketPlayer) event.getPacket();

			if (((ICPacketPlayer) packet).isMoving() || ((ICPacketPlayer) packet).isRotating()) {
				event.setCanceled(!safePackets.contains(packet));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
		if (nullCheck()) {
			clientYaw = mc.player.rotationYaw;
			clientPitch = mc.player.rotationPitch;
			clientX = mc.player.posX;
			clientY = mc.player.getEntityBoundingBox().minY;
			clientZ = mc.player.posZ;

			if (event.getPacket() instanceof SPacketPlayerPosLook) {
				SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
				Vec3d packetVector = vectorMap.remove(packet.getTeleportId());

				if (mode.getValue().equals(Mode.FAST) && packetVector != null && packetVector.x == packet.getX() && packetVector.y == packet.getY() && packetVector.z == packet.getZ()) {
					event.setCanceled(stabilize.getValue());
					return;
				}

				serverYaw = packet.getYaw();
				serverPitch = packet.getPitch();
				serverX = packet.getX();
				serverY = packet.getY();
				serverZ = packet.getZ();

				// update our packet values
				{
					if (packet.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
						serverX += mc.player.posX;
					}

					else {
						mc.player.motionX = 0;
					}

					if (packet.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
						serverY += mc.player.posY;
					}

					else {
						mc.player.motionY = 0;
					}

					if (packet.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
						serverZ += mc.player.posZ;
					}

					else {
						mc.player.motionZ = 0;
					}

					if (packet.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
						serverPitch += mc.player.rotationPitch;
					}

					if (packet.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
						serverYaw += mc.player.rotationYaw;
					}
				}

				if (mode.getValue().equals(Mode.STRICT)) {
					event.setCanceled(stabilize.getValue());

					// teleport us back to the server values
					mc.player.setPosition(serverX, serverY, serverZ);

					if (limitJitter.getValue()) {
						mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
					}

					CPacketPlayer.PositionRotation serverPacket = new CPacketPlayer.PositionRotation(serverX, serverY, serverZ, serverYaw, serverPitch, false);
					safePackets.add(serverPacket);
					mc.player.connection.sendPacket(serverPacket);
				}

				else {
					((ISPacketPlayerPosLook) packet).setYaw(clientYaw);
					((ISPacketPlayerPosLook) packet).setPitch(clientPitch);
					((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.X_ROT);
					((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.Y_ROT);
				}

				lastTeleportId = packet.getTeleportId();
			}
		}
	}

	@SubscribeEvent
	public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
		event.setCanceled(true);
	}

	private void processPackets(double[] motion) {
		Vec3d increment = new Vec3d(motion[0], motion[1], motion[2]);
		Vec3d playerIncrement = mc.player.getPositionVector().add(increment);
		Vec3d bounded = getBoundingVectors(increment, playerIncrement);

		CPacketPlayer.PositionRotation legit = new CPacketPlayer.PositionRotation(playerIncrement.x + (overshoot.getValue() ? ThreadLocalRandom.current().nextDouble(-1, 1) : 0), playerIncrement.y + (overshoot.getValue() ? ThreadLocalRandom.current().nextDouble(-1, 1) : 0), playerIncrement.z + (overshoot.getValue() ? ThreadLocalRandom.current().nextDouble(-1, 1) : 0), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround);
		CPacketPlayer.PositionRotation bounds = new CPacketPlayer.PositionRotation(bounded.x, direction.getValue().equals(Direction.GROUND) ? 0 : bounded.y, bounded.z, mc.player.rotationYaw, mc.player.rotationPitch, !direction.getValue().equals(Direction.GROUND) || mc.player.onGround);

		safePackets.add(legit);
		safePackets.add(bounds);

		mc.player.connection.sendPacket(legit);
		mc.player.connection.sendPacket(bounds);

		if (!vectorMap.containsKey(lastTeleportId)) {
			if (limitJitter.getValue()) {
				mc.player.connection.sendPacket(new CPacketConfirmTeleport(lastTeleportId++));
			}

			mc.player.setPosition(playerIncrement.x, playerIncrement.y, playerIncrement.z);
			vectorMap.put(lastTeleportId, playerIncrement);
		}
	}

	private double[] getMotion(double factor) {
		double motionY = getMotionY();
		double speed;

		if (motionY != 0) {
			speed = 0.026;
		}

		else {
			speed = 0.040;
		}

		double[] motion = MotionUtil.getMoveSpeed(motionY != 0 ? speed : speed * factor);

		if (!isPlayerClipped()) {
			if (motionY != 0 && motionY != -0.0325) {
				motion[0] = 0;
				motion[1] = 0;
			}

			else {
				if (mode.getValue().equals(Mode.STRICT)) {
					motion[0] *= 2.3425;
					motion[1] *= 2.3425;
				}

				else {
					motion[0] *= 3.59125;
					motion[1] *= 3.59125;
				}
			}
		}

		else {
			if (mode.getValue().equals(Mode.STRICT)) {
				motion[0] *= 0.8;
				motion[1] *= 0.8;
			}

			else if (mode.getValue().equals(Mode.SLOW)) {
				motion[0] *= 0.75;
				motion[1] *= 0.75;
			}
		}

		return new double[] {
				motion[0], motionY, motion[1]
		};
	}

	private double getMotionY() {
		double motionY = 0;

		if (!isPlayerClipped()) {
			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				motionY = 0.031;

				if (mc.player.ticksExisted % 18 == 0 && antiKick.getValue())
					motionY = -0.04;
			}

			if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown())
				motionY = -0.031;
		}

		else {
			if (mc.gameSettings.keyBindJump.isKeyDown())
				motionY = 0.017;

			if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown())
				motionY = -0.017;
		}

		if (motionY == 0 && !isPlayerClipped()) {
			if (mc.player.ticksExisted % 4 == 0 && antiKick.getValue())
				motionY = -0.0325;
		}

		return motionY;
	}

	private Vec3d getBoundingVectors(Vec3d actual, Vec3d bound) {
		Vec3d newVector = actual.add(bound);

		switch (direction.getValue()) {
			case UP:
				return newVector.addVector(0, 80085.69, 0);
			case DOWN:
			default:
				return newVector.addVector(0, -80085.69, 0);
			case RANDOM:
				return newVector.addVector(0, ThreadLocalRandom.current().nextDouble(-80085.69, 80085.69), 0);
			case GROUND:
			case NONE:
				return newVector.addVector(0, 0, 0);
		}
	}

	private boolean isPlayerClipped() {
		return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().contract(0.125, 0.15, 0.125)).isEmpty();
	}

	public enum Mode {
		FAST, FACTOR, SLOW, STRICT
	}

	public enum Direction {
		UP, DOWN, GROUND, RANDOM, NONE
	}
}
