package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S2CBuildupProc implements IMessage {
    private int entityId;
    private BuildupEffect effect;

    public S2CBuildupProc() {}
    public S2CBuildupProc(int entityId, BuildupEffect effect) {
        this.entityId = entityId;
        this.effect = effect;
    }

    public S2CBuildupProc(EntityLivingBase entity, BuildupEffect effect) {
        this(entity.getEntityId(), effect);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        entityId = pb.readInt();
        effect = Effects.get(pb.readString(64));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeInt(entityId);
        pb.writeString(effect.getId());
    }

    public static class Handler implements IMessageHandler<S2CBuildupProc, IMessage> {

        @Override
        public IMessage onMessage(S2CBuildupProc message, MessageContext ctx) {
            if (message.effect != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    WorldClient world = Minecraft.getMinecraft().world;
                    Entity entity = world.getEntityByID(message.entityId);
                    if (entity instanceof EntityLivingBase base) message.effect.applyClientEffect(base);
                });
            }
            return null;
        }
    }
}
