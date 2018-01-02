package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.QueueType;
import timboe.destructor.enums.UIMode;
import timboe.destructor.input.BuildingButton;
import timboe.destructor.input.ParticleButton;
import timboe.destructor.input.QueueButton;
import timboe.destructor.input.QueueLengthSlider;
import timboe.destructor.input.YesNoButton;

public class UI {

  private static UI ourInstance;
  public static UI getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new UI(); }
  public void dispose() { ourInstance = null; }

  public UIMode uiMode = UIMode.kNONE;
  public BuildingType buildingBeingPlaced = null;

  private final int SIZE_S = 32;
  private final int SIZE_M = 2*SIZE_S;
  private final int SIZE_L = 2*SIZE_M;

  private final int PAD = 8;

  private Table table;
  private Skin skin;

  private Window mainWindow;
  private Window selectWindow;

  private EnumMap<BuildingType, Window> buildingWindow;
  public EnumMap<BuildingType, ImageButton>buildingWindowQSimple;
  public EnumMap<BuildingType, ImageButton> buildingWindowQSpiral;
  public EnumMap<BuildingType, Label> buildingWindowQSize;

  private EnumMap<Colour, Button> selectButton;
  private EnumMap<Colour, Label> selectLabel;

  public Table getTable() {
    return table;
  }

  private UI() {
    reset();
  }

  private Label getLabel(String label) {
    Label l = new Label(label, skin, "default");
    l.setAlignment(Align.center);
    l.setWidth(SIZE_S);
    l.setHeight(SIZE_S);
    return l;
  }

  private Image getImage(String image) {
    Image i = new Image( Textures.getInstance().getTexture(image, false) );
    i.setWidth(SIZE_S);
    i.setHeight(SIZE_S);
    i.setAlign(Align.center);
    return i;
  }

  private Window getWindow(String window) {
    Window w = new Window(window, skin, "default");
    w.getTitleLabel().setAlignment(Align.center);
    w.setMovable(false);
    return w;
  }

  private ImageButton getImageButton(String image, String style) {
    ImageButton.ImageButtonStyle ibs = new ImageButton.ImageButtonStyle( skin.get(style, ImageButton.ImageButtonStyle.class) );
    ibs.imageUp = new TextureRegionDrawable( Textures.getInstance().getTexture(image, false) );
    ImageButton ib = new ImageButton(skin);
    ib.setProgrammaticChangeEvents(false);
    ib.setStyle(ibs);
    return ib;
  }

  private ImageButton getImageButton(String image) {
    return getImageButton(image, "default");
  }

  private void addToWin(Window w, Actor a, int size) {
    w.add(a).width(size).height(size).pad(size/PAD);
  }

  private void addToWin(Window w, Actor a, int size, int colspan) {
    w.add(a).colspan(colspan).width(size).height(size).pad(size/PAD);
  }

  public void reset() {
    table = new Table();
    table.setFillParent(true);
    table.row().fillY();
    table.top();
    table.right();
    //    table.right().top().pad(Param.TILE_S*2);
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(Textures.getInstance().getUIAtlas());

    GameState.getInstance().getUIStage().addActor(table);

    ParticleButton particleButtonHandler = new ParticleButton();
    BuildingButton buildingButtonHandler = new BuildingButton();
    QueueLengthSlider queueLengthSlider = new QueueLengthSlider();
    QueueButton queueButton = new QueueButton();
    YesNoButton yesNoButton = new YesNoButton();

    mainWindow = getWindow("Particle Destructor");
    selectWindow = getWindow("Select");


    // Main window
    for (BuildingType bt : BuildingType.values()) {
      ImageButton ib = getImageButton("building_" + bt.ordinal());
      ib.setUserObject(bt);
      ib.addListener(buildingButtonHandler);
      addToWin(mainWindow, ib, SIZE_L);
      mainWindow.row();
    }

    // Selected window
    selectButton = new EnumMap<Colour, Button>(Colour.class);
    selectLabel = new EnumMap<Colour, Label>(Colour.class);
    for (Colour c : Colour.values()) {
      Image image = getImage("ball_" + c.getString());
      image.setWidth(SIZE_M);
      image.setHeight(SIZE_M);
      Button b = new Button(skin, "default");
      Container<Actor> cont = new Container<Actor>();
      cont.setActor(image);
      cont.width(SIZE_M).height(SIZE_M);

      b.setUserObject(c);
      b.addListener(particleButtonHandler);
      b.align(Align.center);
      b.add(cont);
      selectButton.put(c, b); // Added to window dynamically

      Label lab = getLabel("");;
      selectLabel.put(c, lab);

    }

    // Building windows
    buildingWindow = new EnumMap<BuildingType, Window>(BuildingType.class);
    buildingWindowQSimple = new EnumMap<BuildingType, ImageButton>(BuildingType.class);
    buildingWindowQSpiral = new EnumMap<BuildingType, ImageButton>(BuildingType.class);
    buildingWindowQSize = new EnumMap<BuildingType, Label>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      Window bw = getWindow(bt.getString());
      buildingWindow.put(bt, bw);
      for (int i = 0; i < BuildingType.N_MODES; ++i) {
        if (bt == BuildingType.kMINE) break;
        HorizontalGroup hg = new HorizontalGroup();
        Particle from = bt.getInput(i);
        Pair<Particle,Particle> to = bt.getOutputs(i);
        addToWin(bw, getImage("ball_" + from.getColourFromParticle().getString()), SIZE_S);
        addToWin(bw, getImage("arrow"), SIZE_S );
        if (to.getKey() != null) addToWin(bw, getImage("ball_" + to.getKey().getColourFromParticle().getString()), SIZE_S);
        if (to.getValue() != null) addToWin( bw, getImage("ball_" + to.getValue().getColourFromParticle().getString()), SIZE_S);
        addToWin(bw, getImage("zap"), SIZE_S);
        bw.row();
        addToWin(bw, getLabel(from.getString()), SIZE_S);
        addToWin(bw, getLabel(""), SIZE_S );
        if (to.getKey() != null) addToWin(bw, getLabel(to.getKey().getString()), SIZE_S);
        if (to.getValue() != null) addToWin(bw, getLabel(to.getValue().getString()), SIZE_S);
        addToWin(bw, getLabel( ""+bt.getOutputEnergy(i)), SIZE_S);
        bw.row();
      }
      ImageButton qSimple = getImageButton("queue_simple", "toggle");
      buildingWindowQSimple.put(bt, qSimple);
      qSimple.setUserObject(QueueType.kSIMPLE);
      addToWin(bw, qSimple, SIZE_L, 5);
      bw.row();
      ImageButton qSpiral = getImageButton("queue_spiral", "toggle");
      buildingWindowQSpiral.put(bt, qSpiral);
      qSpiral.setUserObject(QueueType.kSPIRAL);
      addToWin(bw, qSpiral, SIZE_L, 5);
      qSimple.addListener(queueButton);
      qSpiral.addListener(queueButton);
      bw.row();
      ///////////////////////
      Slider slider = new Slider(1, 99, 1, false, skin, "default-horizontal");
      slider.addListener(queueLengthSlider);
      bw.add(slider).height(SIZE_S).width(SIZE_S*4).colspan(4);
      Label sliderLabel = getLabel("");
      buildingWindowQSize.put(bt, sliderLabel);
      addToWin(bw, sliderLabel, SIZE_S);
      bw.row();
      ///////////////////////
      ImageButton yes = getImageButton("tick");
      yes.setUserObject(1);
      yes.addListener(yesNoButton);
      addToWin(bw, yes, SIZE_M, 2);
      ImageButton no = getImageButton("cross");
      no.setUserObject(0);
      no.addListener(yesNoButton);
      addToWin(bw, no, SIZE_M, 3);
    }

    showMain();

  }

  public void showBuildBuilding(BuildingType bt) {
    table.clear();
    if (bt != BuildingType.kMINE) {
      buildingWindowQSimple.get(bt).setChecked(Param.QUEUE_TYPE == QueueType.kSIMPLE);
      buildingWindowQSpiral.get(bt).setChecked(Param.QUEUE_TYPE == QueueType.kSPIRAL);
      buildingWindowQSize.get(bt).setText("" + Param.QUEUE_SIZE);
    }
    table.add(buildingWindow.get(bt));
    table.debugAll();
    uiMode = UIMode.kPLACE_BUILDING;
    buildingBeingPlaced = bt;
  }

  public void doSelect(final Set<Sprite> selected) {
    Set<Colour> selectedColours = new HashSet<Colour>();
    int counter[] = new int[Colour.values().length];
    for (final Sprite s : selected) {
      Colour c = (Colour) s.getUserObject();
      selectedColours.add(c);
      ++counter[c.ordinal()];
    }
    selectWindow.clear();
    for (Colour c : selectedColours) {
      selectLabel.get(c).setText(Particle.getStringFromColour(c) + " (" + counter[c.ordinal()] + ")");
      selectWindow.add(selectLabel.get(c)).width(SIZE_S*2).height(SIZE_S).pad(SIZE_S/PAD);
      selectWindow.row();
      addToWin(selectWindow, selectButton.get(c), SIZE_M);
      selectWindow.row();
    }
    table.clear();
    table.add(selectWindow);
    table.debugAll();
    uiMode = UIMode.kSELECTING;
  }

  public void showMain() {
    table.clear();
    table.add( mainWindow );
    uiMode = UIMode.kNONE;
  }

}
