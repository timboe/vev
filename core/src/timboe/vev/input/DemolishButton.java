package timboe.vev.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import timboe.vev.Lang;
import timboe.vev.entity.Building;
import timboe.vev.manager.GameState;
import timboe.vev.manager.UI;

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
    Building sb = GameState.getInstance().getSelectedBuilding();
    final int refundAmount = (sb == null ? 0 : sb.refund);
    destroyDialog.align(Align.center);
    destroyDialog.text(UI.getInstance().getLabel(Lang.get("demolish#"+refundAmount),""));
    destroyDialog.row();
    destroyDialog.button(UI.getInstance().getTextButton(Lang.get("UI_YES"),""), 1);
    destroyDialog.button(UI.getInstance().getTextButton(Lang.get("UI_NO"),""), 0);
    destroyDialog.show(GameState.getInstance().getUIStage());
  }
}
