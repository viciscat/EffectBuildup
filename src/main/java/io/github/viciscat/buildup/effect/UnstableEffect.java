package io.github.viciscat.buildup.effect;

public class UnstableEffect extends EffectInstance {
    public UnstableEffect() {
        super(new StackDurationStrategy.RefreshStrategy(60 * 20));
    }

    public float getBuildupMultiplier() {
        if (getStacks() > 10) {
            return 2 + ((getStacks() - 10) * 0.00125f);
        } else {
            return 1f + getStacks() * 0.1f;
        }
    }

}
