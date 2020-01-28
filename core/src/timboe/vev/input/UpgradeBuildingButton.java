package timboe.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.vev.entity.Building;
import timboe.vev.manager.GameState;

public class UpgradeBuildingButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Button b = (Button) actor;
    if (!b.isChecked()) {
      b.setChecked(true); // Do not allow user to un-toggle
      return;
    }
    Building building = GameState.getInstance().getSelectedBuilding();
    boolean canUpgrade = building.upgradeBuilding();
    b.setChecked(canUpgrade);
  }
}