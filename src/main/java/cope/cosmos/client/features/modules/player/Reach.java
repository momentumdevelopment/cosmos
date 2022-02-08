package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.entity.hitbox.EntityHitboxSizeEvent;
import cope.cosmos.client.events.entity.player.interact.ReachEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/31/2021
 */
public class Reach extends Module {
    public static Reach INSTANCE;

    public Reach() {
        super("Reach", Category.PLAYER, "Extends your reach");
        INSTANCE = this;
    }

    public static Setting<Double> reach = new Setting<>("Reach", 0.0, 0.0, 3.0, 2).setDescription("Player reach extension");

    // hitbox interactions
    public static Setting<Boolean> hitBox = new Setting<>("HitBox", true).setDescription("Ignores entity hitboxes");
    public static Setting<Double> hitBoxExtend = new Setting<>("Extend", 0.0, 0.0, 2.0, 2).setDescription("Entity hitbox extension").setVisible(() -> !hitBox.getValue()).setParent(hitBox);
    public static Setting<Boolean> hitBoxPlayers = new Setting<>("PlayersOnly", true).setDescription("Only ignores player hitboxes").setVisible(() -> hitBox.getValue()).setParent(hitBox);

    @Override
    public void onUpdate() {
        // ignore entity hitboxes
        if (hitBox.getValue()) {

            // mining at an entity hitbox
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                // check we are mining at a player hitbox
                if (hitBoxPlayers.getValue() && !(mc.objectMouseOver.entityHit instanceof EntityPlayer)) {
                    return;
                }

                // raytrace to player look at
                RayTraceResult mineResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

                // check if it's valid mine
                if (mineResult != null && mineResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                    // position of the mine
                    BlockPos minePos = mineResult.getBlockPos();

                    // damage block
                    if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                        mc.playerController.onPlayerDamageBlock(minePos, EnumFacing.UP);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onHitboxSize(EntityHitboxSizeEvent event) {
        if (!hitBox.getValue()) {
            // set hitbox size if we allow hitboxes
            event.setHitboxSize(hitBoxExtend.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onReach(ReachEvent event) {
        // add reach on top of vanilla reach
        event.setReach((mc.player.capabilities.isCreativeMode ? 5 : 4.5F) + reach.getValue().floatValue());
    }
}
