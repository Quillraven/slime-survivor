package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.github.quillraven.slimesurvivor.component.Controls;
import io.github.quillraven.slimesurvivor.component.Move;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

import static io.github.quillraven.slimesurvivor.GameScreen.WORLD_HEIGHT;
import static io.github.quillraven.slimesurvivor.GameScreen.WORLD_WIDTH;

public class ControlsSystem extends IteratingSystem {
    private final Vector2 inputMovement = new Vector2();

    private ImmutableArray<Entity> playerEntities = null;

    public ControlsSystem() {
        super(Family.all(Controls.class, Move.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        playerEntities = getEngine().getEntitiesFor(Family.all(Player.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (playerEntities != null && playerEntities.first().getComponent(Player.class).isDead()) {
            // player is dead -> only check for reset game state
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                resetGame();
            }
            return;
        }

        inputMovement.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            inputMovement.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            inputMovement.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputMovement.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            inputMovement.x += 1;
        }

        inputMovement.nor();
        super.update(deltaTime);
    }

    private void resetGame() {
        // reset player
        Entity player = playerEntities.first();
        player.getComponent(Transform.class).rect.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
        player.getComponent(Player.class).reset();

        // remove any non-player entities
        getEngine().getEntitiesFor(Family.exclude(Player.class).get()).forEach(getEngine()::removeEntity);

        // reset music
        getEngine().getSystem(AudioSystem.class).resetMusic();

        // reset enemy spawn and enable relevant systems again
        getEngine().getSystem(SpawnSystem.class).setProcessing(true);
        getEngine().getSystem(SpawnSystem.class).resetTimer();
        getEngine().getSystem(MoveSystem.class).setProcessing(true);
        getEngine().getSystem(AttackSystem.class).setProcessing(true);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = entity.getComponent(Move.class);
        move.direction.set(inputMovement);

        Player player = entity.getComponent(Player.class);
        if (player != null && !inputMovement.isZero()) {
            player.lastDirection.set(inputMovement);
        }
    }
}
