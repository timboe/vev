package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.destructor.LabelDF;
import timboe.destructor.Pair;
import timboe.destructor.Param;
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
  DecimalFormat formatter = new DecimalFormat("###,###");
  LabelDF displayEnergyLabel;

  private YesNoButton yesNoButton = new YesNoButton();

  private EnumMap<BuildingType, Table> buildingWindow;
  public EnumMap<BuildingType, Button> buildingWindowQSimple;
  public EnumMap<BuildingType, Button> buildingWindowQSpiral;
  public EnumMap<BuildingType, Label> buildingWindowQSize;

  public EnumMap<Colour, Button> selectButton;
  private EnumMap<Colour, Label> selectLabel;
  private Button selectTick;
  private Button selectCross;

  private UI() {
    reset();
  }

  private void separator(Table w, int colspan) {
    w.add(new Image( Textures.getInstance().getTexture("separator", false) )).width( (int)( w.getPrefWidth() * 0.5) ).height(4).pad(2).colspan(colspan);
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
//    i.setWidth(SIZE_S);
//    i.setHeight(SIZE_S);
    i.setAlign(Align.center);
    return i;
  }

  private Table getWindow() {
    Table t = new Table();
    t.setBackground(new NinePatchDrawable(new NinePatch(skin.getRegion("window"), 10, 10, 10, 10)));
//    Window w = new Window("", skin, "default");
//    w.
//    w.getTitleLabel().setAlignment(Align.center);
//    w.setMovable(false);
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

  public void reset() {
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
      addToWin(mainWindow, b, SIZE_L, SIZE_L, 2);
      mainWindow.row();
    }
    separator(mainWindow, 2);
    Button select = getImageButton("select", "default", 0);
    addToWin(mainWindow, select, SIZE_L, SIZE_M, 2);
    mainWindow.row();
    Button settings = getImageButton("select", "default", 0);
    addToWin(mainWindow, settings, SIZE_L, SIZE_M, 2);

    // Selected window
    selectButton = new EnumMap<Colour, Button>(Colour.class);
    selectLabel = new EnumMap<Colour, Label>(Colour.class);
    selectTick = getImageButton("tick");
    selectCross = getImageButton("cross");
    for (Colour c : Colour.values()) {
      Button b = getImageButton("ball_" + c.getString(), "toggle", SIZE_M);
      selectButton.put(c, b); // Added to window dynamically
      Label lab = getLabel("");;
      selectLabel.put(c, lab);
    }

    // Building windows
    buildingWindow = new EnumMap<BuildingType, Table>(BuildingType.class);
    buildingWindowQSimple = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSpiral = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSize = new EnumMap<BuildingType, Label>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      Table bw = getWindow();
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
      separator(bw, 6);
      ///////////////////////
      Slider slider = new Slider(1, 99, 1, false, skin, "default-horizontal");
      slider.addListener(queueLengthSlider);
      bw.add(slider).height(SIZE_M).width(SIZE_L+SIZE_M+SIZE_S).colspan(5);
      Label sliderLabel = getLabel("");
      buildingWindowQSize.put(bt, sliderLabel);
      addToWin(bw, sliderLabel, SIZE_S);
      bw.row();
      separator(bw, 6);
      ///////////////////////
      addToWin(bw, getImageButton("tick"), SIZE_L, SIZE_M, 3);
      addToWin(bw, getImageButton("cross"), SIZE_L, SIZE_M, 3);
    }

    showMain();

  }

  public void act(float delta) {
    time += delta;
    if (time < Param.FRAME_TIME) return;
    time -= Param.FRAME_TIME;
    if (Math.abs(displayEnergy - Param.PLAYER_ENERGY) > 1f) {
      displayEnergy += (Param.PLAYER_ENERGY - displayEnergy) * 0.05f;
      displayEnergyLabel.setText( formatter.format(displayEnergy) );
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
      addToWin(selectWindow, selectButton.get(c), SIZE_L, SIZE_M, 2);
      selectWindow.row();
      addToWin(selectWindow, selectLabel.get(c), SIZE_L, SIZE_S, 2);
      selectWindow.row();
      separator(selectWindow, 2);
    }
    addToWin(selectWindow, selectTick, SIZE_L, SIZE_M, 1);
    addToWin(selectWindow, selectCross, SIZE_L, SIZE_M, 1);
    table.clear();
    table.add(selectWindow);
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kSELECTING;
  }

  public void showMain() {
    table.clear();
    table.add( mainWindow );
    if (Param.DEBUG > 0) table.debugAll();
    uiMode = UIMode.kNONE;
  }

}
