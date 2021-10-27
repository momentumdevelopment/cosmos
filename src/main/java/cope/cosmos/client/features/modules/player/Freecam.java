package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freecam extends Module {
    private EntityOtherPlayerMP fake = null;
    private BlockPos origin = null;

    public static final Setting<Double> speed = new Setting<>("Speed", "How fast to move around", 0.1, 2.0, 10.0, 1);

    public Freecam() {
        super("Freecam", Category.PLAYER, "Allows you to freely move your camera through blocks");
    }

    @Override
    public void onEnable() {
        if (!nullCheck()) {
            this.disable();
            return;
        }

        this.origin = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        this.fake = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
        this.fake.copyLocationAndAnglesFrom(mc.player);
        this.fake.inventory.copyInventory(mc.player.inventory);

        mc.world.spawnEntity(this.fake);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            if (this.fake != null) {
                mc.world.removeEntity(this.fake);
                mc.world.removeEntityDangerously(this.fake);
            }

            this.fake = null;

            mc.player.setPosition(this.origin.getX(), this.origin.getY(), this.origin.getZ());
            mc.player.capabilities.isFlying = false;
            mc.player.noClip = false;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUnload(WorldEvent.Unload event) {
        this.disable();
    }

    @Override
    public void onUpdate() {
        mc.player.capabilities.isFlying = true;
        mc.player.noClip = true;

        double[] movement = MotionUtil.getMoveSpeed(speed.getValue() / 10.0);
        mc.player.motionX = movement[0];
        mc.player.motionZ = movement[1];

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.posY += speed.getValue();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.posY -= speed.getValue();
        }
    }
}
