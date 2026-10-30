package io.github.viciscat.buildup.command;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.InstancedEffect;
import io.github.viciscat.buildup.network.BuildupPacketHandler;
import io.github.viciscat.buildup.network.S2CBuildupProc;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildupCommand extends CommandBase {

    public BuildupCommand() {

    }

    @Override
    public String getName() {
        return "buildup";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/buildup effects add <entity> <effect>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) throw new CommandException("usage: /buildup effects add <entity> <effect> <damage>");
        if (args[0].equals("effects")) {
            if (args.length < 2) throw new CommandException("usage: /buildup effects add <entity> <effect> <damage>");
            if (args[1].equals("add")) {
                if (args.length < 5) throw new CommandException("usage: /buildup effects add <entity> <effect> <damage>");
                EntityLivingBase entity = getEntity(server, sender, args[2], EntityLivingBase.class);
                BuildupEffect effect = Effects.get(args[3]);
                if (!(effect instanceof InstancedEffect<?> instancedEffect)) throw new CommandException("Unknown effect: " + args[4]);
                EffectTracker tracker = EffectTracker.get(entity);
                if (tracker == null) throw new CommandException("Entity doesn't have an effect tracker somehow");
                tracker.getOrCreateInstance(instancedEffect).proc(entity, (float) parseDouble(args[4]), null);
                S2CBuildupProc message = new S2CBuildupProc(entity, effect);
                BuildupPacketHandler.INSTANCE.sendToAllTracking(message, entity);
                if (entity instanceof EntityPlayerMP playerMP) BuildupPacketHandler.INSTANCE.sendTo(message, playerMP);
                sender.sendMessage(new TextComponentString("Gave effect to entity"));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) return List.of("effects");
        if (args.length == 2) return List.of("add");
        if (args.length == 3) return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        if (args.length == 4) {
            List<String> list = new ArrayList<>();
            Effects.forEach(effect -> list.add(effect.getId()));
            return list;
        }
        return Collections.emptyList();
    }
}
