package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.destructor.LabelDF;
import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.QueueType;
import timboe.destructor.enums.UIMode;
import timboe.destructor.input.BuildingButton;
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
  private ShaderProgram dfShader;

  private Table mainWindow;
  private Table selectWindow;

  private float displayEnergy;
  private float time;
  public final DecimalFormat formatter = new DecimalFormat("###,###");
  LabelDF displayEnergyLabel;

  private final YesNoButton yesNoButton = new YesNoButton();

  public Button selectParticlesButton;

  private EnumMap<BuildingType, Table> buildingWindow;
  public EnumMap<BuildingType, Button> buildingWindowQSimple;
  public EnumMap<BuildingType, Button> buildingWindowQSpiral;
  public EnumMap<BuildingType, LabelDF> buildingWindowQSize;
  public EnumMap<BuildingType, LabelDF> buildingWindowQPrice;

  private EnumMap<BuildingType, Table> buildingSelectWindow;
  private EnumMap<BuildingType, ProgressBar> buildingSelectProgress;
  public Building selectedBuilding;


  public EnumMap<Colour, Button> selectButton;
  private EnumMap<Colour, Label> selectLabel;
  private Button selectTick;
  private Button selectCross;

  private UI() {
    reset();
  }

  private void separator(Table w, int colspan) {
    w.add(new Image( Textures.getInstance().getTexture("separator", false) )).fillX().height(4).pad(2).colspan(colspan);
    w.row();
  }

  private LabelDF getLabel(String label) {
    LabelDF l = new LabelDF(label, skin, "default", dfShader);
    l.setAlignment(Align.center);
    l.setWidth(SIZE_S);
    l.setHeight(SIZE_S);
    return l;
  }

  private Image getImage(String image) {
    Image i = new Image( Textures.getInstance().getTexture(image, false) );
    i.setAlign(Align.center);
    return i;
  }

  private Table getWindow() {
    Table t = new Table();
    t.setBackground(new NinePatchDrawable(new NinePatch(skin.getRegion("window"), 10, 10, 10, 10)));
    return t;
  }

  private Button getImageButton(String image, String style, int resize) {
    Button ib = new Button(skin, style);
    if (image.equals("tick") || image.equals("cross")) {
      ib.setUserObject( image.equals("cross") ? 0 : 1);
      ib.addListener(yesNoButton);
      resize = SIZE_M;
    }

    Image im = new Image( Textures.getInstance().getTexture(image, false) );
    Container<Actor> cont = new Container<Actor>();
    cont.setActor(im);
    if (resize != 0) cont.width( resize ).height( resize );

    ib.setProgrammaticChangeEvents(false);
    ib.add(cont);
    ib.align(Align.center);
    return ib;
  }

  private Button getImageButton(String image) {
    return getImageButton(image, "default", 0);
  }

  private void addToWin(Table w, Actor a, int size) {
    w.add(a).width(size).height(size).pad(size/PAD);
  }

  private void addToWin(Table w, Actor a, int sizeX, int sizeY, int colspan) {
    w.add(a).colspan(colspan).width(sizeX).height(sizeY).pad(SIZE_S/4);
  }

  private void addBuildingBlurb(Table bw, BuildingType bt) {
    if (bt == BuildingType.kMINE) return;
    for (int i = 0; i < BuildingType.N_MODES; ++i) {
      HorizontalGroup hg = new HorizontalGroup();
      Particle from = bt.getInput(i);
      Pair<Particle, Particle> to = bt.getOutputs(i);
      addToWin(bw, getImage("ball_" + from.getColourFromParticle().getString()), SIZE_S);
      addToWin(bw, getImage("arrow"), SIZE_S);
      addToWin(bw, getImage("zap"), SIZE_S);
      if (to.getKey() != null) {
        addToWin(bw, getImage("ball_" + to.getKey().getColourFromParticle().getString()), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      if (to.getValue() != null) {
        addToWin(bw, getImage("ball_" + to.getValue().getColourFromParticle().getString()), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      addToWin(bw, new Container<Actor>(), SIZE_S);
      bw.row();
      addToWin(bw, getLabel(from.getString()), SIZE_S);
      addToWin(bw, new Container<Actor>(), SIZE_S);
      addToWin(bw, getLabel(formatter.format(bt.getOutputEnergy(i))), SIZE_S);
      if (to.getKey() != null) {
        addToWin(bw, getLabel(to.getKey().getString()), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      if (to.getValue() != null) {
        addToWin(bw, getLabel(to.getValue().getString()), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      addToWin(bw, new Container<Actor>(), SIZE_S);
      bw.row();
    }
  }

  protected void reset() {
    table = new Table();
    table.setFillParent(true);
    table.row().fillY();
    table.top();
    table.right();
    table.pad(Param.TILE_S);
    dfShader = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font.frag"));
    if (!dfShader.isCompiled()) Gdx.app.error("fontShader", "compilation failed:\n" + dfShader.getLog());
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(Textures.getInstance().getUIAtlas());
//    skin.getFont("default-font").getData().setScale(2f);

    GameState.getInstance().getUIStage().addActor(table);

//    ParticleButton particleButtonHandler = new ParticleButton();
    BuildingButton buildingButtonHandler = new BuildingButton();
    QueueLengthSlider queueLengthSlider = new QueueLengthSlider();
    QueueButton queueButton = new QueueButton();

    mainWindow = getWindow();
    selectWindow = getWindow();


    // Main window
    addToWin(mainWindow, getImage("zap"), SIZE_S, SIZE_S, 1);
    displayEnergyLabel = getLabel("");
    addToWin(mainWindow, displayEnergyLabel, SIZE_L, SIZE_S, 1);
    mainWindow.row();
    separator(mainWindow, 2);
    for (BuildingType bt : BuildingType.values()) {
      Image ib = new Image( Textures.getInstance().getTexture("building_" + bt.ordinal(), false) );
      Container<Actor> cont = new Container<Actor>();
      cont.setActor(ib);
      cont.width( ib.getWidth() * 2 ).height( ib.getHeight() * 2 );
      Button b = new Button(skin, "default");
      b.add(cont);
      b.setUserObject(bt);
      b.addListener(buildingButtonHandler);
      addToWin(mainWindow, b, SIZE_L+SIZE_M, SIZE_L, 2);
      mainWindow.row();
    }
    separator(mainWindow, 2);
    selectParticlesButton = getImageButton("select", "toggle", 0);
    addToWin(mainWindow, selectParticlesButton, SIZE_L, SIZE_L, 1);
//    mainWindow.row();
    Button settings = getImageButton("select", "default", 0);
    addToWin(mainWindow, settings, SIZE_L, SIZE_L, 1);

    // Selected window
    selectButton = new EnumMap<Colour, Button>(Colour.class);
    selectLabel = new EnumMap<Colour, Label>(Colour.class);
    selectTick = getImageButton("tick");
    selectCross = getImageButton("cross");
    for (Colour c : Colour.values()) {
      Button b = getImageButton("ball_" + c.getString(), "toggle", SIZE_M);
      Label lab = getLabel("");
      b.row();
      b.add(lab);
      selectButton.put(c, b); // Added to window dynamically
      selectLabel.put(c, lab);
    }

    // Building build windows
    buildingWindow = new EnumMap<BuildingType, Table>(BuildingType.class);
    buildingWindowQSimple = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSpiral = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSize = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowQPrice = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      Table bw = getWindow();
      buildingWindow.put(bt, bw);
      addBuildingBlurb(bw, bt);
      separator(bw, 6);
      /////////////////////
      Button qSimple = getImageButton("queue_simple", "toggle", 0);
      buildingWindowQSimple.put(bt, qSimple);
      qSimple.setUserObject(QueueType.kSIMPLE);
      addToWin(bw, qSimple, SIZE_L, SIZE_L, 3);
      Button qSpiral = getImageButton("queue_spiral", "toggle", 0);
      buildingWindowQSpiral.put(bt, qSpiral);
      qSpiral.setUserObject(QueueType.kSPIRAL);
      addToWin(bw, qSpiral, SIZE_L, SIZE_L, 3);
      qSimple.addListener(queueButton);
      qSpiral.addListener(queueButton);
      bw.row();
      ///////////////////////
      Slider slider = new Slider(1, 99, 1, false, skin, "default-horizontal");
      slider.addListener(queueLengthSlider);
      addToWin(bw, getImage("queue_g_E_N"), SIZE_S, SIZE_S, 1);
      bw.add(slider).height(SIZE_M).width(SIZE_L+SIZE_M+SIZE_S).colspan(4);
      LabelDF sliderLabel = getLabel("");
      buildingWindowQSize.put(bt, sliderLabel);
      addToWin(bw, sliderLabel, SIZE_S);
      bw.row();
      addToWin(bw, getImage("zap"), SIZE_S, SIZE_S, 1);
      LabelDF buildingCost = getLabel( formatter.format(bt.getCost()) );
      buildingWindowQPrice.put(bt, buildingCost);
      addToWin(bw, buildingCost, SIZE_L, SIZE_S, 5);
      bw.row();
      separator(bw, 6);
      ///////////////////////
      addToWin(bw, getImageButton("tick"), SIZE_L, SIZE_L, 3);
      addToWin(bw, getImageButton("cross"), SIZE_L, SIZE_L, 3);
    }

    // Building info windows
    buildingSelectWindow = new EnumMap<BuildingType, Table>(BuildingType.class);
    buildingSelectProgress = new EnumMap<BuildingType, ProgressBar>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      Table bw = getWindow();
      buildingSelectWindow.put(bt, bw);
      addBuildingBlurb(bw, bt);
      separator(bw, 6);
      //////////////////////
      for (int i = 0; i < BuildingType.N_MODES; ++i) {
        Particle p = bt.getOutputs(i).getKey(); // Key and value are always the same
        if (p == null) continue;
        Button b = getImageButton("ball_" + p.getColourFromParticle().getString(), "toggle", SIZE_M);
        Image arrow = getImage("arrow");
        Container<Actor> ac = new Container<Actor>(arrow);
        ac.width(SIZE_M).height(SIZE_M);
        b.add(ac);
        addToWin(bw, b, SIZE_L, SIZE_M, 6);
        bw.row();
      }
      separator(bw, 6);
      //////////////////////
      ProgressBar progressBar = new ProgressBar(0, 100, 1, false, skin, "default-horizontal");
      addToWin(bw, progressBar, SIZE_L+SIZE_L, SIZE_M, 6);
      bw.row();
      addToWin(bw, getImageButton("clock"), SIZE_L, SIZE_L, 6);
      bw.row();
      separator(bw, 6);
      addToWin(bw, getImageButton("wrecking"), SIZE_L, SIZE_L, 3);
      addToWin(bw, getImageButton("cross"), SIZE_L, SIZE_L, 3);
    }

    showMain();

  }

  public void act(float delta) {
    time += delta;
    if (time < Param.FRAME_TIME) return;
    time -= Param.FRAME_TIME;
    if (Math.abs(displayEnergy - Param.PLAYER_ENERGY) > .5f) {
      displayEnergy += (Param.PLAYER_ENERGY - displayEnergy) * 0.05f;
      displayEnergyLabel.setText( formatter.format(Math.round(displayEnergy)) );
    }
  }

  public void showBuildBuilding(BuildingType bt) {
    table.clear();
    if (bt != BuildingType.kMINE) {
      buildingWindowQSimple.get(bt).setChecked(Param.QUEUE_TYPE == QueueType.kSIMPLE);
      buildingWindowQSpiral.get(bt).setChecked(Param.QUEUE_TYPE == QueueType.kSPIRAL);
      buildingWindowQSize.get(bt).setText("" + Param.QUEUE_SIZE);
    }
    table.add(buildingWindow.get(bt));
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kPLACE_BUILDING;
    buildingBeingPlaced = bt;
  }

  public void showBuildingInfo(Building b) {
    table.clear();
    selectedBuilding = b;
    table.add(buildingSelectWindow.get(b.getType()));
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kWITH_BUILDING_SELECTION;
  }

  public void doSelectParticle(final Set<Sprite> selected) {
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
      selectButton.get(c).setChecked(false);
      addToWin(selectWindow, selectButton.get(c), SIZE_L+SIZE_M, SIZE_L, 2);
      selectWindow.row();
    }
    addToWin(selectWindow, selectTick, SIZE_L, SIZE_L, 1);
    addToWin(selectWindow, selectCross, SIZE_L, SIZE_L, 1);
    table.clear();
    table.add(selectWindow);
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kWITH_PARTICLE_SELECTION;
  }

  public void showMain() {
    table.clear();
    table.add( mainWindow );
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kNONE;
  }

}
