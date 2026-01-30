package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.quillraven.slimesurvivor.component.Animation;
import io.github.quillraven.slimesurvivor.component.Attack;
import io.github.quillraven.slimesurvivor.component.Graphic;

public class AnimationSystem extends IteratingSystem {
    public AnimationSystem() {
        super(Family.all(Animation.class, Graphic.class, Attack.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        var gdxAnimation = entity.getComponent(Animation.class).gdxAnimation;
        Attack attack = entity.getComponent(Attack.class);

        float animationDuration = gdxAnimation.getAnimationDuration();
        float animationPerc = 1f - (Math.max(0f, attack.lifeSpan) / Attack.DURATION);
        float stateTime = animationDuration * animationPerc;
        entity.getComponent(Graphic.class).texture = gdxAnimation.getKeyFrame(stateTime, true);
    }
}
