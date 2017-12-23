package timboe.destructor.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import timboe.destructor.Param;
import timboe.destructor.ShapeRendererExtended;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.input.Gesture;
import timboe.destructor.input.Handler;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.World;

public class GameScreen implements Screen {

  private InputMultiplexer multiplexer = new InputMultiplexer();
  private Handler handler = new Handler();
  private Gesture gesture = new Gesture();
  private GestureDetector gestureDetector = new GestureDetector(gesture);
  private ShapeRendererExtended sr = new ShapeRendererExtended();

  private final Camera camera = Camera.getInstance();
  private final GameState state = GameState.getInstance();
  private final World world = World.getInstance();

  public GameScreen() {
    multiplexer.addProcessor(GameState.getInstance().getUIStage());
    multiplexer.addProcessor(handler);
    multiplexer.addProcessor(gestureDetector);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor( multiplexer );
    Gdx.app.log("GameScreen", "Show");
  }

  private void renderClear() {
    Gdx.gl.glClearColor(.1529f, .1255f, .1922f, 1);
//    Gdx.gl.glClearColor(.7f, .7f, .7f, 1);
    Gdx.gl.glLineWidth(3);
    Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
  }


  @Override
  public void render(float delta) {
    ++Param.FRAME;
    renderClear();
    state.act(delta);

    ////////////////////////////////////////////////

    camera.updateTiles(delta);

//    GameState.getInstance().getStage().getRoot().setCullingArea( Camera.getInstance().getCullBox() );
    state.getTileStage().draw();

    ////////////////////////////////////////////////

    state.getWarpStage().act(delta);
    state.getWarpStage().draw();

    // TODO optimise additive mixed batching
    state.getWarpStage().getBatch().begin();
    for (ParticleEffect e : world.warpClouds) {
      e.update(delta);
      e.draw(state.getWarpStage().getBatch());
    }
    for (ParticleEffect e : world.warps.values()) {
      e.update(delta);
      if (!e.isComplete()) e.draw(state.getWarpStage().getBatch());
    }
    state.getWarpStage().getBatch().end();

    ////////////////////////////////////////////////

    if (Param.DEBUG > 2) {
      sr.setProjectionMatrix(Camera.getInstance().getCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(1, 1, 1, 1);
      for (Actor A : state.getTileStage().getActors()) {
        try {
          Tile T = (Tile) A;
          T.renderDebug(sr);
        } catch (Exception e) {
        }
      }
      sr.end();
    }

    camera.updateSprite();
    GameState.getInstance().getSpriteStage().act(delta);
    GameState.getInstance().getSpriteStage().draw();

    sr.setProjectionMatrix(Camera.getInstance().getCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Line);
    sr.setColor(0, 1, 0, 1);
    // Draw selected particles
    for (Sprite s : state.particleSet) {
      s.draw(sr);
    }
    sr.end();

    ////////////////////////////////////////////////



    camera.updateUI();
    GameState.getInstance().getUIStage().draw();

    ///////////////////////////////////////////////

    camera.update(); // Reset shenanigans
    sr.setProjectionMatrix(Camera.getInstance().getCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Line);
    sr.setColor(0, 1, 0, 1);
    if (!state.selectStartWorld.isZero()) { // Draw select box
      sr.rect(state.selectStartWorld.x, state.selectStartWorld.y,
              state.selectEndWorld.x - state.selectStartWorld.x,
              state.selectEndWorld.y - state.selectStartWorld.y);

    }
    sr.end();

  }

  @Override
  public void resize(int width, int height) {
    state.getTileStage().getViewport().update(width, height, true);
    state.getSpriteStage().getViewport().update(width, height, true);
    state.getWarpStage().getViewport().update(width, height, true);
    state.getUIStage().getViewport().update(width, height, true);
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void hide() {
    Gdx.input.setInputProcessor( null );
  }

  @Override
  public void dispose() {
    sr.dispose();
  }


}
