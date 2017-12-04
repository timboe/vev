package timboe.destructor.enums;

public enum Colour {
  kRED,
  kGREEN,
  kBLUE,
  kBLACK;

  public String getString() {
    switch (this) {
      case kRED: return "r";
      case kGREEN: return "g";
      case kBLUE: return "b"; //TODO remove ambiguity
      default: return "b";
    }
  }
}
