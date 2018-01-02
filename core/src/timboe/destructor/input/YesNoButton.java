package timboe.destructor.input;

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
    int YN = (Integer)actor.getUserObject();
    if (YN == 0) UI.getInstance().showMain();
    if (UI.getInstance().uiMode == UIMode.kPLACE_BUILDING) GameState.getInstance().placeBuilding();
  }
}
