package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.vev.enums.UIMode;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.UIIntro;
import timboe.vev.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class YesNoButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    boolean N = ((Integer) actor.getUserObject() == 0);

    // Special - Intro HELP and CREDITS sections
    if (UI.getInstance().uiMode == UIMode.kHELP) {
      if (N) {
        UIIntro.getInstance().helpLevel -= 1;
      } else {
        UIIntro.getInstance().helpLevel += 1;
      }
      if ((Integer) actor.getUserObject() == 2 // "Back" is Obj==2
              || UIIntro.getInstance().helpLevel == 1
              || UIIntro.getInstance().helpLevel == 5) {
        UIIntro.getInstance().helpLevel = 0;
        UIIntro.getInstance().resetTitle("main");
      }
      Camera.getInstance().setHelpPos(UIIntro.getInstance().helpLevel, false);
      return;
    }

    // Special - Intro SETTINGS
    if (UI.getInstance().uiMode == UIMode.kSETTINGS) {
      if (N) {
        UIIntro.getInstance().cancelSettingsChanges();
      }
      UIIntro.getInstance().resetTitle("main");
      return;
    }

    // In game
    if (N) {
      GameState.getInstance().showMainUITable(false);
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
