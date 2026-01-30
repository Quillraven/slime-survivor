package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

public class AudioSystem extends EntitySystem implements Disposable {
    private final Music music = Gdx.audio.newMusic(Gdx.files.internal("nightsplitter.mp3"));

    public AudioSystem() {
        resetMusic();
    }

    public void resetMusic() {
        music.stop();
        music.setLooping(true);
        music.play();
    }

    @Override
    public void dispose() {
        music.dispose();
    }
}
