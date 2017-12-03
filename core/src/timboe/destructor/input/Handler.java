package timboe.destructor.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import timboe.destructor.Param;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;

public class Handler extends InputAdapter {

  @Override
  public boolean scrolled(int amount) {
    Camera.getInstance().modZoom(amount * Param.SCROLL_ZOOM);
    return false;
  }

  @Override
  public boolean keyUp (int keycode) {
    if (keycode == Input.Keys.D) {
      if (++Param.DEBUG == 4) Param.DEBUG = 0;
      GameState.getInstance().getTileStage().setDebugAll( Param.DEBUG > 1 );
    }
    return false;
  }

}
