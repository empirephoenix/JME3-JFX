package com.jme3x.jfx.injfx;

import java.net.URL;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TestColorRgb extends Application {

	public static void main(String[] args) {
		//Platform.runLater(()-> {
			//jme = new JmeForImageView();
			//jme.enqueue((jmeApp)->{return null;});
		//});
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.getClass().getCanonicalName().replace('.', '/')+".fxml");
		fxmlLoader.setLocation(location);
		//final ResourceBundle defaultRessources = fxmlLoader.getResources();
		//fxmlLoader.setResources(this.addCustomRessources(defaultRessources));
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		final Region root = fxmlLoader.load(location.openStream());
		Controller controller = fxmlLoader.getController();

		
		JmeForImageView jme = new JmeForImageView();
		Platform.runLater(() -> {
			//To work on macosx, jme should be launch later
			jme.bind(controller.image);
			jme.enqueue((jmeApp)->{
				jmeApp.getGuiNode().attachChild(TestColorRgb.makeScene(
						jmeApp.getAssetManager(),
						jmeApp.getContext().getSettings().getWidth(),
						jmeApp.getContext().getSettings().getHeight()
				));
				return true;
			});
		});
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent e){
				jme.stop(true);
			}
		});

		//jme.enqueue(TestColorRgb::createScene);

		Scene scene = new Scene(root, 600, 400);
		stage.setTitle(this.getClass().getSimpleName());
		stage.setScene(scene);
		stage.show();
	}

	public static Node makeScene(AssetManager assetManager, int width, int height) {
		BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
		Node root = new Node("sceneRGB");
		int i = 0;
		for(ColorRGBA color: new ColorRGBA[]{ColorRGBA.Black, ColorRGBA.White, ColorRGBA.Red, ColorRGBA.Green, ColorRGBA.Blue}) {
			i++;
			BitmapText txt = new BitmapText(fnt, false);
			txt.setText(color.toString());
			txt.setBox(new Rectangle(0, 0, width, txt.getHeight()));
			txt.setSize(fnt.getPreferredSize() * 2f);
			txt.setColor(color);
			txt.setLocalTranslation((width - txt.getLineWidth()) / 2, i * txt.getHeight(), 0);
			root.attachChild(txt);
		}
		return root;
	}

	@Override
	public void stop() throws Exception {
		Platform.exit();
	}

	public static class Controller {

		@FXML
		public ImageView image;

		@FXML
		public void initialize() {
			//To resize image when parent is resize
			//image is wrapped into a "VBOX" or "HBOX" to allow resize smaller
			//see http://stackoverflow.com/questions/15951284/javafx-image-resizing
			Pane p = (Pane)image.getParent();
			image.fitHeightProperty().bind(p.heightProperty());
			image.fitWidthProperty().bind(p.widthProperty());

			image.setPreserveRatio(false);
		}

	}
}
