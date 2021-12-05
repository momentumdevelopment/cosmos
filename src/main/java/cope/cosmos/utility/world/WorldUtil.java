package cope.cosmos.utility.world;

import com.mojang.authlib.GameProfile;
import cope.cosmos.utility.IUtility;
import net.minecraft.client.entity.EntityOtherPlayerMP;

public class WorldUtil implements IUtility {

    public static void createFakePlayer(GameProfile gameProfile, int fakeID, boolean inventory, boolean health) {
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, gameProfile);
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYaw;

        if (inventory)
            fakePlayer.inventory.copyInventory(mc.player.inventory);

        if (health) {
            fakePlayer.setHealth(mc.player.getHealth());
            fakePlayer.setAbsorptionAmount(mc.player.getAbsorptionAmount());
        }

        mc.world.addEntityToWorld(fakeID, fakePlayer);
    }
}
