package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.InstancedEffect;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;

public record EffectUpdate(InstancedEffect<?> effect, boolean removed, ByteBuf data) {

    public static EffectUpdate removed(InstancedEffect<?> effect) {
        return new EffectUpdate(effect, true, Unpooled.EMPTY_BUFFER);
    }

    public static EffectUpdate update(InstancedEffect<?> effect, ByteBuf data) {
        return new EffectUpdate(effect, false, data);
    }

    public static List<EffectUpdate> readList(PacketBuffer pb) {
        int updateCount = pb.readByte();
        List<EffectUpdate> list = new ArrayList<>(updateCount);
        for (int i = 0; i < updateCount; i++) {
            String effectId = pb.readString(1024);
            int dateSize = pb.readInt();
            InstancedEffect<?> effect = (InstancedEffect<?>) Effects.get(effectId);
            if (dateSize >= 0) {
                list.add(EffectUpdate.update(effect, pb.readBytes(dateSize)));
            } else {
                list.add(EffectUpdate.removed(effect));
            }
        }
        return list;
    }

    public static void writeList(PacketBuffer pb, List<EffectUpdate> updates) {
        pb.writeByte(updates.size());
        for (EffectUpdate update : updates) {
            pb.writeString(update.effect().getId());
            if (update.removed()) {
                pb.writeInt(-1);
            } else {
                pb.writeInt(update.data().readableBytes());
                pb.writeBytes(update.data());
            }
        }
    }
}
