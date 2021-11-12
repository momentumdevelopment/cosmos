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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
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

    public static final Setting<Boolean> strict = new Setting<>("Strict", "Allows you to bypass normal NCP server", false);
    public static final Setting<Boolean> airStrict = new Setting<>("AirStrict", "Allows you to bypass strict NCP servers while in the air", false);
    public static final Setting<Boolean> switchStrict = new Setting<>("SwitchStrict", "Allows you to bypass strict NCP servers", false);
    public static final Setting<Boolean> placeStrict = new Setting<>("PlaceStrict", "Allows you to bypass strict servers", false);

    public static final Setting<Boolean> inventoryMove = new Setting<>("InventoryMove", "Allows you to move around while in GUIs", true);
    public static final Setting<Float> arrowLook = new Setting<>("ArrowLook", "The speed that the arrow keys should rotate you with", 0.0f, 5.0f, 10.0f, 1).setParent(inventoryMove);

    public static final Setting<Boolean> items = new Setting<>("Items", "If to remove the slowdown effect while using items", true);
    public static final Setting<Boolean> soulsand = new Setting<>("SoulSand", "If to remove the slowdown effect when walking on soulsand", false);
    public static final Setting<Boolean> slime = new Setting<>("Slime", "If to remove the slowdown effect when walking on slime", false);

    private boolean isSneaking = false;

    private final KeyBinding[] KEYS = new KeyBinding[] {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindSprint,
            mc.gameSettings.keyBindSneak
    };

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            if (isSneaking && airStrict.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            for (KeyBinding binding : KEYS) {
                binding.setKeyConflictContext(KeyConflictContext.IN_GAME);
            }
        }

        isSneaking = false;
    }

    @Override
    public void onUpdate() {
        if (isSneaking && airStrict.getValue() && !mc.player.isHandActive()) {
            isSneaking = false;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (isSlowed()) {
            if (switchStrict.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem)); // lolololololo thanks FencingF
            }

            if (placeStrict.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(mc.objectMouseOver.getBlockPos(), EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
            }
        }

        if (inventoryMove.getValue() && isInScreen()) {
            mc.currentScreen.allowUserInput = true;

            for (KeyBinding bind : KEYS) {
                ((IKeybinding) bind).setPressed(GameSettings.isKeyDown(bind));
                bind.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);
            }

            if (arrowLook.getValue() != 0) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    mc.player.rotationPitch -= arrowLook.getValue();
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    mc.player.rotationPitch += arrowLook.getValue();
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.player.rotationYaw += arrowLook.getValue();
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.player.rotationYaw -= arrowLook.getValue();
                }

                mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90, 90);
            }
        }

        else {
            for (KeyBinding binding : KEYS) {
                binding.setKeyConflictContext(KeyConflictContext.IN_GAME);
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (isSlowed() && strict.getValue() && event.getPacket() instanceof CPacketPlayer) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (isSlowed()) {
            event.getMovementInput().moveForward *= 5;
            event.getMovementInput().moveStrafe *= 5;
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent event) {
        if (isSlowed() && airStrict.getValue() && !isSneaking) {
            isSneaking = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
    }

    @SubscribeEvent
    public void onSoulSand(SoulSandEvent event) {
        event.setCanceled(soulsand.getValue());
    }

    @SubscribeEvent
    public void onSlime(SlimeEvent event) {
        event.setCanceled(slime.getValue());
    }

    public boolean isInScreen() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiEditSign || mc.currentScreen instanceof GuiRepair);
    }

    private boolean isSlowed() {
        return (mc.player.isHandActive() && items.getValue()) && !mc.player.isRiding() && !mc.player.isElytraFlying();
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

