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
	private static final int	sceneWidth			= 1000;
	private static final int	sceneHeight			= 700;
	
	private static final double	menuInitialOpacity 	= 0.2;
	private static final double	menuMinOpacity 		= 0.0;
	private static final double	menuMaxOpacity 		= 0.9;
	private static final int	menuElementGaps		= 12;
	private static final Insets menuPadding			= new Insets(20,20,20,20);
	
	
	
	private static BorderPane 		rootPane;
	private static Stage 			stage;
	private static WorkingImage 	activeImage;
	
	private static Label 			topBar;
	private static Label 			statusBar;
		
	
	
	/**
	 * Does nothing but start the GUI.
	 */
	public static void main(String[] args) {
		launch(args);
	}
	

	
	/**
	 * Start of the GUI.
	 */
	@Override
	public void start(Stage primaryStage) {
		rootPane = new BorderPane();
		rootPane.setId("root");
		
		stage = primaryStage;
		
		primaryStage.setTitle("Watermarker Beta");
		
		topBar 		= new Label("");
		statusBar 	= new Label("");
		
		


		
		// Menü und Info-Leisten den entsprechenden Bereichen der BorderPane zuordnen.
		rootPane.setRight(makeMenu());
		rootPane.setTop(topBar);
		rootPane.setBottom(statusBar);

		
		// Neue Scene erzeugen, Stylesheet laden und der Stage zuweisen.
		Scene scene = new Scene(rootPane,sceneWidth,sceneHeight);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
	
		
		// Programm soll nicht ohne Rückfrage beendet werden, falls noch eine ungespeicherte Datei offen ist.
		primaryStage.setOnCloseRequest(e->{
			if (activeImage == null || activeImage.getStatusImageSaved())		
				System.exit(0);
			else {
				Alert unsaved = new Alert(AlertType.CONFIRMATION);
				unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle"));
				unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader"));
				unsaved.setContentText(Messages.getString("Main.closeAnyway"));
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
		Image img 		= null;
		String source 	= null;
		
		try {
			source = file.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		img = new Image(source);
		
		if (img.getHeight() > rootPane.getHeight()   ||    img.getWidth() > rootPane.getWidth())
			img =  new Image(source,rootPane.getWidth(), rootPane.getHeight(),true, true);
		
		
		rootPane.setBackground(new Background(new BackgroundImage	(img, 
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
		menuPane.setOpacity(menuInitialOpacity);
		menuPane.setVgap(menuElementGaps);
		menuPane.setHgap(menuElementGaps);
		menuPane.setPadding(menuPadding);
		menuPane.setAlignment(Pos.CENTER);
				

		// Ein- und Ausblendeffekte der Menüleiste
		menuPane.setOnMouseEntered(	e-> {
			FadeTransition fadeIn = new FadeTransition(Duration.millis(100), menuPane);
			fadeIn.setFromValue(menuMinOpacity);
			fadeIn.setToValue(menuMaxOpacity);
			fadeIn.play();
		});
		
		menuPane.setOnMouseExited( 	e-> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuPane);
			fadeOut.setFromValue(menuMaxOpacity);
			fadeOut.setToValue(menuMinOpacity);
			fadeOut.play();
		});
		
		
		
		// Manüeinträge werden durch Labels realisert.
		Label lblOpen 	= new Label(Messages.getString("Main.MenuOpen"));
		Label lblSave 	= new Label(Messages.getString("Main.MenuSave"));
		Label lblSaveAs	= new Label(Messages.getString("Main.MenuSaveAs"));
		Label lblRead 	= new Label(Messages.getString("Main.MenuRead"));
		Label lblWrite 	= new Label(Messages.getString("Main.MenuWrite"));
		Label lblErase 	= new Label(Messages.getString("Main.MenuDelete"));
		Label lblClose	= new Label(Messages.getString("Main.MenuExit"));

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
			unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle")); 
			unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader")); 
			unsaved.setContentText(Messages.getString("Main.openAnyway")); 

			// Neue Datei soll nur geöffnet werden, wenn (1) noch keine Datei offen ist, (2) die geöffnete Datei so 
			// auch gespeichert wurde, (3) der Benutzer bestätigt das die Änderungen verworfen werden sollen.
			if (activeImage == null || activeImage.getStatusImageSaved() || unsaved.showAndWait().get() == ButtonType.OK)		
				file = chooser.showOpenDialog(stage);
				if (file != null){
					activeImage = new WorkingImage(file);
					if (activeImage.getStatusImageLoaded()){
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
						stage.setTitle(file.toString());
						showLoadedImage(file);
					} else {
						showInformationDialog(Messages.getString("Main.error"), Messages.getString("Main.fileOpenError"));
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
				statusBar.setText(Messages.getString("Main.fileSaved"));
				lblSave.setDisable(true);
			} else
				showInformationDialog(Messages.getString("Main.error"), Messages.getString("Main.errorFileNotSaved"));
		});
		
		
		// Menüeintrag Datei speichern unter..
		lblSaveAs.setOnMouseEntered(	e-> {	lblSaveAs.setId("menuLabelMouseOver");		});
		lblSaveAs.setOnMouseExited(		e-> {	lblSaveAs.setId("menuLabel");				});
		lblSaveAs.setOnMouseClicked(	e-> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new ExtensionFilter("PNG-Files", "*.png"));
			File file = chooser.showSaveDialog(stage);
			if (file != null){
				String preCheck = file.getAbsoluteFile().toString();
				if (!preCheck.toLowerCase().endsWith(".png"))
						file = new File(preCheck + ".png");
				if(activeImage.saveImageFileAs(file)){
					activeImage = new WorkingImage(file);
					stage.setTitle(file.toString());
					statusBar.setText(Messages.getString("Main.fileSaved"));
					lblSave.setDisable(true);
				}
			}
		});
				
		
		// Menüeintrag Wasserzeichen auslesen. 
		lblRead.setOnMouseEntered(		e-> {	lblRead.setId("menuLabelMouseOver");		});
		lblRead.setOnMouseExited(		e-> {	lblRead.setId("menuLabel");					});
		lblRead.setOnMouseClicked(		e-> {
			showInformationDialog(Messages.getString("Main.readWatermark"), activeImage.getEmbeddedWatermark());
		});
				
		
		// Menüeintrag Wasserzeichen schreiben.
		lblWrite.setOnMouseEntered(		e-> {	lblWrite.setId("menuLabelMouseOver");		});
		lblWrite.setOnMouseExited(		e-> {	lblWrite.setId("menuLabel");				});
		lblWrite.setOnMouseClicked(		e-> {
			TextInputDialog input = new TextInputDialog(Messages.getString("Main.watermark"));
			input.setTitle(Messages.getString("Main.watermarkInputTitle"));
			input.setHeaderText(Messages.getString("Main.watermarkInputHeader"));
			Optional<String> payload = input.showAndWait();
			payload.ifPresent(watermark -> {  
				if(activeImage.writeWatermark(watermark)){
					lblSave.setDisable	(false);
					lblSaveAs.setDisable(false);
					lblRead.setDisable	(false);
					lblErase.setDisable	(false);
					lblWrite.setDisable	(true);
					
					statusBar.setText(	Messages.getString("Main.watermarkSet") + 
										activeImage.getRedundancy() + 
										Messages.getString("Main.watermarkSetRedundancy"));
					
					stage.setTitle(	activeImage.getImageFile().toString() + 
										Messages.getString("Main.statusUnsaved"));
					
					showLoadedImage(activeImage.getImageFile());;
				} else {
					showInformationDialog (Messages.getString("Main.error"), Messages.getString("Main.errorWatermarkNotSet"));
				}
			});
		});
				
		
		// Menüeintrag Wasserzeichen entfernen.
		lblErase.setOnMouseEntered(		e-> {	lblErase.setId("menuLabelMouseOver");		});
		lblErase.setOnMouseExited(		e-> {	lblErase.setId("menuLabel");				});
		lblErase.setOnMouseClicked(		e-> {
			if(activeImage.removeWatermark()){
				lblSave.setDisable		(false);
				lblSaveAs.setDisable	(false);
				lblWrite.setDisable		(false);
				lblRead.setDisable		(true);
				lblErase.setDisable		(true);
				
				stage.setTitle(activeImage.getImageFile().toString() + Messages.getString("Main.statusUnsaved"));
				statusBar.setText(Messages.getString("Main.watermarkRemoved"));
				showLoadedImage(activeImage.getImageFile());;
			}
		});

				
		// Menüeintrag Programm beenden.
		lblClose.setOnMouseEntered(		e-> {	lblClose.setId("menuLabelMouseOver");		});
		lblClose.setOnMouseExited(		e-> {	lblClose.setId("menuLabel");				});
		lblClose.setOnMouseClicked(		e-> {
			if (activeImage == null || activeImage.getStatusImageSaved())		
				System.exit(0);
			else {
				Alert unsaved = new Alert(AlertType.CONFIRMATION);
				unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle"));
				unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader"));
				unsaved.setContentText(Messages.getString("Main.closeAnyway"));
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
		menuPane.add(lblClose,  0, 12);

		return menuPane;
	}
	
	
	/**
	 * Helper method for informing the user via dialog.
	 * 
	 * @param head		Message header-text.
	 * @param content	Message content-text.
	 */
	private static void showInformationDialog(String head, String content){
		Alert message = new Alert(AlertType.INFORMATION);
		message.setHeaderText(head);
		message.setContentText(content);
		message.show();
	}
	
}
