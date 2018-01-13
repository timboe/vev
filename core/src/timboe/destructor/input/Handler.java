package timboe.destructor.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import timboe.destructor.Param;
import timboe.destructor.enums.Particle;
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
    if (button == Input.Buttons.LEFT && GameState.getInstance().isSelecting()) {
      Gdx.app.log("touchUp", "End selecting");
      GameState.getInstance().doParticleSelect(true);
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
