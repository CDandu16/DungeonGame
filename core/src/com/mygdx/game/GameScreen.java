package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class GameScreen implements Screen {
	//Constants
	public static final String TITLE = "Bubble Game";
	public static int WIDTH;
	public static int HEIGHT;
	public static final int BUBBLE_SPEED = 2;
	public static final int PPM = 100;
    public static final double BOX_SPEED_DIVISOR =2.5;
	final Life gam;

	private static final String[] MUSICS = {//Todo: generante from /assets/sound/music folder instead of listing
			"The Builder.mp3",
			"Monkeys Spinning Monkeys.mp3",
	};

	private OrthographicCamera camera;
	public SpriteBatch batch;
	public BitmapFont font;

	private PolygonShape rectangle;
	private Sprite characterSprite;
	private Sprite mainBubbleSprite;

	//moving background
	private Texture backgroundTexture;
    private int currentBgx;
    private long lastTimeBg;

	private Texture character;
	private Sound dropSound;
	private Music backgroundMusic;

	//Physics
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Array<Body> bubbles;

    //characters and bubbles
	private BodyDef bodyDefRectangle;
	private Body body;
	private BodyDef bodyDef;
	private CircleShape circle;
	private FixtureDef fixtureDef;
	private FixtureDef fixtureDefRec;
	private Fixture fixture;

	//Game elements
	private double bubbleTime;
	private double bubbleTimeStep;
	private int score;

	//movement
	private boolean direction;

	//score
	public static Preferences prefs;


	public GameScreen(final Life gam) {
		this.gam = gam;

		//screen dimensions
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();


		world = new World(new Vector2(0, 0), true);

		//Bubble physics setup
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;

		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0.1f;
		fixtureDef.restitution = 0.6f;
		//End bubble physics setup

		//This shows the wireframes of physics objects
		debugRenderer = new Box2DDebugRenderer();

		//Setting up things
		camera = new OrthographicCamera();
		camera.setToOrtho(false, WIDTH/PPM, HEIGHT/PPM);

        //moving background
		backgroundTexture = new Texture("background.png");
        currentBgx = HEIGHT/PPM;

        // set lastTimeBg to current time
        lastTimeBg = TimeUtils.nanoTime();

		mainBubbleSprite = new Sprite(new Texture("bubble.png"));

		dropSound = Gdx.audio.newSound(Gdx.files.internal("Sound/drop.wav"));

		String musicString = "Sound/Music/" + MUSICS[MathUtils.random(0, MUSICS.length - 1)];//Get a random music
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(musicString));//Set the random music
		backgroundMusic.setLooping(true);//Loop the random music
		backgroundMusic.play();//Play the random music

		//More setup
		batch = new SpriteBatch();
		font = new BitmapFont();

		//character
		characterSprite = new Sprite( new Texture("sprite.png"));

		score = 0;
		bubbleTime = 0;

		bubbles = new Array<Body>();

		//makes rectangle character
		bodyDefRectangle = new BodyDef();
		bodyDefRectangle.type = BodyType.DynamicBody;
		//supposed to center character but it doesn't work??
		bodyDefRectangle.position.set((WIDTH/2f)/PPM,(WIDTH/32f)/PPM);
		body = world.createBody(bodyDefRectangle);
		rectangle = new PolygonShape();
		rectangle.setAsBox((WIDTH/16f)/PPM, (WIDTH/16f)/PPM);
		fixtureDefRec = new FixtureDef();
		fixtureDefRec.shape = rectangle;
		fixtureDefRec.density = 0.1f;
		fixtureDefRec.friction = 0.1f;
		fixtureDefRec.restitution = 0.6f;
		body.createFixture(fixtureDefRec);

        //variable for true makes it so when user touches it flips
		direction = true;

		//And make the first bubbles to start the game
		spawnBubbles(3);

		//sets score to zero
		prefs = Gdx.app.getPreferences("Bubble");
		if (!prefs.contains("highScore")) {
			prefs.putInteger("highScore", 0);
		}


	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0,0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
        batch.draw(backgroundTexture,0, currentBgx - HEIGHT/PPM, WIDTH/PPM, HEIGHT/PPM);
        // draw the second background
        batch.draw(backgroundTexture, 0, currentBgx, WIDTH/PPM, HEIGHT/PPM);
		font.setScale(.1f,.1f);
		font.draw(batch,String.valueOf(score), 0, HEIGHT/PPM);
		//Draw the bubbles
		for (final Body bodyCircle : bubbles) {
			if (bodyCircle.getUserData() != null && bodyCircle.getUserData() instanceof Sprite) {
				//Update the bubble's sprite (image) to match the bubble pysics body
				Sprite sprite = (Sprite) bodyCircle.getUserData();
				sprite.setPosition((bodyCircle.getPosition().x - sprite.getWidth() / 2), (bodyCircle.getPosition().y - sprite.getHeight() / 2));
				sprite.setRotation(bodyCircle.getAngle() / PPM * MathUtils.radiansToDegrees);
				sprite.draw(batch);
			}
			//delete for top thing
			if (!Top(bodyCircle.getPosition().y - 1)) {
				deleteBubble(bodyCircle);
				continue;
			}

			//delete if it touches the bottom
			if(!Bottom(bodyCircle.getPosition().y)){
				deleteBubble(bodyCircle);
			}

			//rebounds if it hits sides of walls
			if(!SidesLeft(bodyCircle.getPosition().x)){
				bodyCircle.applyLinearImpulse(15f/PPM,-5/PPM,bodyCircle.getPosition().x,bodyCircle.getPosition().y,true);
			}

			if(!SidesRight(bodyCircle.getPosition().x)&&Top(bodyCircle.getPosition().y+1/PPM)){
				bodyCircle.applyLinearImpulse(-15f/PPM,-5/PPM,bodyCircle.getPosition().x,bodyCircle.getPosition().y,true);
			}
            //makes sure the bubble isn't going upwards
            if(bodyCircle.getLinearVelocity().y>0){
                bodyCircle.setLinearVelocity(bodyCircle.getLinearVelocity().x,-Math.abs(bodyCircle.getLinearVelocity().y));
            }
		}

		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		batch.end();

        //sets character sprite
        System.out.println(body.getPosition());
        characterSprite.setSize(body.getPosition().x + (WIDTH/16f)/PPM*2, body.getPosition().y + (WIDTH/16f)/PPM*2);
        characterSprite.setOrigin(characterSprite.getWidth(),characterSprite.getHeight());
        body.setUserData(characterSprite);

		//shifts character direction when screen is touched :)
		if(Gdx.input.justTouched()) {
			if (direction) {
				body.setLinearVelocity((float)((WIDTH/PPM)/BOX_SPEED_DIVISOR), 0f);
				direction = false;
			} else {
				body.setLinearVelocity((float)(-(WIDTH/PPM)/BOX_SPEED_DIVISOR), 0f);
				direction = true;
			}
		}

		//kills user if they touch boundaries or get hit by a bubble
		if (!inBounds(body.getPosition().x+Gdx.graphics.getWidth()/(16f*PPM), 1) ||!inBounds(body.getPosition().x-Gdx.graphics.getWidth()/(16f*PPM), 1)||(body.getPosition().y!=(WIDTH/32f)/PPM)) {
            backgroundMusic.dispose();
			if(score>getHighScore())
				setHighScore(score);
			gam.setScreen(new Lose(gam));
		}
		//Timed bubble spawning
		if (bubbleTime > bubbles.size) {
			spawnBubbles(3);//Spawn 3 bubbles at a time
			score++;//adds score if person survives wave
		}

        //Moves Background
        if(TimeUtils.nanoTime() - lastTimeBg > 100000000){
            // move the separator 50px
            currentBgx -= .50;
            // set the current time to lastTimeBg
            lastTimeBg = TimeUtils.nanoTime();
        }

        // if the separator reaches the screen edge, move it back to the first position
        if(currentBgx <= 0){
            currentBgx = HEIGHT/PPM;
        }

		//debugRenderer.render(world, camera.combined);

        /*
         LOGISTIC BUBBLE SPAWNING
         https://www.desmos.com/calculator/xcrski6uif
         Set it so its uniform spawning
         Also is kinda nice because a bubble will always spawn if there are no bubbles on board
         */
		double x = -2;
		double var1 = 3;//Carrying capacity. Max number of bubbles
		double var2 = -5;//kinda controls when the graph starts getting steep
		double var3 = .015;//Controls steepness of graph, max rate of spawn

		bubbleTimeStep = var1 / (1 + Math.pow(Math.E, -(var2 + (var3 * x))));//Pretty much the equation from the link above

		bubbleTime += bubbleTimeStep * Gdx.graphics.getDeltaTime();

		world.step(1 / 60f, 6, 2);
	}

	private void spawnBubble() {

		circle = new CircleShape();

		Body body = world.createBody(bodyDef);

		//Angle and position
		float angle = MathUtils.random(3.926990817f, 5.497787144f);//Random angle from 225 deg to 315 deg
		Float spawn = MathUtils.random(0f, (float)WIDTH);
		int direction = (int) (Math.random() + .50);
		if (direction == 0){//From the angle the bubble is facing, the position is set so it will travel to the center
			body.setTransform((WIDTH - ((MathUtils.cos(angle) * .5f * WIDTH) + (.5f * WIDTH))) / PPM, (HEIGHT - ((MathUtils.sin(angle) * .5f * HEIGHT) + (.5f * HEIGHT))) / PPM, angle);//sets spped
			//System.out.println(body.getPosition());
		}else{
			body.setTransform((spawn) / PPM, (HEIGHT) / PPM, ((float) (3 * Math.PI / 2f)));
			//System.out.println(body.getPosition());
		}
		Float random = MathUtils.random((WIDTH/16f), (WIDTH/8f));//The numbers are arbitrary

        //sets size of bubble
		circle.setRadius(random/(float)(PPM*1.5));

		//for the physics
		fixtureDef.shape = circle;
		body.createFixture(fixtureDef);

		//Sprite setup
		Sprite bubbleSprite = new Sprite(mainBubbleSprite);
		bubbleSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);//Set sprite size to match body. *2 for radius->diameter
		bubbleSprite.setOrigin(bubbleSprite.getWidth() / 2, bubbleSprite.getHeight() / 2);//Set sprite on top of body
		body.setUserData(bubbleSprite);

		float cosine = MathUtils.cos(body.getAngle());

		if(cosine < 0)
			body.setLinearVelocity(-BUBBLE_SPEED,-BUBBLE_SPEED);
		else if(cosine>0)
			body.setLinearVelocity(BUBBLE_SPEED,-BUBBLE_SPEED);
		else if(cosine == 0)
			body.setLinearVelocity(0,-BUBBLE_SPEED);
		circle.dispose();

		bubbles.add(body);

		bubbleTime = 0;
	}

	/*
     Remove a bubble (physics body)
     @args The bubble body to remove.
     */
	private void deleteBubble(Body b) {
		bubbles.removeValue(b, true);
		world.destroyBody(b);
		b.setUserData(null);
	}

    /**
     *Check if a point is within the visible screen area
     */
	private boolean inBounds(float x, float y) {
		return x >= 0 &&
				y >= 0 &&
				x <= WIDTH/PPM &&
				y <= HEIGHT/PPM;
	}

	private boolean Top(float y){
		return y<=HEIGHT/PPM;
	}

	private boolean Bottom(float y){
		return y>=0;
	}

	private boolean SidesLeft(float x){
		return x >= 0;
	}

	private boolean SidesRight(float x){
		return	x <= WIDTH/PPM;
	}

	private void spawnBubbles(int n) {
		for (int i = 1; i <= n; i++) {
			spawnBubble();
		}
	}




	public static void setHighScore(int val) {
		prefs.putInteger("highScore", val);
		prefs.flush();
	}

	public static int getHighScore() {
		return prefs.getInteger("highScore");
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
		dropSound.dispose();
		world.dispose();
		mainBubbleSprite.getTexture().dispose();
		backgroundMusic.dispose();
		rectangle.dispose();
	}
}
