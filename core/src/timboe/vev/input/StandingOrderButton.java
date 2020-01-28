package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.EnumMap;

import timboe.vev.Pair;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.UI;

/**
 * Created by Tim on 09/01/2018.
 */

public class StandingOrderButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Pair<BuildingType, Particle> data = (Pair<BuildingType, Particle>) actor.getUserObject();
    // Enforce one clicked
    if (data == null) return;
    final EnumMap<BuildingType, EnumMap<Particle, Button>> btButtonsMap = UI.getInstance().buildingSelectStandingOrder;
    if (!btButtonsMap.containsKey(data.getKey())) return;
    final EnumMap<Particle, Button> pButtonsMap = btButtonsMap.get(data.getKey());
    for (Particle p : Particle.values()) {
      // For WARP, we iterate over all of these. For buildings, only the important one or two
      if (!pButtonsMap.containsKey(p)) continue;
      Button b = pButtonsMap.get(p);
      if (p == data.getValue()) {
        if (b.isChecked()) { // I was just clicked ON - start doing pathing for this building
          Gdx.app.log("StandingOrderButton", "Clicked " + data.getKey().toString() + " " + data.getValue().toString());
          GameState.getInstance().doingPlacement = true;
          GameState.getInstance().selectedBuildingStandingOrderParticle = p;
          UI.getInstance().selectTickIsEnabled(true);
          Sounds.getInstance().foot();
        } else { // I was just clicked OFF - save the current pathing
          GameState.getInstance().doConfirmStandingOrder();
          UI.getInstance().selectTickIsEnabled(false);
          Sounds.getInstance().OK();
        }
      } else {
        b.setChecked(false);
      }
    }
  }
}
