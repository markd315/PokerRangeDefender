package library;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ranges.Range.removeBoard;


/*
 * This class is meant to be a toolbox for reading holdem strings.
 * 
 * Pocket []pocketsToArray(string cardString)
 * 		This method receives a string with a set of cards in the following format:
 * 		AA, KdKh,22+,AKo,AKs
 * 		It will read the string and build an array of Pocket to be returned.
 * 		Must check for duplicates.
 * 
 * Card []cardsToArray(string cardString)
 * 
 * 		This method looks for a set cards in one of the following formats:
 * 		Ac,Ad,Kh
 * 		AcAdKh
 * 		It will build an array of Cards and return, also will not return a duplicate
 * 
 * TODO:
 * boolean checkDuplicatePocket(string cardString)
 * boolean checkDuplicateCard(string cardString) self-explanatory?
 * 
 * condensestring: figure out how to accept a string and condense is so QJo QTo will read QTo+
 * 
 */

public class HoldemStrings {
	
	public static Card[] cardsToArray(String cardString) {
		Card []cards;
		ArrayList<Card> cardsArray = cardsToArrayList(cardString);
		cards = new Card[cardsArray.size()];
		cards = cardsArray.toArray(cards);
		return cards;
	}
	
	public static ArrayList<Card> cardsToArrayList(String cardString) {
		/*
		 * We need to extract all two character sequences that match a card in order to create the Card array. We will ignore all other characters
		 * for example: AdKy,BdTh5c6d
		 * Should return Cards with values of Ad Th 5c 6d
		 * regex
		 */
		final String CARD_PATTERN = "[akqjtAKQJT98765432][cdhsCDHS]";
		
		ArrayList<Card> cards = new ArrayList<Card>();
		
		Matcher matcher = Pattern.compile(CARD_PATTERN).matcher(cardString);
		
		while(matcher.find()) {
			cards.add(new Card(matcher.group()));
		}
		
		return cards;
	}
	
	/**
	 * Takes a string and extracts all cards in the format of Ad Th etc. All unknown characters are not returned.
	 * @param cardString
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> extractIndividualCards (String cardString) {
		ArrayList<Card> cards = cardsToArrayList(cardString);
		ArrayList<String> cardsStringArrayList = new ArrayList<String>();
		for (Card c: cards) {
			cardsStringArrayList.add(c.getCardStr());
		}
		
		return cardsStringArrayList;
	}
	
	/** TODO:
	 * This is meant to be used with a TextField to make sure there are no bad characters.
	 * @param cardString
	 * @return int the location in the string where a bad character was found
	 */
	public static int checkCardsSinglesString(String cardString) {
		return 0;
	}
	
	
	public static Pocket[] pocketsToArray(String cardString) {
		Pocket []pocket;
		ArrayList<Pocket> handsArray = pocketsToArrayList(cardString);
		pocket = new Pocket[handsArray.size()];
		pocket = handsArray.toArray(pocket);
		return pocket;
	}

    public static Pocket[] pocketsToArray(String s, Card[] board) {
        Pocket[] temp = pocketsToArray(s);
        Set<Pocket> tmp = new HashSet<>(Arrays.asList(temp));
        Set<Card> bard = new HashSet<>(Arrays.asList(board));
        return removeBoard(tmp, bard).toArray(new Pocket[0]);
    }
	
