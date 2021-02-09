package library;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/*
 * The goal here is to have a little message area at the bottom where we can output things like "hey, that's a big calculation"
 */

public class MessageBottomBorderFX extends FlowPane {
		Text message;
		Button moreInfoButton; //added if a complex message is given that needs more info
	
	public MessageBottomBorderFX() {
		message = new Text();
		
		moreInfoButton = new Button("...");
		getChildren().add(message);
		
	}

	public void addSimple(String simpleMessage) {
		message.setText(simpleMessage);
	}
}
