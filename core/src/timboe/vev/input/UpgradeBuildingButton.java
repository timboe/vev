package timboe.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.vev.enums.BuildingType;
import timboe.vev.manager.UI;

public class UpgradeBuildingButton  extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Button b = (Button)actor;
    if (!b.isChecked()) {
      b.setChecked(true); // Do not allow user to un-toggle
      return;
    }
    UI.getInstance().selectedBuilding.upgradeBuilding();
  }
}