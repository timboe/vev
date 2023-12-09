package co.uk.timmartin.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import co.uk.timmartin.vev.Lang;
import co.uk.timmartin.vev.entity.Building;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.Sounds;
import co.uk.timmartin.vev.manager.UI;

public class DemolishButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Dialog destroyDialog = new Dialog("", UI.getInstance().skin) {
      protected void result(Object object) {
        if ((Integer) object == 0) {
          Sounds.getInstance().cancel();
          return;
        }
        GameState.getInstance().killSelectedBuilding();
      }
    };
    Sounds.getInstance().OK();
    Building sb = GameState.getInstance().getSelectedBuilding();
    final UI ui = UI.getInstance();
    final int refundAmount = (sb == null ? 0 : sb.refund);
    destroyDialog.align(Align.center);
    destroyDialog.pad(ui.PAD * 4);
    destroyDialog.text(ui.getLabel(Lang.get("demolish#" + ui.formatter.format(refundAmount)), ""));
    destroyDialog.row();
    destroyDialog.button(ui.getTextButton(Lang.get("UI_YES"), ""), 1);
    destroyDialog.button(ui.getTextButton(Lang.get("UI_NO"), ""), 0);
    destroyDialog.show(GameState.getInstance().getUIStage());
  }
}
