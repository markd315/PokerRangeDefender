package library;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

public class MainJavaFX extends Application implements EquityCalcObserver {

	EquityCalc equityCalc = new EquityCalc(this); // must init equitycalc here so it doesn't reload matrix every time
	
	//Bottom Message area
	MessageBottomBorderFX messagePane;
	
	// Nodes that need to be accessed by various methods
	Scene primaryScene;
	BorderPane mainBorderPane = new BorderPane();
	
	VBox playerVBox;
	VBox calcEnumPane;
	CardTextField boardTextField;
	CardTextField deadTextField;
	ArrayList<PlayerTextField> playerTextField = new ArrayList<PlayerTextField>();
	ArrayList<Text> playerResultsText = new ArrayList<Text>();
	
	TextArea resultsTextArea;
	
	//poker hand keyboard, will be passed to the active textfield which will be passed to the keyboard
	PokerHandKeyboard pokerHandKeyboard = null; 
	
	//calc progress controls
	BooleanProperty calcInProgress;
	boolean stopRequested = false; //used to tell equitycalc to stop
	boolean stopPressed = false;
	ProgressBar progressBar;
	Button startStopButton;
	
	//Time showing
	long startTime;
	long endTime;
	
	public MainJavaFX() {
		
	}

	public void go() {
		launch(new String[]{});
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		GridPane pane = new GridPane();
		
		
		//setup player hand area
		playerVBox = new VBox();
		playerVBox.setSpacing(5);
		for (int c = 0; c<6; c++) {
			playerVBox.getChildren().add(buildPlayerRow(c));
		}

		//keyboard
		pokerHandKeyboard = new PokerHandKeyboard(playerTextField.get(0),PokerHandKeyboard.KBType.RANGES);
		playerTextField.get(0).setKeyboard(pokerHandKeyboard);
		pane.add(pokerHandKeyboard, 0, 0,1,2);
		
		pane.add(playerVBox,1,0);
		pane.add((calcEnumPane = buildCalcEnumPane()),2,0);
		
		pane.add(buildResultsTextAreaPane(), 1, 1,2,1);
		
		pane.setPadding(new Insets(2,2,2,2));
		
		pane.setHgap(5);
		pane.setVgap(5);
		mainBorderPane.setCenter(pane);
		//mainBorderPane.setBottom((messagePane= new MessageBottomBorderFX(primaryStage)));
		primaryScene = new Scene(mainBorderPane);
		
		//Style Sheets
		playerVBox.getStylesheets().add(getClass().getResource("/PokerGUI.css").toExternalForm());
		calcEnumPane.getStylesheets().add(getClass().getResource("/PokerGUI.css").toExternalForm());

		messagePane = new MessageBottomBorderFX();
		mainBorderPane.setBottom(messagePane);
		primaryStage.setTitle("idontchop.com Hand Evaluator (late development)");
		primaryStage.setScene(primaryScene);
		primaryStage.setWidth(860);
		primaryStage.setHeight(480);
		primaryStage.show();
		System.out.println("" + primaryStage.getWidth() + "" + primaryStage.getHeight() );

	}
	
