package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected final Rectangle rect;

    public GameObject(float x, float y, float w, float h) {
        this.rect = new Rectangle(x, y, w, h);
    }

    public boolean overlaps(GameObject other) {
        return rect.overlaps(other.getRect());
    }

    public Rectangle getRect() {
        return rect;
    }

    public void drawDebug(ShapeRenderer shapeRenderer, Color color) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
    }
}