	public static ArrayList<Pocket> pocketsToArrayList(String cardString) {
		/*
		 * To avoid duplicates, it's probably best to use a HashSet and update the Pocket class to comparable.
		 * Then for ease of portability, just write the elements to a normal array list to be returned.
		 * or could just return an iterator?
		 */
		HashSet<Pocket> handsHash = new HashSet<Pocket>();
		ArrayList<Pocket> handsArray = new ArrayList<Pocket>();
		
		/*
		 *  First remove all spaces from cardString
		 */
		cardString = cardString.replaceAll("\\s+","");
		
		/*
		 * Now split at commas
		 * Format of the hands should be recognizable by last character and size of string
		 * 4 chars with +  indicates QTo+ -- all hands QTo or better.
		 * 4 chars without + indicates specific ard KhKd
		 * 3 chars without + indicates a type of hand:  QTo or QTs
		 * 3 chars with + indicates a pair or better pair: 22+
		 * 2 chars indicates a type of pair: AA
		 * 
		 */
		
		String []handStringArray = cardString.split(",");
		
		for (String hand : handStringArray ) {
				if (hand.length() == 4 && hand.charAt(3)=='+') {  // QTo+
					handsHash.addAll(range(hand));
				} else if (hand.length() == 4) { // KhKd
					handsHash.addAll(specific(hand));
				} else if (hand.length() == 3 && hand.charAt(2)=='+') { //22+
					handsHash.addAll(pairplus(hand));
				} else if (hand.length()==3) { // QTo or QTs
					handsHash.addAll(type(hand));
				} else if (hand.length() == 2) {
					handsHash.addAll(pair(hand));
				} else continue;
				
				
		}
		
		handsArray.addAll(handsHash);
		return handsArray;
	}
	
	/*
	 * helper classes, return collections of Cards based on a specific text.
	 * comments kinda spread through different methods as I didn't work on them in order
	 */
	
	private static HashSet<Pocket> range(String hand) { // expects QTo+ or QTs+
		HashSet<Pocket> hands = new HashSet<Pocket>();
		
		if (hand.length() != 4 || !verifyCard(hand.charAt(0)) || !verifyCard(hand.charAt(1)) || hand.charAt(3) != '+' || (hand.charAt(2) != 's' && hand.charAt(2)!= 'o')  ) {
			return hands;
		}
		
		/*
		 * The first rank always stays the same. the second rank will increase up to itself. excluding the pair
		 * (QT is only QT and QJ) 
		 */
		Card.Rank highCard = Card.Rank.TWO;
		Card.Rank lowCard = Card.Rank.TWO;
		
		lowCard = lowCard.setByChar(hand.charAt(1));
		highCard = highCard.setByChar(hand.charAt(0));
		
		if (highCard.compareTo(lowCard) < 0) {  //hmm, somehow second rank is bigger, dummy user messing with input
			Card.Rank placeholder = lowCard;
			lowCard = highCard;
			highCard = placeholder;
		}
		
		while (lowCard != highCard) {
			
			hands.addAll(type(String.format("%c%c%c", highCard.getChar(),lowCard.getChar(),hand.charAt(2))));
					
			
			if ( (lowCard = lowCard.next()) == null  ) break;  // increment lowcard, break if hit top of enum (should not happen unless low card got here somehow as higher)
		}
		
		return hands;
	}
	
	private static HashSet<Pocket> type(String hand) { // expects QTo or QTs
		HashSet<Pocket> hands = new HashSet<Pocket>();
		
		if (hand.length() != 3 || !verifyCard(hand.charAt(0)) || !verifyCard(hand.charAt(1)) || hand.charAt(0) == hand.charAt(1) ) { // should never happen, but if we ever find errors, we just return an empty hashset. User can figure out why
			return hands;
		}
		
		if (Character.toLowerCase(hand.charAt(2)) == 's') {  // suited is only 4 pockets 
			for (Card.Suit s : Card.Suit.values()) {
				hands.add(new Pocket(String.format("%c%c%c%c",hand.charAt(0),s.getChar(),hand.charAt(1),s.getChar())));
			}
			
		} else if (Character.toLowerCase(hand.charAt(2)) == 'o' ) {  // offsuit has 12 pockets so better to loop
			
			for (Card.Suit s1 : Card.Suit.values()) {
				for (Card.Suit s2 : Card.Suit.values()){
					if (s1 == s2) continue; //not suited
					hands.add(new Pocket(String.format("%c%c%c%c", hand.charAt(0),s1.getChar(), hand.charAt(1), s2.getChar())));
					
					
				}
			}
			
		}  //  no else needed, if last char was not 's' or 'o' just returns empty array
		
		return hands;
		
	}
	
