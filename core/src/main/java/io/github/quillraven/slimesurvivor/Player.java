package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Vector2;

public class Player extends GameObject {
    private static final float SIZE = 0.75f;
    private static final float SPEED = 2f;
    private static final float LIFE = 5f;
    private static final float ATTACK_COOLDOWN = 1.0f;

    private final Vector2 moveDirection = new Vector2(1, 0); // default facing right
    private float life = LIFE;
    private float attackTimer;

    public Player(float x, float y) {
        super(x, y, SIZE, SIZE);
        reset(x, y);
    }

    public void reset(float x, float y) {
        rect.setPosition(x, y);
        life = LIFE;
        attackTimer = ATTACK_COOLDOWN;
    }

    public void move(Vector2 direction, float delta, float maxW, float maxH) {
        moveDirection.set(direction);
        float newX = rect.getX() + direction.x * SPEED * delta;
        float newY = rect.getY() + direction.y * SPEED * delta;

        // Clamp to screen bounds
        newX = Math.clamp(newX, 0, maxW - rect.getWidth());
        newY = Math.clamp(newY, 0, maxH - rect.getHeight());

        rect.setPosition(newX, newY);
    }

    public Vector2 getMoveDirection() {
        return moveDirection;
    }

    public float getLife() {
        return life;
    }

    public void subLife(float amount) {
        this.life -= amount;
    }

    public boolean isDead() {
        return life <= 0f;
    }

    public boolean canAttack(float delta) {
        attackTimer -= delta;
        if (attackTimer <= 0f) {
            attackTimer = ATTACK_COOLDOWN;
            return true;
        }

        return false;
    }
}
