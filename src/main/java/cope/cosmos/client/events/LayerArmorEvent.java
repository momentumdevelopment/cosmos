package cope.cosmos.client.events;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LayerArmorEvent extends Event {

    private ModelBiped modelBiped;
    private EntityEquipmentSlot entityEquipmentSlot;

    public LayerArmorEvent(ModelBiped modelBiped, EntityEquipmentSlot entityEquipmentSlot) {
        this.modelBiped = modelBiped;
        this.entityEquipmentSlot = entityEquipmentSlot;
    }

    public EntityEquipmentSlot getEntityEquipmentSlot() {
        return this.entityEquipmentSlot;
    }

    public ModelBiped getModelBiped() {
        return this.modelBiped;
    }
}
