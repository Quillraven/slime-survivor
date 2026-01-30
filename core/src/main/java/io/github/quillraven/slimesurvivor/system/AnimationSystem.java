package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.quillraven.slimesurvivor.component.Animation;
import io.github.quillraven.slimesurvivor.component.Graphic;

public class AnimationSystem extends IteratingSystem {
    public AnimationSystem() {
        super(Family.all(Animation.class, Graphic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Animation animation = entity.getComponent(Animation.class);
        animation.stateTime += deltaTime * animation.speed;
        entity.getComponent(Graphic.class).texture = animation.gdxAnimation.getKeyFrame(animation.stateTime, true);
    }
}
