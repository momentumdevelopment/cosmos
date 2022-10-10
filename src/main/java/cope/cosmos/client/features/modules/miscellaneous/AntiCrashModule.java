package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.client.events.client.ExceptionThrownEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.network.DecodeEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.CrystalUpdateEvent;
import cope.cosmos.client.events.render.entity.RenderCrystalEvent;
import cope.cosmos.client.events.render.world.RenderSkylightEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author linustouchtips
 * @since 12/25/2021
 */
public class AntiCrashModule extends Module {
    public static AntiCrashModule INSTANCE;

    public AntiCrashModule() {
        super("AntiCrash", Category.MISCELLANEOUS,"Prevents you from being kicked/crashed by various exploits");
        INSTANCE = this;
    }

    // **************************** crash/kick exploits ****************************

    public static Setting<Boolean> packets = new Setting<>("Packet", false)
            .setAlias("ChunkBan")
            .setDescription("Prevents you from getting kicked for invalid packets");

    public static Setting<Boolean> bookBan = new Setting<>("BookBan", false)
            .setDescription("Prevents you from getting kicked for packet size limit");

    public static Setting<Boolean> unicode = new Setting<>("UnicodeCharacters", false)
            .setAlias("AntiChina", "ChineseCharacters", "Unicode")
            .setDescription("Prevents unicode characters in chat from lagging you");

    public static Setting<Boolean> offhand = new Setting<>("Offhand", false)
            .setAlias("OffhandCrash", "ItemSwapCrash", "OffhandSwapCrash", "ItemSwap", "OffhandSwap")
            .setDescription("Prevents you from getting crashed from item equip sounds");

    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", true)
            .setAlias("FireworkSpam", "FireworkCrash")
            .setDescription("Prevents firework spam from crashing you");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", false)
            .setAlias("StackedCrystals")
            .setDescription("Prevents stacked crystals spam from lagging you");

    public static Setting<Boolean> skylight = new Setting<>("Skylight", true)
            .setAlias("SkylightCrash")
            .setDescription("Prevents skylight updates from crashing you");

    public static Setting<Boolean> particles = new Setting<>("Particles", false)
            .setAlias("ParticleCrash")
            .setDescription("Prevents laggy particles from crashing you");

    public static Setting<Boolean> slime = new Setting<>("Slime", false)
            .setAlias("SlimeLag")
            .setDescription("Prevents large slime entities from crashing you");

    public static Setting<Boolean> signs = new Setting<>("Signs", false)
            .setAlias("Sign", "SignCrash")
            .setDescription("Prevents signs from kicking you when broken");

    // sign info
    private final List<BlockPos> placedSigns = new ArrayList<>();

    // array of unicode characters
    private final static String[] UNICODE = "ā ȁ ́ Ё ԁ ܁ ࠁ ँ ਁ ଁ ก ༁ ခ ᄁ ሁ ጁ ᐁ ᔁ ᘁ ᜁ ᠁ ᤁ ᨁ ᬁ ᰁ ᴁ ḁ ἁ ℁ ∁ ⌁ ␁ ━ ✁ ⠁ ⤁ ⨁ ⬁ Ⰱ ⴁ ⸁ ⼁ 、 \u3101 ㈁ ㌁ 㐁 㔁 㘁 㜁 㠁 㤁 㨁 㬁 㰁 㴁 㸁 㼁 䀁 䄁 䈁 䌁 䐁 䔁 䘁 䜁 䠁 䤁 䨁 䬁 䰁 䴁 丁 企 倁 儁 刁 匁 吁 唁 嘁 圁 堁 夁 威 嬁 封 崁 币 弁 态 愁 戁 持 搁 攁 昁 朁 栁 椁 樁 欁 氁 洁 渁 漁 瀁 焁 爁 猁 琁 甁 瘁 省 码 礁 稁 笁 簁 紁 縁 缁 老 脁 舁 茁 萁 蔁 蘁 蜁 蠁 褁 訁 謁 谁 贁 踁 輁 送 鄁 鈁 錁 鐁 锁 阁 霁 頁 餁 騁 鬁 鰁 鴁 鸁 鼁 ꀁ ꄁ ꈁ ꌁ ꐁ ꔁ ꘁ ꜁ ꠁ ꤁ ꨁ ꬁ 각 괁 긁 꼁 뀁 넁 눁 댁 됁 딁 똁 뜁 렁 뤁 먁 묁 밁 봁".split(" ");

