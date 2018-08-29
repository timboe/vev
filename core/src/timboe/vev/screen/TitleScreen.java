package timboe.vev.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.enums.Particle;
import timboe.vev.input.Gesture;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

public class TitleScreen implements Screen {

  private final Camera camera = Camera.getInstance();
  private final GameState state = GameState.getInstance();
  private final World world = World.getInstance();
  private final UI ui = UI.getInstance();
  private final ShapeRenderer sr = new ShapeRenderer();
  public float fadeTimer, fadeTimer2, fadeTimer3;

  // Temp
  private final Gesture gesture = new Gesture();
  private final GestureDetector gestureDetector = new GestureDetector(gesture);


  public TitleScreen() {
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor( state.getUIStage() );
//    Gdx.input.setInputProcessor( gestureDetector );
    camera.setCurrentPos(Param.TILES_INTRO_X/2 * Param.TILE_S,Param.TILES_INTRO_Y/2 * Param.TILE_S); // Edge+offset
    camera.setCurrentZoom(.25f);
    addParticles();
    fadeTimer = fadeTimer2 = fadeTimer3 = 0;
  }

  void addParticles() {
    int pType = 0;
    for (double a = -Math.PI; a <= Math.PI; a += (2*Math.PI) / (double)(Particle.values().length - 1) ) { // -1 due to kBlank
      if (pType == (Particle.values().length - 1)) break; // Else rely on floating point in for loop
      Particle p = Particle.values()[ pType++ ];
      int tileX = (Param.TILES_INTRO_X/2) + (int)Math.round( (Param.TILES_INTRO_X/3) * Math.cos(a) );
      int tileY = (Param.TILES_INTRO_Y/2) + (int)Math.round( (Param.TILES_INTRO_Y/3) * Math.sin(a) );
      Tile genTile = World.getInstance().getIntroTile(tileX, tileY);
      for (int i = 0; i < 200; ++i) {
        Sprite s = new Sprite(genTile);
        s.isIntroSprite = true;
        s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);
        s.pathTo(s.findPathingLocation(genTile, false, true, true, true), null, null);
        s.setTexture("ball_" + p.getColourFromParticle().getString(), 6, false);
        s.setUserObject(p);
        s.moveBy(Util.R.nextInt(Param.TILE_S), Util.R.nextInt(Param.TILE_S));
        s.idleTime = s.boredTime; // Start the wanderlust right away
        GameState.getInstance().getIntroSpriteStage().addActor(s);
      }
    }
  }

  @Override
  public void render(float delta) {
    ++Param.FRAME;
    delta = Math.min(delta, Param.FRAME_TIME * 10); // Do not let this get too extreme
    Util.renderClear();

    world.act(delta);
    camera.update(delta);
    state.act(delta);
    ui.act(delta);

//    state.getIntroTileStage().getRoot().setCullingArea( camera.getCullBoxTile() );

    state.getIntroTileStage().draw();
    state.getIntroSpriteStage().draw();
    state.getIntroFoliageStage().draw();
    state.getUIStage().draw();

    if (fadeTimer > 0) {
      sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
      sr.setColor(206f/255f, 101f/255f, 80f/255f, 1f);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      strokeRect(sr, fadeTimer, fadeTimer/4f);
      sr.setColor(176/255f, 78/255f, 80/255f, 1f);
      strokeRect(sr, fadeTimer2, fadeTimer2/4f);
      sr.setColor(136/255f, 57/255f, 80/255f, 1f);
      strokeRect(sr, fadeTimer3, fadeTimer3/4f);
      sr.end();
      Gdx.gl.glLineWidth(5);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(72f/255f, 43f/255f, 81f/255f, 1f);
      strokeRect(sr, fadeTimer, fadeTimer/4f);
      strokeRect(sr, fadeTimer2, fadeTimer2/4f);
      strokeRect(sr, fadeTimer3, fadeTimer3/4f);
      sr.end();
      fadeTimer += (delta * 5);
      fadeTimer *= 1.1;
      if (fadeTimer > 2.5 && fadeTimer2 == 0) fadeTimer2 = fadeTimer - 2.5f;
      if (fadeTimer2 > 0) {
        fadeTimer2 += (delta * 10);
        fadeTimer2 *= 1.1;
      }
      if (fadeTimer2 > 2.5 && fadeTimer3 == 0) fadeTimer3 = fadeTimer2 - 2.5f;
      if (fadeTimer3 > 0) {
        fadeTimer3 += (delta * 10);
        fadeTimer3 *= 1.1;
      }
      if (fadeTimer3 > 1100) {
        GameState.getInstance().setToGameScreen();
      }
    }
  }

  private void strokeRect(ShapeRenderer sr, float width, float angle) {
    final float midX = Param.DISPLAY_X / 2, midY = Param.DISPLAY_Y / 2;
    sr.rect(midX-width, midY-width,
        width,width,2*width,2*width,
        1,1,angle);
  }

  @Override
  public void resize(int width, int height) {
    camera.resize(width, height);
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void hide() {
    Gdx.app.log("GameScreen", "Hide " + Gdx.input.getInputProcessor());
    Gdx.input.setInputProcessor( null );
  }

  @Override
  public void dispose() {

  }
}
