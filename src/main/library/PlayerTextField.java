package library;

import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class PlayerTextField extends TextField {

	PokerHandKeyboard pokerHandKeyboard;
	String stasis = ""; //used for addcards. The last card added is held in stasis, while we wait on second.
	
	public PlayerTextField() {
		focusedProperty().addListener(e -> {
			if (!isFocused()) 
				condenseText();
		});
		
		textProperty().addListener(e -> {
			if (pokerHandKeyboard != null) {
				
				pokerHandKeyboard.setKeyboardFromCardString(HoldemStrings.normalizeCaps(getText()));}
		});
	}
	
	/**
	 * Called by GUI to add a poker hand type such as AJo, JJ
	 * Cannot accept ranges.
	 * 
	 * @param card String from GUI
	 */
	public void addType(String type) {
		//check format
		if (type.length()>3 || !HoldemStrings.verifyCard(type.charAt(0)) || !HoldemStrings.verifyCard(type.charAt(1)))
			throw new IllegalArgumentException("Problem in playertext field add type");
		if (type.length()==3 && (Character.toLowerCase(type.charAt(2))!='s') && (Character.toLowerCase(type.charAt(2))!='o'))
			throw new IllegalArgumentException("Problem in playertext field add type");
		
		String cardString = getText();
		setText(HoldemStrings.addTypetoString(cardString, type));
		
	}
	
	public void addRange(String range) {
		if ((range.length()==3 || range.length()==4)&& (!HoldemStrings.verifyCard(range.charAt(0)) || !HoldemStrings.verifyCard(range.charAt(1))))
			throw new IllegalArgumentException("Problem in playertext field add range");
		if (range.length()==4 && (range.charAt(2)!='s' && range.charAt(2)!='o'))
			throw new IllegalArgumentException("Problem in playertext field add range 2");
		if (range.charAt(range.length()-1) != '+')
			throw new IllegalArgumentException("Problem in playertext field add range 3");
		
		String cardString = getText();
		setText(HoldemStrings.addTypetoString(cardString, range));
				
	}
	
	
	/**
	 * Adds one of two cards for a specific hand.
	 * @param card
	 */
	public void addCard(String card) {
		if (getText().length()>=2) { //check for stasis card
			if (getText().substring(getText().length()-2).equalsIgnoreCase(stasis)) {
				setText(getText() + card);
				stasis = "";
			} else {
				setText(getText() + "," + card);
				stasis =  card;
			}
		} else {
			setText(getText() + card);
			stasis = card;
		}
	}
	
	public void removeCard(String card) {
		if (card.length() != 2)
			throw new IllegalArgumentException("Error in CardTextfield: removeCard");
		setText(getText().replaceAll(card, ""));
	}
	/**
	 * Called by GUI to remove a poker hand such as AJo, JJ
	 * Does not accept ranges.
	 * @param type
	 */
	public void removeType(String type) {
		//check format
		setText(HoldemStrings.removeTypeFromString(getText(), type));
	}
	
	/**
	 * Sets the poker keyboard to be updated whenever this text field changes. This does not have to be set.
	 * Must be unset using removeKeyboard() or the keyboard reference must be set to null whenever the 
	 * user changes focus.
	 * @param keyboard PokerHandKeyboard
	 */
	public void setKeyboard(PokerHandKeyboard keyboard) {
		this.pokerHandKeyboard = keyboard;
	}
	
	/**
	 * Sets the keyboard reference to null
	 */
	public void removeKeyboard() {
		this.pokerHandKeyboard = null;
	}
	
	private void condenseText() {
		setText(HoldemStrings.condenseCardString(getText()));
	}

}
