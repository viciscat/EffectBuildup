package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.InstancedEffect;
import net.minecraft.network.PacketBuffer;

public record BuildupUpdate(BuildupEffect effect, float value, float max, boolean decay) {

    public static BuildupUpdate read(PacketBuffer pb) {
        String effectId = pb.readString(1024);
        BuildupEffect effect = Effects.get(effectId);
        final float value = pb.readFloat();
        final float max = pb.readFloat();
        final boolean decay = pb.readBoolean();
        return new BuildupUpdate(effect, value, max, decay);
    }

    public void write(PacketBuffer pb) {
        pb.writeString(effect().getId());
        pb.writeFloat(value());
        pb.writeFloat(max());
        pb.writeBoolean(decay());
    }
}
