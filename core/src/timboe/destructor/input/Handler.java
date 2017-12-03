package timboe.destructor.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import timboe.destructor.Param;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.World;

public class Handler extends InputAdapter {

  @Override
  public boolean scrolled(int amount) {
    Camera.getInstance().modZoom(amount * Param.SCROLL_ZOOM);
    return false;
  }

  public boolean touchUp (int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT) {
      GameState.getInstance().selectStartWorld.setZero();
      GameState.getInstance().selectEndScreen.setZero();
      return true; // Consume
    }
    return false;
  }


  @Override
  public boolean keyUp (int keycode) {
    if (keycode == Input.Keys.D) {
      if (++Param.DEBUG == 4) Param.DEBUG = 0;
      GameState.getInstance().getTileStage().setDebugAll( Param.DEBUG > 1 );
    } else if (keycode == Input.Keys.N) {
      World.getInstance().generate();
    }
    return false;
  }

}
