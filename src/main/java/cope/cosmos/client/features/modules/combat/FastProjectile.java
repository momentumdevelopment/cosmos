package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.*;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class FastProjectile extends Module {
    public static FastProjectile INSTANCE;

    public FastProjectile() {
        super("FastProjectile", Category.COMBAT, "Allows your projectiles to do more damage", () -> (projectileTimer.passed(5000, Format.SYSTEM) ? "Charged" : "Charging"));
        INSTANCE = this;
    }
    
    public static Setting<Boolean> bows = new Setting<>("Bows", "Allow bows to do more damage", false);
    public static Setting<Boolean> eggs = new Setting<>("Eggs", "Allow eggs to do more damage", false);
    public static Setting<Boolean> snowballs = new Setting<>("Snowballs", "Allow snowballs to do more damage", false);
    public static Setting<Double> ticks = new Setting<>("Ticks", "How many times to send packets", 1.0D, 10.0D, 50.0D, 0);

    private static final Timer projectileTimer = new Timer();
    
    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) event.getPacket()).getAction().equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
            if (InventoryUtil.isHolding(Items.BOW) && bows.getValue() || InventoryUtil.isHolding(Items.EGG) && eggs.getValue() || InventoryUtil.isHolding(Items.SNOWBALL) && snowballs.getValue()) {
                if(projectileTimer.passed(5000, Format.SYSTEM)) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

                Random projectileRandom = new Random();
                for (int tick = 0; tick < ticks.getValue(); tick++) {
                    double sin = -Math.sin(Math.toRadians(mc.player.rotationYaw));
                    double cos = Math.cos(Math.toRadians(mc.player.rotationYaw));

                    if (projectileRandom.nextBoolean()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (sin * 100), mc.player.posY, mc.player.posZ + (cos * 100), false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - (sin * 100), mc.player.posY, mc.player.posZ - (cos * 100), true));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - (sin * 100), mc.player.posY, mc.player.posZ - (cos * 100), true));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (sin * 100), mc.player.posY, mc.player.posZ + (cos * 100), false));
                    }
                }
                    projectileTimer.reset();
                }
                
                
            }
        }
    }
}
