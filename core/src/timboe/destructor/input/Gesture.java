package timboe.destructor.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import timboe.destructor.manager.Camera;
import timboe.destructor.manager.World;

public class Gesture implements GestureDetector.GestureListener {

  float initialZoom = Camera.getInstance().getZoom();

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean tap(float x, float y, int count, int button) {
    World.getInstance().generate();
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    Camera.getInstance().velocity(-velocityX * 0.01f, velocityY * 0.01f);
    return false;
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    Camera.getInstance().translate(-deltaX, deltaY);
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
