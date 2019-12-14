package timboe.vev.entity;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.EnumMap;

import timboe.vev.Util;
import timboe.vev.enums.Particle;
import timboe.vev.pathfinding.IVector2;

/**
 * Created by Tim on 21/01/2018.
 */

public class Patch extends Entity {
  float energy;

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(false);
    json.put("energy", energy);
    return json;
  }

  public Patch(JSONObject json) throws JSONException {
    super(json);
    energy = (float) json.getDouble("energy");
  }

  public Patch(int x, int y) {
    super(x,y);
    energy = 50000f * Util.R.nextFloat(); // Cosmetic only
  }
}
