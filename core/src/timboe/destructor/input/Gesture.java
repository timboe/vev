package timboe.destructor.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import timboe.destructor.Param;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.UIMode;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.UI;

public class Gesture implements GestureDetector.GestureListener {

  private float initialZoom = Camera.getInstance().getZoom();
  private Vector3 v3temp = new Vector3();

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    if (button == Input.Buttons.LEFT) { // Start a SELECT action
     setStartEnd(x,y);
    }
    return false;
  }

  private void setStartEnd(float x, float y) {
    GameState.getInstance().selectStartScreen.set(x, y, 0);
    GameState.getInstance().selectStartWorld.set(x, y, 0);
    GameState.getInstance().selectStartWorld = Camera.getInstance().unproject(GameState.getInstance().selectStartWorld);

    GameState.getInstance().selectEndScreen.set(x, y, 0);
    GameState.getInstance().selectEndWorld.set(x, y, 0);
    GameState.getInstance().selectEndWorld = Camera.getInstance().unproject(GameState.getInstance().selectEndWorld);
  }

  @Override
  public boolean tap(float x, float y, int count, int button) {
    Gdx.app.log("tap","tap "+x+","+"y");
    GameState state = GameState.getInstance();
    UI ui = UI.getInstance();
    v3temp.set(x, y, 0);
    v3temp = Camera.getInstance().unproject(v3temp);

    if (button == Input.Buttons.RIGHT) {
      state.doRightClick();
    } else if (!Param.IS_ANDROID && ui.uiMode == UIMode.kPLACE_BUILDING) {
      state.placeBuilding();
    } else if (!Param.IS_ANDROID && ui.uiMode == UIMode.kWITH_BUILDING_SELECTION && ui.doingPlacement) {
      state.doConfirmStandingOrder();
    } else {
      boolean selectedJustNow = state.doParticleSelect(false); // rangeBased = false
      if (!selectedJustNow && !state.selectedSet.isEmpty()) {
        state.doParticleMoveOrder((int) v3temp.x, (int) v3temp.y);
        if (Param.IS_ANDROID) state.doRightClick();
      }
    }
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    if (Param.IS_ANDROID) {
      GameState.getInstance().startSelectingAndroid();
      setStartEnd(x,y);
    }
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    Camera.getInstance().velocity(-velocityX * 0.01f, velocityY * 0.01f);
    return false;
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    if (Param.IS_ANDROID) {
      // Update both
      if (!UI.getInstance().selectParticlesButton.isChecked()) Camera.getInstance().translate(-deltaX, deltaY);
      GameState.getInstance().selectEndScreen.set(x, y, 0);
      GameState.getInstance().selectEndWorld.set(GameState.getInstance().selectEndScreen);
      GameState.getInstance().selectEndWorld = Camera.getInstance().unproject(GameState.getInstance().selectEndWorld);
    } else { // Non-android
      // Are we panning the screen or the select box?
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) { // Screen
        Camera.getInstance().translate(-deltaX, deltaY);
      }
      if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { // Select box
        GameState.getInstance().selectEndScreen.set(x, y, 0);
        GameState.getInstance().selectEndWorld.set(GameState.getInstance().selectEndScreen);
        GameState.getInstance().selectEndWorld = Camera.getInstance().unproject(GameState.getInstance().selectEndWorld);
      }
    }
    return false;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    float fraction = initialDistance/distance;
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
