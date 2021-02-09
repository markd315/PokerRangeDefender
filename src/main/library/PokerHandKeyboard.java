package library;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;

/**
 * Used as a group of selectable buttons to add poker hands to a textfield
 * @author sacred
 *
 */
public class PokerHandKeyboard extends TabPane {

	//This is the javafx node where the text should be output to when the keyboard changes
	//This should be set by the constructor otherwise this class would be pointless
	TextInputControl textNode = null;
	
	//The Tabs
	Tab handGridTab;
	Tab cardGridTab;
	Tab cardGridToggleTab;
	
	//Variables for the Ranges page
	HashMap<String,ToggleButton> handGrid = new HashMap<String,ToggleButton>();
	HashMap<String,ToggleButton> cardGrid = new HashMap<String,ToggleButton>();
	GridPane handGridPane = new GridPane();
	GridMapClickHandler gridMapClickHandler = new GridMapClickHandler(); //should only need one instance
	
	// For the Card page, grid in stack to center it
	//We have both a pane with toggle buttons and regular buttons depending if we have a playertextfield or card textfield to manage
	StackPane cardSpacerPane = new StackPane();
	GridPane cardGridPane = new GridPane();
	StackPane cardSpacerTogglePane = new StackPane();
	GridPane cardGridTogglePane = new GridPane();
	CardMapClickHandler cardMapClickHandler = new CardMapClickHandler();
	
	
	/**
	 * Received a JavaFX TextInputControl (subclasses TextField, TextArea) as the field this keyboard
	 * is meant to update. The text from this paramater will also set the keyboard the first time? TODO
	 * @param textNode 
	 * @param type The internal enum to tell this class what type of keyboard to display
	 */
	public PokerHandKeyboard(TextInputControl textNode, KBType type) {
		if (!(textNode instanceof TextArea) && !(textNode instanceof PlayerTextField))
			throw new IllegalArgumentException("Poker Keyboard must have something to update");
		this.textNode = textNode;
		
		//Build the Ranges Page
		buildHandGridMap();
		handGridTab = new Tab("Types");
		handGridTab.setClosable(false);
		handGridTab.setContent(handGridPane);
		this.getTabs().add(handGridTab);
		
		//Build the Cards Page
		buildCardGridMap();
		cardGridTab = new Tab("Cards");
		cardGridToggleTab = new Tab("Cards");
		cardGridTab.setClosable(false);
		cardGridToggleTab.setClosable(false);
		cardGridTab.setContent(cardSpacerPane);
		cardGridToggleTab.setContent(cardSpacerTogglePane);
		cardSpacerPane.getChildren().add(cardGridPane);
		cardSpacerTogglePane.getChildren().add(cardGridTogglePane);
		this.getTabs().add(cardGridTab);
		
		
		// Stylesheet
		this.getStylesheets().add(getClass().getResource("/PokerKeyboard.css").toExternalForm());
		setPlayerTextField(textNode,type);
		
	}
	
	/**
	 * Used for setting what options this keyboard should have and which it should start with.
	 * For example, to set the flop, it should only show the Cards keyboard so ONLYCARDS would be used.
	 * @author sacred
	 *
	 */
	public enum KBType {
		CARDS,
		RANGES,
		ONLYCARDS,
		ONLYRANGES;
	}
	
	public void setCardTextField(TextInputControl textNode,KBType kbtype) {
		if (!(textNode instanceof CardTextField)) 
			throw new IllegalArgumentException("Illegal argument in poker keyboard: setCardTextField()");
		this.textNode = textNode;
		
		setTabs(kbtype);
	}

	public void setPlayerTextField(TextInputControl textNode, KBType kbtype) {
		if (!(textNode instanceof PlayerTextField)) 
			throw new IllegalArgumentException("Illegal argument in poker keyboard: setPlayerTextField()");
		this.textNode = textNode;
		setKeyboardFromCardString(textNode.getText());
		
		//fix tabs for what we need
		setTabs(kbtype);
	}
	
	/**
	 * Used to make sure we have the right tabs and set the one based on the argument
	 * @param kbtype
	 */
	private void setTabs(KBType kbtype) {
		if (kbtype == KBType.CARDS) {
			if (setBothTabs())
				this.getSelectionModel().select(cardGridTab);
		} else if (kbtype == KBType.RANGES) {
			if (setBothTabs())
				this.getSelectionModel().select(handGridTab);
		} else if (kbtype == KBType.ONLYCARDS) {
			if (!getTabs().contains(cardGridToggleTab)) {
				getTabs().removeAll(cardGridTab,handGridTab);
				getTabs().add(cardGridToggleTab);
			}
		} else if (kbtype == KBType.ONLYRANGES) {
			if (this.getTabs().contains(handGridTab)) {
				if (this.getTabs().size()==2)
					this.getTabs().remove(cardGridTab);
			} else {
				this.getTabs().add(handGridTab);
				if (this.getTabs().size() == 2)
					this.getTabs().remove(cardGridTab);
			}
		}
	}
	
