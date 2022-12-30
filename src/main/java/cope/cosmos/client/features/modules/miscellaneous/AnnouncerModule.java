package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.events.block.BlockBreakEvent;
import cope.cosmos.client.events.item.ItemUseFinishEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * @author linustouchtips
 * @since 10/25/2022
 */
public class AnnouncerModule extends Module {
    public static AnnouncerModule INSTANCE;

    public AnnouncerModule() {
        super("Announcer", Category.MISCELLANEOUS, "Announces various actions in chat");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> delay = new Setting<>("Delay", 0.0D, 1.0D, 5.0D, 0)
            .setDescription("Delay between messages");

    public static Setting<Boolean> joinLeave = new Setting<>("JoinLeave", true)
            .setAlias("Login", "Logout")
            .setDescription("Notifies players when another player joins or leaves");

    public static Setting<Boolean> eat = new Setting<>("Eat", false)
            .setDescription("Notifies players when eating");

    public static Setting<Boolean> block = new Setting<>("Blocks", false)
            .setAlias("Place")
            .setDescription("Notifies players when placing blocks");

    public static Setting<Boolean> broke = new Setting<>("Break", false)
            .setDescription("Notifies players when breaking blocks");

    public static Setting<Boolean> attack = new Setting<>("Attack", false)
            .setDescription("Notifies players when attacking entities");

    public static final String[] join = {
            "Hey there ",
            "Hi ",
            "Howdy ",
            "Greetings ",
            "Welcome ",
            "How do you do ",
            "Hello ",
            "Cheers "
    };

    public static final String[] leave = {
            "See ya ",
            "Later ",
            "Bye ",
            "Till next time ",
            "Take care "
    };

    // timer for sent messages to prevent spam
    private final Timer joinLeaveTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer foodTimer = new Timer();
    private final Timer attackTimer = new Timer();

    // actions since last message sent
    private int blocks;
    private int eats;
    private int breaks;

    @Override
    public void onEnable() {
        super.onEnable();
        blocks = 0;
        eats = 0;
        breaks = 0;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for placing blocks
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && InventoryUtil.isHolding(ItemBlock.class)) {

            // update placed blocks
            blocks++;

            // notify
            if (block.getValue()) {

                // make sure the messages are delayed
                if (placeTimer.passedTime(10 * delay.getValue().longValue(), Format.SECONDS)) {

                    // announce
                    getCosmos().getChatManager().sendClientMessage("I just placed " + blocks + " blocks!");

                    // reset
                    placeTimer.resetTime();
                    blocks = 0;
                }
            }
        }

        // attack packet
        if (event.getPacket() instanceof CPacketUseEntity) {

            // attack entity
            Entity entity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // entity is invalid
            if (entity == null || entity.equals(mc.player) || entity.getEntityId() < 0 || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                return;
            }
            
            // ignore attacked crystals
            if (entity instanceof EntityEnderCrystal) {
                return;
            }

            // notify
            if (attack.getValue()) {

                // make sure the messages are delayed
                if (attackTimer.passedTime(10 * delay.getValue().longValue(), Format.SECONDS)) {

                    // weapon name
                    String weapon = mc.player.getHeldItemMainhand().getDisplayName();

                    // announce
                    getCosmos().getChatManager().sendClientMessage("I just attacked " + entity.getName() + " with " + weapon + "!");

                    // reset
                    attackTimer.resetTime();
                }
            }
        }

        if (nullCheck()) {

            // packet for player list changes
            if (event.getPacket() instanceof SPacketPlayerListItem) {

                // notify
                if (joinLeave.getValue()) {

                    // player data
                    for (SPacketPlayerListItem.AddPlayerData data : ((SPacketPlayerListItem) event.getPacket()).getEntries()) {

                        // check that the player exists
                        if (data.getProfile().getName() != null && !data.getProfile().getName().isEmpty() || data.getProfile().getId() != null) {

                            // player
                            EntityPlayer player = mc.world.getPlayerEntityByUUID(data.getProfile().getId());

                            // check if the player exists
                            if (player != null) {

                                // make sure the messages are delayed
                                if (joinLeaveTimer.passedTime(4 * delay.getValue().longValue(), Format.SECONDS)) {

                                    // random message
                                    Random random = new Random();

                                    // player join
                                    if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.ADD_PLAYER)) {

                                        // join message
                                        String message = join[random.nextInt(join.length - 1)];

                                        // announce
                                        getCosmos().getChatManager().sendClientMessage(message + player.getName() + "!");
                                    }

                                    // player disconnect
                                    else if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER)) {

                                        // leave message
                                        String message = leave[random.nextInt(leave.length - 1)];

                                        // announce
                                        getCosmos().getChatManager().sendClientMessage(message + player.getName() + "!");
                                    }

                                    // reset
                                    joinLeaveTimer.resetTime();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemUseFinish(ItemUseFinishEvent event) {

        // check if item is a food item
        if (event.getItemStack().getItem() instanceof ItemFood || event.getItemStack().getItem() instanceof ItemPotion) {

            // update food eaten
            eats++;

            // notify
            if (eat.getValue()) {

                // make sure the messages are delayed
                if (foodTimer.passedTime(10 * delay.getValue().longValue(), Format.SECONDS)) {

                    // food name
                    String food = event.getItemStack().getDisplayName();

                    // announce
                    getCosmos().getChatManager().sendClientMessage("I just ate " + eats + " " + food + "!");

                    // reset
                    foodTimer.resetTime();
                    eats = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockDestroy(BlockBreakEvent event) {

        // update block breaks
        breaks++;

        // notify
        if (broke.getValue()) {

            // make sure the messages are delayed
            if (breakTimer.passedTime(10 * delay.getValue().longValue(), Format.SECONDS)) {

                // announce
                getCosmos().getChatManager().sendClientMessage("I just broke " + breaks + " blocks!");

                // reset
                breakTimer.resetTime();
                breaks = 0;
            }
        }
    }
}