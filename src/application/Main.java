package application;
	
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;


public class Main extends Application{
	
	private static BorderPane 	root = new BorderPane();
	private static Stage 		pStage;
	private static WorkingImage activeImage;
	
	private static Label topBar;
	private static Label statusBar;
		
	
	
	/**
	 * Does nothing but starting the GUI.
	 */
	public static void main(String[] args) {
		launch(args);
	}
	

	
	/**
	 * Start of the GUI.
	 */
	@Override
	public void start(Stage primaryStage) {
		root.setId("root");
		pStage = primaryStage;
		
		primaryStage.setTitle("Watermarker Beta");
		
		topBar = new Label("");
		statusBar = new Label("");
		
		


		
		// Menü und Info-Leisten den entsprechenden Bereichen der BorderPane zuordnen.
		root.setRight(makeMenu());
		root.setTop(topBar);
		root.setBottom(statusBar);

		
		// Neue Scene erzeugen, Stylesheet laden und der Stage zuweisen.
		Scene scene = new Scene(root,1000,700);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
	
		
		// Programm soll nicht ohne Rückfrage beendet werden, falls noch eine ungespeicherte Datei offen ist.
		primaryStage.setOnCloseRequest(e->{
			if (activeImage == null || activeImage.isSaved())		
				System.exit(0);
			else {
				Alert unsaved = new Alert(AlertType.CONFIRMATION);
				unsaved.setTitle("Änderungen nicht gespeicht");
				unsaved.setHeaderText("Die Änderungen an der aktuellen Datei wurden nicht gespeichert.");
				unsaved.setContentText("Wollen Sie das Programm trotzdem Beenden?");
				Optional<ButtonType> result = unsaved.showAndWait();
				if (result.get() == ButtonType.OK)
					System.exit(0);
				else
					e.consume();
			}
		});
		primaryStage.show();
		
	}
	
	
	
	
	/**
	 * Helper method for displaying the loaded/manipulated image.
	 * 
	 * @param file		The image-file.
	 */
	private static void showLoadedImage(File file){
		Image img = null;
		String source = null;
		
		try {
			source = file.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		img = new Image(source);
		
		if (img.getHeight() > root.getHeight()   ||    img.getWidth() > root.getWidth())
			img =  new Image(source,root.getWidth(), root.getHeight(),true, true);
		
		
		root.setBackground(new Background(new BackgroundImage	(img, 
																BackgroundRepeat.NO_REPEAT, 
																BackgroundRepeat.NO_REPEAT, 
																BackgroundPosition.CENTER, 
																BackgroundSize.DEFAULT)));
	}

	
	
	/**
	 * Method for realizing the main menu as a GridPane.
	 * 
	 * @return	The main menu including lambda expressions.
	 */
	
	private static GridPane makeMenu(){
		// Erzeugen der Menüleiste
		GridPane menuPane = new GridPane();
		menuPane.setId("menuPane");
		menuPane.setOpacity(0.2);
		menuPane.setVgap(12);
		menuPane.setHgap(12);
		menuPane.setPadding(new Insets(20,20,20,20));
		menuPane.setAlignment(Pos.CENTER);
				

		// Ein- und Ausblendeffekte der Menüleiste
		menuPane.setOnMouseEntered(	e-> {
			FadeTransition fadeIn = new FadeTransition(Duration.millis(100), menuPane);
			fadeIn.setFromValue(0.0);
			fadeIn.setToValue(0.9);
			fadeIn.play();
		});
		
		menuPane.setOnMouseExited( 	e-> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuPane);
			fadeOut.setFromValue(0.9);
			fadeOut.setToValue(0.0);
			fadeOut.play();
		});
		
		
		
		// Manüeinträge werden durch Labels realisert.
		Label lblOpen 	= new Label("Open");
		Label lblSave 	= new Label("Save");
		Label lblSaveAs	= new Label("Save As");
		Label lblRead 	= new Label("Read");
		Label lblWrite 	= new Label("Write");
		Label lblErase 	= new Label("Delete");
		Label lblClose	= new Label("Exit");

