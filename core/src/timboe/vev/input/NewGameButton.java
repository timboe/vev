package timboe.vev.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import timboe.vev.Lang;
import timboe.vev.Param;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Persistence;
import timboe.vev.manager.UI;

public class NewGameButton extends ChangeListener {

  NewGameDiag newGameDialog = null;

  public void setDiag(NewGameDiag d) {
    newGameDialog = d;

    final UI ui = UI.getInstance();
    String header = Lang.get("UI_GAME_LENGTH");
    header += "\n" + Lang.get("UI_SHORT") + Lang.get("UI_N_PARTICLES#" + Param.PARTICLES_SMALL);
    header += Lang.get("UI_BEST_TIME#"+ (Persistence.getInstance().bestTimes.get(0) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(0)) );
    header += "\n" + Lang.get("UI_MED") + Lang.get("UI_N_PARTICLES#" + Param.PARTICLES_MED);
    header += Lang.get("UI_BEST_TIME#"+ (Persistence.getInstance().bestTimes.get(1) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(1)) );
    header += "\n" + Lang.get("UI_LONG") + Lang.get("UI_N_PARTICLES#" + Param.PARTICLES_LARGE);
    header += Lang.get("UI_BEST_TIME#"+ (Persistence.getInstance().bestTimes.get(2) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(2)) );
    header += "\n" + Lang.get("UI_XL") + Lang.get("UI_N_PARTICLES#" + Param.PARTICLES_XL);
    header += Lang.get("UI_BEST_TIME#"+ (Persistence.getInstance().bestTimes.get(3) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(3)) );
    newGameDialog.pad(ui.PAD * 4);
    newGameDialog.getContentTable().pad(ui.PAD * 4);
    newGameDialog.getButtonTable().pad(ui.PAD * 4);
    newGameDialog.align(Align.center);
    newGameDialog.text(ui.getLabel(header, ""));
    newGameDialog.button(ui.getTextButton(Lang.get("UI_SHORT"), ""), 0);
    newGameDialog.button(ui.getTextButton(Lang.get("UI_MED"), ""), 1);
    newGameDialog.button(ui.getTextButton(Lang.get("UI_LONG"), ""), 2);
    newGameDialog.button(ui.getTextButton(Lang.get("UI_XL"), ""), 3);
    newGameDialog.getButtonTable().row();
    Button c = ui.getTextButton(Lang.get("UI_CANCEL"), "");
    newGameDialog.button(c, -1);
    newGameDialog.getButtonTable().getCell(c).colspan(4);
    newGameDialog.key(Input.Keys.ENTER, 2).key(Input.Keys.ESCAPE, 0);
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {
    newGameDialog.show(IntroState.getInstance().getUIStage());
  }
}