	private static HashSet<Pocket> pairplus(String hand) { // expects 22+
		HashSet<Pocket> hands = new HashSet<Pocket>();
		
		if (hand.length() != 3 || hand.charAt(2) != '+' || !verifyCard(hand.charAt(0)) || !verifyCard(hand.charAt(1)) || Character.toLowerCase(hand.charAt(0)) != Character.toLowerCase(hand.charAt(1))){
			return hands;
		}
		// always pair so we only have to work with one char
		
		Card.Rank piter = Card.Rank.TWO;
		piter = piter.setByChar(hand.charAt(0));
		
		do {
			hands.addAll(pair(String.format("%c%c", piter.getChar(), piter.getChar())));
		} while ( (piter = piter.next()) != null);
		
		return hands;
			
	}
	
	private static HashSet<Pocket> pair(String hand) { // expects 22
		HashSet<Pocket> hands = new HashSet<Pocket>();
		
		if (hand.length() != 2 || !verifyCard(hand.charAt(0)) || !verifyCard(hand.charAt(1))) {
			return hands;
		}
		if (Character.toUpperCase(hand.charAt(0)) != Character.toUpperCase(hand.charAt(1))) {
			return hands; //not same 
		}
		/*
		 * kind of a weird one, can't iterate through like two ints, starting after last one?
		 * only 6 hands and the combo shouldn't be repeated, for example, 6c6s is same as 6s6c
		 * oh wait, using a hashset anyway...
		 * good way to check if Pocket comparable is up to speed  TEST
		 */
		
		for (Card.Suit s1 : Card.Suit.values()) {
			for (Card.Suit s2 : Card.Suit.values()) {
				if (s1 == s2) continue;
				hands.add(new Pocket(String.format("%c%c%c%c", hand.charAt(0),s1.getChar(),hand.charAt(1),s2.getChar())));
				
			}
		}
		
		return hands;
		
	}
	
	private static HashSet<Pocket> specific(String hand) { // expects KhKd
		HashSet<Pocket> hands = new HashSet<Pocket>();
		
		if(hand.length() != 4 || !verifyCard(hand.charAt(0)) || !verifyCard(hand.charAt(2)) || !verifyCardSuit(hand.charAt(1)) || !verifyCardSuit(hand.charAt(3))   ) { 
			return hands;
		}
		
		hands.add(new Pocket(hand));
		
		return hands;
	}
	
	public static boolean verifyCard(char c) { 
		/* Used as a quick check the the character in our string is an actual card (2-9) (T-A)
		 *  
		 */
		switch (Character.toLowerCase(c)){
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 't':
		case 'j': 
		case 'q':
		case 'k':
		case 'a': return true;
		default: return false;
		}
		
	}
	
	public static boolean verifyCardSuit(char c) {
		switch (Character.toLowerCase(c)) {
		case 'd':
		case 'c':
		case 's':
		case 'h': return true;
		default: return false;
		
		}
	}
	
	/**
	 * Receives a cardString and adds a single type or range to it, then runs it through the condenser and returns a new cardString.
	 * Note that this method does not check for format. Improperly formatted types will be ignored.
	 * @param cardString
	 * @return cardString
	 */
	public static String addTypetoString(String cardString, String newType) {

		cardString = cardString.concat(String.format(",%s", newType));
		return condenseCardString(cardString);
	}
	

	/**
	 * This method will remove a single type from a cardString and return a new cardString.
	 * The type removed can be single or part of a range.
	 * @param cardString
	 * @param removeType
	 * @return a new condensed cardString
	 */
	public static String removeTypeFromString(String cardString, String removeType) {
		ArrayList<String> types = extractIndividualHandTypes(cardString);
		ArrayList<String> specifics = extractIndividualSpecifics(cardString);
		if (types.contains(removeType)) types.remove(removeType);
		return condenseCardString(mergeTypesSpecificsArrayLists(types,specifics));
	}
	
