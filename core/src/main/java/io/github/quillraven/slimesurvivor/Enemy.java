package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Enemy extends GameObject {
    private static final float SIZE = 0.75f;
    private static final float SPEED = 1.5f;

    private final Player player;

    public Enemy(float x, float y, Player player) {
        super(x, y, SIZE, SIZE);
        this.player = player;
    }

    @Override
    void update(float deltaTime) {
        Vector2 direction = player.getCenter(TMP_VEC2)
            .sub(rect.getX(), rect.getY()) // direction to player
            .nor() // normalize to avoid faster diagonal movement
            .scl(SPEED * deltaTime); // multiply by enemy speed

        // move towards the player center
        rect.setPosition(rect.getX() + direction.x, rect.getY() + direction.y);
    }

    public static Enemy spawn(Viewport gameViewport, Player player) {
        int edge = MathUtils.random(3); // 0: top, 1: right, 2: bottom, 3: left
        float x, y;

        switch (edge) {
            case 0 -> { // Top
                x = MathUtils.random(0, 1) * gameViewport.getWorldWidth();
                y = gameViewport.getWorldHeight();
            }
            case 1 -> { // Right
                x = gameViewport.getWorldWidth();
                y = MathUtils.random(0, 1) * gameViewport.getWorldHeight();
            }
            case 2 -> { // Bottom
                x = MathUtils.random(0, 1) * gameViewport.getWorldWidth();
                y = -Enemy.SIZE;
            }
            default -> { // Left
                x = -Enemy.SIZE;
                y = MathUtils.random(0, 1) * gameViewport.getWorldHeight();
            }
        }

        return new Enemy(x, y, player);
    }
}
