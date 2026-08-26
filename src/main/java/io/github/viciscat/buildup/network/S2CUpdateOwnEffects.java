package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class S2CUpdateOwnEffects implements IMessage {

    private List<EffectUpdate> updates = List.of();

    public S2CUpdateOwnEffects() {}

    public S2CUpdateOwnEffects(List<EffectUpdate> updates) {
        this.updates = updates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        this.updates = EffectUpdate.readList(pb);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        EffectUpdate.writeList(pb, this.updates);
    }

    public static class Handler implements IMessageHandler<S2CUpdateOwnEffects, IMessage> {

        @Override
        public IMessage onMessage(S2CUpdateOwnEffects message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> apply(message));
            return null;
        }

        private void apply(S2CUpdateOwnEffects message) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player == null) return;
            EffectTracker tracker = EffectTracker.get(player);
            if (tracker == null) return;
            tracker.update(message.updates);
        }
    }
}
