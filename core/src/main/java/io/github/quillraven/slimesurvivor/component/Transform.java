package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Transform implements Component, Comparable<Transform> {
    public final Rectangle rect;

    public Transform(float x, float y, float width, float height) {
        this.rect = new Rectangle(x, y, width, height);
    }

    public Transform(float width, float height) {
        this(0f, 0f, width, height);
    }

    public Vector2 getCenter(Vector2 out) {
        out.set(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f);
        return out;
    }

    @Override
    public int compareTo(Transform other) {
        int yDiff = Float.compare(other.rect.y, rect.y);
        if (yDiff == 0) {
            return Float.compare(rect.x, other.rect.x);
        }
        return yDiff;
    }
}