	/**
	 * This method takes a cardString and condenses it, removing any bad hands and redundant hands and outputs
	 * a new card string.
	 * @param cardString
	 * @return
	 */
	public static String condenseCardString(String cardString) {
		ArrayList<String> types;
		ArrayList<String> specifics;
		ArrayList<String> foundRanges = new ArrayList<String>();
		String found = new String(); //used inside the loops

		//First, normalize the capitalization
		cardString = normalizeCaps(cardString);
		
		//First, we need expand everything through the extract methods
		
		types = extractIndividualHandTypes(cardString);
		specifics = extractIndividualSpecifics(cardString);
		removeUnnecessarySpecifics(types,specifics);
		
		//Rank enum array for backwards iteration
		Card.Rank[] ranks = Card.Rank.values();
		
		
		//pair loop, we loop so long as we found it, if we miss one, no reason to go further
		boolean writeFound = false; //vomit, can't figure out another way?
		for (int r = ranks.length-1; r >=0 && types.contains(String.format("%c%c", ranks[r].getChar(),ranks[r].getChar())); r--) {
			//remove it from the array (unless only on ace)
			if (r == ranks.length-2) { //found aces and kings
				types.remove("AA"); //fine, don't think a deck will change
				types.remove("KK");
				found = new String("KK+");
				writeFound = true;
			} else if (r!=ranks.length-1) {
				types.remove(String.format("%c%c", ranks[r].getChar(),ranks[r].getChar()));
				found = String.format("%c%c+", ranks[r].getChar(),ranks[r].getChar());
				writeFound = true;
			}
		}
		
		if (writeFound) foundRanges.add(found);
		
		//unpaired main loop, first loop, we just go down the ranks, inside loops for suited and unsuited
		
		for (int topr = ranks.length-1, botr=0; topr >= 0;topr--) {
			
			//suited - duplicate code but will keep it together in the string easy
			writeFound = false;
			for (botr = topr-1;botr>=0 && 
					types.contains(String.format("%c%cs", ranks[topr].getChar(),ranks[botr].getChar()));
					botr--) {
				//remove it only if found two
				if (botr == topr-2) { // found two
					types.remove(String.format("%c%cs", ranks[topr].getChar(),ranks[topr-1].getChar()));
					types.remove(String.format("%c%cs", ranks[topr].getChar(),ranks[topr-2].getChar()));
					found = String.format("%c%cs+", ranks[topr].getChar(),ranks[topr-2].getChar());
					writeFound = true;
				} else if (botr!=topr-1) { //remove it and update found only if not first one
					types.remove(String.format("%c%cs", ranks[topr].getChar(),ranks[botr].getChar()));
					found = String.format("%c%cs+", ranks[topr].getChar(),ranks[botr].getChar());
					writeFound = true;
				}
			}
			if (writeFound) foundRanges.add(found);
			
			//unsuited
			writeFound = false;
			for (botr = topr-1;botr>=0 && 
					types.contains(String.format("%c%co", ranks[topr].getChar(),ranks[botr].getChar()));
					botr--) {
				//remove it only if found two
				if (botr == topr-2) { // found two
					types.remove(String.format("%c%co", ranks[topr].getChar(),ranks[topr-1].getChar()));
					types.remove(String.format("%c%co", ranks[topr].getChar(),ranks[topr-2].getChar()));
					found = String.format("%c%co+", ranks[topr].getChar(),ranks[topr-2].getChar());
					writeFound = true;
				} else if (botr!=topr-1) { //remove it and update found only if not first one
					types.remove(String.format("%c%co", ranks[topr].getChar(),ranks[botr].getChar()));
					found = String.format("%c%co+", ranks[topr].getChar(),ranks[botr].getChar());
					writeFound = true;
				}
			}
			
			if (writeFound) foundRanges.add(found);
		}

		//build new card string from found ranges and specifics
		//if we didn't find anything, just passthrough cardString so we don't erase user's writing
		if (foundRanges.size()>0 || types.size()>0 || specifics.size()>0) {
		cardString = "";
		for (String fr: foundRanges) {
			cardString += fr + ",";
		}
		for (String ty: types) {
			cardString += ty + ",";
		}
		for (String sp: specifics) {
			cardString += sp + ",";
		}
		cardString = cardString.substring(0, cardString.length()-1); //remove last comma
		}
		
		return cardString;
	}
	
