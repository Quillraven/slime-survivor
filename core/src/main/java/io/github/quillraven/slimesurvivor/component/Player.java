package io.github.quillraven.slimesurvivor.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class Player implements Component {
    public static final float ATTACK_COOLDOWN = 1.6f;

    public int score = 0;
    public float lives;
    public float maxLives;
    public float attackTimer = ATTACK_COOLDOWN;
    public final Vector2 lastDirection = new Vector2(1, 0); // used for attacks; default is right

    public Player(float lives) {
        this.lives = lives;
        this.maxLives = lives;
    }

    public void reset() {
        score = 0;
        lives = maxLives;
        attackTimer = ATTACK_COOLDOWN;
    }

    public boolean isDead() {
        return lives <= 0f;
    }
}
