package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public static final float SIZE = 50f;
    private static final float SPEED = 300f;
    private static final float LIFE = 5f;

    private final Vector2 position;
    private final Rectangle rect;
    private final Vector2 moveDirection = new Vector2(1, 0); // default facing right
    private float life = LIFE;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        rect = new Rectangle(x, y, SIZE, SIZE);
    }

    public void reset(float x, float y) {
        position.set(x, y);
        rect.setPosition(x, y);
        life = LIFE;
    }

    public void move(Vector2 direction, float delta) {
        moveDirection.set(direction);
        position.mulAdd(direction, SPEED * delta);

        // Clamp to screen bounds
        position.x = Math.clamp(position.x, 0, GameScreen.WORLD_WIDTH - SIZE);
        position.y = Math.clamp(position.y, 0, GameScreen.WORLD_HEIGHT - SIZE);

        rect.setPosition(position.x, position.y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getMoveDirection() {
        return moveDirection;
    }

    public Rectangle getRect() {
        return rect;
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
}
