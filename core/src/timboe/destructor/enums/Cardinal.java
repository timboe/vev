package timboe.destructor.enums;

import com.badlogic.gdx.Gdx;

import java.util.EnumSet;
import java.util.HashSet;

public enum Cardinal {
  kN,
  kNE,
  kE,
  kSE,
  kS,
  kSW,
  kW,
  kNW;

  public Cardinal next90() {
    if (this == kW) return kN;
    if (this == kNW) return kNE;
    return values()[ordinal() + 2];
  }

  public Cardinal next45() {
    if (this == kNW) return kN;
    return values()[ordinal() + 1];
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
      default:
        Gdx.app.error("getString","Unknown Cardinal");
        return "";
    }
  }
}