	/**
	 * This will take the comma-delimited arraylist and extract only individual hand types:
	 * QJo, AA, AKs, etc
	 * It will also expand any + ranges such as 88+ or AJo+
	 * 
	 * Any specific hands and bad formats are not returned. This is mainly (solely?) used by GUI keyboard.
	 * 
	 * @param cardString
	 * @return ArrayList<String> containing each hand type
	 */
	public static ArrayList<String> extractIndividualHandTypes(String cardString) {
		HashSet<String> types = new HashSet<String>();

		//see pocketstoarraylist() for explanation
		cardString = cardString.replaceAll("\\s+","");
		String []handStringArray = cardString.split(",");
		
		for (String s: handStringArray) {
			if (s.length()==4 && s.charAt(3)=='+') { // AJo+
				try {types.addAll(expandRange(s));  //since it's a range, pass it to expandRange, ignore exception
				} catch (IllegalArgumentException e) { System.out.print(e.getMessage()); }
			} else if (s.length()==3 && s.charAt(2)=='+') { //pair range
				try {types.addAll(expandRange(s));
				} catch (IllegalArgumentException e) { }
			} else if (s.length() == 3 && verifyCard(s.charAt(0)) && verifyCard(s.charAt(1)) && ((Character.toLowerCase(s.charAt(2))=='o') || (Character.toLowerCase(s.charAt(2))=='s'))) { //should be a specific type
				types.add(s);
			} else if (s.length() == 2 && verifyCard(s.charAt(0)) && verifyCard(s.charAt(1))) {
				types.add(s);
			}
		}

		return new ArrayList<String>(types);
	}
	
	/**
	 * This method searches the comma-delimited cardString for any specific hands in the format of Ac9c. It
	 * returns all found hands as an ArrayList. It does not return any hand types of hand ranges.
	 * 
	 * @param cardString
	 * @return ArrayList<String> containing each found specific
	 */
	public static ArrayList<String> extractIndividualSpecifics(String cardString) {
		HashSet<String> types = new HashSet<String>();

		//see pocketstoarraylist() for explanation
		cardString = cardString.replaceAll("\\s+","");
		String []handStringArray = cardString.split(",");
		
		for (String s: handStringArray) {
			if (s.length() == 4 && verifyCard(s.charAt(0)) && verifyCard(s.charAt(2)) && verifyCardSuit(s.charAt(1)) && verifyCardSuit(s.charAt(3))) {
				//Looks like we have a properly formatted specific. Lets make sure it's not duplicate cards and that high card is first
					Card first = new Card(String.format("%c%c", s.charAt(0), s.charAt(1)));
					Card second = new Card(String.format("%c%c", s.charAt(2),s.charAt(3)));
					if (first.getNum() > second.getNum()) {
						types.add(s);
					} else if (first.getNum()!=second.getNum()) { //do nothing if same card 
						types.add(String.format("%s%s", second.getCardStr(),first.getCardStr()));
					}
			}
		}

		return new ArrayList<String>(types);
	}
	
