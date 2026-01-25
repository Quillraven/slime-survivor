package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public static final float ENEMY_SIZE = 40f;
    public static final float ENEMY_SPEED = 150f;

    private final Rectangle rect;

    public Enemy(float x, float y) {
        rect = new Rectangle(x, y, ENEMY_SIZE, ENEMY_SIZE);
    }

    void update(float playerX, float playerY, float delta) {
        float directionX = playerX - rect.getX();
        float directionY = playerY - rect.getY();
        float len = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (len != 0) {
            directionX /= len;
            directionY /= len;
        }

        rect.setPosition(
            rect.getX() + directionX * ENEMY_SPEED * delta,
            rect.getY() + directionY * ENEMY_SPEED * delta
        );
    }

    public Rectangle getRect() {
        return rect;
    }
}