	private VBox buildCalcEnumPane() {
		Label boardLabel = new Label("Board: ");
		Label deadLabel =  new Label("Dead:  ");
		boardTextField = new CardTextField();
		boardTextField.setId("boardTextField");
		boardTextField.setPrefSize(180, 25);
		Button boardClearButton = new Button("X");
		boardClearButton.setOnAction(e -> boardTextField.setText(""));
		deadTextField = new CardTextField();
		deadTextField.setId("deadTextField");
		deadTextField.setPrefSize(180, 25);
		Button deadClearButton = new Button("X");
		deadClearButton.setOnAction(e -> deadTextField.setText(""));
		
		//Grids for board and dead fields (mainly here for highlighting)
		GridPane boardGridPane = new GridPane();
		GridPane deadGridPane = new GridPane();
		
		boardGridPane.add(boardLabel, 0, 0, 2, 1);
		boardGridPane.add(boardClearButton, 0, 1,1,1);
		boardGridPane.add(boardTextField, 1, 1,1,1);
		boardGridPane.setVgap(3); boardGridPane.setHgap(3);
		
		deadGridPane.add(deadLabel, 0, 0,2,1);
		deadGridPane.add(deadClearButton, 0, 1,1,1);
		deadGridPane.add(deadTextField, 1, 1,1,1);
		deadGridPane.setVgap(3);deadGridPane.setHgap(3);
		
		//keyboard handlers
		boardTextField.focusedProperty().addListener(e -> {
			if (boardTextField.isFocused()) {
				boardTextField.setKeyboard(pokerHandKeyboard);
				pokerHandKeyboard.setCardTextField(boardTextField, PokerHandKeyboard.KBType.ONLYCARDS);
				setSelectedBoardRow(boardTextField);
			}
		});
		deadTextField.focusedProperty().addListener(e -> {
			if (deadTextField.isFocused()) {
				deadTextField.setKeyboard(pokerHandKeyboard);
				pokerHandKeyboard.setCardTextField(deadTextField, PokerHandKeyboard.KBType.ONLYCARDS);
				setSelectedBoardRow(deadTextField);
			}
		});
		
		
		//calcButton, calls equitycalc
		//setup calc start and stops
		startStopButton = new Button("Go");
		startStopButton.setId("startStopButton");
		startStopButton.setPrefWidth(100);
		startStopButton.setOnAction(e -> doCalcEnum());
		calcInProgress = new SimpleBooleanProperty();
		calcInProgress.addListener(e -> changeCalc());
		
		//clear all button. clears all fields
		Button clearAllButton = new Button ("Clear All");
		clearAllButton.setId("ClearAllButton");
		clearAllButton.setPrefWidth(100);
		clearAllButton.setOnAction(e -> {
			for (PlayerTextField p: playerTextField) {
				p.setText("");
			}
			boardTextField.setText("");
			deadTextField.setText("");
			eraseResultsText();
			updateResultsTextArea("");
		});
		
		VBox calcEnumVBox = new VBox(10);
		calcEnumVBox.getChildren().add(boardGridPane);
		calcEnumVBox.getChildren().add(deadGridPane);
		GridPane buttonGridPane = new GridPane();
		buttonGridPane.add(startStopButton, 0, 0);
		buttonGridPane.add(clearAllButton, 1, 0);
		calcEnumVBox.getChildren().add(buttonGridPane);
		
		//progress bar
		progressBar = new ProgressBar();
		progressBar.setProgress(0);
		progressBar.setPrefSize(200, 20);
		//get the progress text into bar
		
		//style
		calcEnumVBox.getStyleClass().add("board-row");
		boardGridPane.getStyleClass().add("board-row");
		deadGridPane.getStyleClass().add("board-row");
		return calcEnumVBox;
	}
	
	private void changeCalc() {
		if (calcInProgress.get()) { //calc in progress
			startStopButton.setText("Stop");
			startStopButton.setOnAction(e-> stopRequested=true);
			Platform.runLater(() -> calcEnumPane.getChildren().add(progressBar));
		} else {
			startStopButton.setText("Go");
			startStopButton.setOnAction(e-> doCalcEnum());
			Platform.runLater( () -> calcEnumPane.getChildren().remove(progressBar));
		}
	}
	
	/**
	 * This is called by update equity to pull the results label next to each hand and update it while an enumeration is in progress.
	 * @param hands corresponds to the hands given, this method expects the percentage
	 */
	private void updatePerHandProgress(float[] hands) {
		ArrayList<Label> resultsLabels = new ArrayList<Label>();
		//ugh, mostly duplicate code
		// yea, this is stupid, just going to have to make a global array for the player hands HBox, maybe even an array for the TextField and Results

	}
	
	private void doCalcEnum() {
		ArrayList<String> playerHands = new ArrayList<String>();
		stopRequested = false; //called by checkStop()
		
		for (Node vboxNode : playerVBox.getChildren()) {
			if (vboxNode instanceof HBox) {
				for (Node hboxNode : ((HBox) vboxNode).getChildren()) {
					if (hboxNode instanceof TextField) {
						if (!((TextField)hboxNode).getText().equals("")) {
							playerHands.add(((TextField)hboxNode).getText());  // TODO: will need to check for syntax around here somewhere to inform the user of bad input
						}
					}
				}
			}
		}
		
		EnumGo enumGo = new EnumGo(this);
		
		enumGo.setPockets(playerHands);
		
		//set board and dead... weird we pass strings here and an arraylist for player hands
		enumGo.setBoard(boardTextField.getText());
		enumGo.setDead(deadTextField.getText());
		
		Thread myThread = new Thread(enumGo);
		myThread.start();
		
	}
	
	//TODO: erase results text
	class EnumGo implements Runnable {
		EquityCalcObserver observer;
		Pocket [][]pockets = new Pocket[0][0];
		Card[] board = new Card[0];
		Card[] dead =  new Card[0];
		
