package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import timboe.vev.Param;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;

public class Handler extends InputAdapter {

  @Override
  public boolean scrolled(int amount) {
    Camera.getInstance().modZoom(amount * Param.SCROLL_ZOOM);
    return false;
  }

  public boolean touchUp (int screenX, int screenY, int pointer, int button) {
    if (button == Input.Buttons.LEFT && GameState.getInstance().isSelecting()) {
      Gdx.app.log("touchUp", "End selecting");
      boolean result = GameState.getInstance().doParticleSelect(true);
      if (!result) GameState.getInstance().showMainUITable(false); // Cancel
      return true; // Consume
    }
    return false;
  }


  @Override
  public boolean keyUp (int keycode) {
    if (keycode == Input.Keys.B) {
      if (++GameState.getInstance().debug == 4) GameState.getInstance().debug = 0;
      GameState.getInstance().getTileStage().setDebugAll( GameState.getInstance().debug > 1 );
    } else if (keycode == Input.Keys.G) {
      GameState.getInstance().tryNewParticles(true, null, 1);
    } else if (keycode == Input.Keys.ESCAPE) {
      Gdx.app.exit();
    }
    return false;
  }

}
