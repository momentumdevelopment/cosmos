package cope.cosmos.client.events.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a living entity (extended from the generic render model) is rendered
 * @author linustouchtips
 * @since 05/23/2021
 */
@Cancelable
public class RenderLivingEntityEvent extends Event {

    // info
    private final ModelBase modelBase;
    private final EntityLivingBase entityLivingBase;
    private final float limbSwing;
    private final float limbSwingAmount;
    private final float ageInTicks;
    private final float netHeadYaw;
    private final float headPitch;
    private final float scaleFactor;

    public RenderLivingEntityEvent(ModelBase modelBase, EntityLivingBase entityLivingBase, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        this.modelBase = modelBase;
        this.entityLivingBase = entityLivingBase;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.scaleFactor = scaleFactor;
    }

    /**
     * Gets the entity model
     * @return The entity model
     */
    public ModelBase getModelBase() {
        return modelBase;
    }

    /**
     * Gets the entity being rendered
     * @return The entity being rendered
     */
    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

    /**
     * Gets the entity's limb swing
     * @return The entity's limb swing
     */
    public float getLimbSwing() {
        return limbSwing;
    }

    /**
     * Gets the entity's limb swing amount
     * @return The entity's limb swing amount
     */
    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    /**
     * Gets the entity's tick age
     * @return The entity's tick age
     */
    public float getAgeInTicks() {
        return ageInTicks;
    }

    /**
     * Gets the entity's head yaw
     * @return The entity's head yaw
     */
    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    /**
     * Gets the entity's head pitch
     * @return The entity's head pitch
     */
    public float getHeadPitch() {
        return headPitch;
    }

    /**
     * Gets the entity's render scale factor
     * @return The entity's render scale factor
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    public static class RenderLivingEntityPreEvent extends RenderLivingEntityEvent {

        // pre render
        public RenderLivingEntityPreEvent(ModelBase modelBase, EntityLivingBase entityLivingBase, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
            super(modelBase, entityLivingBase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        }
    }

    public static class RenderLivingEntityPostEvent extends RenderLivingEntityEvent {

        // post render
        public RenderLivingEntityPostEvent(ModelBase modelBase, EntityLivingBase entityLivingBase, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
            super(modelBase, entityLivingBase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        }
    }
}