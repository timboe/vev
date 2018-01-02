package timboe.destructor.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Colour;
import timboe.destructor.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class BuildingButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    BuildingType bt = (BuildingType) actor.getUserObject();
    UI.getInstance().showBuildBuilding(bt);
  }
}
