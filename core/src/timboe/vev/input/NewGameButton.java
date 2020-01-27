package timboe.vev.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import timboe.vev.DistanceField.TextButtonDF;
import timboe.vev.Lang;
import timboe.vev.Param;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Persistence;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;

public class NewGameButton extends ChangeListener {

  NewGameDiag newGameDialog = null;

  public void setDiag(NewGameDiag d) {
    newGameDialog = d;

    final UI ui = UI.getInstance();
    Table head = newGameDialog.getContentTable();
    head.pad(ui.PAD * 4);
    head.add(ui.getLabel(Lang.get("UI_GAME_LENGTH"), "")).colspan(3).row();
    //
    head.add(new Image( Textures.getInstance().getTexture("separator", false) )).padTop(10).fillX().colspan(3);
    head.row();
    //
    head.add(ui.getLabel(Lang.get("UI_LENGTH"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Lang.get("UI_PARTICLES_"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Lang.get("UI_BEST"), "")).pad(ui.PAD * 4).row();
    //
    head.add(new Image( Textures.getInstance().getTexture("separator", false) )).fillX().colspan(3);
    head.row();
    //
    head.add(ui.getLabel(Lang.get("UI_SHORT"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Integer.toString(Param.PARTICLES_SMALL), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel((Persistence.getInstance().bestTimes.get(0) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(0) + "s"), "") ).pad(ui.PAD * 4).row();
//
    head.add(ui.getLabel(Lang.get("UI_MED"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Integer.toString(Param.PARTICLES_MED), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel((Persistence.getInstance().bestTimes.get(1) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(1) + "s"), "") ).pad(ui.PAD * 4).row();
    //
    head.add(ui.getLabel(Lang.get("UI_LONG"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Integer.toString(Param.PARTICLES_LARGE), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel((Persistence.getInstance().bestTimes.get(2) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(2) + "s"), "") ).pad(ui.PAD * 4).row();
    //
    head.add(ui.getLabel(Lang.get("UI_XL"), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel(Integer.toString(Param.PARTICLES_XL), "")).pad(ui.PAD * 4);
    head.add(ui.getLabel((Persistence.getInstance().bestTimes.get(3) == 0 ? "N/A" : Persistence.getInstance().bestTimes.get(3) + "s"), "") ).pad(ui.PAD * 4).row();
    //
    newGameDialog.pad(ui.PAD * 4);
    newGameDialog.getButtonTable().pad(ui.PAD * 4);
    newGameDialog.align(Align.center);
    TextButtonDF A = (TextButtonDF)ui.getTextButton(Lang.get("UI_SHORT"), "");
    TextButtonDF B = (TextButtonDF)ui.getTextButton(Lang.get("UI_MED"), "");
    TextButtonDF C = (TextButtonDF)ui.getTextButton(Lang.get("UI_LONG"), "");
    TextButtonDF D = (TextButtonDF)ui.getTextButton(Lang.get("UI_XL"), "");
    TextButtonDF E = (TextButtonDF)ui.getTextButton(Lang.get("UI_CANCEL"), "");
    A.getLabelCell().width(ui.SIZE_L*3);
    B.getLabelCell().width(ui.SIZE_L*3);
    C.getLabelCell().width(ui.SIZE_L*3);
    D.getLabelCell().width(ui.SIZE_L*3);
    E.getLabelCell().width(ui.SIZE_L*3);
    newGameDialog.button(A, 0);
    newGameDialog.button(B, 1);
    newGameDialog.getButtonTable().row();
    newGameDialog.button(C, 2);
    newGameDialog.button(D, 3);
    newGameDialog.getButtonTable().row();
    newGameDialog.button(E, -1);
    newGameDialog.getButtonTable().getCell(E).colspan(4);
    newGameDialog.key(Input.Keys.ENTER, 2).key(Input.Keys.ESCAPE, 0);
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Sounds.getInstance().OK();
    newGameDialog.show(IntroState.getInstance().getUIStage());
  }
}