	/**
	 * Helper method to setTabs() to make sure we have both tabs already and if not 
	 * set them.
	 * returns true if it had to add one, false if not
	 */
	private boolean setBothTabs() {
		boolean added = false;
			if (!this.getTabs().contains(handGridTab)) {
				this.getTabs().add(handGridTab);
				added = true; 
			}
			if (!getTabs().contains(cardGridTab)) {
				getTabs().add(cardGridTab);
				added = true;
			}
			
			if (getTabs().contains(cardGridToggleTab)) {
				getTabs().remove(cardGridToggleTab);
			}
			
			return added;
	}
	
	/**
	 * This method will receive a string used to set the buttons toggled. The string should be in
	 * standard format such as 22+, AQo, KQs+
	 * @param cardString
	 */
	public void setKeyboardFromCardString(String cardString) {
		ArrayList<String> selected = HoldemStrings.extractIndividualHandTypes(cardString);
		
		for (String s : handGrid.keySet()) {
			if (selected.contains(s)) { //toggle on
				handGrid.get(s).setSelected(true);
			} else { //toggle off
				handGrid.get(s).setSelected(false);
			}
			
		}
	}
	
	public void setKeyboardFromCards(String cardString) {
		ArrayList<String> selected = HoldemStrings.extractIndividualCards(cardString);
		
		for (String s : cardGrid.keySet()) {
			if (selected.contains(s)) {
				cardGrid.get(s).setSelected(true);
			} else {
				cardGrid.get(s).setSelected(false);
			}
		}
	}

	/**
	 * this inner class will handle selecting the buttons on the ranges map
	 * Will need to look for shift and ctrl held down
	 * @author sacred
	 *
	 */
	private class GridMapClickHandler implements EventHandler <MouseEvent> {

		@Override
		@SuppressWarnings(value = { "static-access" }) 
		public void handle(MouseEvent event) {
			if (!(event.getSource() instanceof ToggleButton))
				return; //weird weird
			ToggleButton t = (ToggleButton) event.getSource(); // for less typing
			
			if (event.isShiftDown()) {
				shiftClickModifier(t.getText());
			}
			
			if (event.isControlDown()) {
				boolean on=true;
				if (t.isSelected()) on=false;
				ctrlClickModifier(handGridPane.getRowIndex(t),handGridPane.getColumnIndex(t),on);
			}
			
			if (t.isSelected()) {  //was just turned on, so add to textNode
				if (textNode instanceof PlayerTextField) 
					((PlayerTextField) textNode).addType(t.getText());
			} else {  //was just unselected so remove from textNode
				((PlayerTextField) textNode).removeType(t.getText());
			}
			
			
			
		}
		
	}
	
	/**
	 * Takes the button that was clicked and adds a range with it as the bottom.
	 * @param buttonText
	 */
	private void shiftClickModifier(String buttonText) {
		if (textNode instanceof PlayerTextField) {
			((PlayerTextField) textNode).addRange(String.format("%s+", buttonText));
		}
	}
	
	/**
	 * Takes the button and highlights the grid everything above and to the left of the paramers
	 * @param row in gridpane
	 * @param col in gridpane
	 */
	@SuppressWarnings(value = { "static-access" }) 
	private void ctrlClickModifier(int row, int col, boolean on) {
		if (!(textNode instanceof PlayerTextField))
			throw new IllegalArgumentException("Problem in poker keyboard: ctrlClickModifier");
		for (ToggleButton t : handGrid.values()) {
			if (handGridPane.getRowIndex(t) <= row && handGridPane.getColumnIndex(t) <= col) {
				if (!t.isSelected() && on==true)
					((PlayerTextField) textNode).addType(t.getText());
				else if (t.isSelected() && on==false) ((PlayerTextField) textNode).removeType(t.getText());
			}
		}
	}
	
	/**
	 * This inner class will handle clicks on the cards map. It will need to 
	 * determine if the textnode is a player text field or board text field.
	 * @author sacred
	 *
	 */
	private class CardMapClickHandler implements EventHandler <MouseEvent> {
		
		@Override
		public void handle(MouseEvent event) {
		
		if (event.getSource() instanceof Button) {
			
			Button b = (Button) event.getSource();
			
			//really doesn't matter which one we have
			if (textNode instanceof PlayerTextField)
				((PlayerTextField) textNode).addCard(b.getText());
			else if (textNode instanceof CardTextField)
				((CardTextField) textNode).addCard(b.getText());
			
		} else if (event.getSource() instanceof ToggleButton) {
			
			ToggleButton tb = (ToggleButton) event.getSource();
			
			if (tb.isSelected()) { //just pressed
			if (textNode instanceof PlayerTextField)
				((PlayerTextField) textNode).addCard(tb.getText());
			else if (textNode instanceof CardTextField)
				((CardTextField) textNode).addCard(tb.getText());
			} else { //just unpressed
				if (textNode instanceof PlayerTextField)
					((PlayerTextField) textNode).removeCard(tb.getText());
				else if (textNode instanceof CardTextField)
					((CardTextField) textNode).removeCard(tb.getText());
				
			}
			
		}
		
		}
			
	}
	
