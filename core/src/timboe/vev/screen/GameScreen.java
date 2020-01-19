package timboe.vev.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Building;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.entity.Warp;
import timboe.vev.enums.FSM;
import timboe.vev.enums.UIMode;
import timboe.vev.input.Gesture;
import timboe.vev.input.Handler;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.StateManager;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

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

  public float fadeIn = 0;
  public float[] transitionOutTimers = new float[3];

  public GameScreen() {
    setMultiplexerInputs();
  }

  public void setMultiplexerInputs() {
    gestureDetector.setLongPressSeconds(Param.LONG_PRESS_TIME);
    multiplexer.clear();
    multiplexer.addProcessor(state.getUIStage());
    multiplexer.addProcessor(handler);
    multiplexer.addProcessor(gestureDetector);
  }

  @Override
  public void show() {
    transitionOutTimers[0] = transitionOutTimers[1] = transitionOutTimers[2] = 0;
  }

  public void doInputHandles() {
    Gdx.input.setInputProcessor( multiplexer );
  }

  @Override
  public void render(float delta) {
    delta = Math.min(delta, Param.FRAME_TIME * 10); // Do not let this get too extreme
    Util.renderClear();
    final boolean paused = (ui.uiMode == UIMode.kSETTINGS);
    final float fxDelta = (paused ? 0 : delta);
    if (!paused) ++Param.FRAME;

    world.act(delta);
    camera.update(delta);
    ui.act(delta);
    state.act(delta);

    ////////////////////////////////////////////////
    // Tiles, buildings and warpMap

    state.getTileStage().getRoot().setCullingArea( camera.getCullBoxTile() );
    state.getBuildingStage().getRoot().setCullingArea( camera.getCullBoxTile() );
    state.getWarpStage().getRoot().setCullingArea( camera.getCullBoxTile() );

    state.getTileStage().draw();
    state.getBuildingStage().draw();
    state.getWarpStage().draw(); // Note - warp stage has different blending

    ////////////////////////////////////////////////
    // Debug render

    if (GameState.getInstance().debug > 2) {
      sr.setProjectionMatrix(Camera.getInstance().getTileCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(1, 1, 1, 1);
      for (Actor A : state.getTileStage().getActors()) {
        try {
          Tile T = (Tile) A;
          T.renderDebug(sr);
        } catch (Exception e) {
        }
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
    // Particles & Foliage (x2 zoom)

    state.getSpriteStage().getRoot().setCullingArea( camera.getCullBoxSprite() );
    state.getFoliageStage().getRoot().setCullingArea( camera.getCullBoxSprite() );
    state.getSpriteStage().draw();
    state.getFoliageStage().draw();

    ////////////////////////////////////////////////
    // FX

    // TODO optimise additive mixed batching
    Batch batch = state.getTileStage().getBatch();
    batch.begin();
    for (Warp w : state.getWarpMap().values()) {
      w.warpCloud.draw(batch, fxDelta);
      if (!w.zap.isComplete()) w.zap.draw(batch, fxDelta);
    }
    for (int i = state.dustEffects.size - 1; i >= 0; i--) {
      ParticleEffectPool.PooledEffect e = state.dustEffects.get(i);
      e.draw(batch, fxDelta);
      if (e.isComplete()) {
        e.free();
        state.dustEffects.removeIndex(i);
      }
    }
    batch.end();

    ////////////////////////////////////////////////
    // Building select and pathing

    sr.setProjectionMatrix(camera.getTileCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    for (Building b : state.getBuildingMap().values()) {
      b.drawSelected(sr);
      b.drawPath(sr);
    }
    for (Warp w : state.getWarpMap().values()) {
      w.drawSelected(sr);
      w.drawPath(sr);
    }
    if (state.doingPlacement) {
      for (Tile x[] : world.getTile(false)) {
        for (Tile xy : x) {
          xy.drawSelected(sr);
        }
      }
    }
    sr.end();

    sr.setProjectionMatrix(camera.getSpriteCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    for (Sprite s : state.getParticleMap().values()) s.drawSelected(sr);
    sr.end();

    ////////////////////////////////////////////////
    // Select box

    if (state.isSelecting()) { // Draw select box
      sr.setProjectionMatrix(Camera.getInstance().getTileCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(0, 1, 0, 1);
      sr.rect(state.selectStartWorld.x, state.selectStartWorld.y,
          state.selectEndWorld.x - state.selectStartWorld.x,
          state.selectEndWorld.y - state.selectStartWorld.y);

      sr.end();
    }

    ////////////////////////////////////////////////
    // UI

    state.getFireworkStage().draw();

    state.getUIStage().draw();

    ////////////////////////////////////////////////
    // Fade in

    if (StateManager.getInstance().fsm == FSM.kFADE_TO_GAME) {
      sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      sr.setColor(136 / 255f, 57 / 255f, 80 / 255f, fadeIn / 100f);
      sr.rect(0, 0, Param.DISPLAY_X, Param.DISPLAY_Y);
      sr.end();
      fadeIn -= delta * Param.FADE_SPEED_GAME;
      if (fadeIn < 0) {
        state.initialZap();
        state.doRightClick();
        StateManager.getInstance().gameScreenFadeComplete();
      }
    }

    // Debug - border
    if (Param.DEBUG_INITIAL > 0) {
      sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      sr.setColor(1, 0, 0, 1);
      sr.rect(0, 0, Param.DISPLAY_X, Param.DISPLAY_Y);
      sr.end();
    }

    if (StateManager.getInstance().fsm == FSM.kTRANSITION_TO_INTRO_SAVE
            || StateManager.getInstance().fsm == FSM.kTRANSITION_TO_INTRO_NOSAVE) {
      final boolean finished = Util.doFade(sr, delta, transitionOutTimers);
      if (finished) {
        StateManager.getInstance().setToTitleScreen();
      }
    }
  }

  @Override
  public void resize(int width, int height) {
    camera.resize(width, height);
  }

  @Override
  public void pause() {
    Sounds.getInstance().pause();
    UI.getInstance().showSettings();
  }

  @Override
  public void resume() {
    Sounds.getInstance().resume();
  }

  @Override
  public void hide() {
  }

  @Override
  public void dispose() {
    sr.dispose();
  }


}
