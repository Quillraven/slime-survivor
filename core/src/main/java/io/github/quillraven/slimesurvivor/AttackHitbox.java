package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Vector2;

public class AttackHitbox extends GameObject {
    private static final float SIZE = 60f;
    private static final float DURATION = 0.2f;

    private float lifeSpan = DURATION;

    public AttackHitbox(float x, float y, Vector2 direction) {
        float hitboxX = x - SIZE / 2 + direction.x * SIZE * 0.75f;
        float hitboxY = y - SIZE / 2 + direction.y * SIZE * 0.75f;
        super(hitboxX, hitboxY, SIZE, SIZE);
    }

    public boolean updateLifeSpan(float delta) {
        this.lifeSpan -= delta;
        return this.lifeSpan <= 0f;
    }
}
