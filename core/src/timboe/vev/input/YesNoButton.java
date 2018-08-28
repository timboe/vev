package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.vev.manager.GameState;
import timboe.vev.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class YesNoButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    boolean N = ((Integer) actor.getUserObject() == 0);
    if (N) {
      GameState.getInstance().doRightClick();
    } else { // Y
      switch (UI.getInstance().uiMode) {
        case kPLACE_BUILDING:
          GameState.getInstance().placeBuilding();
          break;
        case kWITH_BUILDING_SELECTION:
          GameState.getInstance().doConfirmStandingOrder();
          break;
        default:
          Gdx.app.error("YesNoButton", "YES is not defined for this ui mode" + UI.getInstance().uiMode.toString());
      }
    }
  }
}
