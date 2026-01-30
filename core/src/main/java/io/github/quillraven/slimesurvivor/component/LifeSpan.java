package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;

public class LifeSpan implements Component {
    public float duration;

    public LifeSpan(float duration) {
        this.duration = duration;
    }
}
