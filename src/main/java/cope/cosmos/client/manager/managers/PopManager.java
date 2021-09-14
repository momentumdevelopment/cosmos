package cope.cosmos.client.manager.managers;

import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.misc.Notifier;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ChatUtil;
import cope.cosmos.util.combat.EnemyUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PopManager extends Manager implements Wrapper {
    public PopManager() {
        super("PopManager", "Keeps track of all the totem pops", 17);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private final Map<Entity, Integer> totemPops = new HashMap<>();

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (nullCheck()) {
            new ArrayList<>(mc.world.loadedEntityList).forEach(entity -> {
                if (totemPops.containsKey(entity) && EnemyUtil.isDead(entity)) {
                    if (Notifier.INSTANCE.isEnabled() && Notifier.popNotify.getValue()) {
                        ChatUtil.sendMessage(TextFormatting.DARK_PURPLE + entity.getName() + TextFormatting.RESET + " died after popping " + totemPops.get(entity) + " totems!");
                    }

                    totemPops.remove(entity);
                }
            });
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        totemPops.put(event.getPopEntity(), totemPops.containsKey(event.getPopEntity()) ? totemPops.get(event.getPopEntity()) + 1 : 1);
    }

    public int getTotemPops(Entity entity) {
        return totemPops.getOrDefault(entity, 0);
    }
}
