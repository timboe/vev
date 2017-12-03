package timboe.destructor.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import timboe.destructor.Param;
import timboe.destructor.entity.Tile;
import timboe.destructor.input.Gesture;
import timboe.destructor.input.Handler;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;

public class GameScreen implements Screen {

  private InputMultiplexer multiplexer = new InputMultiplexer();
  private Handler handler = new Handler();
  private Gesture gesture = new Gesture();
  private GestureDetector gestureDetector = new GestureDetector(gesture);
  private ShapeRenderer sr = new ShapeRenderer();

  public GameScreen() {
    multiplexer.addProcessor(GameState.getInstance().getUIStage());
    multiplexer.addProcessor(gestureDetector);
    multiplexer.addProcessor(handler);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor( multiplexer );
    Gdx.app.log("GameScreen", "Show");
  }

  protected void renderClear() {
    Gdx.gl.glClearColor(.1529f, .1255f, .1922f, 1);
//    Gdx.gl.glClearColor(.7f, .7f, .7f, 1);
    Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
  }


  @Override
  public void render(float delta) {
    renderClear();
    Camera.getInstance().update(delta);

//    GameState.getInstance().getStage().getRoot().setCullingArea( Camera.getInstance().getCullBox() );
    GameState.getInstance().getTileStage().draw();


    for (Actor A : GameState.getInstance().getWarpStage().getRoot().getChildren()) {
      A.rotateBy((Float)A.getUserObject() * delta);
    }
    GameState.getInstance().getWarpStage().draw();

    if (Param.DEBUG > 2) {
      sr.setProjectionMatrix(Camera.getInstance().getCamera().combined);
      sr.begin(ShapeRenderer.ShapeType.Line);
      Gdx.gl.glLineWidth(3);
      sr.setColor(1, 1, 1, 1);
      for (Actor A : GameState.getInstance().getTileStage().getActors()) {
        try {
          Tile T = (Tile) A;
          T.renderDebug(sr);
        } catch (Exception e) {
        }
      }
      sr.end();
    }

    Camera.getInstance().updateSprite();
    GameState.getInstance().getSpriteStage().draw();

    Camera.getInstance().updateUI();
//    GameState.getInstance().getUIStage().draw();

  }

  @Override
  public void resize(int width, int height) {
    GameState.getInstance().getTileStage().getViewport().update(width, height, true);
    GameState.getInstance().getSpriteStage().getViewport().update(width, height, true);
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
