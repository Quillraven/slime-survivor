package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.quillraven.slimesurvivor.component.LifeSpan;

public class LifeSpanSystem extends IteratingSystem {
    public LifeSpanSystem() {
        super(Family.all(LifeSpan.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LifeSpan lifeSpan = entity.getComponent(LifeSpan.class);
        lifeSpan.duration -= deltaTime;
        if (lifeSpan.duration <= 0f) {
            getEngine().removeEntity(entity);
        }
    }
}
