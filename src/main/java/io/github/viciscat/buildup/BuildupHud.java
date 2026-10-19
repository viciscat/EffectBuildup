package io.github.viciscat.buildup;

import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.network.BuildupUpdate;
import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class BuildupHud extends Gui {
    public static final BuildupHud INSTANCE = new BuildupHud();
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/buildup_hud.png");
    private final LinkedList<TimedUpdate> updates = new LinkedList<>();
    private final Object2FloatMap<BuildupEffect> buildupProgress = new Object2FloatLinkedOpenHashMap<>(15);

    private BuildupHud() {}

    public void addUpdate(BuildupUpdate update) {
        ListIterator<TimedUpdate> iterator = updates.listIterator();
        while (iterator.hasNext()) {
            TimedUpdate next = iterator.next();
            if (next.update().effect() == update.effect()) {
                if (update.decay()) {
                    iterator.set(new TimedUpdate(next.timestamp(), update));
                    return;
                } else {
                    iterator.remove();
                    break;
                }
            }
        }
        if (!update.decay()) {
            long time = System.currentTimeMillis();
            updates.add(new TimedUpdate(time, update));
        }
        buildupProgress.put(update.effect(), update.value() / update.max());
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        long millis = System.currentTimeMillis();
        updates.removeIf(timedUpdate -> millis - timedUpdate.timestamp() > 4_000L);
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        Minecraft.getMinecraft().getTextureManager().bindTexture(HUD_TEXTURE);
        if (updates.size() <= 3) {
            Iterator<TimedUpdate> iterator = updates.descendingIterator();
            final int textureWidth = 158;
            final int middleX = event.getResolution().getScaledWidth() / 2;
            final int startY = event.getResolution().getScaledHeight() - 60 - 30;
            final int barX = middleX - textureWidth / 2;
            for (int i = 0; i < 3 && iterator.hasNext(); i++) {
                final int barY = startY - i * 28;
                BuildupUpdate update = iterator.next().update();
                renderEffectIcon(update.effect(), middleX - textureWidth / 2f + 4, barY + 4);
                drawTexturedModalRect(barX, barY, 0, 128, textureWidth, 24);
                final int fillX = barX + 27;
                final int fillY = barY + 7;
                final int fillWidth = 127;
                drawRect(fillX, fillY, (int) (fillX + update.value() / update.max() * fillWidth), fillY + 10, update.effect().getDisplay().color());
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableBlend();
            }
        } else {
            MutableInt mutableInt = new MutableInt();
            Effects.forEach(effect -> {
                final int id = mutableInt.getAndIncrement();
                final int x = (id % 5) * 20 + 10;
                final int y = (id / 5) * 20 + 40;
                float progress = buildupProgress.getFloat(effect);
                drawRect(x, (int) (y + (1 - progress) * 20), x + 20, y + 20, effect.getDisplay().color());
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableBlend();
                renderEffectIcon(effect, x + 2, y + 2);
            });
        }
    }

    private void renderEffectIcon(BuildupEffect effect, float x, float y) {
        final int iconX = effect.getDisplay().iconId() % 8;
        final int iconY = effect.getDisplay().iconId() / 8;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(0.5, 0.5, 1);
        drawTexturedModalRect(0, 0, iconX * 32, iconY * 32, 32, 32);
        GlStateManager.popMatrix();
    }

    private record TimedUpdate(long timestamp, BuildupUpdate update) {}
}
