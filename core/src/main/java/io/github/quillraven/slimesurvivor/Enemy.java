package io.github.quillraven.slimesurvivor;

public class Enemy extends GameObject {
    public static final float SIZE = 0.75f;
    private static final float SPEED = 1.5f;

    public Enemy(float x, float y) {
        super(x, y, SIZE, SIZE);
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
            rect.getX() + directionX * SPEED * delta,
            rect.getY() + directionY * SPEED * delta
        );
    }
}
