package cope.cosmos.client.events.combat;

import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
