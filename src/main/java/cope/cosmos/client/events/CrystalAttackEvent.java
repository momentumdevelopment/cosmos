package cope.cosmos.client.events;

import net.minecraft.util.DamageSource;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class CrystalAttackEvent extends Event {

    private final DamageSource damageSource;

    public CrystalAttackEvent(DamageSource damageSource) {
        this.damageSource = damageSource;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }
}
