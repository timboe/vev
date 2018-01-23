package timboe.destructor.entity;

import timboe.destructor.Util;
import timboe.destructor.pathfinding.IVector2;

/**
 * Created by Tim on 21/01/2018.
 */

public class Patch extends Entity {
  float energy;

  public Patch(int x, int y) {
    super(x,y);
    energy = 50000f + Util.R.nextFloat(); // Cosmetic only
  }
}
