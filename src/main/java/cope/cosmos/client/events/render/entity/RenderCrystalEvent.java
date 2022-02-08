package cope.cosmos.client.events.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when an end crystal is rendered
 * @author linustouchtips
 * @since 05/23/2021
 */
@Cancelable
public class RenderCrystalEvent extends Event {

    public static class RenderCrystalPreEvent extends RenderCrystalEvent {

        // info
        private final ModelBase modelBase;
        private final Entity entity;
        private final float limbSwing;
        private final float limbSwingAmount;
        private final float ageInTicks;
        private final float netHeadYaw;
        private final float headPitch;
        private final float scaleFactor;

        public RenderCrystalPreEvent(ModelBase modelBase, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
            this.modelBase = modelBase;
            this.entity = entity;
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
        public Entity getEntity() {
            return entity;
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
    }

    public static class RenderCrystalPostEvent extends RenderCrystalEvent {

        // info
        private final ModelBase modelBase;
        private final ModelBase modelNoBase;
        private final EntityEnderCrystal entityEnderCrystal;
        private final double x;
        private final double y;
        private final double z;
        private final float entityYaw;
        private final float partialTicks;

        public RenderCrystalPostEvent(ModelBase modelBase, ModelBase modelNoBase, EntityEnderCrystal entityEnderCrystal, double x, double y, double z, float entityYaw, float partialTicks) {
            this.modelBase = modelBase;
            this.modelNoBase = modelNoBase;
            this.entityEnderCrystal = entityEnderCrystal;
            this.x = x;
            this.y = y;
            this.z = z;
            this.entityYaw = entityYaw;
            this.partialTicks = partialTicks;
        }

        /**
         * Gets the full entity model with the base model
         * @return The full entity model with the base model
         */
        public ModelBase getModelBase() {
            return modelBase;
        }

        /**
         * Gets the entity model without the base model
         * @return The entity model without the base model
         */
        public ModelBase getModelNoBase() {
            return modelNoBase;
        }

        /**
         * Gets the crystal entity
         * @return The crystal entity
         */
        public EntityEnderCrystal getEntityEnderCrystal() {
            return entityEnderCrystal;
        }

        /**
         * Gets the entity's x position
         * @return The entity's x position
         */
        public double getX() {
            return x;
        }

        /**
         * Gets the entity's y position
         * @return The entity's y position
         */
        public double getY() {
            return y;
        }

        /**
         * Gets the entity's z position
         * @return The entity's z position
         */
        public double getZ() {
            return z;
        }

        /**
         * Gets the entity's yaw
         * @return The entity's yaw
         */
        public float getEntityYaw() {
            return entityYaw;
        }

        /**
         * Gets the render partial ticks
         * @return The render partial ticks
         */
        public float getPartialTicks() {
            return partialTicks;
        }
    }
}
