package timboe.vev.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Entity;
import timboe.vev.enums.FSM;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.StateManager;
import timboe.vev.manager.UIIntro;
import timboe.vev.manager.World;

public class TitleScreen implements Screen {

  private final Camera camera = Camera.getInstance();
  private final IntroState state = IntroState.getInstance();
  private final World world = World.getInstance();
  private final UIIntro ui = UIIntro.getInstance();
  private final ShapeRenderer sr = new ShapeRenderer();
  public float fadeIn = 0;
  public float[] transitionOutTimers = new float[3];

  public TitleScreen() {
  }

  @Override
  public void show() {
    camera.setHelpPos(0, true);
    state.addParticles();
    transitionOutTimers[0] = transitionOutTimers[1] = transitionOutTimers[2] = 0;
  }

  public void doInputHandles() {
    Gdx.input.setInputProcessor( state.getUIStage() );
  }

  @Override
  public void render(float delta) {
    ++Entity.FRAME_COUNTER;
    delta = Math.min(delta, Param.FRAME_TIME * 10); // Do not let this get too extreme
    Util.renderClear();

    world.act(delta);
    camera.update(delta);
    state.act(delta);

//    state.getIntroTileStage().getRoot().setCullingArea( camera.getCullBoxTile() );

    state.getIntroTileStage().draw();
    state.getIntroFoliageStage().draw();
    state.getIntroBuildingStage().draw();
    state.getIntroSpriteStage().draw();
    state.getIntroWarpStage().draw(); // Different blending

    ////////////////////////////////////////////////
    // Building select and pathing

    sr.setProjectionMatrix(camera.getTileCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    for (Entity e: state.demoBuildings) {
      e.drawPath(sr);
    }
    sr.end();

    ////////////////////////////////////////////////
    // FX

    Batch batch = state.getIntroTileStage().getBatch();
    batch.begin();
    state.demoWarp.warpCloud.draw(batch, delta);
    //if (!state.demoWarp.zap.isComplete()) state.demoWarp.zap.draw(batch, delta);     // We currently don't zap
    batch.end();

    ////////////////////////////////////////////////
    // UI

    state.getIntroHelpStage().draw();


    state.getUIStage().draw();

    if (StateManager.getInstance().fsm == FSM.kTRANSITION_TO_GAME) {
      final boolean finished = Util.doFade(sr, delta, transitionOutTimers);
      if (finished) {
        if (World.getInstance().getGenerated()) {
          if (GameState.getInstance().warpParticles != -1) {
            GameState.getInstance().warpParticles = World.getInstance().warpParticlesCached;
          }
          StateManager.getInstance().setToGameScreen();
        } else {
          World.getInstance().doGenerate = true;
        }
      }
    }

    if (StateManager.getInstance().fsm == FSM.kFADE_TO_INTRO) {
      sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      sr.setColor(136 / 255f, 57 / 255f, 80 / 255f, fadeIn / 100f);
      sr.rect(0, 0, Param.DISPLAY_X, Param.DISPLAY_Y);
      sr.end();
      fadeIn -= delta * Param.FADE_SPEED_INTRO;
      if (fadeIn < 0) {
        StateManager.getInstance().titleScreenFadeComplete();
      }
    }

    UIIntro.getInstance().generating.setVisible( World.getInstance().doGenerate && !World.getInstance().getGenerated() );
    state.getIntroGeneratingStage().draw();
  }

  @Override
  public void resize(int width, int height) {
    camera.resize(width, height);
  }

  @Override
  public void pause() {
    Sounds.getInstance().pause();
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

  }
}
