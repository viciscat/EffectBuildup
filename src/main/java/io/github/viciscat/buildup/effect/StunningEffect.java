package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class StunningEffect extends EffectInstance {
    protected int stunTime;
    private boolean shouldStun;

    protected StunningEffect(StackDurationStrategy stackDurationStrategy) {
        super(stackDurationStrategy);
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        if (target instanceof EntityLiving living && !living.world.isRemote) {
            if (stunTime > 0) {
                stunTime--;
                if (stunTime <= 0) {
                    living.tasks.removeTask(AIStun.INSTANCE);
                } else {
                    living.getNavigator().clearPath();
                    living.tasks.addTask(-1, AIStun.INSTANCE);
                }
            }
            boolean prevShouldStun = shouldStun;
            shouldStun = stunTime > 0;
            if (shouldStun != prevShouldStun) {
                shouldSync = true;
            }
        }
    }

    public boolean shouldBeStunned() {
        return shouldStun;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("stunTime", stunTime);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        stunTime = nbt.getInteger("stunTime");
    }

    @Override
    public void read(PacketBuffer buf) {
        super.read(buf);
        shouldStun = buf.readBoolean();
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeBoolean(shouldStun);
    }

    // Thank you ender skills, cleaner than cancelling the update method in a mixin imo :p
    private static class AIStun extends EntityAIBase {
        private static final AIStun INSTANCE = new AIStun();

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }
    }

    protected static final class EventHandler {
        private final InstancedEffect<? extends StunningEffect> effect;

        protected EventHandler(InstancedEffect<? extends StunningEffect> effect) {
            this.effect = effect;
        }

        @SubscribeEvent
        public void cancelMovementIfStunned(InputUpdateEvent event) {
            EffectTracker tracker = EffectTracker.get(event.getEntityLiving());
            if (tracker == null) return;
            if (tracker.getInstance(effect).filter(StunningEffect::shouldBeStunned).isPresent()) {
                MovementInput input = event.getMovementInput();
                input.backKeyDown = false;
                input.forwardKeyDown = false;
                input.leftKeyDown = false;
                input.rightKeyDown = false;
                input.moveStrafe = 0f;
                input.moveForward = 0f;
                input.jump = false;
                input.sneak = false;
            }
        }

        @SubscribeEvent
        public void cancelMouseMovementIfStunned(MouseEvent event) {
            EffectTracker tracker = EffectTracker.get(Minecraft.getMinecraft().player);
            if (tracker == null) return;
            if (tracker.getInstance(effect).filter(StunningEffect::shouldBeStunned).isPresent()) {
                Minecraft.getMinecraft().mouseHelper.deltaX = Minecraft.getMinecraft().mouseHelper.deltaY = 0;
            }
        }

        @SubscribeEvent
        public void cancelMouseClickIfStunned(InputEvent.MouseInputEvent event) {
            EffectTracker tracker = EffectTracker.get(Minecraft.getMinecraft().player);
            if (tracker == null) return;
            if (tracker.getInstance(effect).filter(StunningEffect::shouldBeStunned).isPresent()) {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
            }
        }


    }
}
