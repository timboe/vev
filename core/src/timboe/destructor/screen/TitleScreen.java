package timboe.destructor.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.LinkedList;
import java.util.List;

import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Particle;
import timboe.destructor.input.Gesture;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.UI;
import timboe.destructor.manager.World;

public class TitleScreen implements Screen {

  private final Camera camera = Camera.getInstance();
  private final GameState state = GameState.getInstance();
  private final World world = World.getInstance();
  private final UI ui = UI.getInstance();
  private final ShapeRenderer sr = new ShapeRenderer();

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
  }

  void addParticles() {
    int pType = 0;
    for (double a = -Math.PI; a <= Math.PI; a += (2*Math.PI) / (double)Particle.values().length ) {
      if (pType == Particle.values().length) break; // Else rely on floating point in for loop
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
