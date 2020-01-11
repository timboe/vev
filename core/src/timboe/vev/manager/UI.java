package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.vev.DistanceField.LabelDF;
import timboe.vev.DistanceField.TextButtonDF;
import timboe.vev.DistanceField.TextTooltipDF;
import timboe.vev.Lang;
import timboe.vev.Pair;
import timboe.vev.Param;
import timboe.vev.entity.Building;
import timboe.vev.entity.Sprite;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Particle;
import timboe.vev.enums.QueueType;
import timboe.vev.enums.UIMode;
import timboe.vev.input.BuildingButton;
import timboe.vev.input.ButtonHover;
import timboe.vev.input.DemolishButton;
import timboe.vev.input.ParticleSelectButton;
import timboe.vev.input.QueueButton;
import timboe.vev.input.QueueLengthSlider;
import timboe.vev.input.StandingOrderButton;
import timboe.vev.input.UpgradeBuildingButton;
import timboe.vev.input.YesNoButton;

public class UI {

  private static UI ourInstance = null;
  public static UI getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new UI(); }
  public static boolean constructed() { return ourInstance != null; }
  public void dispose() { ourInstance = null; }
  private static DecimalFormat df2 = new DecimalFormat(".##");


  public UIMode uiMode = UIMode.kNONE;

  public final int SIZE_S = 32;
  public final int SIZE_M = 2*SIZE_S;
  public final int SIZE_L = 2*SIZE_M;

  public final int PAD = 4;

  private Table table;
  public Skin skin;
  public ShaderProgram dfShader;
//  public ShaderProgram dfShader_small;
  public ShaderProgram dfShader_medium;
  public ShaderProgram dfShader_large;

  private Table mainWindow;
  private Table settingsWindow;
  private Table selectWindow;

  private float displayPlayerEnergy = Param.PLAYER_STARTING_ENERGY;
  private float displayInWorldParticles = 0;
  private float displayWarpParticles = Param.PARTICLES_SMALL;
  private float time;
  public final DecimalFormat formatter = new DecimalFormat("###,###");
  private LabelDF displayPlayerEnergyLabel;
  private LabelDF displayPLayerParticleLabel;
  private Set<LabelDF> displayPlayerEnergyLabelSet = new HashSet<LabelDF>();
  private Set<LabelDF> displayPlayerParticleLabelSet = new HashSet<LabelDF>();
  private LabelDF displayWarpParticlesLabel;

  private final YesNoButton yesNoButton = new YesNoButton();
  private final StandingOrderButton standingOrderButton = new StandingOrderButton();
  private final UpgradeBuildingButton upgradeBuildingButton = new UpgradeBuildingButton();
  private final DemolishButton demolishButton = new DemolishButton();
  private final ButtonHover buttonHover = new ButtonHover();