    @Override
    public void onUpdate() {

        // we are in a sign gui
        if (mc.currentScreen instanceof GuiEditSign) {
           if (signs.getValue()) {

               // check if the sign is being broken
               ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((integer, destroyBlockProgress) -> {

                   // sign we placed
                   if (placedSigns.contains(destroyBlockProgress.getPosition())) {

                       // close the gui screen
                       mc.player.closeScreen();
                   }
               });
           }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (slime.getValue()) {
            mc.world.loadedEntityList.forEach(entity -> {

                // remove if the slime size is too large
                if (entity instanceof EntitySlime && ((EntitySlime) entity).getSlimeSize() > 4) {
                    mc.world.removeEntityDangerously(entity);
                }
            });
        }
    }

    @SubscribeEvent
    public void onExceptionThrown(ExceptionThrownEvent event) {

        // prevent the exception from being thrown
        if (event.getException() instanceof NullPointerException || event.getException() instanceof IOException || event.getException() instanceof ConcurrentModificationException) {
            if (packets.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDecode(DecodeEvent event) {

        // prevent packets from having a packet limit
        if (bookBan.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // player is placing a sign
            if (InventoryUtil.isHolding(Items.SIGN)) {
                placedSigns.add(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos());
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC)) {

            // prevent item equip sounds from being played
            if (offhand.getValue()) {
                event.setCanceled(true);
            }
        }

        // packet for particle spawns (for 254n_m's crash plugin)
        if (event.getPacket() instanceof SPacketParticles) {
            if (particles.getValue()) {

                // cancels particles from rendering
                if (((SPacketParticles) event.getPacket()).getParticleCount() > 200) {
                    event.setCanceled(true);
                }
            }
        }

        // packet for server chat messages
        if (event.getPacket() instanceof SPacketChat) {

            // message in the chat
            String chatMessage = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText();

            // make sure it's a system message
            if (unicode.getValue()) {

                // check each letter in the chat message
                for (int i = 0; i < chatMessage.length(); i++) {

                    // character
                    char character = chatMessage.charAt(i);

                    // check unicode list
                    for (String unicode : UNICODE) {

                        // check if its included in the unicode list
                        if (unicode.equalsIgnoreCase(String.valueOf(character))) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {
        if (event.getEntity() instanceof EntityFireworkRocket) {

            // prevent firework rocket entities from spawning
            if (fireworks.getValue() && !((EntityFireworkRocket) event.getEntity()).isAttachedToEntity()) {
                event.setCanceled(true);
            }
        }

        if (event.getEntity() instanceof EntitySlime && ((EntitySlime) event.getEntity()).getSlimeSize() > 4) {

            // prevent the entity from spawning if the slime size is too large
            if (slime.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(EntityWorldEvent.EntityUpdateEvent event) {
        if (event.getEntity() instanceof EntityFireworkRocket) {

            // prevent firework rocket entities from updating
            if (fireworks.getValue() && !((EntityFireworkRocket) event.getEntity()).isAttachedToEntity()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderSkylight(RenderSkylightEvent event) {

        // cancels skylight updates
        if (skylight.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderCrystal(RenderCrystalEvent.RenderCrystalPreEvent event) {

        // prevent crystals from rendering
        if (crystals.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCrystalUpdate(CrystalUpdateEvent event) {

        // prevent crystals from updating
        if (crystals.getValue()) {
            event.setCanceled(true);
        }
    }
}
