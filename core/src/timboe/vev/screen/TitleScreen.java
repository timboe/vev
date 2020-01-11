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
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.UI;
import timboe.vev.manager.UIIntro;
import timboe.vev.manager.World;

public class TitleScreen implements Screen {

  private final Camera camera = Camera.getInstance();
  private final IntroState state = IntroState.getInstance();
  private final World world = World.getInstance();
  private final UI ui = UI.getInstance();
  private final ShapeRenderer sr = new ShapeRenderer();
  public float fadeIn = 0;
  public float[] fadeTimer = new float[3];

  // Temp
  private final Gesture gesture = new Gesture();
  private final GestureDetector gestureDetector = new GestureDetector(gesture);


  public TitleScreen() {
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor( state.getUIStage() );
//    Gdx.input.setInputProcessor( gestureDetector );
    camera.setHelpPos(0, true);
    GameState.getInstance().doRightClick();
    state.addParticles();
    fadeTimer[0] = fadeTimer[1] = fadeTimer[2] = 0;
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
    state.getIntroHelpStage().draw();
    state.getUIStage().draw();

    if (fadeTimer[0] > 0) {
      final boolean finished = Util.doFade(sr, delta, fadeTimer);
      if (finished) {
        GameState.getInstance().setToGameScreen();
      }
    }

    if (fadeIn > 0) {
      sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      sr.setColor(136 / 255f, 57 / 255f, 80 / 255f, fadeIn / 100f);
      sr.rect(0, 0, Param.DISPLAY_X, Param.DISPLAY_Y);
      sr.end();
      fadeIn -= delta * 70f;
    }
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
    Gdx.app.log("GameScreen", "Hide " + Gdx.input.getInputProcessor());
    Gdx.input.setInputProcessor( null );
  }

  @Override
  public void dispose() {

  }
}
