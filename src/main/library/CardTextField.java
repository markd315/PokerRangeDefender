package library;

import javafx.scene.control.TextField;

/*
 * Should have used inheritance
 */
public class CardTextField extends TextField {
	
	PokerHandKeyboard pokerHandKeyboard;

	public CardTextField() {
		
		textProperty().addListener(e -> {
			if (pokerHandKeyboard != null) {
				pokerHandKeyboard.setKeyboardFromCards(getText());
			}
		});
	}
	
	/**
	 * Called by the keyboard to send a card with the format Ac
	 * @param card
	 */
	public void addCard(String card) {
		if (card.length()!=2)
			throw new IllegalArgumentException("Error in CardTextField: addCard");
		setText(getText()+card);
	}

	public void removeCard(String card) {
		if (card.length() != 2)
			throw new IllegalArgumentException("Error in CardTextfield: removeCard");
		String regex = String.format("[%c%c][%c%c]", Character.toUpperCase(card.charAt(0)),Character.toLowerCase(card.charAt(0)),Character.toUpperCase(card.charAt(0)),Character.toLowerCase(card.charAt(1)));

		setText(getText().replaceAll(regex, ""));
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
	
}
