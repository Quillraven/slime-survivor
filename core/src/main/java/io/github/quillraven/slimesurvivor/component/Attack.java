package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;

public class Attack implements Component {
    public static final float DURATION = 0.4f;

    public float lifeSpan = DURATION;
}
