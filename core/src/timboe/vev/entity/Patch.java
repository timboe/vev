package timboe.vev.entity;

import timboe.vev.Util;

/**
 * Created by Tim on 21/01/2018.
 */

public class Patch extends Entity {
  float energy;

  public Patch(int x, int y) {
    super(x,y);
    energy = 50000f * Util.R.nextFloat(); // Cosmetic only
  }
}
