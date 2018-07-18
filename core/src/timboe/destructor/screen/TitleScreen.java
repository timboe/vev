package timboe.destructor.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;

import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;
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
//    Gdx.input.setInputProcessor( state.getUIStage() );
      Gdx.input.setInputProcessor( gestureDetector );
      camera.setCurrentPos(250+200,150+150); // Edge+offset
      camera.setCurrentZoom(.25f);
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
    state.getSpriteStage().draw();
    state.getIntroFoliageStage().draw();

    sr.setProjectionMatrix(Camera.getInstance().getTileCamera().combined);
    sr.begin(ShapeRenderer.ShapeType.Line);
    sr.setColor(0, 0, 1, 1);
    sr.rect(camera.getCullBoxTile().getX(), camera.getCullBoxTile().getY(), camera.getCullBoxTile().getWidth(), camera.getCullBoxTile().getHeight());
    sr.end();
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
