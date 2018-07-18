package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.enums.Particle;

import java.util.Random;

public class Camera {

  private static Camera ourInstance;
  public static Camera getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Camera(); }
  public void dispose() {  ourInstance = null; }

  private final Rectangle cullBoxTile = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  private final Rectangle cullBoxSprite = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

  private float currentZoom = 1f;
  private float desiredZoom = 1f;

  private final Vector2 currentPos = new Vector2(0,0);
  private final Vector2 desiredPos = new Vector2(0,0);
  private final Vector2 velocity = new Vector2(0,0);
  private float shake;
  private float shakeAngle;

  private final Random R = new Random();

  private OrthographicCamera tileCamera;
  private OrthographicCamera spriteCamera;
  private OrthographicCamera uiCamera;

  private FitViewport tileViewport;
  private FitViewport spriteViewport;
  private FitViewport uiViewport;

  private Camera() {
    reset();
  }

  public void setCurrentPos(float x, float y) {
    this.currentPos.set(x,y);
    this.desiredPos.set(x,y);
  }

  public void setCurrentZoom(float currentZoom) {
    this.currentZoom = currentZoom;
    this.desiredZoom = currentZoom;
  }

  public FitViewport getTileViewport() {
    return tileViewport;
  }

  public FitViewport getSpriteViewport() {
    return spriteViewport;
  }

  public FitViewport getUiViewport() {
    return uiViewport;
  }

  public void resize(int width, int height) {
    tileViewport.update(width, height, true);
    spriteViewport.update(width, height, true);
    uiViewport.update(width, height, true);
  }

  public OrthographicCamera getTileCamera() {
    return tileCamera;
  }

  public OrthographicCamera getSpriteCamera() {
    return spriteCamera;
  }

  public OrthographicCamera getUiCamera() {
    return uiCamera;
  }

  public Rectangle getCullBoxTile() {
    return cullBoxTile;
  }

  public Rectangle getCullBoxSprite() {
    return cullBoxSprite;
  }

  public Vector3 unproject(Vector3 v) {
    return tileCamera.unproject(v);
  }

  public void addShake(Rectangle r, float amount) {
    if (!onScrean(r)) return;
    shake += amount;
  }

  public float distanceToCamera(int x, int y) {
    return (float)Math.hypot(x - currentPos.x, y - currentPos.y);
  }

  public boolean onScrean(Rectangle r) {
    return cullBoxTile.contains(r) || cullBoxTile.overlaps(r);
  }

  private void reset() {
    tileCamera = new OrthographicCamera();
    spriteCamera = new OrthographicCamera();
    uiCamera = new OrthographicCamera();

    tileViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, tileCamera);
    spriteViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, spriteCamera);
    uiViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, uiCamera);
  }

  public void translate(float x, float y) {
    desiredPos.add(x * currentZoom, y * currentZoom);
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


  public void update(float delta) {
    float frames = delta / Param.FRAME_TIME;

    desiredPos.add(velocity);
    velocity.scl((float)Math.pow(0.9f, frames));

    shake *= (float)Math.pow(0.9f, frames);
    shakeAngle = R.nextFloat() * (float)Math.PI * 2f;

    currentPos.set(desiredPos);
    currentPos.add(shake * (float)Math.cos(shakeAngle), shake * (float)Math.sin(shakeAngle));
    currentZoom = desiredZoom;

    tileCamera.position.set(currentPos, 0);
    tileCamera.zoom = currentZoom;
    tileCamera.update();

    spriteCamera.position.set(currentPos, 0);
    spriteCamera.zoom = currentZoom;
    spriteCamera.zoom *= (float)Param.SPRITE_SCALE;
    spriteCamera.position.scl((float)Param.SPRITE_SCALE);
    spriteCamera.update();

    uiCamera.position.set(uiViewport.getWorldWidth()/2, uiViewport.getWorldHeight()/2, 0f);
    float tempShakeAngle = R.nextFloat() * (float)Math.PI * 2f;
    uiCamera.position.add(shake * (float)Math.cos(tempShakeAngle) / currentZoom , shake * (float)Math.sin(tempShakeAngle) / currentZoom, 0);
    uiCamera.update();

//    Gdx.app.log("Camera","camera current " + currentPos);

    cullBoxTile.set(tileCamera.position.x - tileViewport.getScreenWidth()/2*currentZoom,
        tileCamera.position.y - tileViewport.getScreenHeight()/2*currentZoom,
        tileViewport.getScreenWidth() * currentZoom,
        tileViewport.getScreenHeight() * currentZoom);

    // TODO fix
    cullBoxSprite.set(spriteCamera.position.x - Param.SPRITE_SCALE*spriteViewport.getScreenWidth()/2*currentZoom,
        spriteCamera.position.y - Param.SPRITE_SCALE*spriteViewport.getScreenHeight()/2*currentZoom,
        Param.SPRITE_SCALE * spriteViewport.getScreenWidth() * currentZoom,
        Param.SPRITE_SCALE * spriteViewport.getScreenHeight() * currentZoom);
  }

}