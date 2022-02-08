package cope.cosmos.client.events.render.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