		public EnumGo (EquityCalcObserver observer) {
			this.observer = observer;
		}
		
		public void setPockets(ArrayList<String> playerHands) {
			
			if (playerHands.size()<2) return; //TODO: notify user
			
			int size=0;
			for (String hand: playerHands) { // first check for syntax and set size of pockets TODO: notify user if wrong
				if (HoldemStrings.pocketsToArray(hand).length > 0) size++;
				//else playerHands.remove(hand); // remove it so below loop works
			}
			pockets = new Pocket[size][];
			//TODO: if size does not equal playerHands size, there was bad input
			for (int c=0; c<size; c++) {
				pockets[c] = HoldemStrings.pocketsToArray(playerHands.get(c));
			}
			
		}
		
		public void setBoard(String board) {
			if (board.length()>2)
				this.board = HoldemStrings.cardsToArray(board);
			
		}
		
		public void setDead(String dead) {
			if (dead.length()>2)
				this.dead = HoldemStrings.cardsToArray(dead);
		}
		
		@Override
		public void run() {
			long [][]results;
			eraseResultsText(); //clear the playerResultsText
			startTime = System.currentTimeMillis();
			if (board.length>0 && dead.length>0) {
				results = equityCalc.calcEnum(pockets,board,dead);
			} else if (board.length>0) {
				System.out.print("here:");
				System.out.println(board[0].getCardStr());
				results = equityCalc.calcEnum(pockets,board);
			} else if (dead.length>0) {
				results = equityCalc.calcEnum(pockets,board,dead);
			} else results = equityCalc.calcEnum(pockets);
			endTime = System.currentTimeMillis();
			if (results.length<2)
				updateResultsTextArea("Valid hands not found");
			else updateResultsTextArea(results);
		}
		
	}
	
	/**
	 * Will receive the results from equityCalc and update the text area. updateEquity() should have taken care of the results label.
	 * @param results
	 */
	//TODO: account for ties equity better
	private void updateResultsTextArea(long[][] results) {
		String output = new String();
		float[] resultsPercent = new float[results.length];
		output += "                                    Wins                            Ties                       Total Equity\n";
		for(int c = 0; c< results.length; c++) {
			output += c==0 ? String.format("Hero:      ",c+1) : String.format("Villian %d: ",c);
			output += String.format("%,15d (%.2f%%)",results[c][0],(double)(((double)results[c][0]/(double)results[c][2])*100));
			output += String.format("%,15d (%.2f%%)", results[c][1],((double)results[c][1]/(double)results[c][2])*100);
			output += String.format("%,15d (%.2f%%)", results[c][0]+(results[c][1]/2),(((double)results[c][0]+((double)results[c][1])/2)/(double)results[c][2])*100);
			resultsPercent[c] = (float)(((double)results[c][0]/(double)results[c][2])*100);
			output += String.format("\n");
			for (int cc=c;cc<playerTextField.size();cc++) { // account for skipped field
				if (!playerTextField.get(cc).getText().equals("")) {
					output += "     " + "[" + playerTextField.get(cc).getText() + "]\n";
					break;
				}
			}
			
		}
		output += String.format("\nNum Games: %,15d\n", results[0][2]);
		output += String.format("Elapsed Time: %d  Hands/s: %d", (endTime-startTime),((results[0][2]/(endTime-startTime))*1000));
		
		resultsTextArea.setText(output);
		
	}
	
	/**
	 * Will receive a string to update in the text area
	 * @param message
	 */
	private void updateResultsTextArea(String message) {
		resultsTextArea.setText(message);
	}
	
	private BorderPane buildResultsTextAreaPane() {
		BorderPane pane = new BorderPane();
		FlowPane topFlowPane = new FlowPane();
		
		topFlowPane.getChildren().add(new Label("Results  "));
		
		Button toClipboardButton = new Button("C");
		toClipboardButton.setOnAction(e -> copyTextAreaToClipboard());
		topFlowPane.getChildren().add(toClipboardButton);
		
		pane.setTop(topFlowPane);
		
		resultsTextArea = new TextArea();
		pane.setCenter(resultsTextArea);
		
		return pane;
		
	}
	
	private void copyTextAreaToClipboard() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		
		content.putString(resultsTextArea.getText());
		clipboard.setContent(content);
		
