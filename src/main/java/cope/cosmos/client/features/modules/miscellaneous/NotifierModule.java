package cope.cosmos.client.features.modules.miscellaneous;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.events.client.ModuleToggleEvent;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author linustouchtips
 * @since 08/22/2021
 */
public class NotifierModule extends Module {
    public static NotifierModule INSTANCE;

    public NotifierModule() {
        super("Notifier", Category.MISCELLANEOUS, "Sends notifications in chat");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> enableNotify = new Setting<>("EnableNotify", false)
            .setAlias("EnableMessages")
            .setDescription("Send a chat message when a modules is toggled");

    public static Setting<Boolean> popNotify = new Setting<>("PopNotify", false)
            .setAlias("TotemPopNotify", "TotemPopNotifier", "PopNotifier")
            .setDescription("Send a chat message when a nearby player is popped");

    public static Setting<Boolean> donkeyNotify = new Setting<>("DonkeyNotify", false)
            .setAlias("DonkeyAlert", "LlamaNotify", "LlamaAlert", "MuleNotify", "MuleAlert")
            .setDescription("Send a chat message when a nearby player is popped");

    public static Setting<Boolean> visualNotify = new Setting<>("VisualNotify", false)
            .setAlias("VisualRange")
            .setDescription("Send a chat message when a nearby player is popped");

    // list of players in visual range
    private final List<Entity> visualPlayers = new CopyOnWriteArrayList<>();

    // list of chest-able entities in visual range
    private final List<Entity> visualDonkeys = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();

        // create log
        if (visualNotify.getValue()) {

            // clear old log
            visualPlayers.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if player
                if (entity instanceof EntityPlayer) {

                    // log player
                    visualPlayers.add(entity);
                }
            }
        }

        // log chest-able entities
        if (donkeyNotify.getValue()) {

            // clear old log
            visualDonkeys.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if chest-able entity
                if (entity instanceof EntityDonkey || entity instanceof EntityLlama || entity instanceof EntityMule) {

                    // log chest-able entity
                    visualDonkeys.add(entity);
                }
            }
        }
    }

    @Override
    public void onUpdate() {

        // notify chest-able entities
        if (donkeyNotify.getValue()) {

            // give chance for visual log to collect
            if (mc.player.ticksExisted > 20) {

                // collect new visual range of chest-able entities who entered
                List<Entity> enterVisualDonkeys = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityDonkey || entity instanceof EntityLlama || entity instanceof EntityMule).filter(entity -> !visualDonkeys.contains(entity)).collect(Collectors.toList());

                // notify enters
                for (Entity entity : enterVisualDonkeys) {

                    // notify
                    getCosmos().getChatManager().sendClientMessage("[DonkeyNotify] Chest-able entity found at " + ChatFormatting.GRAY + "[X: " + MathUtil.roundDouble(entity.posX, 1) + ", Y: " + MathUtil.roundDouble(entity.posY, 1) + ", Z: " + MathUtil.roundDouble(entity.posZ, 1) + "]");
                }
            }

            // clear old log
            visualDonkeys.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if chest-able entity
                if (entity instanceof EntityDonkey || entity instanceof EntityLlama || entity instanceof EntityMule) {

                    // log chest-able entity
                    visualDonkeys.add(entity);
                }
            }
        }

        // notify players in visual range
        if (visualNotify.getValue()) {

            // give chance for visual log to collect
            if (mc.player.ticksExisted > 20) {

                // collect new visual range of players who exited
                List<Entity> exitVisualRange = visualPlayers.stream().filter(player -> !mc.world.loadedEntityList.contains(player)).collect(Collectors.toList());

                // collect new visual range of players who entered
                List<Entity> enterVisualRange = mc.world.loadedEntityList.stream().filter(player -> player instanceof EntityPlayer).filter(player -> !player.equals(mc.player)).filter(player -> !visualPlayers.contains(player)).collect(Collectors.toList());

                // notify exits
                for (Entity player : exitVisualRange) {

                    // ignore local player
                    if (!player.getName().equals(mc.player.getName())) {

                        // notify
                        getCosmos().getChatManager().sendClientMessage("[VisualNotify] " + (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND) ? ChatFormatting.AQUA : ChatFormatting.GRAY) + player.getName() + ChatFormatting.RESET + " has " + ChatFormatting.RED + "left " + ChatFormatting.RESET + "your visual range!");
                    }
                }

                // notify enters
                for (Entity player : enterVisualRange) {

                    // ignore local player
                    if (!player.getName().equals(mc.player.getName())) {

                        // notify
                        getCosmos().getChatManager().sendClientMessage("[VisualNotify] " + (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND) ? ChatFormatting.AQUA : ChatFormatting.GRAY) + player.getName() + ChatFormatting.RESET + " has " + ChatFormatting.BLUE + "entered " + ChatFormatting.RESET + "your visual range!");
                    }
                }
            }

            // clear old log
            visualPlayers.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if player
                if (entity instanceof EntityPlayer) {

                    // log player
                    visualPlayers.add(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {

        // create log
        if (visualNotify.getValue()) {

            // clear old log
            visualPlayers.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if player
                if (entity instanceof EntityPlayer) {

                    // log player
                    visualPlayers.add(entity);
                }
            }
        }

        // log chest-able entities
        if (donkeyNotify.getValue()) {

            // clear old log
            visualDonkeys.clear();

            // check all entities in world
            for (Entity entity : mc.world.loadedEntityList) {

                // check if chest-able entity
                if (entity instanceof EntityDonkey || entity instanceof EntityLlama || entity instanceof EntityMule) {

                    // log chest-able entity
                    visualDonkeys.add(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {

        // notify pops
        if (popNotify.getValue()) {

            // if the player is in range
            if (mc.player.getDistance(event.getPopEntity()) < 10) {

                // formatted message for the pop notification
                String popMessage = TextFormatting.DARK_PURPLE + event.getPopEntity().getName() + TextFormatting.RESET + " has popped " + getCosmos().getPopManager().getTotemPops(event.getPopEntity()) + " totems!";

                // send notification
                getCosmos().getChatManager().sendClientMessage(popMessage);
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {

        // notify totem pops
        if (getCosmos().getPopManager().getTotemPops(event.getEntity()) > 0) {

            // notify the player if necessary
            if (popNotify.getValue()) {
                getCosmos().getChatManager().sendClientMessage(TextFormatting.DARK_PURPLE + event.getEntity().getName() + TextFormatting.RESET + " died after popping " + getCosmos().getPopManager().getTotemPops(event.getEntity()) + " totems!");
            }

            // remove the totem info associated with the entity
            getCosmos().getPopManager().removePops(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onModuleEnable(ModuleToggleEvent.ModuleEnableEvent event) {

        // notify the module enable
        if (enableNotify.getValue()) {

            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an enable notification
                getCosmos().getChatManager().sendClientMessage(event.getModule());
            }
        }
    }

    @SubscribeEvent
    public void onModuleDisable(ModuleToggleEvent.ModuleDisableEvent event) {

        // notify the module disable
        if (enableNotify.getValue()) {

            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an disable notification
                getCosmos().getChatManager().sendClientMessage(event.getModule());
            }
        }
    }
}
