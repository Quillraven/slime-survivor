package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public static final float ENEMY_SIZE = 40f;
    public static final float ENEMY_SPEED = 150f;

    private final Vector2 position;
    private final Rectangle rect;

    public Enemy(float x, float y) {
        position = new Vector2(x, y);
        rect = new Rectangle(x, y, ENEMY_SIZE, ENEMY_SIZE);
    }

    void update(float playerX, float playerY, float delta) {
        float directionX = playerX - position.x;
        float directionY = playerY - position.y;
        float len = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (len != 0) {
            directionX /= len;
            directionY /= len;
        }

        position.x += directionX * ENEMY_SPEED * delta;
        position.y += directionY * ENEMY_SPEED * delta;
        rect.setPosition(position.x, position.y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getRect() {
        return rect;
    }
}
