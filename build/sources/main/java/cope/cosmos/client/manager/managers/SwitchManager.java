package cope.cosmos.client.manager.managers;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SwitchManager extends Manager {
    public SwitchManager() {
        super("SwitchManager", "Manages the NCP switch cooldown", 14);
        MinecraftForge.EVENT_BUS.register(this);
    }

    Timer switchTimer = new Timer();

    @Override
    public void initialize(Manager manager) {
        manager = new SwitchManager();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketHeldItemChange)
            switchTimer.reset();
    }

    public boolean switchAttackReady(long switchDelay) {
        return !switchTimer.passed(switchDelay, Format.SYSTEM);
    }
}
