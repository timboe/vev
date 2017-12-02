package timboe.destructor.entity;

import timboe.destructor.Param;

public class Sprite extends Entity {

  public Sprite(int x, int y) {
    super(x, y, Param.TILE_S * Param.SPRITE_SCALE, Param.WIGGLE);
  }

}