	private void buildCardGridMap() {
		
		//cardGridPane.setHgap(1);
		//cardGridPane.setVgap(1);
		
		for (Card.Suit suit : Card.Suit.values()) { // suits
			for (Card.Rank rank : Card.Rank.values()) {
				cardGridPane.add((Button)buildCardButton(suit,rank,false), rank.getValue()-2, suit.getValue());
				cardGrid.put(String.format("%c%c",Character.toUpperCase(rank.getChar()),Character.toLowerCase(suit.getChar())),(ToggleButton)buildCardButton(suit,rank,true) );
				cardGridTogglePane.add(cardGrid.get(String.format("%c%c",Character.toUpperCase(rank.getChar()),Character.toLowerCase(suit.getChar()))), rank.getValue()-2, suit.getValue());
			}
		}
	}
	
	private ButtonBase buildCardButton (Card.Suit suit, Card.Rank rank, boolean isToggleButton) {
		ButtonBase b;
		if (isToggleButton) {
			b = new ToggleButton();
		} else {
			b = new Button();
		}
		b.setPrefWidth(29);
		b.setPrefHeight(34);

		b.setText(String.format("%c%c",Character.toUpperCase(rank.getChar()),Character.toLowerCase(suit.getChar())));
		switch (suit.getValue()) {
		case 0: b.getStyleClass().add("button-clubs"); break;
		case 1: b.getStyleClass().add("button-diamonds"); break;
		case 2: b.getStyleClass().add("button-hearts"); break;
		case 3: b.getStyleClass().add("button-spades"); break;
		}
		b.setOnMouseClicked(cardMapClickHandler);
		
		return b;
	}
	
	
	/**
	 * Builds the HashMap<String,ToggleButton> handGrid and Hand Grid Pane.
	 * This is an unchanging poker hand grid to pick ranges.
	 * This also uses the builder buildGridToggleButton to generate the ToggleButtons 
	 */
	private void buildHandGridMap() {
		String []buttons = new String[] {
			"AA","AKs","AQs","AJs","ATs","A9s","A8s","A7s","A6s","A5s","A4s","A3s","A2s",  
			"AKo","KK","KQs","KJs","KTs","K9s","K8s","K7s","K6s","K5s","K4s","K3s","K2s",
			"AQo","KQo","QQ","QJs","QTs","Q9s","Q8s","Q7s","Q6s","Q5s","Q4s","Q3s","Q2s",
			"AJo","KJo","QJo","JJ","JTs","J9s","J8s","J7s","J6s","J5s","J4s","J3s","J2s",
			"ATo","KTo","QTo","JTo","TT","T9s","T8s","T7s","T6s","T5s","T4s","T3s","T2s",
			"A9o","K9o","Q9o","J9o","T9o","99","98s","97s","96s","95s","94s","93s","92s",
			"A8o","K8o","Q8o","J8o","T8o","98o","88","87s","86s","85s","84s","83s","82s",
			"A7o","K7o","Q7o","J7o","T7o","97o","87o","77","76s","75s","74s","73s","72s",
			"A6o","K6o","Q6o","J6o","T6o","96o","86o","76o","66","65s","64s","63s","62s",
			"A5o","K5o","Q5o","J5o","T5o","95o","85o","75o","65o","55","54s","53s","52s",
			"A4o","K4o","Q4o","J4o","T4o","94o","84o","74o","64o","54o","44","43s","42s",
			"A3o","K3o","Q3o","J3o","T3o","93o","83o","73o","63o","53o","43o","33","32s",
			"A2o","K2o","Q2o","J2o","T2o","92o","82o","72o","62o","52o","42o","32o","22"
		};
		
		for (String s: buttons ) { 
			handGrid.put(s, buildGridToggleButton(s));
		}
		
		//place them in the Grid Pane
		for (int row = 0, bkey = 0; row < 13 ; row++) {  //column 
			for (int col = 0; col < 13; col++ ) { //row
				handGridPane.add(handGrid.get(buttons[bkey]), col, row);
				bkey++;
			}
		}
		
	}
	
	
	/**
	 * Builder for ToggleButtons for the top border (to change panes)
	 * @param text
	 * @return JavaFx ToggleButton
	 */
	private ToggleButton buildGridToggleButton(String text) {
		ToggleButton tb = new ToggleButton(text);
		tb.setPrefWidth(29);
		tb.setPrefHeight(34);

		// Style
		tb.getStyleClass().add("toggle-button-grid");
		if (text.length()==2) { //pair style name
			tb.getStyleClass().add("toggle-button-pair");
		} else if (text.charAt(2)=='s') {
			tb.getStyleClass().add("toggle-button-suited");
		} else tb.getStyleClass().add("toggle-button-offsuit");
		
		//actions:
		//ideally: Ctrl+ selects everything in colum above, Shift+ selects everything blocked above
		tb.setOnMouseClicked(gridMapClickHandler);
		return tb;
	}
	
}
