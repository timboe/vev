package timboe.destructor.enums;

/**
 * Created by Tim on 01/01/2018.
 */

public enum Particle {
  kH,
  kW,
  kZ,
  kE,
  kM,
  kQ;

  public String getString() {
    switch (this) {
      case kH: return "H";
      case kW: return "W";
      case kZ: return "Z";
      case kE: return "e";
      case kM: return "μ";
      case kQ: return "q";
      default: return "?";
    }
  }

  public static Particle getParticleFromColour(Colour c) {
    switch (c) {
      case kBLACK: return kH;
      case kRED_DARK: return kW;
      case kGREEN_DARK: return kZ;
      case kRED: return kE;
      case kGREEN: return kM;
      case kBLUE: return kQ;
      default: return null;
    }
  }

  public Colour getColourFromParticle() {
    switch (this) {
      case kH: return Colour.kBLACK;
      case kW: return Colour.kRED_DARK;
      case kZ: return Colour.kGREEN_DARK;
      case kE: return Colour.kRED;
      case kM: return Colour.kGREEN;
      case kQ: return Colour.kBLUE;
      default: return null;
    }
  }

  public static String getStringFromColour(Colour c) {
    return getParticleFromColour(c).getString();
  }

}
