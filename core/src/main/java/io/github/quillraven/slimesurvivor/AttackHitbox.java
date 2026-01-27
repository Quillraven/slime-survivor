package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Vector2;

public class AttackHitbox extends GameObject {
    private static final float SIZE = 1f;
    private static final float DURATION = 0.2f;

    private float lifeSpan = DURATION;

    public AttackHitbox(Vector2 position, Vector2 direction) {
        float hitboxX = position.x - SIZE / 2 + direction.x * SIZE * 0.75f;
        float hitboxY = position.y - SIZE / 2 + direction.y * SIZE * 0.75f;
        super(hitboxX, hitboxY, SIZE, SIZE);
    }

    @Override
    void update(float deltaTime) {
        // Update life span
        this.lifeSpan -= deltaTime;
    }

    public boolean isDone() {
        return this.lifeSpan <= 0f;
    }
}
