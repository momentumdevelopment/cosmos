package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.DeathEvent;
import cope.cosmos.client.events.DisconnectEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class FakePlayer extends Module {
	public static FakePlayer INSTANCE;
	
	public FakePlayer() {
        super("FakePlayer", Category.MISC, "Spawns in a indestructible client-side player");
        INSTANCE = this;
        setExempt(true);
    }

    public static Setting<Boolean> inventory = new Setting<>("Inventory", true).setDescription("Sync the fake player inventory");
    public static Setting<Boolean> health = new Setting<>("Health", true).setDescription("Sync the fakeplayer health");

    // entity id of fakeplayer
    private int id = -1;

    @Override
    public void onEnable() {
        super.onEnable();

        // create a fake player
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

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
    public void onDeath(DeathEvent event) {
        if (event.getEntity().equals(mc.player)) {
            // remove fake player from world
            mc.world.removeEntityFromWorld(id);
            id = -1;

            // disable module
            disable();
        }
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {
        // remove fake player from world
        mc.world.removeEntityFromWorld(id);
        id = -1;

        // disable module
        disable();
    }
}
