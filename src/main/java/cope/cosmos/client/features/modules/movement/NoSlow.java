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
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
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

    public static final Setting<Bypass> bypass = new Setting<>("Bypass", "How to bypass NoSlow patches", Bypass.NCP);

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
            if (isSneaking && bypass.getValue() == Bypass.Sneak) {
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
        if (isSneaking && bypass.getValue() == Bypass.Sneak && !mc.player.isHandActive()) {
            isSneaking = false;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (mc.player.isHandActive() && bypass.getValue() == Bypass.New) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem)); // lolololololo thanks FencingF
        }

        if (inventoryMove.getValue() && isInScreen()) {
            mc.currentScreen.allowUserInput = true;

            for (KeyBinding binding : KEYS) {
                ((IKeybinding) binding).setPressed(GameSettings.isKeyDown(binding));
                binding.setKeyConflictContext(ConflictContext.FAKE_CONTEXT);
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
        if (isSlowed() && bypass.getValue() == Bypass.NCP && event.getPacket() instanceof CPacketPlayer) {
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
        if (isSlowed() && bypass.getValue() == Bypass.Sneak && !isSneaking) {
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

    public enum Bypass {
        NCP, Sneak, New
    }
}

