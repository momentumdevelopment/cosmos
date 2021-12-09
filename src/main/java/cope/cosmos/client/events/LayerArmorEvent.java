package cope.cosmos.client.events;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.inventory.EntityEquipmentSlot;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class LayerArmorEvent extends Event {

    private final ModelBiped modelBiped;
    private final EntityEquipmentSlot entityEquipmentSlot;

    public LayerArmorEvent(ModelBiped modelBiped, EntityEquipmentSlot entityEquipmentSlot) {
        this.modelBiped = modelBiped;
        this.entityEquipmentSlot = entityEquipmentSlot;
    }

    public EntityEquipmentSlot getEntityEquipmentSlot() {
        return entityEquipmentSlot;
    }

    public ModelBiped getModelBiped() {
        return modelBiped;
    }
}
