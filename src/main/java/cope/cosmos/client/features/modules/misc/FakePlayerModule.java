package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MoverType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linustouchtips
 * @since 06/08/2021
 * @author EBSmash
 * @since 7/25/22
 * noatmc/delta used for reference
 */
public class FakePlayerModule extends Module {
	public static FakePlayerModule INSTANCE;
	
	public FakePlayerModule() {
        super("FakePlayer", Category.MISC, "Spawns in a indestructible client-side player");
        INSTANCE = this;
        setExempt(true);
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> inventory = new Setting<>("Inventory", true)
            .setDescription("Sync the fake player inventory");

    public static Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Sync the fake player health");

    public static Setting<Boolean> move = new Setting<>("Move", true)
            .setDescription("Allows the fake player to move");

    // entity id of fakeplayer
    private int id = -1;

    // create a fake player
    private EntityOtherPlayerMP fakePlayer;

    private Random random = new Random();

    @Override
    public void onEnable() {
        super.onEnable();


        fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

        // copy rotations from player
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYaw;

        // copy inventory from player
        if (inventory.getValue()) {
            fakePlayer.inventory.copyInventory(mc.player.inventory);
            fakePlayer.inventoryContainer = mc.player.inventoryContainer;
        }

        // copy health from player
        if (health.getValue()) {
            fakePlayer.setHealth(mc.player.getHealth());
            fakePlayer.setAbsorptionAmount(mc.player.getAbsorptionAmount());
        }

        // set player traits
        fakePlayer.setSneaking(mc.player.isSneaking());
        fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

        // add the fake player to world
        id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        mc.world.addEntityToWorld(id, fakePlayer);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove fake player from world
        mc.world.removeEntityFromWorld(id);
        id = -1;
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        if (event.getEntity().equals(mc.player)) {

            // remove fake player from world
            mc.world.removeEntityFromWorld(id);
            id = -1;

            // disable module
            disable(true);
        }
    }

    @Override
    public void onUpdate() {
        if (fakePlayer != null) {
            fakePlayer.moveForward = mc.player.moveForward + (random.nextInt(5) / 10F);
            fakePlayer.moveStrafing = mc.player.moveStrafing + (random.nextInt(5) / 10F);
            if (move.getValue()) {
                travel(fakePlayer.moveStrafing, fakePlayer.moveVertical, fakePlayer.moveForward);
            }
        }
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {

        // disable module
        disable(true);
    }


    public void travel(float strafe, float vertical, float forward) {
        double d0 = fakePlayer.posY;
        float f1 = 0.8F;
        float f2 = 0.02F;
        float f3 = (float) EnchantmentHelper.getDepthStriderModifier(fakePlayer);

        if (f3 > 3.0F) {
            f3 = 3.0F;
        }

        if (!fakePlayer.onGround) {
            f3 *= 0.5F;
        }

        if (f3 > 0.0F) {
            f1 += (0.54600006F - f1) * f3 / 3.0F;
            f2 += (fakePlayer.getAIMoveSpeed() - f2) * f3 / 4.0F;
        }

        fakePlayer.moveRelative(strafe, vertical, forward, f2);
        fakePlayer.move(MoverType.SELF, fakePlayer.motionX, fakePlayer.motionY, fakePlayer.motionZ);
        fakePlayer.motionX *= f1;
        fakePlayer.motionY *= 0.800000011920929D;
        fakePlayer.motionZ *= f1;

        if (!fakePlayer.hasNoGravity()) {
            fakePlayer.motionY -= 0.02D;
        }

        if (fakePlayer.collidedHorizontally && fakePlayer.isOffsetPositionInLiquid(fakePlayer.motionX, fakePlayer.motionY + 0.6000000238418579D - fakePlayer.posY + d0, fakePlayer.motionZ)) {
            fakePlayer.motionY = 0.30000001192092896D;
        }
    }
}
