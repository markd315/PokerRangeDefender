package library;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

/* Copyright 2018 Nathan Dunn
 *     This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


/**
 * Holds the deck. Cards should always be removed from the deck first and the methods here used
 * to obtain the card object
 * 
 * Sorting the deck is unimportant... we don't card about the order. If we need to print it in order
 * for the user, we can sort it at that point.
 * 
 * Major overhaul: November 17, 2016
 * 
 * Restructuring ArrayList into HashMaps for fast retrieval. No emphasis will be put on deck modifications, but retrieving any
 * value should be as fast as possible.
 */
public class Deck {
	
	//nested HashMaps
	HashMap<Card.Rank,HashMap<Card.Suit,Card>> rankMap;
	
	HashMap<Card.Rank, Integer> numRankLeft; //For fast access when call to find out how much of a rank is left.
	
	
	public Deck () {
		setDeck();
	}
	
	public void setDeck() {
		// use Rank and Suit enum inside Card to set the deck
		numRankLeft = new HashMap<Card.Rank,Integer>();
		for (Card.Rank r: Card.Rank.values()) {
			numRankLeft.put(r, 4);
		}
		
		//nested maps
		rankMap = new HashMap<Card.Rank,HashMap<Card.Suit,Card>>();
		for (Card.Rank r: Card.Rank.values()) {
			rankMap.put(r, new HashMap<Card.Suit,Card>());
			for (Card.Suit s: Card.Suit.values()) {
				rankMap.get(r).put(s, new Card(r,s));
			}
		}
	}
	
	/**
	 * Returns the number of cards of a particular rank left in the deck.
	 * @param r Card.Rank enum
	 * @return int
	 */
	public int getNumRankLeft(Card.Rank r) {
		
		return numRankLeft.get(r);
	}
	
	/**
	 * Returns the number of cards of a particular rank left in the deck except suit of notSuit
	 * @param r rank to check
	 * @param notSuit suit to skip
	 * @return
	 */
	int getNumRankLeftNotSuit(Card.Rank r, Card.Suit notSuit) {
		if (rankMap.get(r).containsKey(notSuit))
			return rankMap.get(r).size()-1;
		else return rankMap.get(r).size();
	}
	int numCardsLeft() {
		int num=0;
		for (HashMap<Card.Suit,Card> s:rankMap.values()) {
			num+=s.size();
		}
		return num;
	}
	
	/**
	 * Chooses a random card and removes it from the deck.
	 * @return Card
	 */
	Card drawRandomCard() {
		if (rankMap.size() == 0)
			throw new IllegalArgumentException("Trying to draw from empty deck");
		ArrayList<Card> deckArray = getDeckArrayList();
		int draw = (int) (Math.random() * deckArray.size());
		Card c = deckArray.get(draw);
		reduceNumRankLeft(c.rank);
		return rankMap.get(c.rank).remove(c.suit);
	}
	
	/**
	 * Chooses a random card without removing it from the deck.
	 * Not optimized, could be slow. Don't use for sampling, draw the array and access it with random ints.
	 * @return Card
	 */
	Card pickRandomCard() {
		
		//not optimized, could be slow. 
		if (rankMap.size() == 0) 
			throw new IllegalArgumentException("Trying to pick from empty deck");
		ArrayList<Card> deckArray = getDeckArrayList();
		int draw = (int) (Math.random() * deckArray.size());
		Card c = deckArray.get(draw);
		reduceNumRankLeft(c.rank);
		return rankMap.get(c.rank).remove(c.suit);
	}
	
	/**
	 * picks specific card but doesn't remove from deck
	 * @param r rank
	 * @param s suit
	 * @return Card reference
	 */
	Card pickSpecificCard(Card.Rank r, Card.Suit s) {
		return rankMap.get(r).get(s);
	}
	
	/**
	 * draws specific card based on Rs format
	 * @param String Rs = Ranksuit such as Ah
	 * @return Card the Card
	 */

	Card drawSpecificCard(String newcard) {
		// draws a card based on Rs format
		// Not optimized: used drawSpecificCard(Rank,Suit)
		if(newcard.length() != 2) return new Card(); // return default
		
		Card c = new Card(newcard);
		
		if (rankMap.get(c.rank).get(c.suit)!=null) { //card exists
			reduceNumRankLeft(c.rank);
			return rankMap.get(c.rank).remove(c.suit);
		}
		
		// if we got here that is bad, and user gets default Card
		return new Card();
	}
	
	/**
	 * Removes a specific card object from the deck. Will match based on rank and suit.
	 * @param newCard Card object
	 * @return true if removed, false if card not in deck
	 */
	public boolean removeSpecificCard(Card newCard)  {
		if (rankMap.get(newCard.rank).get(newCard.suit)!=null) {
			reduceNumRankLeft(newCard.rank);
			rankMap.get(newCard.rank).remove(newCard.suit);
			return true;}
		else return false;
		
	}
	
	/**
	 * returns boolean if passed card is in the deck
	 * @param r Card.Rank
	 * @param s Card.Suit
	 * @return boolean
	 */
	public boolean containsCard(Card.Rank r, Card.Suit s) {
		if (rankMap.get(r).get(s) != null) return true;
		else return false;
	}
	
	Card[] getDeckArray() {
		// this method will be used for equity evaluation. It returns all the cards left in an array
		ArrayList<Card> deckArray = getDeckArrayList();
		Card[] d = new Card[deckArray.size()];
		deckArray.toArray(d);
		return d;
	}
	
	ArrayList<Card> getDeckArrayList() {
		ArrayList<Card> deckArray = new ArrayList<Card>();
		for (HashMap<Card.Suit,Card> s: rankMap.values()) {
			for (Card c : s.values()) {
				deckArray.add(c);
			}
		}
		
		return deckArray;
	}
	
	private void reduceNumRankLeft(Card.Rank r) {
		numRankLeft.put(r, numRankLeft.get(r)-1);
		if (numRankLeft.get(r)<0)
			throw new IllegalArgumentException("Mismatch in deck numRankLeft");

	}
	
	public void print () {
		//test
		for (Card.Rank r : Card.Rank.values()) {
			for (Card.Suit s: Card.Suit.values()) {
				rankMap.get(r).get(s).print();
			}
		}
		System.out.println();
	}
	
	
}
