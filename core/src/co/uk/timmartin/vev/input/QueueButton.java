package co.uk.timmartin.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import co.uk.timmartin.vev.enums.BuildingType;
import co.uk.timmartin.vev.enums.QueueType;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.Sounds;
import co.uk.timmartin.vev.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class QueueButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    GameState.getInstance().queueType = (QueueType) actor.getUserObject();
    BuildingType bt = GameState.getInstance().buildingBeingPlaced;
    UI.getInstance().buildingWindowQSimple.get(bt).setChecked(GameState.getInstance().queueType == QueueType.kSIMPLE);
    UI.getInstance().buildingWindowQSpiral.get(bt).setChecked(GameState.getInstance().queueType == QueueType.kSPIRAL);
    Sounds.getInstance().OK();
  }
}
