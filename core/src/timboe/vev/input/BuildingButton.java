package timboe.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.vev.enums.BuildingType;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.UI;

/**
 * Created by Tim on 02/01/2018.
 */

public class BuildingButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    BuildingType bt = (BuildingType) actor.getUserObject();
    UI.getInstance().showBuildBuilding(bt);
    Sounds.getInstance().OK();
  }
}
