package application.gui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PrimaryMenu {
	
	private static final double	MENU_INITIAL_OPACITY 	= 0.2;
	private static final int	MENU_ELEMENT_GAPS		= 12;
	private static final Insets MENU_PADDING			= new Insets(20,20,20,20);

		
	private GridPane  primaryMenuPane;
	
	private Label     menuItemOpenFile;
	private Label     menuItemSaveFile;
	private Label     menuItemSaveFileAs;
	private Label     menuItemReadMark;
	private Label     menuItemWriteMark;
	private Label     menuItemEraseMark;
	private Label     menuItemExitProgram;
	
	
	
	
	protected PrimaryMenu(Stage primaryStage) {
		primaryMenuPane = new GridPane();
		configureMenuPane();
		configureMenuItems();
		primaryMenuPane.setOnMouseEntered	(e -> Control.makeMenuFadeIn(primaryMenuPane));
		primaryMenuPane.setOnMouseExited	(e -> Control.makeMenuFadeOut(primaryMenuPane));
	}
	
	
	
	
	protected GridPane getPrimaryMenuPane() {
		return primaryMenuPane;
	}

	
	protected Label getMenuItemOpenFile() {
		return menuItemOpenFile;
	}


	protected Label getMenuItemSaveFile() {
		return menuItemSaveFile;
	}


	protected Label getMenuItemSaveFileAs() {
		return menuItemSaveFileAs;
	}


	protected Label getMenuItemReadMark() {
		return menuItemReadMark;
	}


	protected Label getMenuItemWriteMark() {
		return menuItemWriteMark;
	}


	protected Label getMenuItemEraseMark() {
		return menuItemEraseMark;
	}


	protected Label getMenuItemExitProgram() {
		return menuItemExitProgram;
	}


	
	
	private void configureMenuPane() {
		primaryMenuPane.setId("menuPane");
		primaryMenuPane.setOpacity(MENU_INITIAL_OPACITY);
		primaryMenuPane.setVgap(MENU_ELEMENT_GAPS);
		primaryMenuPane.setHgap(MENU_ELEMENT_GAPS);
		primaryMenuPane.setPadding(MENU_PADDING);
		primaryMenuPane.setAlignment(Pos.CENTER);
	}
	
		
	
	
	private void configureMenuItems() {
		setMenuItemNames();
		setMenuItemInitialCSSLabels();
		setMenuItemsColorChangeOnMouseEntered();
		setMenuItemsColorChangeOnMouseExited();
		setMenuItemActionsOnMouseClicked();
		setMenuItemsInitialEnableDisableStatus();
		addMenuItemsToMenuPane();
	}


	private void setMenuItemNames() {
		menuItemOpenFile 		= new Label("Open");
		menuItemSaveFile 		= new Label("Save");
		menuItemSaveFileAs 		= new Label("Save As");
		menuItemReadMark		= new Label("Read");
		menuItemWriteMark 		= new Label("Write");
		menuItemEraseMark 		= new Label("Erase");
		menuItemExitProgram 	= new Label("Exit");
	}
	
	
	private void setMenuItemInitialCSSLabels() {
		menuItemOpenFile.setId			("menuLabel");
		menuItemSaveFile.setId			("menuLabel");
		menuItemSaveFileAs.setId		("menuLabel");
		menuItemReadMark.setId			("menuLabel");
		menuItemWriteMark.setId			("menuLabel");
		menuItemEraseMark.setId			("menuLabel");
		menuItemExitProgram.setId		("menuLabel");
	}
	
	
	private void setMenuItemsColorChangeOnMouseEntered() {
		menuItemOpenFile.setOnMouseEntered		(e->	Control.setMenuItemHighlighted(true, menuItemOpenFile)		);
		menuItemSaveFile.setOnMouseEntered		(e-> 	Control.setMenuItemHighlighted(true, menuItemSaveFile)		);
		menuItemSaveFileAs.setOnMouseEntered	(e-> 	Control.setMenuItemHighlighted(true, menuItemSaveFileAs)	);
		menuItemReadMark.setOnMouseEntered		(e-> 	Control.setMenuItemHighlighted(true, menuItemReadMark)		);
		menuItemWriteMark.setOnMouseEntered		(e-> 	Control.setMenuItemHighlighted(true, menuItemWriteMark)		);
		menuItemEraseMark.setOnMouseEntered		(e-> 	Control.setMenuItemHighlighted(true, menuItemEraseMark)		);
		menuItemExitProgram.setOnMouseEntered	(e-> 	Control.setMenuItemHighlighted(true, menuItemExitProgram)	);
	}
	
	
	private void setMenuItemsColorChangeOnMouseExited() {
		menuItemOpenFile.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemOpenFile)		);
		menuItemSaveFile.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemSaveFile)		);
		menuItemSaveFileAs.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemSaveFileAs)	);
		menuItemReadMark.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemReadMark)		);
		menuItemWriteMark.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemWriteMark)	);
		menuItemEraseMark.setOnMouseExited		(e-> 	Control.setMenuItemHighlighted(false, menuItemEraseMark)	);
		menuItemExitProgram.setOnMouseExited	(e-> 	Control.setMenuItemHighlighted(false, menuItemExitProgram)	);
	}
	
	
	private void setMenuItemActionsOnMouseClicked() {
		menuItemOpenFile.setOnMouseClicked		(e -> 	Control.menuActionOpenFile()			);
		menuItemSaveFile.setOnMouseClicked		(e -> 	Control.menuActionSaveFile()			);
		menuItemSaveFileAs.setOnMouseClicked	(e -> 	Control.menuActionSaveFileAs()			);
		menuItemReadMark.setOnMouseClicked		(e -> 	Control.menuActionReadWatermark()		);
		menuItemWriteMark.setOnMouseClicked		(e -> 	Control.menuActionWriteWatermark()		);
		menuItemEraseMark.setOnMouseClicked		(e -> 	Control.menuActionEraseWatermark()		);
		menuItemExitProgram.setOnMouseClicked	(e -> 	Control.menuActionExitProgram()			);
	}
	
	
	private void setMenuItemsInitialEnableDisableStatus() {
		menuItemOpenFile.setDisable		(false);
		menuItemSaveFile.setDisable		(true);
		menuItemSaveFileAs.setDisable	(true);
		menuItemReadMark.setDisable		(true);
		menuItemWriteMark.setDisable	(true);
		menuItemEraseMark.setDisable	(true);
		menuItemExitProgram.setDisable	(false);

	}
	

	
	
	private void addMenuItemsToMenuPane() {
		primaryMenuPane.add(menuItemOpenFile, 		0,  0);
		primaryMenuPane.add(menuItemSaveFile, 		0,  1);
		primaryMenuPane.add(menuItemSaveFileAs, 	0,  2);
		primaryMenuPane.add(menuItemReadMark, 		0,  5);
		primaryMenuPane.add(menuItemWriteMark,		0,  6);
		primaryMenuPane.add(menuItemEraseMark,		0,  7);
		primaryMenuPane.add(menuItemExitProgram,	0, 12);
	}

}
