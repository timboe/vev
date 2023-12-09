package co.uk.timmartin.vev;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("VEV");
		config.setHdpiMode(HdpiMode.Logical); //useHDPI = true;
		config.useVsync(true);;
		config.setForegroundFPS(60);
//		config.samples = 2;
//		config.forceExit = false;
		config.setWindowIcon( "ic_launcher_128.png");
//		config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
//		config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
//		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
//		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
  	config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
		config.setResizable(false);
		new Lwjgl3Application(new VEVGame(), config);
	}
}
