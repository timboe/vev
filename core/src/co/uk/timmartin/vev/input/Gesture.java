package co.uk.timmartin.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.enums.UIMode;
import co.uk.timmartin.vev.manager.Camera;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.UI;

public class Gesture implements GestureDetector.GestureListener {

  private float initialZoom = Camera.getInstance().getZoom();
  private Vector3 v3temp = new Vector3();

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    if (button == Input.Buttons.LEFT && UI.getInstance().uiMode != UIMode.kSETTINGS) { // Start a SELECT action
      setStartEnd(x, y);
    }
    return false;
  }

  private void setStartEnd(float x, float y) {
    GameState.getInstance().selectStartWorld.set(x, y, 0);
    GameState.getInstance().selectStartWorld = Camera.getInstance().unproject(GameState.getInstance().selectStartWorld);

    GameState.getInstance().selectEndWorld.set(x, y, 0);
    GameState.getInstance().selectEndWorld = Camera.getInstance().unproject(GameState.getInstance().selectEndWorld);
  }

  @Override
  public boolean tap(float x, float y, int count, int button) {
    GameState state = GameState.getInstance();
    UI ui = UI.getInstance();
    v3temp.set(x, y, 0);
    v3temp = Camera.getInstance().unproject(v3temp);
    ++GameState.getInstance().taps;

    if (button == Input.Buttons.RIGHT) {
      state.showMainUITable(false);
    }

    if (!Param.IS_ANDROID) {
      if (ui.uiMode == UIMode.kPLACE_BUILDING) {
        state.placeBuilding();
      } else if (ui.uiMode == UIMode.kWITH_BUILDING_SELECTION && GameState.getInstance().doingPlacement) {
        state.doConfirmStandingOrder();
      }
    }

    if (ui.uiMode == UIMode.kSETTINGS) {
      return false;
    }

    boolean selectedJustNow = state.doParticleSelect(false); // rangeBased = false
    if (!selectedJustNow && !state.selectedSet.isEmpty()) {
      state.doParticleMoveOrder((int) v3temp.x, (int) v3temp.y);
      if (Param.IS_ANDROID) {
        state.showMainUITable(false);
      }
    }

    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    if (Param.IS_ANDROID && UI.getInstance().uiMode != UIMode.kSETTINGS && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
      GameState.getInstance().startSelectingAndroid();
      setStartEnd(x, y);
    }
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    if (!Param.IS_ANDROID && button == Input.Buttons.LEFT) {
      // Only allow fling from the right mouse button
      return false;
    }
    Camera.getInstance().velocity(-velocityX, velocityY);
    return false;
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ||
            Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) ||
            (UI.getInstance().selectParticlesButton != null && !UI.getInstance().selectParticlesButton.isChecked() && Param.IS_ANDROID)) {
      Camera.getInstance().translate(-deltaX, deltaY);
    }
    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && UI.getInstance().uiMode != UIMode.kSETTINGS) { // Select box
      GameState.getInstance().selectEndWorld.set(x, y, 0);
      GameState.getInstance().selectEndWorld = Camera.getInstance().unproject(GameState.getInstance().selectEndWorld);
    }
    return false;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    float fraction = initialDistance / distance;
    Camera.getInstance().setZoom(fraction * initialZoom);
    return false;
  }

  @Override
  public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
    return false;
  }

  @Override
  public void pinchStop() {
    initialZoom = Camera.getInstance().getZoom();
  }
}
