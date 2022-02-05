package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;

/**
 * @author linustouchtips
 * @since 11/25/2021
 */
public class SoundManager extends Manager {
    public SoundManager() {
        super("SoundManager", "Manages client sounds");
    }

    /**
     * Plays a specified sound
     * @param in The sound to play
     */
    public void playSound(String in) {
        mc.getSoundHandler().playSound(new ISound() {

            @Nonnull
            @Override
            public ResourceLocation getSoundLocation() {
                return new ResourceLocation("cosmos", "sounds/" + in + ".ogg");
            }

            //@Nullable // This always returns a non-null value so why tf is this here lol.
            @Nonnull
            @Override
            public SoundEventAccessor createAccessor(SoundHandler handler) {
                return new SoundEventAccessor(new ResourceLocation("cosmos", "sounds/" + in + ".ogg"), in);
            }

            @Nonnull
            @Override
            public Sound getSound() {
                return new Sound(in, 1, 1, 1, Sound.Type.SOUND_EVENT, false);
            }

            @Nonnull
            @Override
            public SoundCategory getCategory() {
                return SoundCategory.PLAYERS;
            }

            @Override
            public boolean canRepeat() {
                return false;
            }

            @Override
            public int getRepeatDelay() {
                return 0;
            }

            @Override
            public float getVolume() {
                return 1;
            }

            @Override
            public float getPitch() {
                return 1;
            }

            @Override
            public float getXPosF() {
                return mc.player != null ? (float) mc.player.posX : 0;
            }

            @Override
            public float getYPosF() {
                return mc.player != null ? (float) mc.player.posY: 0;
            }

            @Override
            public float getZPosF() {
                return mc.player != null ? (float) mc.player.posZ : 0;
            }

            @Nonnull
            @Override
            public AttenuationType getAttenuationType() {
                return AttenuationType.LINEAR;
            }
        });
    }
}
