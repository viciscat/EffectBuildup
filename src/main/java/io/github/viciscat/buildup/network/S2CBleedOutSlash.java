package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.particle.ParticleSequenceBleedOut;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S2CBleedOutSlash implements IMessage {
    private int entityID;
    public S2CBleedOutSlash() {}
    public S2CBleedOutSlash(int entityID) {
        this.entityID = entityID;
    }
    public S2CBleedOutSlash(EntityLivingBase entity) {
        this(entity.getEntityId());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
    }

    public static class Handler implements IMessageHandler<S2CBleedOutSlash, IMessage> {

        @Override
        public IMessage onMessage(S2CBleedOutSlash message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                Entity entity = mc.world.getEntityByID(message.entityID);
                if (entity instanceof EntityLivingBase base) {
                    new ParticleSequenceBleedOut(base);
                }
            });
            return null;
        }
    }
}
