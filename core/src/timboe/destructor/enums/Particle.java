package timboe.destructor.enums;

import com.badlogic.gdx.graphics.Color;

import timboe.destructor.Param;

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

  public Color getHighlightColour() {
    switch (this) {
      case kH: return Param.PARTICLE_H;
      case kW: return Param.PARTICLE_W;
      case kZ: return Param.PARTICLE_Z;
      case kE: return Param.PARTICLE_E;
      case kM: return Param.PARTICLE_M;
      case kQ: return Param.PARTICLE_Q;
      default: return null;
    }
  }

  public int getStandingOrderOffset() {
    switch (this) {
      case kH: return 0;
      case kW: return 2;
      case kZ: return 4;
      case kE: return 6;
      case kM: return 8;
      case kQ: return 10;
      default: return 0;
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
