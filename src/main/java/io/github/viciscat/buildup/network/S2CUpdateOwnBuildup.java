package io.github.viciscat.buildup.network;

import io.github.viciscat.buildup.BuildupHud;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class S2CUpdateOwnBuildup implements IMessage {

    private List<BuildupUpdate> updates = List.of();


    public S2CUpdateOwnBuildup() {}

    public S2CUpdateOwnBuildup(List<BuildupUpdate> updates) {
        this.updates = List.copyOf(updates);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        int updateCount = pb.readByte();
        List<BuildupUpdate> list = new ArrayList<>(updateCount);
        for (int i = 0; i <updateCount; i++) {
            list.add(BuildupUpdate.read(pb));
        }
        updates = list;

    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeByte(updates.size());
        for (BuildupUpdate update : updates) {
            update.write(pb);
        }
    }

    public static class Handler implements IMessageHandler<S2CUpdateOwnBuildup, IMessage> {

        @Override
        public IMessage onMessage(S2CUpdateOwnBuildup message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> message.updates.forEach(BuildupHud.INSTANCE::addUpdate));
            return null;
        }
    }
}