		lblOpen.setId	("menuLabel");
		lblSave.setId	("menuLabel");
		lblSaveAs.setId	("menuLabel");
		lblRead.setId	("menuLabel");
		lblWrite.setId	("menuLabel");
		lblErase.setId	("menuLabel");
		lblClose.setId	("menuLabel");

		
		// Menüeinträge die beim Start nicht verfügbar sein sollen.
		lblSave.setDisable		(true);
		lblSaveAs.setDisable	(true);
		lblRead.setDisable		(true);
		lblWrite.setDisable		(true);
		lblErase.setDisable		(true);
		

		//Events für alle Menüeinträge. Jeweils für MouseEntered, MouseExited und MouseClicked.

		// Menüeintrag Datei Öffnen.
		lblOpen.setOnMouseEntered(		e-> {		lblOpen.setId("menuLabelMouseOver");	});
		lblOpen.setOnMouseExited(		e-> {		lblOpen.setId("menuLabel");				});
		lblOpen.setOnMouseClicked(		e-> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new ExtensionFilter("PNG-Files", "*.png"));
			File file = null;;
			Alert unsaved = new Alert(AlertType.CONFIRMATION);
			unsaved.setTitle("Änderungen nicht gespeicht");
			unsaved.setHeaderText("Die Änderungen an der aktuellen Datei wurden nicht gespeichert.");
			unsaved.setContentText("Wollen Sie die Änderungen verwerfen?");

