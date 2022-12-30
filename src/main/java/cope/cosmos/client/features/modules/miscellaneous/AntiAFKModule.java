package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.PresenceManager;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.*;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * @author linustouchtips
 * @since 12/16/2021
 */
public class AntiAFKModule extends Module {
    public static AntiAFKModule INSTANCE;

    public AntiAFKModule() {
        super("AntiAFK", Category.MISCELLANEOUS, "Prevents servers for kicking you for being AFK");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> chat = new Setting<>("Chat", true)
            .setDescription("Send messages in the chat to avoid AFK detection");

    public static Setting<Boolean> jump = new Setting<>("Jump", true)
            .setDescription("Jumps to avoid AFK detection");

    public static Setting<Boolean> rotate = new Setting<>("Rotation", false)
            .setAlias("Rotate")
            .setDescription("Rotates to avoid AFK detection");

    public static Setting<Boolean> reply = new Setting<>("Reply", true)
            .setAlias("AutoReply")
            .setDescription("Replies to incoming messages");

    // timer keeping track of afk time
    private final Timer awayTimer = new Timer();

    // AFK prevention method timers
    private final Timer chatTimer = new Timer();
    private final Timer jumpTimer = new Timer();
    private final Timer rotateTimer = new Timer();

    @Override
    public void onEnable() {
        super.onEnable();

        // reset time
        awayTimer.resetTime();
        chatTimer.resetTime();
        jumpTimer.resetTime();
        rotateTimer.resetTime();
    }

    @Override
    public void onUpdate() {

        // if we are moving, then we should reset our away progress
        if (MotionUtil.isMoving() || mc.player.movementInput.jump || mc.player.movementInput.sneak || mc.gameSettings.keyBindUseItem.isPressed()) {
            awayTimer.resetTime();
        }

        // if we've been motionless for twenty seconds -> we can assume we are AFK
        else if (awayTimer.passedTime(20, Format.SECONDS)) {

            // jump to prevent server from detecting no movement input
            if (jump.getValue()) {

                // jump if we've passed two seconds
                if (jumpTimer.passedTime(5, Format.SECONDS) && mc.player.onGround) {
                    mc.player.jump();

                    // reset clearance
                    jumpTimer.resetTime();
                }
            }

            // chats to prevent server from detecting no input
            if (chat.getValue()) {

                // add random delay to chat messages, helps to bypass more
                Random chatRandom = new Random();

                // send a message in chat if we've passed time
                if (chatTimer.passedTime(10 + chatRandom.nextInt(5), Format.SECONDS)) {

                    // get a random chat message
                    String message = PresenceManager.getPresenceDetails()[chatRandom.nextInt(PresenceManager.getPresenceDetails().length - 1)];

                    // send chat message
                    getCosmos().getChatManager().sendChatMessage(message);

                    // reset clearance
                    chatTimer.resetTime();
                }
            }

            // jump to prevent server from detecting no rotation input
            if (rotate.getValue()) {

                // random angle
                Random rotateRandom = new Random();

                // rotate if we've cleared time
                if (rotateTimer.passedTime(5, Format.SECONDS)) {

                    // rotate
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw + rotateRandom.nextInt(5), mc.player.rotationPitch, mc.player.onGround));

                    // reset clearance
                    rotateTimer.resetTime();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // reply to messages
        if (reply.getValue()) {

            // packet for server chat messages
            if (event.getPacket() instanceof SPacketChat) {

                // message in the chat
                String[] chatMessage = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().split(" ");

                // make sure it's a system message
                if (chat.getValue() && ((SPacketChat) event.getPacket()).getType().equals(ChatType.SYSTEM)) {

                    // if it's a direct message, reply that we are AFK at the moment
                    if (chatMessage[1].equals("whispers:")) {

                        // send chat message
                        getCosmos().getChatManager().sendChatMessage("/r [Cosmos] I am currently AFK.");
                    }
                }
            }
        }
    }
}
