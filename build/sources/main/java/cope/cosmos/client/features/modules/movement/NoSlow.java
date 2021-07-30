package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IKeybinding;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.SlimeEvent;
import cope.cosmos.client.events.SoulSandEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class NoSlow extends Module {
    public static NoSlow INSTANCE;

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT, "Removes various slowdown effects");
        INSTANCE = this;
    }

    public static Setting<Boolean> inventoryMove = new Setting<>("InventoryMove", "Allows you to move while in GUI's", true);
    public static Setting<Boolean> strict = new Setting<>("Strict", "Spoofs slowdown state with packets", false);
    public static Setting<Boolean> airStrict = new Setting<>("AirStrict", "Spoofs slowdown state with packets while in air", false);
    public static Setting<Boolean> soulSand = new Setting<>("SoulSand", "Removes the soulsand slowdown effect", true);
    public static Setting<Boolean> slime = new Setting<>("Slime", "Removes the slime slowdown effect", true);

    boolean airPacket;

    @Override
    public void onUpdate() {
        if (airPacket && airStrict.getValue() && hasSlowDown()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            airPacket = false;
        }

        if (inventoryMove.getValue() && isInScreen()) {
            ((IKeybinding) mc.gameSettings.keyBindForward).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
            mc.gameSettings.keyBindForward.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);

            ((IKeybinding) mc.gameSettings.keyBindBack).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindBack));
            mc.gameSettings.keyBindBack.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);

            ((IKeybinding) mc.gameSettings.keyBindRight).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindRight));
            mc.gameSettings.keyBindRight.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);

            ((IKeybinding) mc.gameSettings.keyBindLeft).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindLeft));
            mc.gameSettings.keyBindLeft.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);

            ((IKeybinding) mc.gameSettings.keyBindJump).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
            mc.gameSettings.keyBindJump.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);

            {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP))
                    mc.player.rotationPitch -= 5;
                else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
                    mc.player.rotationPitch += 5;
                else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
                    mc.player.prevRotationYaw += 5;
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
                    mc.player.prevRotationYaw -= 5;

                mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90, 90);
            }
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent event) {
        if (!airPacket && airStrict.getValue()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            airPacket = true;
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (hasSlowDown()) {
            event.getMovementInput().moveStrafe *= 5;
            event.getMovementInput().moveForward *= 5;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && strict.getValue() && hasSlowDown()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), EnumFacing.DOWN));
        }
    }

    @SubscribeEvent
    public void onSoulSand(SoulSandEvent event) {
        event.setCanceled(soulSand.getValue());
    }

    @SubscribeEvent
    public void onSlime(SlimeEvent event) {
        event.setCanceled(slime.getValue());
    }

    public boolean hasSlowDown() {
        if (!mc.player.isRiding() && mc.player.isHandActive()) {
            Item activeItem = mc.player.getActiveItemStack().getItem();
            return activeItem instanceof ItemFood || activeItem instanceof ItemPotion || activeItem instanceof ItemBow;
        }

        return mc.player.isSneaking();
    }

    public boolean isInScreen() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiEditSign || mc.currentScreen instanceof GuiRepair);
    }

    public enum ConflictContext implements IKeyConflictContext {

        FAKE_CONTEXT {
            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public boolean conflicts(IKeyConflictContext other) {
                return false;
            }
        }

    }
}

