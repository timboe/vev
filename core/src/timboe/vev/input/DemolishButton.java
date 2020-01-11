package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import timboe.vev.Param;
import timboe.vev.entity.Building;
import timboe.vev.enums.BuildingType;
import timboe.vev.manager.GameState;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

public class DemolishButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Dialog destroyDialog = new Dialog("", UI.getInstance().skin) {
      protected void result(Object object) {
        if ((Integer)object == 0) {
          return;
        }
        GameState.getInstance().killSelectedBuilding();
      }
    };
    destroyDialog.align(Align.center);
    destroyDialog.text(UI.getInstance().getLabel("Demolish Building?",""));
    destroyDialog.row();
    destroyDialog.button(UI.getInstance().getTextButton("YES",""), 1);
    destroyDialog.button(UI.getInstance().getTextButton("NO",""), 0);
    destroyDialog.show(GameState.getInstance().getUIStage());
  }
}
