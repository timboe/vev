package timboe.destructor.enums;

public enum Colour {
  kRED,
  kGREEN,
  kBLACK;

  public String getString() {
    switch (this) {
      case kRED: return "r";
      case kGREEN: return "g";
      default: return "b";
    }
  }
}
