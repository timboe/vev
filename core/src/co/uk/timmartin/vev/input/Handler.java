package co.uk.timmartin.vev.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.manager.Camera;
import co.uk.timmartin.vev.manager.GameState;

public class Handler extends InputAdapter {

  @Override
  public boolean scrolled(float amountX, float amountY) {
    Camera.getInstance().modZoom(amountY * Param.SCROLL_ZOOM);
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT && GameState.getInstance().isSelecting()) {
      boolean result = GameState.getInstance().doParticleSelect(true);
      if (!result) GameState.getInstance().showMainUITable(false); // Cancel
      return true; // Consume
    }
    return false;
  }


  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

}
