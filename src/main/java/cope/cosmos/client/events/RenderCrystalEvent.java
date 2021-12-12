package cope.cosmos.client.events;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderCrystalEvent extends Event {

    public static class RenderCrystalPreEvent extends RenderCrystalEvent {

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

        public ModelBase getModelBase() {
            return modelBase;
        }

        public Entity getEntity() {
            return entity;
        }

        public float getLimbSwing() {
            return limbSwing;
        }

        public float getLimbSwingAmount() {
            return limbSwingAmount;
        }

        public float getAgeInTicks() {
            return ageInTicks;
        }

        public float getNetHeadYaw() {
            return netHeadYaw;
        }

        public float getHeadPitch() {
            return headPitch;
        }

        public float getScaleFactor() {
            return scaleFactor;
        }
    }

    public static class RenderCrystalPostEvent extends RenderCrystalEvent {

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

        public ModelBase getModelBase() {
            return modelBase;
        }

        public ModelBase getModelNoBase() {
            return modelNoBase;
        }

        public EntityEnderCrystal getEntityEnderCrystal() {
            return entityEnderCrystal;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public float getEntityYaw() {
            return entityYaw;
        }

        public float getPartialTicks() {
            return partialTicks;
        }
    }
}