		messagePane.addSimple("Results copied to system clipboard.");
	}
	
	private void eraseResultsText() {
		for (Text t: playerResultsText) {
			t.setText("");
		}
	}
	
	private HBox buildPlayerRow(int num) {
		
		//Label
		Label playerLabel;
		if (num == 0) playerLabel = new Label("Hero");
		else playerLabel = new Label(String.format("Villian %d ", num));
		playerLabel.setPrefWidth(55);
		playerLabel.setPrefHeight(25);
		playerLabel.setAlignment(Pos.CENTER);
		
		
		//clear button
		Button playerClearButton = new Button("X");
		playerClearButton.getStyleClass().add("player-row-button");
		
		PlayerTextField playerTextField = new PlayerTextField();
		playerTextField.setId(String.format("player%dTextField", num));
		playerTextField.setPrefSize(130, 25);   //player hand text size
		Text playerResultsText = new Text();
		playerResultsText.setId(String.format("player%dResults", num));
		this.playerResultsText.add(playerResultsText);
		playerClearButton.setOnAction(e -> playerTextField.setText(""));
		HBox playerHBox = new HBox(0,playerLabel,playerTextField,playerClearButton,this.playerResultsText.get(num));
		playerHBox.setPrefWidth(270);
		//When focus is placed on the playerTextField, we need to pass it the keyboard reference
		playerTextField.focusedProperty().addListener(e -> {
			if (playerTextField.isFocused()) {
				playerTextField.setKeyboard(pokerHandKeyboard);
				pokerHandKeyboard.setPlayerTextField(playerTextField, PokerHandKeyboard.KBType.RANGES);
			}
			setSelectedPlayerRow(playerTextField);
		});
		//When focus leaves the playerTextField, we need to remove the border
		//not useful since button clicks undue focus
		/*
		playerTextField.focusedProperty().addListener(e -> {
			if (!playerTextField.isFocused()) ((HBox)playerTextField.getParent()).setStyle(null);
		});
		*/
		
		//So user can select fields without clicking perfectly
		playerLabel.setOnMouseClicked(e -> {
			playerTextField.requestFocus();
		});

		
		
		//Style
		playerHBox.getStyleClass().add("player-row");
		
		this.playerTextField.add(playerTextField);
		return playerHBox;
	}

	/**
	 * Highlights the Player Row. Called by focus listeners. Pass null to clear all highlights on Player Text Fields
	 * @param newSelected the Player Text Field
	 */
	private void setSelectedPlayerRow(PlayerTextField newSelected) {
		for (PlayerTextField p : playerTextField) {
			if (!(p.getParent() instanceof HBox)) {
				throw new IllegalArgumentException("HBox not parent of playerTextField.");
			}
			//clear all set styles
			((HBox) p.getParent()).setStyle(null);
			
		}
		//clear our card and dead
		((GridPane)boardTextField.getParent()).setStyle(null);
		((GridPane)deadTextField.getParent()).setStyle(null);
		
		//set new selected
		if (newSelected != null)
			((HBox)newSelected.getParent()).setStyle("-fx-border-color: blue");
		
	}
	
	private void setSelectedBoardRow(CardTextField newSelected) {
		setSelectedPlayerRow(null);
		if (!(newSelected.getParent() instanceof GridPane) && newSelected != null) {
			throw new IllegalArgumentException("GridPane not parent of Card Text Field");
		}
		
		
		if (newSelected != null)
			((GridPane)newSelected.getParent()).setStyle("-fx-border-color: blue");
	}
	
	@Override
	public void updateEquity(float[] percentage, int progress) {
		//TODO: update HoldemStrings with method that just checks for valid hands to save memory
		if (progress < 50 && !calcInProgress.get()) {
			//setup buttons here and progress bar
			//we put this here because we dont' want to bother changing buttons if we only get one return due to a quick calc
			Platform.runLater(() -> calcInProgress.set(true));
		} else if (progress == 100 ) {
			//will always get a 100, restore go button and remove progress bar
			Platform.runLater(() ->calcInProgress.set(false));
		} 
		progressBar.setProgress((double)progress/100.0);
		//always do this
		for (int c = 0, pc=0; c<playerTextField.size(); c++) {
			if (HoldemStrings.pocketsToArray(playerTextField.get(c).getText()).length>0) {  //check if the textfield has valid hands so we don't update wrong one
				playerResultsText.get(c).setText(String.format("%.2f%%", percentage[pc]));
				pc++;
				//resultsTextArea.appendText(String.format("%.2f%%", percentage[c]));
			}
		}
	}

	@Override
	public boolean checkStop() {
		return stopRequested;
	}

}
