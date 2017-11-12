package timboe.destructor.input;

import com.badlogic.gdx.InputAdapter;
import timboe.destructor.Param;
import timboe.destructor.manager.Camera;

public class Handler extends InputAdapter {

  @Override
  public boolean scrolled(int amount) {
    Camera.getInstance().modZoom(amount * Param.SCROLL_ZOOM);
    return false;
  }
}
