package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import timboe.vev.Param;
import timboe.vev.manager.GameState;
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
          GameState.getInstance().warpParticles = Param.PARTICLES_SMALL;
          break;
        case 1:
          GameState.getInstance().warpParticles = Param.PARTICLES_MED;
          break;
        case 2:
          GameState.getInstance().warpParticles = Param.PARTICLES_LARGE;
          break;
        case 3:
          GameState.getInstance().warpParticles = Param.PARTICLES_XL;
          break;
        default:
          Gdx.app.error("NewGame", "Unknown button " + object);
      }
      if (!World.getInstance().getGenerated()) {
        World.getInstance().launchAfterGen = true;
      } else {
        StateManager.getInstance().transitionToGameScreen();
      }
    } else {
      Gdx.app.log("result","Pressed CANCEL");
    }
  }
}
