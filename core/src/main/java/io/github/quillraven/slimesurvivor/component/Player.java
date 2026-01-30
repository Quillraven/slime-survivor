package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class Player implements Component {
    private static final float LIFE = 5f;
    public static final float ATTACK_COOLDOWN = 1.6f;

    public int score = 0;
    public float lifes = LIFE;
    public float attackTimer = ATTACK_COOLDOWN;
    public final Vector2 lastDirection = new Vector2(1, 0); // used for attacks; default is right

    public void reset() {
        score = 0;
        lifes = LIFE;
        attackTimer = ATTACK_COOLDOWN;
    }

    public boolean isDead() {
        return lifes <= 0f;
    }
}
