package cope.cosmos.client.events.render.other;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a particle is spawned
 * @author linustouchtips
 * @since 02/24/2022
 */
@Cancelable
public class RenderParticleEvent extends Event {

    // particle type
    private final EnumParticleTypes particleTypes;

    public RenderParticleEvent(EnumParticleTypes particleTypes) {
        this.particleTypes = particleTypes;
    }

    /**
     * Gets the particle type of the spawned particle
     * @return The particle type of the spawned particle
     */
    public EnumParticleTypes getParticleType() {
        return particleTypes;
    }
}
