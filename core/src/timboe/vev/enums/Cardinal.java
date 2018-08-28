package timboe.vev.enums;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timboe.vev.pathfinding.IVector2;

public enum Cardinal {
  kN,
  kNE,
  kE,
  kSE,
  kS,
  kSW,
  kW,
  kNW,
  kNONE;

  public static final List<Cardinal> NESW = new ArrayList<Cardinal>(Arrays.asList(kN, kE, kS, kW));

  public static final List<Cardinal> n8 = new ArrayList<Cardinal>(Arrays.asList(kN, kNE, kE, kSE, kS, kSW, kW, kNW));

  public static final List<Cardinal> corners = new ArrayList<Cardinal>(Arrays.asList(kNW, kNE, kSE, kSW));

  public static Cardinal fromAngle(float a) {
    Cardinal toReturn = kE;
    float threshold = 22.5f;
    while (threshold <= 360) {
      if (a < threshold) return toReturn;
      threshold += 45f;
      toReturn = toReturn.next45(false);
    }
    return toReturn;
  }

  public void modVec(IVector2 v) {
    switch (this) {
      case kN: case kNE: case kNW: ++v.x; break;
      case kS: case kSE: case kSW: --v.x; break;
    }
    switch (this) {
      case kE: case kNE: case kSE: ++v.y; break;
      case kW: case kNW: case kSW: --v.y; break;
    }
  }

  public Cardinal next90(boolean clockwise) {
    if (this == kNONE) Gdx.app.error("Cardinal", "Called next90(clockwise="+clockwise+") on "+kNONE.getString());
    if (clockwise) {
      if (this == kW) return kN;
      if (this == kNW) return kNE;
      return values()[ordinal() + 2];
    } else {
      if (this == kN) return kW;
      if (this == kNE) return kNW;
      return values()[ordinal() - 2];
    }
  }

  public Cardinal next45(boolean clockwise) {
    if (this == kNONE) Gdx.app.error("Cardinal", "Called next45(clockwise="+clockwise+") on "+kNONE.getString());
    if (clockwise) {
      if (this == kNW) return kN;
      return values()[ordinal() + 1];
    } else {
      if (this == kN) return kNW;
      return values()[ordinal() - 1];
    }
  }

  public String getString() {
    switch (this) {
      case kN: return "N";
      case kNE: return "NE";
      case kE: return "E";
      case kSE: return "SE";
      case kS: return  "S";
      case kSW: return "SW";
      case kW: return "W";
      case kNW: return "NW";
      case kNONE: return "NONE";
      default:
        Gdx.app.error("getString","Unknown Cardinal");
        return "";
    }
  }
}
