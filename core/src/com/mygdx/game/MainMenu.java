package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;


/**
 * Created by Chaitu on 4/25/2015.
 */
public class MainMenu implements Screen {
    //main menu of class needs nicer graphics and what not
    final GameController game;
    OrthographicCamera camera;
    private Texture backgroundTexture;

    public MainMenu(final GameController gam){
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
        game.font.draw(game.batch, "Welcome to Drop!!! ", (float)Gdx.graphics.getWidth()/2, (float)(Gdx.graphics.getHeight()/1.5));
        game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);
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
