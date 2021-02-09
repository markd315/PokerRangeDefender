package library;

/*
 * This class will represent a Hold'em player's two pocket cards.
 * Mainly just so we don't have to deal with an array of 2 cards everywhere
 */

public class Pocket implements Comparable<Pocket> {
	private Card one;
	private Card two;
	
	public Pocket () {
		one = new Card();
		two = new Card();
	}
	
	public Pocket (Card one, Card two) {
		this.one = one;
		this.two = two;
	}
	
	public Pocket (String one, String two) {
		this.one = new Card(one);
		this.two = new Card(two);
	}
	
	public Pocket (String whole) {
		// allow a string to be sent as "RsRs" or "Rs Rs"
		if(whole.charAt(2) == ' ') {
			//found a space, lets remove it
			whole = "" + whole.charAt(0) + whole.charAt(1) + whole.charAt(3) + whole.charAt(4);
		}
		if(whole.length() != 4) {
			this.one = new Card();
			this.two = new Card();
		}
		else {
			String one = "" + whole.charAt(0) + whole.charAt(1);
			String two = "" + whole.charAt(2) + whole.charAt(3);
			this.one = new Card(one);
			this.two = new Card(two);
		}
	}
	
	/**
	 * Returns one of the cards: 0 or 1
	 * @param which 0 or 1
	 * @return Card class
	 */
	public Card getCard (int which) {
		if (which == 0) return one;
		else return two;   // possible bug here: Note that which card to return is a true/false choice
	}
	
	public Card[] getCards() {
		return new Card[] {one,two};
	}
	
	public int getNum() {
		// returns a unique int representing the cards in this hand, mostly used for compare to.
		int num; //num to return
		if (one.compareTo(two)>0) {
			num = one.getNum()*100;
			num += two.getNum();
		} else {
			num = two.getNum()*100;
			num += one.getNum();
		}
		return num;
	}

	@Override
	public String toString() {
		String p;
		p = one.getCardStr() + two.getCardStr();
		return p;
	}
	
	public int compareTo(Pocket c) {
		return getNum() - c.getNum();
	}
	
	/*
	 * This is used for the preflop odds matrix. It determines which matchup these pockets are
	 * offrank cards: 14 matchups
	 * one rank same: 10 matchups
	 * pair vs offrank: 6 matchups
	 * pair vs pair: 6 matchups
	 
	public int compareMatchupTo(Pocket c) {
		if (one.getRankVal() == two.getRankVal() && c.getCard(0).getRankVal() == c.getCard(1).getRankVal()) { //pair vs pair
			// for pair vs pair, it doesn't matter which pocket is bigger
			// can't brute force all this, must be a better way
			if (one.getSuitVal() == two.getSuitVal() && c.getCard(0).getSuitVal() == c.getCard(1).getSuitVal() && c.getCard(0).getSuitVal() == one.getSuitVal()) return 0;
			if ()
		}
	}*/
	
	public boolean equals(Object cObj) {
		Pocket c = (Pocket) cObj;
		return getNum() == c.getNum();
	}
	
	@Override
	public int hashCode() {
		Integer i = new Integer(getNum());
		return i.hashCode(); // ehh could prob just return getNum()?
	}
	
	/* Attempt at utility methods for preflop matrix
	 * 
	 */
	public Card getHighCard() {
		if (one.getNum() > two.getNum()) return one;
		else return two;
	}
	
	public Card getLowCard() {
		if (one.getNum() < two.getNum()) return one;
		else return two;
	}
	
