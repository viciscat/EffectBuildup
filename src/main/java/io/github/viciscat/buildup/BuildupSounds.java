package io.github.viciscat.buildup;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;

public final class BuildupSounds {
    private BuildupSounds() {}

    private static SoundEvent sound(String path) {
        return new SoundEvent(BuildupMod.loc(path));
    }

    private static SoundEvent proc(String path) {
        return sound("proc." + path);
    }

    public static void playAt(EntityLivingBase entity, SoundEvent sound, float volume, float pitch) {
        if (entity.world.isRemote) {
            entity.getEntityWorld().playSound(entity.posX, entity.posY, entity.posZ, sound, entity.getSoundCategory(), volume, pitch, true);
        } else {
            entity.getEntityWorld().playSound(null, entity.posX, entity.posY, entity.posZ, sound, entity.getSoundCategory(), volume, pitch);
        }
    }

    public static final SoundEvent ABLAZE_PROC = proc("ablaze");
    public static final SoundEvent BLEED_OUT_PROC = proc("bleed_out");
    public static final SoundEvent BLEED_OUT_22_PROC = proc("bleed_out.22");
    public static final SoundEvent CHAIN_LIGHTNING_PROC = proc("chain_lightning");
    public static final SoundEvent DEADLY_POISON_PROC = proc("deadly_poison");
    public static final SoundEvent DECAYED_PROC = proc("decayed");
    public static final SoundEvent DECAYED_20_PROC = proc("decayed.20");
    public static final SoundEvent DECONSTRUCTION_PROC = proc("desconstruction");
    public static final SoundEvent DESTINED_DEATH_PROC = proc("destined_death");
    public static final SoundEvent ENTROPY_PROC = proc("entropy");
    public static final SoundEvent FROZEN_PROC = proc("frozen");
    public static final SoundEvent FROZEN_20_PROC = proc("frozen.20");
    public static final SoundEvent JUDGEMENT_PROC = proc("judgement");
    public static final SoundEvent JUDGEMENT_2_PROC = proc("judgement.2");
    public static final SoundEvent MEGABONK_PROC = proc("megabonk");
    public static final SoundEvent MELT_PROC = proc("armor_melt");
    public static final SoundEvent SHOCKWAVE_PROC = proc("shockwave");
    public static final SoundEvent UNSTABLE_PROC = proc("unstable");
    public static final SoundEvent WOUND_PROC = proc("wound");

}
