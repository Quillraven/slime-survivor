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
    public static final float WORLD_WIDTH = 1920f;
    public static final float WORLD_HEIGHT = 1080f;
    private static final float ENEMY_SPAWN_INTERVAL = 1.5f;
    private static final float DAMAGE_PER_SECOND = 1.0f;

    // General
    private final Batch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final BitmapFont font;

    // Player
    private final Player player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
    private final Vector2 inputMovement = new Vector2();

    // Combat
    private final Array<AttackHitbox> attackHitboxes = new Array<>();

    // Enemies
    private final Array<Enemy> enemies = new Array<>();
    private float enemySpawnTimer;

    // Game State
    private int score;
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
        player.reset(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);

        attackHitboxes.clear();

        enemies.clear();
        enemySpawnTimer = 0f;

        score = 0;
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
            player.move(inputMovement, delta);
        }
    }

    private void updateLogic(float delta) {
        // Attack timer
        if (player.canAttack(delta)) {
            triggerAttack();
        }

        // Attack hitbox duration
        var iterator = attackHitboxes.iterator();
        while (iterator.hasNext()) {
            AttackHitbox attackHitbox = iterator.next();
            if (attackHitbox.updateLifeSpan(delta)) {
                iterator.remove();
            }
        }

        // Enemy spawning
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL) {
            enemySpawnTimer = 0f;
            spawnEnemy();
        }

        // Update enemies
        float playerCenterX = player.getRect().getX() + Player.SIZE * 0.5f;
        float playerCenterY = player.getRect().getY() + Player.SIZE * 0.5f;
        for (Enemy enemy : enemies) {
            enemy.update(playerCenterX, playerCenterY, delta);
        }
    }

    private void triggerAttack() {
        float hitboxX = player.getRect().getX() + Player.SIZE * 0.5f;
        float hitboxY = player.getRect().getY() + Player.SIZE * 0.5f;
        attackHitboxes.add(new AttackHitbox(hitboxX, hitboxY, player.getMoveDirection()));
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
        for (AttackHitbox attackHitbox : attackHitboxes) {
            var iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                if (attackHitbox.getRect().overlaps(enemy.getRect())) {
                    iterator.remove();
                    score++;
                }
            }
        }

        // Check enemies vs player
        int numHits = 0;
        for (Enemy enemy : enemies) {
            if (player.getRect().overlaps(enemy.getRect())) {
                ++numHits;
            }
        }

        if (numHits > 0) {
            player.subLife(DAMAGE_PER_SECOND * numHits * delta);
            gameOver = player.isDead();
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
        shapeRenderer.rect(player.getRect().getX(), player.getRect().getY(), Player.SIZE, Player.SIZE);

        // Draw enemies (red)
        shapeRenderer.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            shapeRenderer.rect(enemy.getRect().getX(), enemy.getRect().getY(), Enemy.ENEMY_SIZE, Enemy.ENEMY_SIZE);
        }

        // Draw attack hitbox (yellow)
        shapeRenderer.setColor(Color.YELLOW);
        for (AttackHitbox attackHitbox : attackHitboxes) {
            Rectangle rect = attackHitbox.getRect();
            shapeRenderer.rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        }
        shapeRenderer.end();

        // Draw UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, WORLD_HEIGHT - 20);
        font.draw(batch, "Life: " + String.format("%.1f", player.getLife()), 20, WORLD_HEIGHT - 60);
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
