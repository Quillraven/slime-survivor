package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class GameObject {
    protected static final Vector2 TMP_VEC2 = new Vector2();

    protected final Rectangle rect;

    public GameObject(float x, float y, float w, float h) {
        this.rect = new Rectangle(x, y, w, h);
    }

    public boolean overlaps(GameObject other) {
        return rect.overlaps(other.rect);
    }

    public void drawDebug(ShapeRenderer shapeRenderer, Color color) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
    }

    public Vector2 getCenter(Vector2 out) {
        out.set(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f);
        return out;
    }

    abstract void update(float deltaTime);
}
