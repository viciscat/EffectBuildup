package io.github.viciscat.buildup.particle;

import io.github.viciscat.buildup.BuildupUtils;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BuildupParticleManager {
    public static final Random RANDOM = new Random();
    public static final BuildupParticleManager INSTANCE = new BuildupParticleManager();
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/particles.png");
    private final List<BasicParticle> particles = new LinkedList<>();

    public static TextureAtlasSprite createSprite(String name, int x, int y, int width, int height) {
        Sprite sprite = new Sprite(name, width, height);
        sprite.initSprite(256, 256, x, y, false);
        return sprite;
    }

    public static Vec3d randomPositionAround(Entity entity) {
        return randomPositionAround(entity, 0f);
    }

    public static Vec3d randomPositionAround(Entity entity, float extraRandomDistance) {
        float v = BuildupUtils.INV_SQRT_2_F * entity.width + extraRandomDistance * RANDOM.nextFloat();
        float randomAngle = (float) (RANDOM.nextDouble() * Math.TAU);
        float cos = MathHelper.cos(randomAngle);
        float sin = MathHelper.sin(randomAngle);
        return entity.getPositionVector().add(cos * v, 0, sin * v);
    }

    private BuildupParticleManager() {}

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().world == null) return;
        update();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) return;
        particles.clear();
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        renderParticles(event.getPartialTicks());
    }

    public void addParticle(BasicParticle particle) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("This should never happen! Current thread: " + Thread.currentThread());
        }
        particles.add(particle);
    }

    public void update() {
        Iterator<BasicParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            BasicParticle particle = iterator.next();
            particle.onUpdate();
            if (!particle.isAlive()) iterator.remove();
        }
    }

    public void renderParticles(float partialTicks) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) return;
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        float x = ActiveRenderInfo.getRotationX();
        float z = ActiveRenderInfo.getRotationZ();
        float yz = ActiveRenderInfo.getRotationYZ();
        float xy = ActiveRenderInfo.getRotationXY();
        float xz = ActiveRenderInfo.getRotationXZ();
        Particle.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        Particle.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        Particle.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
        Particle.cameraViewDir = entity.getLook(partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 1 / 255f);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        particles.forEach(particle -> {
            float prevAlpha = particle.getAlpha();
            Vec3d position = ActiveRenderInfo.getCameraPosition();
            double distance = position.squareDistanceTo(particle.getPosition());
            if (distance < 0.3) {
                float alpha = (float) distance / 0.3f;
                particle.setAlphaF(prevAlpha * alpha);
            }
            particle.renderParticle(buffer, entity, partialTicks, x, xz, z, yz, xy);
            particle.setAlphaF(prevAlpha);
        });
        tessellator.draw();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
    }

    private static class Sprite extends TextureAtlasSprite {

        protected Sprite(String spriteName, int width, int height) {
            super(spriteName);
            this.width = width;
            this.height = height;
        }
    }
}
