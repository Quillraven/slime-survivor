package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class Move implements Component {
    public final Vector2 direction = new Vector2();
    public float speed;

    public Move(float speed) {
        this.speed = speed;
    }
}
