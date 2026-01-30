package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Transform implements Component, Comparable<Transform> {
    public final Rectangle rect = new Rectangle();

    public Vector2 getCenter(Vector2 out) {
        out.set(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f);
        return out;
    }

    @Override
    public int compareTo(Transform other) {
        int yDiff = Float.compare(rect.y, other.rect.y);
        if (yDiff == 0) {
            return Float.compare(rect.x, other.rect.x);
        }
        return yDiff;
    }
}
