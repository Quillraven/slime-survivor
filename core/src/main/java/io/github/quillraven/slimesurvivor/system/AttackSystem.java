package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.quillraven.slimesurvivor.component.Animation;
import io.github.quillraven.slimesurvivor.component.Attack;
import io.github.quillraven.slimesurvivor.component.Graphic;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

public class AttackSystem extends IteratingSystem implements Disposable {
    private static final float SIZE = 1f;
    private static final Vector2 TMP_VEC2 = new Vector2();

    private final Sound slashSfx = Gdx.audio.newSound(Gdx.files.internal("slash.wav"));
    private final Array<Texture> attackTextures = loadAttackTextures();
    private final com.badlogic.gdx.graphics.g2d.Animation<Texture> attackAnimation = new com.badlogic.gdx.graphics.g2d.Animation<>(1 / 12f, attackTextures);

    public AttackSystem() {
        super(Family.all(Player.class).get());
    }

    private Array<Texture> loadAttackTextures() {
        Array<Texture> textures = new Array<>(14);
        for (int i = 0; i <= 13; i++) {
            textures.add(new Texture(Gdx.files.internal(String.format("slash_%02d.png", i))));
        }
        return textures;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Player player = entity.getComponent(Player.class);
        player.attackTimer -= deltaTime;
        if (player.attackTimer > 0f) {
            return;
        }

        player.attackTimer = Player.ATTACK_COOLDOWN;
        slashSfx.play();

        Transform playerTransform = entity.getComponent(Transform.class);
        Vector2 playerCenter = playerTransform.getCenter(TMP_VEC2);
        float hitboxX = playerCenter.x - SIZE / 2 + player.lastDirection.x * SIZE * 0.75f;
        float hitboxY = playerCenter.y - SIZE / 2 + player.lastDirection.y * SIZE * 0.75f;

        Entity attackEntity = getEngine().createEntity();
        Transform attackTransform = new Transform();
        attackTransform.rect.setPosition(hitboxX, hitboxY);
        attackTransform.rect.setSize(SIZE, SIZE);
        attackEntity.add(attackTransform);
        attackEntity.add(new Graphic());
        attackEntity.add(new Attack());
        Animation animation = new Animation();
        animation.gdxAnimation = attackAnimation;
        attackEntity.add(animation);
        getEngine().addEntity(attackEntity);
    }

    @Override
    public void dispose() {
        slashSfx.dispose();
        attackTextures.forEach(Texture::dispose);
    }
}