			// Neue Datei soll nur geöffnet werden, wenn (1) noch keine Datei offen ist, (2) die geöffnete Datei so 
			// auch gespeichert wurde, (3) der Benutzer bestätigt das die Änderungen verworfen werden sollen.
			if (activeImage == null || activeImage.isSaved() || unsaved.showAndWait().get() == ButtonType.OK)		
				file = chooser.showOpenDialog(pStage);
				if (file != null){
					activeImage = new WorkingImage(file);
					if (activeImage.isLoaded()){
						if (activeImage.getEmbeddedWatermark().equals("")){
							lblWrite.setDisable	(false);
							lblRead.setDisable	(true);
							lblErase.setDisable	(true);
						} else {
							lblRead.setDisable	(false);
							lblErase.setDisable	(false);
							lblWrite.setDisable	(true);
						}
						lblSave.setDisable		(true);
						lblSaveAs.setDisable	(false);
						pStage.setTitle(file.toString());
						showLoadedImage(file);
					} else {
						message("FEHLER", "Fehler beim Öffnen der Datei");
					}
				}
			else {
					return;
			}
		
				
		});
				
		
		// Menüeintrag Datei speichern.
		lblSave.setOnMouseEntered( 		e-> {		lblSave.setId("menuLabelMouseOver");	});
		lblSave.setOnMouseExited(		e-> {		lblSave.setId("menuLabel");				});
		lblSave.setOnMouseClicked(		e-> {
			if(activeImage.saveImageFile()){
				statusBar.setText("Datei gespeichert.");
				lblSave.setDisable(true);
			} else
				message("FEHLER", "Datei konnte nicht gespeichert werden");
		});
		
		
		// Menüeintrag Datei speichern unter..
		lblSaveAs.setOnMouseEntered(	e-> {	lblSaveAs.setId("menuLabelMouseOver");		});
		lblSaveAs.setOnMouseExited(		e-> {	lblSaveAs.setId("menuLabel");				});
		lblSaveAs.setOnMouseClicked(	e-> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new ExtensionFilter("PNG-Files", "*.png"));
			File file = chooser.showSaveDialog(pStage);
			if (file != null){
				String preCheck = file.getAbsoluteFile().toString();
				if (!preCheck.toLowerCase().endsWith(".png"))
						file = new File(preCheck + ".png");
				if(activeImage.saveImageFileAs(file)){
					activeImage = new WorkingImage(file);
					pStage.setTitle(file.toString());
					statusBar.setText("Datei gespeichert");
					lblSave.setDisable(true);
				}
			}
		});
				
		
		// Menüeintrag Wasserzeichen auslesen. 
		lblRead.setOnMouseEntered(		e-> {	lblRead.setId("menuLabelMouseOver");		});
		lblRead.setOnMouseExited(		e-> {	lblRead.setId("menuLabel");					});
		lblRead.setOnMouseClicked(		e-> {
			message("Ausgelenes Wasserzeichen", activeImage.getEmbeddedWatermark());
		});
				
		
		// Menüeintrag Wasserzeichen schreiben.
		lblWrite.setOnMouseEntered(		e-> {	lblWrite.setId("menuLabelMouseOver");		});
		lblWrite.setOnMouseExited(		e-> {	lblWrite.setId("menuLabel");				});
		lblWrite.setOnMouseClicked(		e-> {
			TextInputDialog input = new TextInputDialog("Wasserzeichen");
			input.setTitle("Eingabe");
			input.setHeaderText("Bitte Wasserzeichen eingeben.");
			Optional<String> payload = input.showAndWait();
			payload.ifPresent(watermark -> {  
				if(activeImage.writeWatermark(watermark)){
					lblSave.setDisable	(false);
					lblSaveAs.setDisable(false);
					lblRead.setDisable	(false);
					lblErase.setDisable	(false);
					lblWrite.setDisable	(true);
					
					statusBar.setText("Wasserzeichen gesetzt mit " + activeImage.getRedundancy() + " x Redundanz");
					pStage.setTitle(activeImage.getImageFile().toString() + " - >>> unsaved <<<");
					showLoadedImage(activeImage.getImageFile());;
				} else {
					message ("Fehler", "Wasserzeichen konnte nicht geschrieben werden");
				}
				});
		});
				
		
		// Menüeintrag Wasserzeichen entfernen.
		lblErase.setOnMouseEntered(		e-> {	lblErase.setId("menuLabelMouseOver");		});
		lblErase.setOnMouseExited(		e-> {	lblErase.setId("menuLabel");				});
		lblErase.setOnMouseClicked(		e-> {
			if(activeImage.removeEmbeddedWatermark()){
				lblSave.setDisable		(false);
				lblSaveAs.setDisable	(false);
				lblWrite.setDisable		(false);
				lblRead.setDisable		(true);
				lblErase.setDisable		(true);
				
				pStage.setTitle(activeImage.getImageFile().toString() + " - >>> unsaved <<<");
				statusBar.setText("Wasserzeichen entfernt");
				showLoadedImage(activeImage.getImageFile());;
			}
		});

				
		// Menüeintrag Programm beenden.
		lblClose.setOnMouseEntered(		e-> {	lblClose.setId("menuLabelMouseOver");		});
		lblClose.setOnMouseExited(		e-> {	lblClose.setId("menuLabel");				});
		lblClose.setOnMouseClicked(		e-> {
			if (activeImage == null || activeImage.isSaved())		
				System.exit(0);
			else {
				Alert unsaved = new Alert(AlertType.CONFIRMATION);
				unsaved.setTitle("Änderungen nicht gespeicht");
				unsaved.setHeaderText("Die Änderungen an der aktuellen Datei wurden nicht gespeichert.");
				unsaved.setContentText("Wollen Sie das Programm trotzdem Beenden?");
				Optional<ButtonType> result = unsaved.showAndWait();
				if (result.get() == ButtonType.OK)
					System.exit(0);
				else
					return;
			}
				
		});
		
		
		// Menüeinträge zum Menü hinzufügen.
		menuPane.add(lblOpen, 	0, 0);
		menuPane.add(lblSave, 	0, 1);
		menuPane.add(lblSaveAs, 0, 2);
		menuPane.add(lblRead, 	0, 5);
		menuPane.add(lblWrite, 	0, 6);
		menuPane.add(lblErase, 	0, 7);
		menuPane.add(lblClose, 0, 12);

		return menuPane;
	}
	
	
	/**
	 * Helper method for informing the user via dialog.
	 * 
	 * @param head		Message header-text.
	 * @param content	Message content-text.
	 */
	private static void message(String head, String content){
		Alert message = new Alert(AlertType.INFORMATION);
		message.setHeaderText(head);
		message.setContentText(content);
		message.show();
	}
	
}
