package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import timboe.vev.Param;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.World;

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
      if (!result) GameState.getInstance().doRightClick(); // Cancel
      return true; // Consume
    }
    return false;
  }


  @Override
  public boolean keyUp (int keycode) {
    if (keycode == Input.Keys.D) {
      if (++GameState.getInstance().debug == 4) GameState.getInstance().debug = 0;
      GameState.getInstance().getTileStage().setDebugAll( GameState.getInstance().debug > 1 );
    } else if (keycode == Input.Keys.N) {
      World.getInstance().generate();
    } else if (keycode == Input.Keys.G) {
      GameState.getInstance().tryNewParticles(true);
    } else if (keycode == Input.Keys.ENTER) {
      if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
        if (!Gdx.graphics.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else Gdx.graphics.setWindowedMode(Param.DISPLAY_X, Param.DISPLAY_Y);
      }
    } else if (keycode == Input.Keys.ESCAPE) {
      Gdx.app.exit();
    }
    return false;
  }

}
