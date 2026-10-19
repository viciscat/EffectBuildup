package io.github.viciscat.buildup.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class ParticleSequence {
    protected static final Vec3d UP = new Vec3d(0, 1, 0);
    protected LocalVectors vectors;
    protected int ticks;

    protected ParticleSequence() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected void computeLocalVectors(EntityLivingBase target) {
        Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (cameraEntity == null) return;
        Vec3d cameraEyes = ActiveRenderInfo.getCameraPosition();
        Vec3d front = cameraEyes.subtract(target.getPositionVector());
        front = new Vec3d(front.x, 0, front.z).normalize();
        Vec3d side = front.crossProduct(UP).normalize();
        vectors = new LocalVectors(front, side);
    }

    @SubscribeEvent
    public final void tickEvent(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().world == null) {
            end();
            return;
        }
        if (event.phase == TickEvent.Phase.START) return;
        tick();
        ticks++;
    }

    protected abstract void tick();

    protected void end() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    protected record LocalVectors(Vec3d front, Vec3d side) {}
}
