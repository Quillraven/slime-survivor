package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;

public class Animation implements Component {
    public com.badlogic.gdx.graphics.g2d.Animation<Texture> gdxAnimation;
    public float speed;
    public float stateTime = 0f;

    public Animation(com.badlogic.gdx.graphics.g2d.Animation<Texture> gdxAnimation, float speed) {
        this.gdxAnimation = gdxAnimation;
        this.speed = speed;
    }
}
