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
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.input.Gesture;
import timboe.destructor.input.Handler;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.UI;
import timboe.destructor.manager.World;

public class GameScreen implements Screen {

  private final InputMultiplexer multiplexer = new InputMultiplexer();
  private final Handler handler = new Handler();
  private final Gesture gesture = new Gesture();
  private final GestureDetector gestureDetector = new GestureDetector(gesture);
  private final ShapeRenderer sr = new ShapeRenderer();

  private final Camera camera = Camera.getInstance();
  private final GameState state = GameState.getInstance();
  private final World world = World.getInstance();
  private final UI ui = UI.getInstance();

  public GameScreen() {
    setMultiplexerInputs();
  }

  public void setMultiplexerInputs() {
    multiplexer.clear();
    multiplexer.addProcessor(state.getUIStage());
//    multiplexer.addProcessor(state.getWarpStage());
//    multiplexer.addProcessor(state.getSpriteStage());
    multiplexer.addProcessor(handler);
    multiplexer.addProcessor(gestureDetector);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor( multiplexer );
    Gdx.app.log("GameScreen", "Show " + Gdx.input.getInputProcessor());
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
    delta = Math.min(delta, Param.FRAME_TIME * 10); // Do not let this get too extreme
    renderClear();
    camera.update(delta);
    state.act(delta);
    ui.act(delta);

    ////////////////////////////////////////////////
    camera.getTileViewport().apply();

//    GameState.getInstance().getStage().getRoot().setCullingArea( Camera.getInstance().getCullBoxTile() );

    state.getTileStage().getRoot().setCullingArea( camera.getCullBoxTile() );
    state.getTileStage().draw();
    state.getBuildingStage().draw();
    state.getWarpStage().draw(); // Note - warp stage has different blending

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
      sr.setProjectionMatrix(Camera.getInstance().getTileCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(1, 1, 1, 1);
      for (Actor A : state.getTileStage().getActors()) {
        Tile T = (Tile) A;
        T.renderDebug(sr);
      }
      sr.setColor(0, 0, 1, 1);
      sr.rect(camera.getCullBoxTile().getX(), camera.getCullBoxTile().getY(), camera.getCullBoxTile().getWidth(), camera.getCullBoxTile().getHeight());
      sr.end();

      sr.setProjectionMatrix(Camera.getInstance().getSpriteCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(1, 1, 1, 1);
      sr.rect(camera.getCullBoxSprite().getX(), camera.getCullBoxSprite().getY(), camera.getCullBoxSprite().getWidth(), camera.getCullBoxSprite().getHeight());
      sr.end();
    }

    ////////////////////////////////////////////////
    camera.getSpriteViewport().apply();

    state.getSpriteStage().getRoot().setCullingArea( camera.getCullBoxSprite() );
    state.getFoliageStage().getRoot().setCullingArea( camera.getCullBoxSprite() );
    state.getSpriteStage().draw();
    state.getFoliageStage().draw();

    sr.setProjectionMatrix(camera.getSpriteCamera().combined);
    sr.setColor(1, 0, 0, 1);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    for (Sprite s : state.particleSet) s.drawSelected(sr);
    sr.end();

    ////////////////////////////////////////////////
    camera.getTileViewport().apply();

    sr.setProjectionMatrix(camera.getTileCamera().combined);
    sr.setColor(1, 0, 0, 1);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    for (Building b : state.buildingSet) b.drawSelected(sr);
    sr.end();
    sr.begin(ShapeRenderer.ShapeType.Line);
    Gdx.graphics.getGL20().glLineWidth(4f / camera.getZoom());
    for (Building b : state.buildingSet) b.drawPath(sr);
    sr.end();

    ////////////////////////////////////////////////
    camera.getUiViewport().apply();

    if (state.isSelecting()) { // Draw select box
      sr.setProjectionMatrix(Camera.getInstance().getTileCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(0, 1, 0, 1);
      sr.rect(state.selectStartWorld.x, state.selectStartWorld.y,
          state.selectEndWorld.x - state.selectStartWorld.x,
          state.selectEndWorld.y - state.selectStartWorld.y);

      sr.end();
    }

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
    sr.dispose();
  }


}
