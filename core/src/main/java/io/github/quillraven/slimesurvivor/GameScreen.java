package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
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

    // General
    private final Batch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final BitmapFont font;

    // Player
    private Vector2 playerPosition;
    private Rectangle playerRect;
    private final Vector2 inputMovement = new Vector2();
    private final Vector2 lastMovementDirection = new Vector2();

    // Combat
    private float attackTimer;
    private float attackHitboxTimer;
    private Rectangle attackHitbox;
    private boolean attackActive;

    // Enemies
    private final Array<Enemy> enemies = new Array<>();
    private float enemySpawnTimer;

    // Game State
    private int score;
    private float life;
    private boolean gameOver;

    public GameScreen(GdxGame game) {
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();

        // Generate font from TrueType file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("CherryCreamSoda-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void show() {
        resetGame();
    }

    private void resetGame() {
        playerPosition = new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        playerRect = new Rectangle(playerPosition.x, playerPosition.y, PLAYER_SIZE, PLAYER_SIZE);
        lastMovementDirection.set(1, 0); // default facing right

        attackTimer = 0f;
        attackHitboxTimer = 0f;
        attackActive = false;
        attackHitbox = new Rectangle();

        enemies.clear();
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
            checkCollisions(delta);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        draw();
    }

    private void processInput(float delta) {
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

        if (!inputMovement.isZero()) {
            inputMovement.nor();
            lastMovementDirection.set(inputMovement);
            playerPosition.mulAdd(inputMovement, PLAYER_SPEED * delta);

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
            enemy.update(playerPosition.x + PLAYER_SIZE / 2, playerPosition.y + PLAYER_SIZE / 2, delta);
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

    private void checkCollisions(float delta) {
        // Check attack hitbox vs enemies
        if (attackActive) {
            var iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                if (attackHitbox.overlaps(enemy.getRect())) {
                    iterator.remove();
                    score++;
                }
            }
        }

        // Check enemies vs player
        int numHits = 0;
        for (Enemy enemy : enemies) {
            if (playerRect.overlaps(enemy.getRect())) {
                ++numHits;
            }
        }

        if (numHits > 0) {
            life -= DAMAGE_PER_SECOND * numHits * delta;
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
            shapeRenderer.rect(enemy.getPosition().x, enemy.getPosition().y, Enemy.ENEMY_SIZE, Enemy.ENEMY_SIZE);
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
            font.draw(batch, "GAME OVER", WORLD_WIDTH / 2 - 200, WORLD_HEIGHT / 2 + 50);
            font.draw(batch, "Press R to Restart", WORLD_WIDTH / 2 - 250, WORLD_HEIGHT / 2 - 50);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
