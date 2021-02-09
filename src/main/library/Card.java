package library;
/*
 * need to put in compareto and 
 * this object will mostly be created within Deck.java
 */
public class Card implements Comparable<Card> {

	/* these will be accessed directly by all functions */
	public Rank rank;  
	public Suit suit;
	
	
	private long rankMap;
	private int singleRanksMap;
	/**
	 * This sets variables used to make quick gets, mainly for HandValue.java
	 * Must always be called any time the rank or suit is set.
	 */
	private void setSpeedVars() {
			rankMap = (1L << (getRankBit()*3));
			singleRanksMap = (1 << getRankBit());
	}

	public enum Suit {
		/* enum for Suit. Outside of this class, can be accessed Card.Suit.S
		 * Lets not have to spell out full rank everytime...
		 * Using byte instead of int probably won't matter
		 */
		C(0),
		D(1),
		H(2),
		S(3) {
			@Override
			public Suit next() {
				return C;
			}
		};
		public int value;

		public Suit next() {
			return values()[ordinal() +1];
		}
		
		private Suit (int value) {
			this.value = value;
		}
		
		int getValue() {
			return (int) value;
		}
		
		public static Suit not(Card.Suit s) {
			if (s == C) return D;
			else return C;
		}
		char getChar() {
			char c = ' ';
		
			switch(value)
			{
			case 0:c = 'c';break;
			case 1:c = 'd';break;
			case 2:c = 'h';break;
			case 3:c = 's';break;
			}
			
			return c;
		}
		
		public Suit setByChar(char s) {
			/*
			 * could use valueof instead for this one, but making this function
			 * to match Rank
			 */
			switch(Character.toLowerCase(s)){
			case 'c': return C;
			case 'd': return D;
			case 'h': return H;
			case 's': return S;
			}
			
			return S;
		}
	}
	
	public enum Rank {
		/*
		 *  enum for Rank.
		 */
		TWO(2),
		THREE(3),
		FOUR(4),
		FIVE(5),
		SIX(6),
		SEVEN(7),
		EIGHT(8),
		NINE(9),
		TEN(10),
		JACK(11),
		QUEEN(12),
		KING(13),
		ACE(14) {
			@Override
			public Rank next() {
				return null;
			}
		};
		
		
		public int value;
		
		public Rank next() {
			return values()[ordinal() +1];
		}
		
		Rank(int value) {
			this.value = value;
		}
		
		int getValue() {
			return (int) value;
		}
		
		public Rank setByValue(int r) {
			switch(r) {
			case 2: return TWO;
			case 3: return THREE;
			case 4: return FOUR;
			case 5: return FIVE;
			case 6: return SIX;
			case 7: return SEVEN;
			case 8: return EIGHT;
			case 9: return NINE;
			case 10: return TEN;
			case 11: return JACK;
			case 12: return QUEEN;
			case 13: return KING;
			case 14: return ACE;
			default: throw new IllegalArgumentException("Card.Rank Enum setByValue");
			}
		}
		public Rank setByChar(char r) {
			/*
			 *  mostly for use by HoldemStrings
			 *  returns enum based on character passed
			 */
			switch(Character.toLowerCase(r)){
			case '2': return TWO;
			case '3': return THREE;
			case '4': return FOUR;
			case '5': return FIVE;
			case '6': return SIX;
			case '7': return SEVEN;
			case '8': return EIGHT;
			case '9': return NINE;
			case 't': return TEN;
			case 'j': return JACK;
			case 'q': return QUEEN;
			case 'k': return KING;
			}
			return ACE;
		}
		
		
		public char getChar() {
			char c = ' ';
			switch (value) {
			case 2: c = '2';break;
			case 3: c = '3';break;
			case 4: c = '4';break;
			case 5: c = '5';break;
			case 6: c = '6';break;
			case 7: c = '7';break;
			case 8: c = '8';break;
			case 9: c = '9';break;
			case 10: c = 'T';break;
			case 11: c = 'J';break;
			case 12: c = 'Q';break;
			case 13: c = 'K';break;
			case 14: c = 'A';break;
			}
			return c;
		}
	}
	

	public Card () {  // this should probably throw an exception actually... but for now we just get to be the ace of spades
		rank = Rank.ACE;
		suit = Suit.S;
		setSpeedVars();
	}
	
	public Card (String newcard) {
		if (newcard.length() != 2) {
			rank = Rank.ACE; suit = Suit.S;
		}
		switch(Character.toUpperCase(newcard.charAt(0))){
		case '2': rank = Rank.TWO; break;
		case '3': rank = Rank.THREE; break;
		case '4': rank = Rank.FOUR; break;
		case '5': rank = Rank.FIVE; break;
		case '6': rank = Rank.SIX; break;
		case '7': rank = Rank.SEVEN; break;
		case '8': rank = Rank.EIGHT; break;
		case '9': rank = Rank.NINE; break;
		case 'T': rank = Rank.TEN; break;
		case 'J': rank = Rank.JACK; break;
		case 'Q': rank = Rank.QUEEN; break;
		case 'K': rank = Rank.KING; break;
		case 'A': rank = Rank.ACE; break;
		default: throw new IllegalArgumentException(String.format("Error Creating card. Received Rank %c",newcard.charAt(0)));
		}
		switch(Character.toUpperCase(newcard.charAt(1))){
		case 'C': suit = Suit.C; break;
		case 'D': suit = Suit.D; break;
		case 'H': suit = Suit.H; break;
		case 'S': suit = Suit.S; break;
		default: throw new IllegalArgumentException(String.format("Error Creating card. Received Suit %c",newcard.charAt(1)));
		}
		setSpeedVars();
		}
	
