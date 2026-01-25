package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Enemy {
    public static final float ENEMY_SIZE = 40f;
    public static final float ENEMY_SPEED = 150f;

    Vector2 position;
    Rectangle rect;

    Enemy(float x, float y) {
        position = new Vector2(x, y);
        rect = new Rectangle(x, y, ENEMY_SIZE, ENEMY_SIZE);
    }

    void update(Vector2 playerPos, float delta) {
        Vector2 direction = new Vector2(playerPos).sub(position).nor();
        position.mulAdd(direction, ENEMY_SPEED * delta);
        rect.setPosition(position.x, position.y);
    }
}
