package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.quillraven.slimesurvivor.component.Attack;

public class LifeSpanSystem extends IteratingSystem {
    public LifeSpanSystem() {
        super(Family.all(Attack.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = entity.getComponent(Attack.class);
        attack.lifeSpan -= deltaTime;
        if (attack.lifeSpan <= 0f) {
            getEngine().removeEntity(entity);
        }
    }
}