	/**
	 * This accepts one range such as 88+ and returns all the individual types that make up the range
	 * @param rangeString Expects a 3 or 4 character string consisting of either 88+, ATo+, or ATs+
	 * @return ArrayList<String> each individual hand type
	 */
	private static ArrayList<String> expandRange(String rangeString) {
		if (rangeString.length() != 3 && rangeString.length() != 4) {
			throw new IllegalArgumentException(String.format("method: expandRange() must receive String of length 3 or 4. Received: %s",rangeString));
		} else if (rangeString.charAt(rangeString.length()-1) != '+') {
			throw new IllegalArgumentException(String.format("method: expandRange() must receive String ending in +. Received %s",rangeString));
		} else if (!verifyCard(rangeString.charAt(0)) || (!verifyCard(rangeString.charAt(1)))) {
			throw new IllegalArgumentException(String.format("method: expandRange() must receive format of ATs+, ATo+, or 99+. Received: %s",rangeString));
		} 
		
		ArrayList<String> range = new ArrayList<String>();
		Card.Rank rank=Card.Rank.ACE, top=Card.Rank.ACE; //used for iteration
		
		if (rangeString.length()==3) { //pair
			if (Character.toLowerCase(rangeString.charAt(0)) != Character.toLowerCase(rangeString.charAt(1))) throw new IllegalArgumentException(String.format("method: expandRange(), invalid format: %s",rangeString));

			rank = rank.setByChar(rangeString.charAt(0));  //set here so we get everything above
			do {  //loop to top
				range.add(String.format("%c%c", rank.getChar(), rank.getChar()));
			} while ((rank = rank.next())!=null);
		} else { //suited or unsuited... this always ranges up only to the first card.  96o+ only goes to 98o+
			rank = rank.setByChar(rangeString.charAt(1));
			top = top.setByChar(rangeString.charAt(0));
			if (rank.getValue()>=top.getValue()) throw new IllegalArgumentException(String.format("method: expandRange: invalid format1: %s", rangeString));
			if (Character.toLowerCase(rangeString.charAt(2)) != 'o' && Character.toLowerCase(rangeString.charAt(2)) != 's') throw new IllegalArgumentException(String.format("method: expandRange: invalid format2: %s", rangeString));
			do {
				range.add(String.format("%c%c%c",top.getChar(),rank.getChar(),rangeString.charAt(2)));
			} while (((rank = rank.next())!=null) && (rank.getValue() < top.getValue()));
			
		}
		
		return range;
		
	}
	
	/**
	 * Takes an ArrayList containing hand types and an ArrayList containing specifics. These will likely be
	 * created with methods extractIndividualHandTypes and extractIndividualSpecifics. If it finds a specific
	 * to be covered by a type, it will remove it from the specifics ArrayList.
	 */
	public static void removeUnnecessarySpecifics(ArrayList<String> types, ArrayList<String> specifics) {
		
		for (int c = 0; c < specifics.size(); c++) {
			Pocket p = new Pocket(specifics.get(c));
			if (types.contains(p.getTypeString())) {
				specifics.remove(c);
				c--;
			}
		}
	}
	
	/**
	 * Just makes sure Ranks are caps and Suites are lower
	 * TODO: normalize type format... so JQo gets changed to QJo
	 * @param cardString
	 * @return
	 */
	public static String normalizeCaps(String cardString) {
		char[] rankLower = {'t','j','q','k','a'};
		char[] rankUpper = {'T','J','Q','K','A'};
		char[] suitLower = {'c','h','d','s'};
		char[] suitUpper = {'C','H','D','S'};
		
		String replace, with;
		
		for (int c = 0; c < rankLower.length; c++) {
			replace = new String(Character.toString(rankLower[c]));
			with = new String(Character.toString(rankUpper[c]));
			cardString = cardString.replaceAll(replace, with);
		}
		
		for (int c =  0; c < suitLower.length; c++) {
			replace = new String(Character.toString(suitUpper[c]));
			with = new String(Character.toString(suitLower[c]));
			cardString = cardString.replaceAll(replace, with);
		}
		
		return cardString;
		}
	

	/**
	 * Takes the arraylist of types and specifics and merges them into a string.
	 * Normally this string would then be run through condenseCardString()
	 * @param types
	 * @param specifics
	 * @return
	 */
	private static String mergeTypesSpecificsArrayLists(ArrayList<String> types, ArrayList<String> specifics) {
		String cardString = "";
		for (String t: types) {
			cardString += t + ",";
		}
		for (String s: specifics) {
			cardString += s + ",";
		}
		
		if (cardString.length()>0) 
			cardString = cardString.substring(0, cardString.length()-1); // remove last comma
		return cardString;
	}
}
