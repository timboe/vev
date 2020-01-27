package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import timboe.vev.Param;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.StateManager;
import timboe.vev.manager.World;

public class NewGameDiag extends Dialog {

  public NewGameDiag(String title, Skin skin) {
    super(title, skin);
  }

  protected void result(Object object) {
    if ((Integer) object >= 0) {
      GameState.getInstance().difficulty = (Integer) object;
      switch ((Integer) object) {
        case 0:
          World.getInstance().warpParticlesCached = Param.PARTICLES_SMALL;
          break;
        case 1:
          World.getInstance().warpParticlesCached = Param.PARTICLES_MED;
          break;
        case 2:
          World.getInstance().warpParticlesCached = Param.PARTICLES_LARGE;
          break;
        case 3:
          World.getInstance().warpParticlesCached = Param.PARTICLES_XL;
          break;
        default:
          Gdx.app.error("NewGame", "Unknown button " + object);
      }
      Sounds.getInstance().OK();
      StateManager.getInstance().transitionToGameScreen();
    } else {
      Sounds.getInstance().cancel();
      Gdx.app.log("result","Pressed CANCEL");
    }
  }
}
