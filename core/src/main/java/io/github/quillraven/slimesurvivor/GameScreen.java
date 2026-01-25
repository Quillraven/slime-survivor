package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private static final float WORLD_WIDTH = 1920f;
    private static final float WORLD_HEIGHT = 1080f;
    private static final float PLAYER_SIZE = 50f;
    private static final float PLAYER_SPEED = 300f;
    private static final float PLAYER_LIFE = 5f;
    private static final float ATTACK_COOLDOWN = 1.0f;
    private static final float ATTACK_DURATION = 0.2f;
    private static final float ENEMY_SPAWN_INTERVAL = 1.5f;
    private static final float HITBOX_SIZE = 60f;
    private static final float HITBOX_OFFSET = 60f;
    private static final float DAMAGE_PER_SECOND = 1.0f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // Player
    private Vector2 playerPosition;
    private Rectangle playerRect;
    private Vector2 lastMovementDirection;

    // Combat
    private float attackTimer;
    private float attackHitboxTimer;
    private Rectangle attackHitbox;
    private boolean attackActive;

    // Enemies
    private List<Enemy> enemies;
    private float enemySpawnTimer;

    // Game State
    private int score;
    private float life;
    private boolean gameOver;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);

        playerPosition = new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        playerRect = new Rectangle(playerPosition.x, playerPosition.y, PLAYER_SIZE, PLAYER_SIZE);
        lastMovementDirection = new Vector2(1, 0); // Default facing right

        attackTimer = 0f;
        attackHitboxTimer = 0f;
        attackActive = false;
        attackHitbox = new Rectangle();

        enemies = new ArrayList<>();
        enemySpawnTimer = 0f;

        score = 0;
        life = PLAYER_LIFE;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        if (!gameOver) {
            processInput(delta);
            updateLogic(delta);
            checkCollisions();
        }

        draw();
    }

    private void processInput(float delta) {
        Vector2 movement = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement.x += 1;
        }

        if (!movement.isZero()) {
            movement.nor();
            lastMovementDirection.set(movement);
            playerPosition.mulAdd(movement, PLAYER_SPEED * delta);

            // Clamp to screen bounds
            playerPosition.x = Math.clamp(playerPosition.x, 0, WORLD_WIDTH - PLAYER_SIZE);
            playerPosition.y = Math.clamp(playerPosition.y, 0, WORLD_HEIGHT - PLAYER_SIZE);

            playerRect.setPosition(playerPosition.x, playerPosition.y);
        }
    }

    private void updateLogic(float delta) {
        // Attack timer
        attackTimer += delta;
        if (attackTimer >= ATTACK_COOLDOWN) {
            attackTimer = 0f;
            triggerAttack();
        }

        // Attack hitbox duration
        if (attackActive) {
            attackHitboxTimer += delta;
            if (attackHitboxTimer >= ATTACK_DURATION) {
                attackActive = false;
                attackHitboxTimer = 0f;
            }
        }

        // Enemy spawning
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL) {
            enemySpawnTimer = 0f;
            spawnEnemy();
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            enemy.update(new Vector2(playerPosition.x + PLAYER_SIZE / 2, playerPosition.y + PLAYER_SIZE / 2), delta);
        }
    }

    private void triggerAttack() {
        attackActive = true;
        attackHitboxTimer = 0f;

        // Calculate hitbox position based on last movement direction
        float hitboxX = playerPosition.x + PLAYER_SIZE / 2 - HITBOX_SIZE / 2 + lastMovementDirection.x * HITBOX_OFFSET;
        float hitboxY = playerPosition.y + PLAYER_SIZE / 2 - HITBOX_SIZE / 2 + lastMovementDirection.y * HITBOX_OFFSET;

        attackHitbox.set(hitboxX, hitboxY, HITBOX_SIZE, HITBOX_SIZE);
    }

    private void spawnEnemy() {
        int edge = MathUtils.random(3); // 0: top, 1: right, 2: bottom, 3: left
        float x, y;

        switch (edge) {
            case 0 -> { // Top
                x = MathUtils.random(0, 1) * WORLD_WIDTH;
                y = WORLD_HEIGHT;
            }
            case 1 -> { // Right
                x = WORLD_WIDTH;
                y = MathUtils.random(0, 1) * WORLD_HEIGHT;
            }
            case 2 -> { // Bottom
                x = MathUtils.random(0, 1) * WORLD_WIDTH;
                y = -Enemy.ENEMY_SIZE;
            }
            default -> { // Left
                x = -Enemy.ENEMY_SIZE;
                y = MathUtils.random(0, 1) * WORLD_HEIGHT;
            }
        }

        enemies.add(new Enemy(x, y));
    }

    private void checkCollisions() {
        // Check attack hitbox vs enemies
        if (attackActive) {
            var iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                if (attackHitbox.overlaps(enemy.rect)) {
                    iterator.remove();
                    score++;
                }
            }
        }

        // Check enemies vs player
        boolean playerHit = false;
        for (Enemy enemy : enemies) {
            if (playerRect.overlaps(enemy.rect)) {
                playerHit = true;
                break;
            }
        }

        if (playerHit) {
            life -= DAMAGE_PER_SECOND * Gdx.graphics.getDeltaTime();
            if (life <= 0) {
                life = 0;
                gameOver = true;
            }
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw player (green)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(playerPosition.x, playerPosition.y, PLAYER_SIZE, PLAYER_SIZE);

        // Draw enemies (red)
        shapeRenderer.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            shapeRenderer.rect(enemy.position.x, enemy.position.y, Enemy.ENEMY_SIZE, Enemy.ENEMY_SIZE);
        }

        // Draw attack hitbox (yellow)
        if (attackActive) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }

        shapeRenderer.end();

        // Draw UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, WORLD_HEIGHT - 20);
        font.draw(batch, "Life: " + String.format("%.1f", life), 20, WORLD_HEIGHT - 60);
        if (gameOver) {
            font.draw(batch, "GAME OVER", WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
