package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;

public class Graphic implements Component {
    public Texture texture;

    public Graphic(Texture texture) {
        this.texture = texture;
    }

    public Graphic() {
        this(null);
    }
}