//  public Button selectParticlesButton;

  private EnumMap<BuildingType, Button> buildBuildingButtonsEnabled = null;
  private EnumMap<BuildingType, Button> buildBuildingButtonsDisabled = null;
  private EnumMap<BuildingType, TextTooltipDF> buildBuildingTooltips = null;
  private Table buildingBuildTable = null;

  private EnumMap<BuildingType, Table> buildingWindow;
  public EnumMap<BuildingType, Button> buildingWindowQSimple;
  public EnumMap<BuildingType, Button> buildingWindowQSpiral;
  public EnumMap<BuildingType, LabelDF> buildingWindowQSize;
  public EnumMap<BuildingType, LabelDF> buildingWindowQPrice;
  private EnumMap<BuildingType, Slider> buildingWindowQSlider;
  private EnumMap<BuildingType, LabelDF> buildingWindowTimeLabelA;
  private EnumMap<BuildingType, LabelDF> buildingWindowTimeLabelB;
  private EnumMap<BuildingType, LabelDF> buildingWindowTimeLabelC;
  private EnumMap<BuildingType, Button> buildingWindowUpgradeButton;

  private EnumMap<BuildingType, Table> buildingSelectWindow = null;
  public EnumMap<BuildingType, ProgressBar> buildingSelectProgress = null;
  public EnumMap<BuildingType, EnumMap<Particle, Button>> buildingSelectStandingOrder = null;
  private EnumMap<BuildingType, LabelDF> buildingUpgradeLabelCost = null;
  private EnumMap<BuildingType, LabelDF> buildingUpgradeLabelTime = null;
  private EnumMap<BuildingType, LabelDF> buildingUpgradeLabelBonus = null;
  private EnumMap<BuildingType, TextTooltipDF> buildingUpgradeToolTip = null;

  private EnumMap<Particle, Button> selectButton;
  private EnumMap<Particle, Label> selectLabel;
  private Button selectCross;




  private float perSec = 0; // Do once per second

  private UI() {
    ShaderProgram.pedantic = false;

    dfShader_large = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font_large.frag"));
    if (!dfShader_large.isCompiled()) Gdx.app.error("dfShader_large", "compilation failed:\n" + dfShader_large.getLog());

    dfShader_medium = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font_medium.frag"));
    if (!dfShader_medium.isCompiled()) Gdx.app.error("dfShader_medium", "compilation failed:\n" + dfShader_medium.getLog());

//    dfShader_small = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font_small.frag"));
//    if (!dfShader_small.isCompiled()) Gdx.app.error("dfShader_small", "compilation failed:\n" + dfShader_small.getLog());

    dfShader = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font.frag"));
    if (!dfShader.isCompiled()) Gdx.app.error("fontShader", "compilation failed:\n" + dfShader.getLog());

    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(Textures.getInstance().getUIAtlas());
  }

  private void separator(Table w, int colspan) {
    w.add(new Image( Textures.getInstance().getTexture("separator", false) )).fillX().height(4).pad(6,2,6,2).colspan(colspan);
    w.row();
  }

  public void separator(Table w, int colspan, int width) {
    w.add(new Image( Textures.getInstance().getTexture("separator", false) )).fillX().height(4).pad(6,2,6,2).colspan(colspan).width(width);
    w.row();
  }

  private void TT(Actor a, String tt) {
    if (tt.equals("")) return;
    TextTooltipDF ttDF = new TextTooltipDF(Lang.get(tt), skin);
    ttDF.setInstant(true);
    ttDF.getActor().setAlignment(Align.center);
    a.addListener(ttDF);
  }

  public LabelDF getLabel(String label, String tt) {
    LabelDF l = new LabelDF(label, skin, "default", dfShader);
    l.setAlignment(Align.center);
    l.setWidth(SIZE_S);
    l.setHeight(SIZE_S);
    TT(l,tt);
    return l;
  }

  public Button getTextButton(String label, String tt) {
    TextButtonDF b = new TextButtonDF(label, skin, "default");
    b.getLabelCell().pad(10,30,10,30);
    TT(b,tt);
    b.addListener(buttonHover);
    if (label.equals("<") || label.equals(">") || label.equals(Lang.get("UI_BACK"))) {
      b.setUserObject( label.equals("<") ? 0 : 1);
      if ( label.equals(Lang.get("UI_BACK")) ) {
        b.setUserObject( 2 );
      }
      b.addListener(yesNoButton);
    }
    return b;
  }

  public Image getImage(String image, String tt) {
    Image i = new Image( Textures.getInstance().getTexture(image, false) );
    i.setUserObject(image);
    i.setAlign(Align.center);
    TT(i,tt);
    return i;
  }

  public Table getWindow() {
    Table t = new Table();
    t.setBackground(new NinePatchDrawable(new NinePatch(skin.getRegion("window"), 10, 10, 10, 10)));
    return t;
  }

  public Button getImageButton(String image, String style, int resize, String tt) {
    Button ib = new Button(skin, style);
    ib.addListener(buttonHover);
    if (image.equals("tick") || image.equals("cross")) {
      ib.setUserObject( image.equals("cross") ? 0 : 1);
      ib.addListener(yesNoButton);
      resize = SIZE_L;
    }

    if (image.equals("wrecking")) {
      ib.addListener(demolishButton);
    }

    Image im = new Image( Textures.getInstance().getTexture(image, false) );
    Container<Actor> cont = new Container<Actor>();
    cont.setActor(im);
    if (resize != 0) cont.width( resize ).height( resize );

    ib.setProgrammaticChangeEvents(false);
    ib.add(cont);
    ib.align(Align.center);
    TT(ib,tt);
    return ib;
  }

  public Button getImageButton(String image, String tt) {
    return getImageButton(image, "default", 0, tt);
  }

  public void addToWin(Table w, Actor a, int size) {
    w.add(a).width(size).height(size).pad(PAD);
  }

  public void addToWin(Table w, Actor a, int sizeX, int sizeY, int colspan) {
    w.add(a).colspan(colspan).width(sizeX).height(sizeY).pad(PAD);
  }

  private Button getAndAddStandingOrderButton(BuildingType bt, Particle p) {
    assert p.getColourFromParticle() != null;
    String path = (p == Particle.kBlank ? "queue_full" : "ball_" + p.getColourFromParticle().getString());
    String pName = p.getString();
    Button b = getImageButton(path, "toggle", SIZE_M, (p == Particle.kBlank ? "standingBlank" : "standingOrder#"+pName));
    b.setUserObject(new Pair<BuildingType, Particle>(bt, p));
    b.addListener(standingOrderButton);
    buildingSelectStandingOrder.get(bt).put(p, b);
    Image arrow = getImage("arrow","");
    Container<Actor> ac = new Container<Actor>(arrow);
    ac.width(SIZE_M).height(SIZE_M);
    b.add(ac).padLeft(PAD*2);
    if (p != Particle.kBlank) {
      b.row();
      b.add(getLabel(p.getString(), "")).colspan(2);
    }
    return b;
  }

  private void addBuildingBlurb(Table bw, BuildingType bt) {
    if (bt == BuildingType.kMINE || bt == BuildingType.kWARP) return;
    for (int mode = 0; mode < BuildingType.N_MODES; ++mode) {
      Particle from = bt.getInput(mode);
      Pair<Particle, Particle> to = bt.getOutputs(mode);
      assert from != null;
      assert from.getColourFromParticle() != null;
      addToWin(bw, getImage("ball_" + from.getColourFromParticle().getString(),""), SIZE_S);
      addToWin(bw, getImage("arrow",""), SIZE_S);
      addToWin(bw, getImage("zap","UI_ENERGY"), SIZE_S);
      if (to.getKey() != null) {
        assert to.getKey().getColourFromParticle() != null;
        addToWin(bw, getImage("ball_" + to.getKey().getColourFromParticle().getString(),""), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      if (to.getValue() != null) {
        assert to.getValue().getColourFromParticle() != null;
        addToWin(bw, getImage("ball_" + to.getValue().getColourFromParticle().getString(),""), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      LabelDF disassembleTimeLabel = getLabel("", "disassembleTime");
      addToWin(bw, disassembleTimeLabel, SIZE_S);
      switch(mode) {
        case 0: buildingWindowTimeLabelA.put(bt, disassembleTimeLabel); break;
        case 1: buildingWindowTimeLabelB.put(bt, disassembleTimeLabel); break;
        case 2: buildingWindowTimeLabelC.put(bt, disassembleTimeLabel); break;
      }
      bw.row();
      addToWin(bw, getLabel(from.getString(), ""), SIZE_S);
      addToWin(bw, new Container<Actor>(), SIZE_S);
      addToWin(bw, getLabel(formatter.format(bt.getOutputEnergy(mode)),"disassembleEnergy"), SIZE_S);
      if (to.getKey() != null) {
        addToWin(bw, getLabel(to.getKey().getString(), ""), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      if (to.getValue() != null) {
        addToWin(bw, getLabel(to.getValue().getString(), ""), SIZE_S);
      } else {
        addToWin(bw, new Container<Actor>(), SIZE_S);
      }
      addToWin(bw, new Container<Actor>(), SIZE_S);
      bw.row();
    }
  }

  public void updateButtonPriceStatus() {
    if (buildBuildingButtonsEnabled == null) {
      return;
    }
    buildingBuildTable.clear();
    for (BuildingType bt : BuildingType.values()) {
      if (bt == BuildingType.kWARP) continue;
       final int price = GameState.getInstance().getBuildingPrice(true, bt);
      String s = Lang.get("buildBuilding_A") + bt.getString();
      if (bt != BuildingType.kMINE) {
        s += Lang.get("buildBuilding_B") + bt.getInput(0).getString() + ", "
            + bt.getInput(1).getString() + ", "
            + bt.getInput(2).getString();
      } else {
        s += Lang.get("buildBuilding_B_Mine");
      }
      s += Lang.get("buildBuilding_C") + formatter.format( price ) + " " + Lang.get("UI_ENERGY");
      buildBuildingTooltips.get(bt).getActor().setText(s);
      final boolean disabled = (price > GameState.getInstance().playerEnergy);
      Button b = (disabled ? buildBuildingButtonsDisabled.get(bt) : buildBuildingButtonsEnabled.get(bt));
      addToWin(buildingBuildTable, b, SIZE_L+SIZE_M, SIZE_L, 1);
      buildingBuildTable.row();
    }
  }


  protected void resetGame() {
    Gdx.app.log("resetGame", "made game UI");

    table = new Table();
    table.setFillParent(true);
    table.row().fillY();
    table.top();
    table.right();
    table.pad(Param.TILE_S);

    Stage stage = GameState.getInstance().getUIStage();
    stage.clear();
    stage.addActor(table);

    BuildingButton buildingButtonHandler = new BuildingButton();
    QueueLengthSlider queueLengthSlider = new QueueLengthSlider();
    QueueButton queueButton = new QueueButton();
    ParticleSelectButton particleSelectButton = new ParticleSelectButton();

    mainWindow = getWindow();
    selectWindow = getWindow();

    displayPlayerEnergyLabelSet.clear();
    displayPlayerParticleLabelSet.clear();

    displayPLayerParticleLabel = getLabel("", "particles");
    displayPlayerParticleLabelSet.add(displayPLayerParticleLabel);

    displayPlayerEnergyLabel = getLabel("", "energy");
    displayPlayerEnergyLabelSet.add(displayPlayerEnergyLabel);

    // Main window
    addPlayerEnergy(mainWindow, 2);
    /////////////////////
    buildBuildingButtonsEnabled = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildBuildingButtonsDisabled = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildBuildingTooltips = new EnumMap<BuildingType, TextTooltipDF>(BuildingType.class);
    buildingBuildTable = new Table();
    for (BuildingType bt : BuildingType.values()) {
      if (bt == BuildingType.kWARP) continue;

      Image ibEnabled = new Image( Textures.getInstance().getTexture("building_" + bt.ordinal(), false) );
      Container<Actor> contEnabled = new Container<Actor>();
      contEnabled.setActor(ibEnabled);
      contEnabled.width( ibEnabled.getWidth() * 2 ).height( ibEnabled.getHeight() * 2 );

      Button bEnabled = new Button(skin, "default");
      bEnabled.add(contEnabled);

      Image ibDisabled = new Image( Textures.getInstance().getTexture("building_" + bt.ordinal() + "_gs", false) );
      Container<Actor> contDisabled = new Container<Actor>();
      contDisabled.setActor(ibDisabled);
      contDisabled.width( ibDisabled.getWidth() * 2 ).height( ibDisabled.getHeight() * 2 );

      Button bDisabled = new Button(skin, "default");
      bDisabled.add(contDisabled);

      if (bt != BuildingType.kMINE) {
        Table vertEnabled = new Table();
        Table vertDisabled = new Table();
        for (int mode = 0; mode < BuildingType.N_MODES; ++mode) {
          Image ipEnabled = new Image(Textures.getInstance().getTexture("ball_" + bt.getInput(mode).getColourFromParticle().getString(), false));
          Container<Actor> contIpEnabled = new Container<Actor>();
          contIpEnabled.setActor(ipEnabled);
          contIpEnabled.width(ipEnabled.getWidth() * 2).height(ipEnabled.getHeight() * 2);
          vertEnabled.add(contIpEnabled);
          vertEnabled.row();

          Image ipDisabled = new Image(Textures.getInstance().getTexture("ball_grey", false));
          Container<Actor> contIpDisabled = new Container<Actor>();
          contIpDisabled.setActor(ipDisabled);
          contIpDisabled.width(ipDisabled.getWidth() * 2).height(ipDisabled.getHeight() * 2);
          vertDisabled.add(contIpDisabled);
          vertDisabled.row();
        }
        bEnabled.add(vertEnabled).padLeft(SIZE_S / 2);
        bDisabled.add(vertDisabled).padLeft(SIZE_S / 2);
      }

      bEnabled.setUserObject(bt);
      bEnabled.addListener(buildingButtonHandler);
      TextTooltipDF ttt = new TextTooltipDF("L", skin);
      ttt.getActor().setAlignment(Align.center);
      ttt.setInstant(true);
      bEnabled.addListener( ttt );
      buildBuildingButtonsEnabled.put(bt, bEnabled);
      buildBuildingTooltips.put(bt, ttt);

      bDisabled.addListener( ttt );
      bDisabled.setDisabled(true);
      buildBuildingButtonsDisabled.put(bt, bDisabled);
    }
    mainWindow.add(buildingBuildTable).colspan(2);
    mainWindow.row();
    separator(mainWindow, 2);

    mainWindow.row();
    Button settings = getImageButton("settings", "default", SIZE_L, "pause");
    settings.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        UI.getInstance().showSettings();
      }
    });
    addToWin(mainWindow, settings, SIZE_L + SIZE_M, SIZE_L, 2);

    // Selected window
    selectButton = new EnumMap<Particle, Button>(Particle.class);
    selectLabel = new EnumMap<Particle, Label>(Particle.class);
    selectCross = getImageButton("cross", "");
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      assert p.getColourFromParticle() != null;
      String pColString = p.getColourFromParticle().getString();
      Button b = getImageButton("ball_" + pColString, "default", SIZE_M, "particleSelect#"+ p.getString());
      b.setUserObject(p);
      b.addListener(particleSelectButton);
      Label lab = getLabel("", "");
      b.row();
      b.add(lab);
      selectButton.put(p, b); // Added to window dynamically
      selectLabel.put(p, lab);
    }

    // Building build windows
    buildingWindow = new EnumMap<BuildingType, Table>(BuildingType.class);
    buildingWindowQSimple = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSpiral = new EnumMap<BuildingType, Button>(BuildingType.class);
    buildingWindowQSize = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowQPrice = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowQSlider = new EnumMap<BuildingType, Slider>(BuildingType.class);
    buildingWindowTimeLabelA = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowTimeLabelB = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowTimeLabelC = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingWindowUpgradeButton = new EnumMap<BuildingType, Button>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      Table bw = getWindow();
      buildingWindow.put(bt, bw);
      addPlayerEnergy(bw, bt != BuildingType.kMINE ? 6 : 2);
      /////////////////////
      addBuildingBlurb(bw, bt);
      int colspan = 2;
      /////////////////////
      if (bt != BuildingType.kMINE) {
        colspan = 6;
        separator(bw, colspan);
        Button qSimple = getImageButton("queue_simple", "toggle", 0, "qSimple");
        buildingWindowQSimple.put(bt, qSimple);
        qSimple.setUserObject(QueueType.kSIMPLE);
        addToWin(bw, qSimple, SIZE_L, SIZE_L, 3);
        Button qSpiral = getImageButton("queue_spiral", "toggle", 0, "qSpiral");
        buildingWindowQSpiral.put(bt, qSpiral);
        qSpiral.setUserObject(QueueType.kSPIRAL);
        addToWin(bw, qSpiral, SIZE_L, SIZE_L, 3);
        qSimple.addListener(queueButton);
        qSpiral.addListener(queueButton);
        bw.row();
        ///////////////////////
        Slider slider = new Slider(1, 99, 1, false, skin, "default-horizontal");
        slider.addListener(queueLengthSlider);
        buildingWindowQSlider.put(bt, slider);
        addToWin(bw, getImage("queue_g_E_N",""), SIZE_S, SIZE_S, 1);
        bw.add(slider).height(SIZE_M).width(SIZE_L + SIZE_M + SIZE_S).colspan(4);
        LabelDF sliderLabel = getLabel("", "qSize");
        buildingWindowQSize.put(bt, sliderLabel);
        addToWin(bw, sliderLabel, SIZE_S);
        bw.row();
      }
      addToWin(bw, getImage("zap","UI_ENERGY"), SIZE_S, SIZE_S, 1);
      LabelDF buildingCost = getLabel( "", "buildingPrice" );
      buildingWindowQPrice.put(bt, buildingCost);
      addToWin(bw, buildingCost, SIZE_L, SIZE_S, colspan-1);
      bw.row();
      ///////////////////////
      // TODO android mode
//      separator(bw, colspan);
//      addToWin(bw, getImageButton("tick", ""), SIZE_L, SIZE_L, colspan/2);
//      addToWin(bw, getImageButton("cross", ""), SIZE_L, SIZE_L, colspan/2);
    }

    // Building info windows
    buildingSelectWindow = new EnumMap<BuildingType, Table>(BuildingType.class);
    buildingSelectProgress = new EnumMap<BuildingType, ProgressBar>(BuildingType.class);
    buildingSelectStandingOrder = new EnumMap<BuildingType, EnumMap<Particle, Button>>(BuildingType.class);
    buildingUpgradeLabelCost = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingUpgradeLabelTime = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingUpgradeLabelBonus = new EnumMap<BuildingType, LabelDF>(BuildingType.class);
    buildingUpgradeToolTip = new EnumMap<BuildingType, TextTooltipDF>(BuildingType.class);
    for (final BuildingType bt : BuildingType.values()) {
      buildingSelectStandingOrder.put(bt, new EnumMap<Particle, Button>(Particle.class));
      Table bw = getWindow();
      buildingSelectWindow.put(bt, bw);
      addPlayerEnergy(bw, 6);
      //////////////////////
      if (bt == BuildingType.kWARP) {
        addToWin(bw, getImage("ball_grey", "UI_PARTICLES"), SIZE_S, SIZE_S, 3);
        displayWarpParticlesLabel = getLabel("", "wParticles");
        addToWin(bw, displayWarpParticlesLabel, SIZE_L+SIZE_M-SIZE_S, SIZE_S, 3);
        bw.row();
        for (Particle p : Particle.values()) {
          if (p == Particle.kBlank) continue;
          Button b = getAndAddStandingOrderButton(bt, p);
          addToWin(bw, b, SIZE_L+SIZE_M, SIZE_L, 6);
          bw.row();
        }
      } else {
        if (bt != BuildingType.kMINE) {
          addBuildingBlurb(bw, bt);
          separator(bw, 6);
          for (int i = 0; i < BuildingType.N_MODES; ++i) {
            Particle p = bt.getOutputs(i).getKey(); // Key and value are always the same
            if (p == null) continue;
            Button b = getAndAddStandingOrderButton(bt, p);
            addToWin(bw, b, SIZE_L + SIZE_M, SIZE_L, 6);
            bw.row();
          }
          Button bBlank = getAndAddStandingOrderButton(bt, Particle.kBlank);
          addToWin(bw, bBlank, SIZE_L + SIZE_M, SIZE_M + SIZE_S, 6);
          bw.row();
//          separator(bw, 6);
        }
        //////////////////////
        ProgressBar progressBar = new ProgressBar(0, 1, 0.01f, false, skin, "default-horizontal");
        addToWin(bw, progressBar, SIZE_L+SIZE_L, SIZE_M, 6);
        buildingSelectProgress.put(bt, progressBar);
        bw.row();
//        separator(bw, 6);
        //////////////////////////////
        TextTooltipDF ttt = new TextTooltipDF("", skin);
        ttt.setInstant(true);
        ttt.getActor().setAlignment(Align.center);
        buildingUpgradeToolTip.put(bt, ttt);
        Button ib = getImageButton("clock", "toggle", SIZE_M, "");
        ib.addListener( ttt );
        ib.addListener(upgradeBuildingButton);
        addToWin(ib, getImage("zap", ""), SIZE_S, SIZE_S, 1);
        LabelDF cost = getLabel("10000", "");
        buildingUpgradeLabelCost.put(bt, cost);
        ib.add( cost );
        ib.row();
        LabelDF bonus = getLabel("x1.00", "");
        buildingUpgradeLabelBonus.put(bt, bonus);
        ib.add( bonus );
        LabelDF time = getLabel("10 s", "");
        buildingUpgradeLabelTime.put(bt, time);
        ib.add( time ).colspan(2);
        addToWin(bw, ib, 2*SIZE_L, SIZE_L, 6);
        buildingWindowUpgradeButton.put(bt, ib);
      }
      bw.row();
      //TODO android
//      separator(bw, 6, Param.UI_WIDTH_GAME);
//      addToWin(bw, getImageButton("tick", ""), SIZE_L, SIZE_L, 3);
//      addToWin(bw, getImageButton("cross", ""), SIZE_L, SIZE_L, 3);
      if (bt != BuildingType.kWARP) {
        String percent = Float.toString(Param.BUILDING_REFUND_AMOUND * 100f);
        addToWin(bw, getImageButton("wrecking", "default", SIZE_M, "wrecking#" + percent), SIZE_L + SIZE_M, SIZE_M + SIZE_S, 6);
      }
    }


    // Settings window
    settingsWindow = getWindow();
    addPlayerEnergy(settingsWindow, 2);
    //
    Button saveAndQuit = getTextButton(Lang.get("UI_SAVE_AND_QUIT"),"saveAndQuit");
    saveAndQuit.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        GameState.getInstance().transitionToTitleScreen();
      }
    });
    addToWin(settingsWindow, saveAndQuit, SIZE_L*3, SIZE_L+SIZE_M, 2);
    settingsWindow.row();
    separator(settingsWindow, 2);
    //
    Button resume = getTextButton(Lang.get("UI_RESUME"),"resume");
    resume.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        showMain();
      }
    });
    addToWin(settingsWindow, resume, SIZE_L*3, SIZE_L, 2);
    settingsWindow.row();
    //

    for (LabelDF l : displayPlayerEnergyLabelSet) {
      l.setText(formatter.format(Math.round(displayPlayerEnergy)));
    }
    for (LabelDF l : displayPlayerParticleLabelSet) {
      l.setText(formatter.format(Math.round(displayInWorldParticles)));
    }

    updateButtonPriceStatus();
    showMain();

  }

  private void addPlayerEnergy(Table w, int colspan) {
    addToWin(w, getImage("zap", "UI_ENERGY"), SIZE_S, SIZE_S, colspan/2); // Player Energy
    LabelDF energy = getLabel("", "energy"); // Player Energy
    displayPlayerEnergyLabelSet.add(energy); // Player Energy
    addToWin(w, energy, SIZE_L, SIZE_S, colspan/2); // Player energy
    w.row(); // Player energy
    LabelDF particles = getLabel("", "particles"); // Player Energy
    displayPlayerParticleLabelSet.add(particles);
    addToWin(w, getImage("ball_grey", "UI_PARTICLES"), SIZE_S, SIZE_S, colspan/2); // Player Energy
    addToWin(w, particles, SIZE_L, SIZE_S, colspan/2); // PLayer Energy
    w.row(); // Player Energy
    separator(w, colspan); // Player energy
  }

  public void act(float delta) {
    time += delta;

    perSec += delta;
    if (perSec > 1) {
      perSec -= 1f;
      updateButtonPriceStatus(); // Re-enables purchase buttons if have enough money
      refreshBuildingLabels(); // Re-enabled upgrade button if have enough money
    }

    if (time < Param.FRAME_TIME) return;
    time -= Param.FRAME_TIME;
    // Check for existance of main UI component
    if (Math.abs(displayPlayerEnergy - GameState.getInstance().playerEnergy) > .5f) {
      displayPlayerEnergy += (GameState.getInstance().playerEnergy - displayPlayerEnergy) * 0.05f;
      for (LabelDF l : displayPlayerEnergyLabelSet) {
        l.setText(formatter.format(Math.round(displayPlayerEnergy)));
      }
    }
    if (Math.abs(displayInWorldParticles - GameState.getInstance().inWorldParticles) > .5f) {
      displayInWorldParticles += (GameState.getInstance().inWorldParticles - displayInWorldParticles) * 0.05f;
      for (LabelDF l : displayPlayerParticleLabelSet) {
        l.setText(formatter.format(Math.round(displayInWorldParticles)));
      }
    }
    if (displayWarpParticlesLabel != null) {
      if (Math.abs(displayWarpParticles - GameState.getInstance().warpParticles) > .5f) {
        displayWarpParticles += (GameState.getInstance().warpParticles - displayWarpParticles) * 0.05f;
        displayWarpParticlesLabel.setText(formatter.format(Math.round(displayWarpParticles)));
      }
    }
  }

  public void showBuildBuilding(BuildingType bt) {
    table.clear();
    GameState.getInstance().buildingBeingPlaced = bt;
    if (bt != BuildingType.kMINE) {
      buildingWindowQSimple.get(bt).setChecked(GameState.getInstance().queueType == QueueType.kSIMPLE);
      buildingWindowQSpiral.get(bt).setChecked(GameState.getInstance().queueType == QueueType.kSPIRAL);
      buildingWindowQSlider.get(bt).setValue(GameState.getInstance().queueSize);
    }
    if (bt != BuildingType.kWARP) {
      while (GameState.getInstance().getBuildingPrice(false, bt) > GameState.getInstance().playerEnergy) {
        GameState.getInstance().queueSize -= 1;
      }
      buildingWindowQPrice.get(bt).setText( formatter.format( GameState.getInstance().getBuildingPrice(false, bt) ) );
    }
    if (bt != BuildingType.kMINE && bt != BuildingType.kWARP) {
      buildingWindowTimeLabelA.get(bt).setText("");
      buildingWindowTimeLabelB.get(bt).setText("");
      buildingWindowTimeLabelC.get(bt).setText("");
    }
    table.add(buildingWindow.get(bt));
    if (GameState.getInstance().debug > 0) table.debugAll();
    uiMode = UIMode.kPLACE_BUILDING;
    GameState.getInstance().doingPlacement = true;
  }

  public void showBuildingInfo(Building b) {
    table.clear();
    GameState.getInstance().selectedBuilding = b.id;
    table.add(buildingSelectWindow.get(b.getType()));
    for (Particle p : Particle.values()) {
      if (!buildingSelectStandingOrder.get(b.getType()).containsKey(p)) continue;
      buildingSelectStandingOrder.get(b.getType()).get(p).setChecked(false);
    }
    if (GameState.getInstance().debug > 0) table.debugAll();
    uiMode = UIMode.kWITH_BUILDING_SELECTION;
    refreshBuildingLabels();
  }

  public void showSettings() {
    table.clear();
    table.add(settingsWindow);
    uiMode = UIMode.kSETTINGS;
  }

  public void refreshBuildingLabels() {
    final int bSel = GameState.getInstance().selectedBuilding;
    if (bSel == 0) return;
    Building b = GameState.getInstance().getBuildingMap().get( bSel );
    if (b == null) return; // True if b is a Warp (stored in getWarpMap)
    if (b.getType() != BuildingType.kMINE) {
      buildingWindowTimeLabelA.get(b.getType()).setText(Math.round(b.getDisassembleTime(0)) + "s");
      buildingWindowTimeLabelB.get(b.getType()).setText(Math.round(b.getDisassembleTime(1)) + "s");
      buildingWindowTimeLabelC.get(b.getType()).setText(Math.round(b.getDisassembleTime(2)) + "s");
    }
    final int cost = b.getUpgradeCost();
    Button upgradeButton = buildingWindowUpgradeButton.get(b.getType());
    upgradeButton.setChecked( b.doUpgrade );
    float progress = 0;
    if (b.doUpgrade) progress = b.timeUpgrade / b.getUpgradeTime();
    else if (b.spriteProcessing != 0)  progress = b.timeDisassemble / b.getTimeDisassembleMax;
    buildingSelectProgress.get(b.getType()).setValue(progress);
    LabelDF bonus = buildingUpgradeLabelBonus.get(b.getType());
    LabelDF costLabel = buildingUpgradeLabelCost.get(b.getType());
    LabelDF upgradeTime = buildingUpgradeLabelTime.get(b.getType());
    String usb = "x" + df2.format(1f/b.getAverageNextUpgradeFactor());
    bonus.setText( usb );
    String ugc = String.valueOf(b.getUpgradeCost());
    costLabel.setText( ugc );
    String ugt = df2.format(b.getUpgradeTime()) + " s";
    upgradeTime.setText( ugt );
    com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle style = skin.get(com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);
    final boolean canAfford = (cost <= GameState.getInstance().playerEnergy);
    if (!canAfford) {
      upgradeButton.setDisabled(true);
      bonus.getStyle().fontColor = style.disabledFontColor;
      costLabel.getStyle().fontColor = style.disabledFontColor;
      upgradeTime.getStyle().fontColor = style.disabledFontColor;
    } else {
      upgradeButton.setDisabled(false);
      bonus.getStyle().fontColor = style.fontColor;
      costLabel.getStyle().fontColor = style.fontColor;
      upgradeTime.getStyle().fontColor = style.fontColor;
    }
    TextTooltipDF ttt = buildingUpgradeToolTip.get(b.getType());
    String s = Lang.get("upgradeBuilding_A")
        + Lang.get("upgradeBuilding_B") + ugc + " " + Lang.get("UI_ENERGY")
        + Lang.get("upgradeBuilding_C") + ugt;
    if (b.getType() == BuildingType.kMINE) {
      s += Lang.get("upgradeBuilding_D_Mine") + usb;
    } else {
      s += Lang.get("upgradeBuilding_D") + usb;
    }
    ttt.getActor().setText(s);
  }

  public void doSelectParticle(final Set<Integer> selected) {
    Set<Particle> selectedParticles = new HashSet<Particle>();
    int counter[] = new int[Particle.values().length];
    for (final int sID : selected) {
      Sprite s = GameState.getInstance().getParticleMap().get(sID);
      selectedParticles.add(s.getParticle());
      ++counter[s.getParticle().ordinal()];
    }
    selectWindow.clear();
    /////////////////////
    addToWin(selectWindow, getImage("zap", "UI_ENERGY"), SIZE_S, SIZE_S, 1); // Player Energy
    addToWin(selectWindow, displayPlayerEnergyLabel, SIZE_L, SIZE_S, 1); // Player energy
    selectWindow.row(); // Player energy
    addToWin(selectWindow, getImage("ball_grey", "UI_PARTICLES"), SIZE_S, SIZE_S, 1); // Player energy
    addToWin(selectWindow, displayPLayerParticleLabel, SIZE_L, SIZE_S, 1); // Player energy
    selectWindow.row(); // Player energy
    separator(selectWindow, 2); // Player energy
    selectWindow.row(); // Player energy
    /////////////////////
    for (Particle p : selectedParticles) {
      selectLabel.get(p).setText(p.getString() + " (" + counter[p.ordinal()] + ")");
      selectButton.get(p).setChecked(false);
      addToWin(selectWindow, selectButton.get(p), SIZE_L+SIZE_M, SIZE_L, 2);
      selectWindow.row();
    }
    // TODO android
//    addToWin(selectWindow, selectCross, SIZE_L+SIZE_M, SIZE_L, 2);
    table.clear();
    table.add(selectWindow);
    if (GameState.getInstance().debug > 0) table.debugAll();
    uiMode = UIMode.kWITH_PARTICLE_SELECTION;
    Sounds.getInstance().selectOrder();
  }

  public void showMain() {
    Gdx.app.log("showMain", "Show main In Game UI");
    table.clear();
    table.add(mainWindow);
    if (GameState.getInstance().debug > 0) table.debugAll();
    uiMode = UIMode.kNONE;
    GameState.getInstance().doingPlacement = false;
    if (GameState.getInstance().selectedBuilding != 0) {
      GameState.getInstance().getSelectedBuilding().cancelUpdatePathingList();
    }
    GameState.getInstance().selectedBuilding = 0;
    GameState.getInstance().selectedBuildingStandingOrderParticle = null;
    GameState.getInstance().buildingBeingPlaced = null;
  }

}
