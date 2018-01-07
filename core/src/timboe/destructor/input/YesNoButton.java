package timboe.destructor.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.destructor.Param;
import timboe.destructor.enums.QueueType;
import timboe.destructor.enums.UIMode;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.UI;

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
        case kSELECTING:
          GameState.getInstance().reduceSelectedSet();
          break;
        default:
          Gdx.app.error("YesNoButton", "YES is not defined for this ui mode" + UI.getInstance().uiMode.toString());
      }
    }
  }
}
