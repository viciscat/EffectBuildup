package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StunningEffect extends EffectInstance {
    protected int stunTime;
    private boolean shouldStun;

    protected StunningEffect(StackDurationStrategy stackDurationStrategy) {
        super(stackDurationStrategy);
    }

    @Override
    public void tick(EntityLivingBase base) {
        super.tick(base);
        if (base.world.isRemote) return;
        if (stunTime > 0) {
            stunTime--;
            if (base instanceof EntityLiving living) {
                if (stunTime <= 0) {
                    living.tasks.removeTask(AIStun.INSTANCE);
                } else {
                    living.getNavigator().clearPath();
                    living.tasks.addTask(-1, AIStun.INSTANCE);
                }
            }
        }
        boolean prevShouldStun = shouldStun;
        shouldStun = stunTime > 0;
        if (shouldStun != prevShouldStun) {
            shouldSync = true;
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


    }
}
