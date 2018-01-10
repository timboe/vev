package timboe.destructor.entity;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Arrays;
import java.util.List;

import timboe.destructor.Param;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.manager.Textures;

/**
 * Created by Tim on 10/01/2018.
 */

public class Warp extends Building {

  private float[] rotAngle = {0f, 90f, 0f, -90f};
  private final float[] rotV = {Param.WARP_ROTATE_SPEED, Param.WARP_ROTATE_SPEED, -Param.WARP_ROTATE_SPEED, -Param.WARP_ROTATE_SPEED};

  public Warp(Tile t) {
    super(t, BuildingType.kWARP);
    setTexture( Textures.getInstance().getTexture("void", false), 0);
    setTexture( Textures.getInstance().getTexture("void", true), 1);
    setTexture( Textures.getInstance().getTexture("void", false), 2);
    setTexture( Textures.getInstance().getTexture("void", true), 3);
    moveBy(0, -Param.TILE_S/2);
  }

  @Override
  public void act(float delta) {
    for (int i = 0; i < 4; ++i) {
      rotAngle[i] += delta * rotV[i];
    }
  }

  @Override
  public void draw(Batch batch, float alpha) {
    for (int i = 0; i < 4; ++i) {
      batch.draw(textureRegion[i],this.getX(),this.getY(),this.getOriginX(),this.getOriginY(),this.getWidth(),this.getHeight(),this.getScaleX(),this.getScaleY(),rotAngle[i]);
    }
  }

}