	public Card(Card card) {
		this(card.getNum());
	}
	public Card(Rank rank, Suit suit) {
		/*
		 * pass like this?  Card(Card.Rank.TWO,Card.Rank.H);  
		 */
		this.rank = rank;
		this.suit = suit;
		setSpeedVars();
	}
	
	public Card(int num) { //accepts 0-51
		if (num>51 || num < 0) {
			setRankByInt(12);
			setSuitByInt(3);
		} else {
		setRankByInt(num/4);
		setSuitByInt(num%4);
		setSpeedVars(); }
	}

	void setRankByInt( int c) { //accepts 0-12
		switch (c) {
		case 0: rank = Card.Rank.TWO; break;
		case 1: rank = Card.Rank.THREE; break;
		case 2: rank = Card.Rank.FOUR; break;
		case 3: rank = Card.Rank.FIVE; break;
		case 4: rank = Card.Rank.SIX; break;
		case 5: rank = Card.Rank.SEVEN; break;
		case 6: rank = Card.Rank.EIGHT; break;
		case 7: rank = Card.Rank.NINE; break;
		case 8: rank = Card.Rank.TEN; break;
		case 9: rank = Card.Rank.JACK; break;
		case 10: rank = Card.Rank.QUEEN; break;
		case 11: rank = Card.Rank.KING; break;
		case 12: rank = Card.Rank.ACE; break;
		}
		setSpeedVars();
	}
	
	void setSuitByInt( int c) { //accepts 0-3
		switch (c) {
		case 0: suit = Card.Suit.C; break;
		case 1: suit = Card.Suit.D; break;
		case 2: suit = Card.Suit.H; break;
		case 3: suit = Card.Suit.S; break;
		default: suit = Card.Suit.S; System.out.println(c); break;
		}
		setSpeedVars();
	}
	
	void setRankByChar( char c ) {
		switch(Character.toUpperCase(c)) {
		case '2': rank = Card.Rank.TWO; break;
		case '3': rank = Card.Rank.THREE; break;
		case '4': rank = Card.Rank.FOUR; break;
		case '5': rank = Card.Rank.FIVE; break;
		case '6': rank = Card.Rank.SIX; break;
		case '7': rank = Card.Rank.SEVEN; break;
		case '8': rank = Card.Rank.EIGHT; break;
		case '9': rank = Card.Rank.NINE; break;
		case 'T': rank = Card.Rank.TEN; break;
		case 'J': rank = Card.Rank.JACK; break;
		case 'Q': rank = Card.Rank.QUEEN; break;
		case 'K': rank = Card.Rank.KING; break;
		default:
		case 'A': rank = Card.Rank.ACE; break;
		}
		setSpeedVars();
	}
	
	void setSuitByChar ( char c ) {
		switch(Character.toUpperCase(c)) {
		case 'C': suit = Card.Suit.C; break;
		case 'D': suit = Card.Suit.D; break;
		case 'H': suit = Card.Suit.H; break;
		default:
		case 'S': suit = Card.Suit.S; break;
		}
		setSpeedVars();
	}
	
	boolean equalsRankByChar(char c){
		if (Character.toLowerCase(c) == rank.getChar()) {
			return true;
		} else return false;
	}
	
	boolean equalsSuitByChar(char c) {
		if (Character.toLowerCase(c) == suit.getChar()) {
			return true;
		} else return false;
	}
	

	boolean equalsStr(String stest) {
		// This function takes a two character string in format Rs and tests if the card here is equal to it
		if (stest.length()!= 2) return false;
		if (equalsRankByChar(stest.charAt(0)) && equalsSuitByChar(stest.charAt(1))) {
			return true;
		} else return false;
	}

	String getCardStr() {
		String s = "" + rank.getChar() + suit.getChar();
		return s;
		
	}
	
	public char getRank() {
		return rank.getChar();
	}
	
	char getSuit() {
		return suit.getChar();
	}
	
	int getRankVal() {
		return rank.getValue();
	}
	
	//returns the rank starting at 0 for deuce
	int getRankBit() {
		return rank.getValue()-2;
	}
	
	int getSuitVal() {
		return suit.getValue();
	}
	
	public int getNum() { // will return 0-51, aces being high
		int num =0;
		num += rank.getValue()*4 + suit.getValue();
		num -= 8;
		
		return num;
	}
	
	public long getRankMap() {
		return rankMap;
	}
	
	public int getSingleRanksMap() {
		return singleRanksMap;
	}
		
	public void print() {  // testing purposes only
		System.out.printf("%c%c",rank.getChar(),suit.getChar());
	}

	public int compareTo(Card c) {  // suit matters in this case
		return this.getNum() - c.getNum();
	}
	
	@Override
	public int hashCode() {
		return getNum();
	}
	
	public boolean equals(Object c) {
		Card a = (Card) c;
		return this.getNum() == a.getNum();
	}
	
	int rankvalIfAce() {
		return rank.getValue();
	}
	
	
}