	/**
	 * Used to determine if the pocket is suited and if it is a certain suit. Passing null to this method
	 * will return true if the pocket is suited, false if not. Passing a suit to this method will return
	 * true if suited to the passed suit.
	 * @param s Suit to check for suited
	 * @return boolean
	 */
	public boolean isSuited(Card.Suit s) {
		if (s == null) {
			if (one.suit == two.suit) return true;
			else return false;
		} else if (one.suit == s && two.suit==s) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Used to determine if this pocket has one or more of the passed suit. If null is passed, will return false;
	 * @param s Card.Suit
	 * @return boolean
	 */
	public boolean hasSuit(Card.Suit s) {
		if (one.suit==s || two.suit ==s) return true;
		else return false;
	}
	/**
	 * This method returns a string of the type of class this Pocket is. For example, if 
	 * Pocket has value of AcAs, it will return AA. If it is 9s5c, it will return 95o.
	 * Ts8s will return T8s
	 * @return String
	 */
	public String getTypeString() {
		Card high = null, low = null;
		if (one.getNum() < two.getNum()) {
			high = new Card(two);
			low = new Card(one);
		} else {
			high = new Card(one);
			low = new Card(two);
		} 
		
		String type = "";
		
		if (high.getRankVal() == low.getRankVal()) { //pair
			type = String.format("%c%c", high.getRank(), high.getRank());
		} else if (high.getSuit() == low.getSuit()) { //suited
			type = String.format("%c%cs", high.getRank(),low.getRank());
		} else { //offsuit
			type = String.format("%c%co",high.getRank(),low.getRank());
		}
		
		return type;
	}
	
	public String normalizeMatchup(Pocket p) {
		String highString, lowString; //used to normalize, at end combined into matchup
		//use temp Pockets to seperate high and low
		Pocket high; Pocket low;
		if (getNum() > p.getNum()) {high = this; low = p; }
		else if (getNum() < p.getNum()){ high=p; low = this; }
		else return "--"; //same exact hand
		
		//set high hand string
		highString = high.getHighCard().getRank() + "s"; //high card in high hand always normalized to spades
		if (high.getHighCard().getSuit() == high.getLowCard().getSuit()) highString += high.getLowCard().getRank() + "s";
		else highString += high.getLowCard().getRank() + "h";  //high hand is always either SS or SH
		
		//set low hand string
		//check against high hand high card, then high hand low card, if high card doesn't match, set to d, if low card doesn't match, set to c
		//high card
		if (low.getHighCard().getSuit() == high.getHighCard().getSuit()) 
			lowString = low.getHighCard().getRank() + "s";
		else if (low.getHighCard().getSuit() == high.getLowCard().getSuit())
			lowString = low.getHighCard().getRank() + "h";
		else lowString = low.getHighCard().getRank() + "d";
		
		//low card (must check if suited but doesn't match high hand)
		if (low.getLowCard().getSuit() == high.getHighCard().getSuit())
			lowString += low.getLowCard().getRank() + "s";
		else if (low.getLowCard().getSuit() == high.getLowCard().getSuit())
			lowString += low.getLowCard().getRank() + "h";
		else if (low.getLowCard().getSuit() == low.getHighCard().getSuit())
			lowString += low.getLowCard().getRank() + "d";
		else lowString += low.getLowCard().getRank() + "c"; // rainbow
		
		return highString + lowString;
	}
	
	public int normalizeMatchupBits(Pocket p) {
		int bits;
		//use temp Pockets to seperate high and low
		Pocket high; Pocket low;
		if (getNum() > p.getNum()) {high = this; low = p; }
		else if (getNum() < p.getNum()){ high=p; low = this; }
		else return 0; //same exact hand
		
		// spades=3, hearts=2, diamonds=1, clubs=0
		
		//set high hand string
		bits = ((high.getHighCard().getRankBit()*4 + 3) << 18); //high card in high hand always normalized to spades
		if (high.getHighCard().getSuit() == high.getLowCard().getSuit()) bits += ((high.getLowCard().getRankBit()*4 + 3) << 12); //if same as high card, normalize to spades
		else bits += ((high.getLowCard().getRankBit()*4 + 2) << 12);  //high hand is always either SS or SH
		//set low hand string
		//check against high hand high card, then high hand low card, if high card doesn't match, set to d, if low card doesn't match, set to c
		//high card
		if (low.getHighCard().getSuit() == high.getHighCard().getSuit()) 
			bits += ((low.getHighCard().getRankBit()*4 + 3) << 6);
		else if (low.getHighCard().getSuit() == high.getLowCard().getSuit())
			bits += ((low.getHighCard().getRankBit()*4 + 2) << 6);
		else bits += ((low.getHighCard().getRankBit()*4 + 1) << 6);
		//low card (must check if suited but doesn't match high hand)
		if (low.getLowCard().getSuit() == high.getHighCard().getSuit())
			bits += low.getLowCard().getRankBit()*4 + 3;
		else if (low.getLowCard().getSuit() == high.getLowCard().getSuit())
			bits += low.getLowCard().getRankBit()*4 + 2;
		else if (low.getLowCard().getSuit() == low.getHighCard().getSuit())
			bits += low.getLowCard().getRankBit()*4 + 1;
		else bits += low.getLowCard().getRankBit()*4 + 0; // rainbow
		return bits;
	}
}
