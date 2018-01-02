package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.UIMode;
import timboe.destructor.input.BuildingButton;
import timboe.destructor.input.ParticleButton;
import timboe.destructor.screen.GameScreen;

public class UI {

  private static UI ourInstance;
  public static UI getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new UI(); }
  public void dispose() { ourInstance = null; }

  public UIMode uiMode = UIMode.kNONE;
  public BuildingType buildingBeingPlaced = null;

  private Table table;
  private Skin skin;

  private Window mainWindow;
  private Window selectWindow;
  private EnumMap<BuildingType, Window> buildingWindow;

  private EnumMap<Colour, ImageTextButton> selectButton = new EnumMap<Colour, ImageTextButton>(Colour.class);

  public Table getTable() {
    return table;
  }

  private UI() {
    reset();
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

    mainWindow = new Window("Particle Destructor", skin, "default");
    mainWindow.setMovable(false);

    selectWindow = new Window("Select", skin, "default");
    selectWindow.setMovable(false);

    table.add(mainWindow);//.height(Gdx.graphics.getHeight());//width(Gdx.graphics.getWidth()/4);

    // Main window
    for (BuildingType bt : BuildingType.values()) {
      ImageButton.ImageButtonStyle s = new ImageButton.ImageButtonStyle( skin.get(ImageButton.ImageButtonStyle.class) );
      s.imageUp = new TextureRegionDrawable( Textures.getInstance().getTexture("building_" + bt.ordinal(), false) );
      ImageButton ib = new ImageButton(skin);
      ib.setUserObject(bt);
      ib.setStyle(s);
      ib.addListener(buildingButtonHandler);
      mainWindow.add(ib).width(128).height(128).pad(16);
      mainWindow.row();
    }

    // Selected window
    for (Colour c : Colour.values()) {
      ImageTextButton.ImageTextButtonStyle s = new ImageTextButton.ImageTextButtonStyle( skin.get(ImageTextButton.ImageTextButtonStyle.class) );
      s.imageUp = new TextureRegionDrawable( Textures.getInstance().getTexture("ball_" + c.getString() + "_0", false) );
      ImageTextButton b = new ImageTextButton(Particle.getStringFromColour(c), skin);
      b.setStyle(s);
      b.setUserObject(c);
      b.addListener(particleButtonHandler);
      selectButton.put(c, b); // Added to window dynamically
    }

    // Building windows
    buildingWindow = new EnumMap<BuildingType, Window>(BuildingType.class);
    for (BuildingType bt : BuildingType.values()) {
      Window bw = new Window(bt.getString(), skin, "default");
      bw.setMovable(false);
      buildingWindow.put(bt, bw);
      for (int i = 0; i < BuildingType.N_MODES; ++i) {
        if (bt == BuildingType.kMINE) break;
        Particle from = bt.getInput(i);
        Pair<Particle,Particle> to = bt.getOutputs(i);
        bw.add( new Image( Textures.getInstance().getTexture("ball_" + from.getColourFromParticle().getString() + "_0", false) ) ).width(32).height(32);
        bw.add( new Image( Textures.getInstance().getTexture("arrow", false) )).width(32).height(32);
        if (to.getKey() != null) bw.add( new Image( Textures.getInstance().getTexture("ball_" + to.getKey().getColourFromParticle().getString() + "_0", false) ) ).width(32).height(32);
        if (to.getValue() != null) bw.add( new Image( Textures.getInstance().getTexture("ball_" + to.getValue().getColourFromParticle().getString() + "_0", false) ) ).width(32).height(32);
        bw.add(new Image( Textures.getInstance().getTexture("zap", false) )).width(32).height(32);
        bw.row();
        bw.add( new Label(from.getString(), skin, "default") ).width(32).height(32);
        bw.add( new Label("", skin, "default") ).width(32).height(32);
        if (to.getKey() != null) bw.add( new Label(to.getKey().getString(), skin, "default") ).width(32).height(32);
        if (to.getValue() != null) bw.add( new Label(to.getValue().getString(), skin, "default") ).width(32).height(32);
        bw.add(new Label("", skin, "default")).width(32).height(32);
        bw.row();
      }

    }


//    final TextButton button = new TextButton("Click me", skin, "default");
//    window.align(Align.bottom);
//    window.add(button).width(128).height(128).pad(16);
//    button.addListener(new ChangeListener(){
//      @Override
//      public void changed(ChangeEvent event, Actor actor){
//        Gdx.app.log("button","clicked");
//        button.setText("You clicked the button");
//        GameState.getInstance().uiMode = UIMode.kPLACE_BUILDING;
//      }
//    });

    table.debugAll();
  }

  public void showBuildBuilding(BuildingType bt) {
    table.clear();
    table.add(buildingWindow.get(bt));
    uiMode = UIMode.kPLACE_BUILDING;
    buildingBeingPlaced = bt;
  }

  public void doSelect(final Set<Sprite> selected) {
    Set<Colour> selectedColours = new HashSet<Colour>();
    for (final Sprite s : selected) {
      selectedColours.add( (Colour) s.getUserObject() );
    }
    selectWindow.clear();
    for (Colour c : selectedColours) {
      selectWindow.add( selectButton.get(c) ).width(128).height(128);
      selectWindow.row();
    }
    table.clear();
    table.add(selectWindow);
    uiMode = UIMode.kSELECTING;
  }

  public void showMain() {
    table.clear();
    table.add( mainWindow );
    uiMode = UIMode.kNONE;
  }

}
