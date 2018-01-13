package timboe.destructor.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.destructor.Param;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Particle;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class QueueLengthSlider extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    GameState.getInstance().queueSize = (int) ((Slider)actor).getValue();
    BuildingType bt = UI.getInstance().buildingBeingPlaced;
    UI.getInstance().buildingWindowQSize.get(bt).setText(""+ GameState.getInstance().queueSize);
    UI.getInstance().buildingWindowQPrice.get(bt).setText( UI.getInstance().formatter.format( bt.getCost() ) );
  }
}
