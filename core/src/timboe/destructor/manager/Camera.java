package timboe.destructor.manager;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import timboe.destructor.Param;
import timboe.destructor.Util;

import java.util.Random;

public class Camera {

  private static Camera ourInstance;
  public static Camera getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Camera(); }
  public void dispose() {  ourInstance = null; }

  private Rectangle cullBox = new Rectangle(0, 0, Param.DISPLAY_X, Param.DISPLAY_Y);

  private float currentZoom = 1f;
  private float desiredZoom = 1f;

  private Vector2 currentPos = new Vector2(0,0);
  private Vector2 desiredPos = new Vector2(0,0);
  private Vector2 velocity = new Vector2(0,0);
  private float shake;
  private float shakeAngle;

  private Random R = new Random();

  private OrthographicCamera camera;
  private FitViewport viewport;

  private Camera() {
    reset();
  }

  public FitViewport getViewport() {
    return viewport;
  }

  public OrthographicCamera getCamera() {
    return camera;
  }

  public Rectangle getCullBox() {
    return cullBox;
  }

  public Vector3 unproject(Vector3 v) {
    return camera.unproject(v);
  }

  public void addShake(float amount) {
    shake += amount;
  }

  public float distanceToCamera(int x, int y) {
    return (float)Math.hypot(x - currentPos.x, y - currentPos.y);
  }

  public boolean onScrean(Rectangle r) {
    return cullBox.contains(r) || cullBox.overlaps(r);
  }

  private void reset() {
    camera = new OrthographicCamera();
    viewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, camera);
  }

  public void translate(float x, float y) {
    desiredPos.add(x, y);
  }

  public void velocity(float x, float y) {
    this.velocity.set(x, y);
  }

  public void modZoom(float z){
    desiredZoom += z;
    desiredZoom = Util.clamp(desiredZoom, Param.ZOOM_MIN, Param.ZOOM_MAX);
  }

  public void setZoom(float z) {
    desiredZoom = z;
    desiredZoom = Util.clamp(desiredZoom, Param.ZOOM_MIN, Param.ZOOM_MAX);
  }

  public float getZoom() {
    return desiredZoom;
  }

  public void update() {
    camera.position.set(currentPos, 0);
    camera.zoom = currentZoom;
    camera.update();
  }

  public void updateUI() {
    camera.position.set(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2, 0f);
//    camera.position.set(viewport.getWorldWidth(), viewport.getWorldHeight(), 0f);
//    camera.position.set(0f, 0f, 0f);
    float tempShakeAngle = R.nextFloat() * (float)Math.PI * 2f;
    camera.position.add(shake * (float)Math.cos(tempShakeAngle) / currentZoom, shake * (float)Math.sin(tempShakeAngle) / currentZoom, 0);
    camera.zoom = 1f;
    camera.update();
  }

  public void updateSprite() { // Note - expects to come from "update"
    camera.zoom *= (float)Param.SPRITE_SCALE;
    camera.position.scl((float)Param.SPRITE_SCALE);
    camera.update();
  }

  public void updateTiles(float delta) {
    float frames = delta / Param.FRAME_TIME;

    desiredPos.add(velocity);
    velocity.scl((float)Math.pow(0.9f, frames));

    shake *= (float)Math.pow(0.9f, frames);
    shakeAngle = R.nextFloat() * (float)Math.PI * 2f;

    currentPos.set(desiredPos);
    currentPos.add(shake * (float)Math.cos(shakeAngle), shake * (float)Math.sin(shakeAngle));
    currentZoom = desiredZoom;

    update();

    // TODO take into consideration ZOOM
    int startX = (int)camera.position.x - viewport.getScreenWidth()/2;
    int startY = (int)camera.position.y - viewport.getScreenHeight()/2;
    cullBox.set(startX, startY, viewport.getScreenWidth(), viewport.getScreenHeight());
  }

}