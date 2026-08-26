package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class S2CUpdateEntityEffects implements IMessage {

    private List<EffectUpdate> updates = List.of();
    private int entityId;

    public S2CUpdateEntityEffects() {}

    public S2CUpdateEntityEffects(EntityLivingBase entity, List<EffectUpdate> updates) {
        this.updates = updates;
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        entityId = pb.readInt();
        this.updates = EffectUpdate.readList(pb);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeInt(entityId);
        EffectUpdate.writeList(pb, updates);
    }

    public static class Handler implements IMessageHandler<S2CUpdateEntityEffects, IMessage> {

        @Override
        public IMessage onMessage(S2CUpdateEntityEffects message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> apply(message));
            return null;
        }

        private void apply(S2CUpdateEntityEffects message) {
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
            if (!(entity instanceof EntityLivingBase target)) return;
            EffectTracker tracker = EffectTracker.get(target);
            if (tracker == null) return;
            tracker.update(message.updates);
        }
    }
}
