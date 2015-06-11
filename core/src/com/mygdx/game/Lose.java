package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Chaitu on 4/25/2015.
 */
public class Lose implements Screen {
    final GameController game;
    private Texture backgroundTexture;
    public GameScreen score;
    public Lose(final GameController gam){
        game = gam;
        backgroundTexture = new Texture(Gdx.files.internal("sprite.png"));
    }
    @Override
    public void render(float delta) {
        delta = 1/60f;
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "Tap anywhere to play again", (float) Gdx.graphics.getWidth() / 2, (float) (Gdx.graphics.getHeight() / 1.5));
        game.font.draw(game.batch, "score:" + GameScreen.getHighScore(), 100, 100);
        game.batch.end();
        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
