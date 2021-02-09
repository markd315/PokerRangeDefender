package library;

/* Copyright 2018 Nathan Dunn
 * 
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  */

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Robust class should be designed to be easily portable.
 *  Main Method to call:
 *  	float []calcEnum(Pocket pockets[][], Card board[], Card dead[]);
 *  
 *   		Takes an array of possible pockets for multiple players as well as known board and dead cards
 *   		This function enumerates, and won't care how it long it takes
 *   		Returns an array of floats to show equities
 *   
 *   	float []calcSample(Pocket pockets[][], Card board[], Card dead[], long sample);
 *   		
 *   		Same as above except will determine equity based on a sample.  (TODO)
 *   
 *   	long numCalcsEnum(Pocket pockets[][], Card board[], Card dead[]);
 *   
 *   		Returns the number of calculations expected to have to make.  
 *   
 *   
 *   	EquityCalcObserver interface will couple this class with the class responsible for displaying values.
 *   		calcEnum mainly will use it to update progress. Required in constructor.  
 */
public class EquityCalc {
	
	private static final Card[] EMPTY_CARD_ARRAY = new Card[0]; // for use with overloaded methods
	private static final int HUGAMES = 1712304; 				// Number of enumerations when heads up preflop
	
	PreflopOddsMatrix preflopOddsMatrix = null;  	//only initialized by getFromMatrix(), no need to use memory if we don't need to
	boolean useMatrix = true; 						//for debugging purposes, false will force enumeration
	
	private Deck deck;   //only one deck will ever be used for calcs
	Card cardArray[]; 	 //built from deck for iteration
	
	EquityCalcObserver resultsObserver;
	/**
	 * This observer is the way all the functions will notify the user of the in-progress results. This is meant to be implemented by a GUI
	 */
	public EquityCalc (EquityCalcObserver o) {
		resultsObserver = o;
	}
	
	//time variables debug
	long startTime;
	long startEnumTime;
	long endTime;
	
	/**
	 * This method can be called by the GUI to inform the user how many calculations are expected and to 
	 * draw a progress bar and (possibly) to calculate the estimated time.
	 * 
	 * @param pockets  A range of possible pockets for a number of players
	 * @param board The cards on the board
	 * @param dead Any dead cards
	 * @return long The number of expected calculations in an enumeration
	 */
	public long numCalcsEnum(Pocket pockets[][], Card board[], Card dead[]) {
		/* Don't pass null if board or dead are empty.  Pass an empty array.
		 * Alternatively, there are overloaded functions numCalcsEnum(Pocket pockets[][]) ...
		 * 
		 */
		//TODO: must account for duplicate cards - done nov 18, not thoroughly tested
		double numCalcs=0;
		int deckSize = 52;
		deck = new Deck();
		
		// check for invalid parameters
		if (pockets == null || board == null || dead == null) { return 0;} 
		if (pockets.length < 2) { return 0; }
		
		//see calcEnum for more explanation on following algo
		iterator = new int[pockets.length];
		for (int c=0;c<iterator.length;c++) {
			iterator[c] = 0;  
		}
		
		//TODO: here... why is my head all messed up about this? clean this up.  [why? it works? It's a self-contained piece w/ 1 break statement lol]
		int c=0,cc=0;
		while (c>=0) {
			for (c = iterator.length-1;c>=0;c--) { 
				/*
				 * choosing hands, all must compare, this loop looks for a single iterator to increase. if an iterator is already at the end
				 * of the list of hands for a player, it goes to the next until it finds one it can increment.
				 */
				//do work here
				if (iterator[c]+1 == pockets[c].length) { 
					iterator[c] = 0;
					if (c==0) c--;
				} else {  
					iterator[c]++;
					break;
					}
			}
		
			deck.setDeck();
			//check for dups here, could speed this up a touch by not using a deck, but doesn't seem necessary
				for (Card b: board) {
					deck.removeSpecificCard(b);
			}
			for (Card d: dead) {
				deck.removeSpecificCard(d);
			}
			
			for (cc =0; cc<pockets.length;cc++) {
				if (!deck.removeSpecificCard(pockets[cc][iterator[cc]].getCard(0)) ||
				!deck.removeSpecificCard(pockets[cc][iterator[cc]].getCard(1))) { //cards not found, break
					break;
				}
			}
			if (cc==pockets.length) //found all cards in deck
				numCalcs++;
		}
		
		deckSize -= (board.length + dead.length + pockets.length*2);
		//System.out.println(numCalcs);
		if(board.length == 0) { //preflop
			numCalcs *= combinations(deckSize,5);
		}
		else if(board.length == 3) { //flop
			numCalcs *= combinations(deckSize,2);
		}
		else if(board.length == 4) { //turn
			numCalcs *= deckSize;
		}
		else return 1L; // river?  lol
		
		if (numCalcs > Long.MAX_VALUE) return Long.MAX_VALUE;
		else return (long) numCalcs;
		
	}
	
	/*
	 * These variables are used within the loops and between methods when calculating.
	 */

	long results[][];  		// [][0] = numWins, [][1] = numTies, [0][2] = numGames TODO: replace numWins,numTies,numGames with this
	long numWins[], numTies[],numGames;		//used to populate the results array at end
	long totalNumGames;  	//used to update observers
	int iterator[];			//each int in this array will hold a value corresponding to which pocket is used for current hand.
	int comboL;				//length of the combos in the enumerations
	int deckIterator[];		//for brute force enum
	int handValue[];		//used to termporarily hold hand values to determine bestValue
	int bestValue;			//used to hold which hand has bestValue in this enum
	
	Pocket pockets[][]; 	//these are passed to either calcEnum or calcSample and then set here
	Card board[];
	Card dead[];
	/**
	 * The main method of this class. The purpose of this method is to determine an equity percentage of a Holdem Hand or range versus another 
	 * Holdem Hand or range. For example, if a player has JJ and is unsure of his opponents holdings but is pretty sure it contains all pairs
	 * above 99, all AKs, and all AQs, the known JJ hand (JsJh) will be passed as pockets[0][0]. The opponents range will then be passed 
	 * into pockets[1][...] which would be a possible 68 total hands: 9h9d 9s9d.... AsAh.
	 * 
	 * The JJ hand would be tested against all the 68 hands (over 1.7 million games per matchup), and the final number of wins for each
	 * would be returned. It will also update the progress on the enumeration through the EquityCalcObserver interface.
	 * @param pockets[][] Pocket class that represents a preflop Holdem hand.
	 * @param board[] Card class for the cards on the board
	 * @param dead[] Any dead cards that should be excluded
	 * @return Each player's wins, ties, and the number of games For example: [0][0] wins [0][1] ties [0][2] number of games (number of games should be same for every player
	 */
    public double[] eqPercents(Pocket[][] pocketsP, Card[] boardB, Card[] deadD){
	    long[][] res = calcEnum(pocketsP, boardB, deadD);
	    double[] ret = new double[res.length];
	    for(int i = 0; i<res.length; i++){
	        long[] arr = res[i];
            ret[i] = (arr[0] + arr[1]*0.5) / arr[2] * 100.0;
        }
	    return ret;
    }

	long [][]calcEnum(Pocket pocketsP[][], Card boardB[], Card deadD[]) {
		//debug
		//set instance from arguments
		startTime = System.currentTimeMillis();
		pockets=pocketsP; board=boardB; dead=deadD;
		
		results = new long[pockets.length][3];  // updated at end based on numGames and numWins
		numWins = new long[pockets.length]; numTies = new long[pockets.length];
		numGames=0;
		deck = new Deck();
		totalNumGames = numCalcsEnum(pockets,board,dead); //totaNumGames used to update progress for observers

		if (pockets.length < 2) {
			return new long[0][0]; //not throwing exceptions in this class, callers need to be aware of what they are passing
		}
		/*
		 * TODO: handle stupid large calcs... or just don't worry about it (might at least need to check for when long isn't big enough)
		 */
		
		//each int in this array will hold a value corresponding to which pocket is used for current hand.
		iterator = new int[pockets.length];
		for (int c=0;c<iterator.length;c++) {
			iterator[c] = 0;  
		}
		
		for (int c = 0; c < results.length; c++) { // init arrays
			results[c][0]=0; results[c][1]=0; results[c][2]=0;
			numWins[c]=0;numTies[c]=0;
		}
		
		boolean done = false;  // if this is set to true, we break out 
		top:
		while (!done && !resultsObserver.checkStop()) {
			/* Break and Continues: in code to END while checking for duplicate cards
			 */
			for (int c = iterator.length-1;c>=0;c--) { 
				/*
				 * choosing hands, all must compare, this loop looks for a single iterator to increase. if an iterator is already at the end
				 * of the list of hands for a player, it goes to the next until it finds one it can increment, then breaks out of loop
				 */
				if (iterator[c]+1 == pockets[c].length) { 
					iterator[c] = 0;
					if (c == 0) done = true;   
				} else {  
					iterator[c]++; 		//put remove from deck here so we can pick next combo if has duplicates?
					break;
				}
			}
			
			/*
			 * done choosing hands, now it's time to loop through each hand and get its value, determine winner, update games, etc.
			 */
			//remove all cards from the deck (have to create new one for each hand combo
			deck.setDeck();
			for (int c = 0; c < pockets.length; c++) {
				if (!removeFromDeck(pockets[c][iterator[c]].getCards())) {
					continue top;  // if we found a duplicate card, it's ok, we just start main loop over and choose new combo
				}
			}
			
			if (!removeFromDeck(board) || !removeFromDeck(dead)) {
				continue;
			}
			
			//Matrix:
			//if we only have two hands, try to get results from the matrix
			if(pockets.length==2 && board.length==0 && dead.length==0) { //anytime we have only 2 pockets and no board or dead cards, the matrix should contain the numWins
				if(useMatrix==true && doHeadsupMatrix()) {					
					continue; //all done so continue top loop
				}
					
			}
			/* END: break and continue statements, done removing cards
			 * We  now have a set of pockets to evaluate and a deck to use
			 */
			//here we know which hands we are comparing
			
			if (board.length == 0) {  //if we are preflop, we loop looking for like boards
				doPreflopEnum();

			} else if(board.length == 5){ //River, just evaluate best hand no brute forcing
                handValue = new int[pockets.length];
                for(int i=0; i<pocketsP.length; i++){
                    int games = 0, wins = 0, ties = 0;
                    Pocket[] range = pocketsP[i], opponent;
                    if(pocketsP.length > 2){
                        throw new Error("multiway river!");
                    }
                    if(i==0){
                        opponent = pocketsP[1];
                    }else {
                        opponent = pocketsP[0];
                    }
                    for(Pocket p : range){
                        Card[] myHand = {p.getCard(0), p.getCard(1), board[0], board[1], board[2], board[3], board[4]};
                        int myHandValue = HandValue.getHandValue(myHand);
                        //Only solving rivers heads up for now..
                        for(Pocket o : opponent){
                            Card[] theirHand = {o.getCard(0), o.getCard(1), board[0], board[1], board[2], board[3], board[4]};
                            int theirHandValue = HandValue.getHandValue(theirHand);
                            games++;
                            if(myHandValue > theirHandValue){
                                wins++;
                            }
                            else if(myHandValue == theirHandValue){
                                ties++;
                            }
                        }
                    }
                    numWins[i] = wins;
                    numTies[i] = ties;
                    numGames = games;
                }
                // create a 3d array to return
                long[][] returnResults = new long[pockets.length][3];
                for (int c = 0; c<pockets.length;c++) {
                    returnResults[c][0] = numWins[c];
                    returnResults[c][1] = numTies[c];
                    returnResults[c][2] = numGames;
                }
                return returnResults;
             }
			    else{
				doBruteForceEnum();
			}
			
			//test code - debug	
			//System.out.printf("E:%s vs %s, %d %d\n", pockets[0][iterator[0]].getString(),pockets[1][iterator[1]].getString(),(numWins[0]-numWins0),(numWins[1]-numWins1));
		}  // end top loop
		
		/*
		 * TODO:
		 * check if numgames==0, which means we got duplicate cards most likely and should notify
		 */
		endTime = System.currentTimeMillis();
		//System.out.println("Time to start enum: " + (startEnumTime-startTime));
		//System.out.println("Time to do enum: " + (endTime - startEnumTime));
		if(numGames==0) {} 
		
		// TODO: change all these updateResults calls to be in a RunLater
		updateResults(numWins,numTies,numGames,totalNumGames,true);
		// create a 3d array to return
		long[][] returnResults = new long[pockets.length][3];
		for (int c = 0; c<pockets.length;c++) {
			returnResults[c][0] = numWins[c];
			returnResults[c][1] = numTies[c];
			returnResults[c][2] = numGames;
		}
		return returnResults;
	}
	
	
	
	
	
	
	Card.Rank rankValues[] = new Card.Rank[5];
	int[] rankCount = new int[5];
	Deck fullDeck = new Deck(); //used when flushes don't matter for single hand evals
	void doPreflopEnum() {
		handValue = new int[pockets.length];
		startEnumTime = System.currentTimeMillis();
		Card.Rank it[] = new Card.Rank[5]; //iterator array for Ranks
		int C=0,D=0;				//these variables are used to multiply combinations
		int c=0,cc=0,ccc=0,cccc=0; 	//our loop variable, often will need to check if we progressed whole loop
		int newGames = 0;	//set by the total possible number of each board type. As we check for flushes, those boards are subtracted from this
		boolean hasC = false, hasD = false, hasH = false, hasS = false; 	//used to determine if we need to check for 4 flushes
		boolean suitedC = false, suitedD = false, suitedH = false, suitedS = false; //used to determine if we need to check for 3 to the flush, can skip a ton if not
		Card boardCombo[] = new Card[5];  	//this is used to build a board to send to findbestvalue where using a switch is too unweildy (slower using array)
		for ( cc =0; cc< pockets.length; cc++ ) { //loop through pockets looking for suited pockets and single suits
			if (suitedC == false && pockets[cc][iterator[cc]].isSuited(Card.Suit.C)) {suitedC = true; hasC = true;}
			else if (suitedD == false && pockets[cc][iterator[cc]].isSuited(Card.Suit.D)) {suitedD = true; hasD = true;}
			else if (suitedH == false && pockets[cc][iterator[cc]].isSuited(Card.Suit.H)) {suitedH = true; hasH = true;}
			else if (suitedS == false && pockets[cc][iterator[cc]].isSuited(Card.Suit.S)) {suitedS = true; hasS = true;}
			if (hasC == false && pockets[cc][iterator[cc]].hasSuit(Card.Suit.C)) hasC = true;
			if (hasD == false && pockets[cc][iterator[cc]].hasSuit(Card.Suit.D)) hasD = true;
			if (hasH == false && pockets[cc][iterator[cc]].hasSuit(Card.Suit.H)) hasH = true;
			if (hasS == false && pockets[cc][iterator[cc]].hasSuit(Card.Suit.S)) hasS = true;
		}
		
		for (it[0] = Card.Rank.TWO; it[0]!=null && !resultsObserver.checkStop(); it[0] = it[0].next()) {
			for (it[1] = it[0];it[1] !=null;it[1] = it[1].next()) {
				for (it[2] = it[1]; it[2] !=null; it[2] = it[2].next()) {
					for (it[3] = it[2]; it[3] != null; it[3] = it[3].next()) {
						for (it[4] = it[3];it[4] != null; it[4] = it[4].next()) {
							if (it[0] == it[1] && it[1]==it[2] && it[2]==it[3] && it[3]==it[4]) continue; //5 of one rank
							fillRankCount(it);
							sortRankCount(); //this sets an easy to use int and enum array with the most cards in this combo in the first slot
							C=0;D=0;newGames=0;
							if (rankCount[4] > 0) { //XYZTU - 5,4,3
								newGames = deck.getNumRankLeft(rankValues[0]) * deck.getNumRankLeft(rankValues[1]) * deck.getNumRankLeft(rankValues[2]) * deck.getNumRankLeft(rankValues[3]) * deck.getNumRankLeft(rankValues[4]);
								if (newGames!=0) { //if newGames == 0 then one of the ranks is completely gone and this combo can't be made
								//debug
								//for ( Card.Rank rr : it) System.out.print(rr.getChar());
								//System.out.print(" newGames: " + newGames);
								
								//end debug
								//5toflush
								for (Card.Suit s: Card.Suit.values()) {
									for (cc=0; cc< 5; cc++){
										if (!deck.containsCard(rankValues[cc], s)) break;
									}
									if (cc==5) {  //ok, all cards for this suit exist
										newGames--;
										doFindBestValue(1,deck.pickSpecificCard(rankValues[0], s),deck.pickSpecificCard(rankValues[1], s),deck.pickSpecificCard(rankValues[2], s), deck.pickSpecificCard(rankValues[3], s),deck.pickSpecificCard(rankValues[4], s));
									}
								}
								
								// 4toflush, 
								// easy, just move the off-flush card for each suit
								for (Card.Suit s : Card.Suit.values()) {
									if (s == Card.Suit.C && hasC ==false) continue; //no need to loop if no suited cards in this suit
									else if(s== Card.Suit.D && hasD==false) continue;
									else if(s== Card.Suit.H && hasH==false) continue;
									else if(s== Card.Suit.S && hasS==false) continue;
									

									for (cc=0; cc<5; cc++) { //moving off card
										if (deck.getNumRankLeftNotSuit(rankValues[cc],s)!=0) {
											for (c=0; c<5; c++) { //cycling through to check
												if (c!=cc && !deck.containsCard(rankValues[c], s)) break;
											}
											if (c == 5) { //if we get here, we must have at least one off suit card and all suited cards in this combo
												newGames = newGames - deck.getNumRankLeftNotSuit(rankValues[cc],s);
												//HERE just need to call findbestvalue but have to send cards based on position of cc: a switch? ugh
												switch (cc) {
												case 0: doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[cc],s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1], s),deck.pickSpecificCard(rankValues[2], s), deck.pickSpecificCard(rankValues[3], s),deck.pickSpecificCard(rankValues[4], s));
													break;
												case 1: doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[cc],s),deck.pickSpecificCard(rankValues[0], s),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[2], s), deck.pickSpecificCard(rankValues[3], s),deck.pickSpecificCard(rankValues[4], s));
													break;
												case 2: doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[cc],s),deck.pickSpecificCard(rankValues[0], s),deck.pickSpecificCard(rankValues[1], s),fullDeck.pickSpecificCard(rankValues[2], Card.Suit.not(s)), deck.pickSpecificCard(rankValues[3], s),deck.pickSpecificCard(rankValues[4], s));
													break;
												case 3: doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[cc],s),deck.pickSpecificCard(rankValues[0], s),deck.pickSpecificCard(rankValues[1], s),deck.pickSpecificCard(rankValues[2], s), fullDeck.pickSpecificCard(rankValues[3], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[4], s));
													break;
												case 4: doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[cc],s),deck.pickSpecificCard(rankValues[0], s),deck.pickSpecificCard(rankValues[1], s),deck.pickSpecificCard(rankValues[2], s), deck.pickSpecificCard(rankValues[3], s),fullDeck.pickSpecificCard(rankValues[4], Card.Suit.not(s)));
													break;
												}
											}
										}
										
									}
								}
								
								// 3toflush, this will use suitC... variables to determine if we need to cycle through
								//will need a nested loop to move the suited cards
								for (Card.Suit s: Card.Suit.values()) {
									if (s == Card.Suit.C && suitedC==false) continue; //no need to loop if no suited cards in this suit
									else if(s== Card.Suit.D && suitedD==false) continue;
									else if(s== Card.Suit.H && suitedH==false) continue;
									else if(s== Card.Suit.S && suitedS==false) continue;
									
									for (c=0;c<5;c++) {
										for (cc=c+1;cc<5;cc++) {
											for (ccc=cc+1;ccc<5;ccc++) {
												//We just query to deck to see if all the cards are in. If one is missing, C will end up value 0
												if (!deck.containsCard(it[c], s) || !deck.containsCard(it[cc], s) || !deck.containsCard(it[ccc], s))
													C=0;
												else C=1;
												//now cycle through it[] to look for num left of leftover cards, anytime C==0, we are done
												for (cccc=0;cccc<5 && C!=0;cccc++) {
													if (cccc!=c&&cccc!=cc&&cccc!=ccc) { //check if one of suited cards
														C *= deck.getNumRankLeftNotSuit(it[cccc],s);
														boardCombo[cccc] = fullDeck.pickSpecificCard(it[cccc], Card.Suit.not(s));
													}
												}
											
											if (C!=0) {
												newGames-=C;
												boardCombo[c] = deck.pickSpecificCard(it[c], s); // we used array here because though slower would be unreadable and tedious code
												boardCombo[cc] = deck.pickSpecificCard(it[cc], s);
												boardCombo[ccc] = deck.pickSpecificCard(it[ccc], s);
												doFindBestValue(C,boardCombo);
											}
											}
										}
									}
									
								}
								//debug
								//System.out.println(newGames);
								
								//end debug
								
								//Do end. Here we can just pick specific cards and send the leftover newGames
								doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[2], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[3], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[4], Card.Suit.C));
							}} else if (rankCount[3] > 0) { //XXUYZ - 4,3
								newGames = deck.getNumRankLeft(rankValues[1]) * deck.getNumRankLeft(rankValues[2]) * deck.getNumRankLeft(rankValues[3]);
								if (newGames!=0 && deck.getNumRankLeft(rankValues[0]) > 1) { //must have at least two of the first value and one of the rest
								//set newGames based on combos of pair
								switch (deck.getNumRankLeft(rankValues[0])) {
								case 2: break; //only 1 combo
								case 3: newGames = newGames * 3; break;
								case 4: newGames = newGames * 6; break;
								}
								//debug
								//for ( Card.Rank rr : it) System.out.print(rr.getChar());
								//System.out.print(" newGames: " + newGames);
								
								//end debu
								
								//4toflush - all must be available
								for (Card.Suit s: Card.Suit.values()) {
									if (s == Card.Suit.C && hasC ==false) continue; //no need to loop if no suited cards in this suit
									else if(s== Card.Suit.D && hasD==false) continue;
									else if(s== Card.Suit.H && hasH==false) continue;
									else if(s== Card.Suit.S && hasS==false) continue;
									

									for (c=0;c<4;c++) {
										if (!deck.containsCard(rankValues[c], s)) break;
									}
									if (c == 4) { //all 4 exist 
										newGames-= deck.getNumRankLeftNotSuit(rankValues[0], s);
										doFindBestValue(deck.getNumRankLeftNotSuit(rankValues[0], s),deck.pickSpecificCard(rankValues[0], s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1], s), deck.pickSpecificCard(rankValues[2], s),deck.pickSpecificCard(rankValues[3], s));
									}
								}
								
								//3 to flush, one empty spot
								for (Card.Suit s: Card.Suit.values()) {
									if (s == Card.Suit.C && suitedC==false) continue; //no need to loop if no suited cards in this suit
									else if(s== Card.Suit.D && suitedD==false) continue;
									else if(s== Card.Suit.H && suitedH==false) continue;
									else if(s== Card.Suit.S && suitedS==false) continue;
									for (cc=0;cc<4;cc++) { //moving offsuit card
										if (deck.getNumRankLeftNotSuit(rankValues[cc], s)!=0 && (cc!=0 || deck.getNumRankLeftNotSuit(rankValues[cc], s) > 1)) { //in the case of the pair, must check that we have at least 2 cards
											for (c =0;c<4;c++) { //cycling to check all suited cards
												if (c!=cc && !deck.containsCard(rankValues[c], s)) break;
											}
											if (c == 4) { //if we get here, must have all suited cards and at least one leftover
												if (cc==0) { //for newGames, special case when using the pair
													switch (deck.getNumRankLeftNotSuit(rankValues[0],s)) { //non flush card is pair
													case 2: C=1; break;
													case 3: C=3; break;
													case 4: C=6; break;
													}
													newGames -= C;
												} else { //pair is flush card
													C = (deck.getNumRankLeftNotSuit(rankValues[0], s) * deck.getNumRankLeftNotSuit(rankValues[cc], s));
													newGames -= C;
												}
												
												//another switch for efficency
												switch (cc) {
												//nonflush card is pair
												case 0: doFindBestValue(C,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1], s), deck.pickSpecificCard(rankValues[2], s),deck.pickSpecificCard(rankValues[3], s));
													break;
												//nonflush card is not pair
												case 1: doFindBestValue(C,deck.pickSpecificCard(rankValues[0], s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.not(s)), deck.pickSpecificCard(rankValues[2], s),deck.pickSpecificCard(rankValues[3], s));
													break;
												case 2: doFindBestValue(C,deck.pickSpecificCard(rankValues[0], s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1], s), fullDeck.pickSpecificCard(rankValues[2], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[3], s));
													break;
												case 3: doFindBestValue(C,deck.pickSpecificCard(rankValues[0], s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1], s), fullDeck.pickSpecificCard(rankValues[2],s),fullDeck.pickSpecificCard(rankValues[3], Card.Suit.not(s)));
													break;
												}
												
											}
										}
									}
								}
								//debug
								//System.out.println(" " + newGames + " P");
								doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[2], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[3], Card.Suit.C));

								//end debug
							}} else if (rankCount[2] > 0 && rankCount[0]==3) { //XXXYZ - 3 
								switch(deck.getNumRankLeft(rankValues[0])) {
								case 3: C=1;break;
								case 4: C=4;break;
								default: C=0; 
								}
								newGames = C * deck.getNumRankLeft(rankValues[1]) * deck.getNumRankLeft(rankValues[2]);
								//debug
								//for ( Card.Rank rr : it) System.out.print(rr.getChar());
								//System.out.print(" newGames: " + newGames);
								
								//end debu
								

								if (newGames != 0) {
									
									//only need to check for 3 to flush
									for (Card.Suit s : Card.Suit.values()) {
										if (s == Card.Suit.C && suitedC==false) continue; //no need to loop if no suited cards in this suit
										else if(s== Card.Suit.D && suitedD==false) continue;
										else if(s== Card.Suit.H && suitedH==false) continue;
										else if(s== Card.Suit.S && suitedS==false) continue;
										
										if (deck.containsCard(rankValues[0], s) && deck.containsCard(rankValues[1], s) && deck.containsCard(rankValues[2], s)) {
											//have 3toflush
											if (C==1) {newGames--;
											doFindBestValue(1,deck.pickSpecificCard(rankValues[0],s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1],s),deck.pickSpecificCard(rankValues[2],s));
											} else {newGames -= 3;
											doFindBestValue(3,deck.pickSpecificCard(rankValues[0],s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1],s),deck.pickSpecificCard(rankValues[2],s));
											}
											
										}
										
									}

									//debug
									//System.out.println(" " + newGames + " P");
									doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[2], Card.Suit.C));
	
									
								}//end newGames if
							} else if (rankCount[2] > 0 && rankCount[0]==2) { //XXYYZ - 3 
								switch(deck.getNumRankLeft(rankValues[0])) {
								case 0: case 1: C=0; break;
								case 2: C=1; break;
								case 3: C=3; break;
								case 4: C=6; break;
								}
								switch(deck.getNumRankLeft(rankValues[1])) {
								case 0: case 1: D=0; break;
								case 2: D=1; break;
								case 3: D=3; break;
								case 4: D=6; break;
								}
								newGames = (C * D * deck.getNumRankLeft(rankValues[2]));
								//debug
								//for ( Card.Rank rr : it) System.out.print(rr.getChar());
								//System.out.print(" newGames: " + newGames);
								
								//end debu
								
								if (newGames != 0) {
									
									//only need to check for 3 to flush
									for (Card.Suit s : Card.Suit.values()) {
										if (s == Card.Suit.C && suitedC==false) continue; //no need to loop if no suited cards in this suit
										else if(s== Card.Suit.D && suitedD==false) continue;
										else if(s== Card.Suit.H && suitedH==false) continue;
										else if(s== Card.Suit.S && suitedS==false) continue;
										
										if (deck.containsCard(rankValues[0], s) && deck.containsCard(rankValues[1], s) && deck.containsCard(rankValues[2], s)) {
											//have 3toflush
											C = deck.getNumRankLeftNotSuit(rankValues[0], s) * deck.getNumRankLeftNotSuit(rankValues[1], s); //just multiply how many non-flush suit cards we have in the pairs, shouldn't be here if less than 2
											newGames -= C;
											doFindBestValue(C,deck.pickSpecificCard(rankValues[0],s),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[1],s),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.not(s)),deck.pickSpecificCard(rankValues[2],s));
										}
										
									}

									//debug
									//System.out.println(" " + newGames + " P");
									doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[2], Card.Suit.C));
									
								} //end newGames if()
							} else if (rankCount[0] == 3) { //XXXYY - 0 //never has flushes
								switch (deck.getNumRankLeft(rankValues[0])) {
								case 0: case 1: case 2: C=0; break;
								case 3: C=1; break;
								case 4: C=4; break;
								}
								switch (deck.getNumRankLeft(rankValues[1])) {
								case 0: case 1: D=0; break;
								case 2: D=1; break;
								case 3: D=3; break;
								case 4: D=6; break;
								}
								
								newGames = (C*D);
								if (newGames!=0) //if 0, must not be enough of one rank to make combo
									doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.C));
							} else { //XXXXY - 0 //never has flushes
								switch (deck.getNumRankLeft(rankValues[0])) {
								case 4: C=1; break;
								default: C=0;
								}
								newGames = (C * deck.getNumRankLeft(rankValues[1]));
								if (newGames != 0) //if 0, must be missing enough ranks to make combo
									doFindBestValue(true,newGames,fullDeck.pickSpecificCard(rankValues[0], Card.Suit.C),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.D),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.H),fullDeck.pickSpecificCard(rankValues[0], Card.Suit.S),fullDeck.pickSpecificCard(rankValues[1], Card.Suit.C));
							}
							/* ways of isomorphic by rank:
							 * XXXXY
							 * XXXYY
							 * XXXYZ
							 * XXYYZ
							 * XXUYZ
							 * XYZTU
							 */
							if (numGames%400000==0 || (numGames==1&&totalNumGames>100000)) { updateResults(numWins,numTies,numGames,totalNumGames,false);} // TODO: make time based 
		
						}
					}
				}
			}
		}

	}
	
	int sortJ,sortI,sortKey; Card.Rank valueKey;
	private void sortRankCount() { //simple sort, we use class variables to save variable init
		for (sortJ = 1; sortJ < 5; sortJ++) {
			sortKey = rankCount[sortJ];
			valueKey = rankValues[sortJ];
			for (sortI=sortJ-1; sortI>=0 && rankCount[sortI] < sortKey;sortI--) {
				rankCount[sortI+1] = rankCount[sortI];
				rankValues[sortI+1] = rankValues[sortI];
			}
			rankCount[sortI+1] = sortKey;
			rankValues[sortI+1] = valueKey;
		}
	}
	
	private void resetRankCount() {
		
	}
	
	private void fillRankCount(Card.Rank[] rint) {
		int c;
		for (c=0;c<5;c++) {
			rankCount[c]=0;
		}
		
		for (Card.Rank r: rint) {
			for (c=0; c<5;c++) {
				if (r == rankValues[c]) {
					rankCount[c]++; break;
				}
			}
			if (c==5) {
				for (c =0; c<5; c++) {
					if (rankCount[c] == 0) {
						rankValues[c] = r;
						rankCount[c]++;
						break;
					}
				}
			}
		}
	}
	
	void doBruteForceEnum() {
		/* This method  used when we have a flop or turn
		 * Time to cycle through the deck and declare a winner and update numWins[], numTies[], and numGames
		 * get an array of cards to represent the deck and then cycle through based on the length of board.
		 * hmm, how to make this efficient without using 3 different loops for pre, flop, and turn? recursion?
		 * 
		 * We are going to have a 7 card array and so will draw from the deck based on how many spots to fill
		 * it will always be 2 + board.length. an iterator array will work fine
		 */

		comboL = 7-2-board.length;  // the length of the combinations will have to get from deck
		deckIterator = new int[comboL];

		cardArray = deck.getDeckArray();
		Card[] comboArray = new Card[comboL];

		
		int deckL = cardArray.length;
		int cc=0; //cc manages deckIterator, ii manages cardArray 
		int ii=0; // variables to build combos into deckIterator
		handValue = new int[pockets.length];
		while (cc>=0 && !resultsObserver.checkStop())  
		{
			
			if (ii <= (deckL + (cc - comboL))) { //StackOverflow algorithm 
				deckIterator[cc] = ii;
				
				if (cc == comboL-1) {
					ii++;
					
					//magic here
					//first get the new combos

					for (int c=0;c<comboL;c++){
						comboArray[c] = cardArray[deckIterator[c]];
					}
					//TODO: There is optimization here somewhere by removing the above reference changes
					//somehow have to pass the cardArray[deckIterator[c]] directly to handvalue without using comboArray
					
					doFindBestValue(1,comboArray);
						
					/*
					 * DEBUG
					 *
					System.out.printf("Comparing: %s to %s on ",pockets[0][iterator[0]].getString(),pockets[1][iterator[1]].getString());
					for (Card c: board) {
						System.out.print(c.getCardStr());
					}
					for (Card c: comboArray) {
						System.out.print(c.getCardStr());
					}
					System.out.println();
					System.out.printf("%d %d:", deckL,comboL);
					for (int i : deckIterator) {
						System.out.printf("%d ", i);
					}
					for (int i : handValue) {
						System.out.print(i + " ");
					}
					System.out.println("Bestvalue: " + bestValue);
					updateResults(numWins,numTies,numGames,totalNumGames);
					/* DEBUG END
					 */
					/* all done?
					 */
					
					
					/* update
					 * We'll just update at a 100,000 hands for now. Later may need to adjust
					 */
					
					if (numGames%400000==0 || (numGames==1&&totalNumGames>100000)) { updateResults(numWins,numTies,numGames,totalNumGames,false);} // TODO: make time based 
				}
				else {
					ii = deckIterator[cc]+1;
					cc++;
				}
				
			} else {
				cc--;
				if (cc > 0) ii = deckIterator[cc]+1;
				else ii = deckIterator[0]+1;
			}
			
			
			
			
		}
	}
	
	
	/**
	 * Overloaded to void doFindBestValue(boolean noFlush,int newGames,Card... comboArray).
	 * This call will always send false to noFlush and will check for flushes.
	 * @param newGames
	 * @param comboArray
	 */
	void doFindBestValue(int newGames, Card... comboArray) {
		doFindBestValue(false,newGames,comboArray);
	}
	
	boolean tie;
	//debug variables
	void doFindBestValue(boolean noFlush, int newGames, Card... comboArray) {
		/*
		 * This method expects the following variables to be ready:
		 * board, iterator[]
		 * board + comboArray must be 5 cards, can't waste cpu by checking
		 */
		//if (newGames==0) return; //could happen
		if (comboArray.length+board.length != 5) {
			System.out.println("comboArray = " + comboArray.length);
			throw new IllegalArgumentException("doFindBestValue, comboArray = " + comboArray.length);
			
		}
		numGames+=newGames;;
		//Find values for each hand
		bestValue=0;
		tie = false;

		for (int c=0;c<pockets.length;c++) {
			if((handValue[c]=findHandValue(noFlush,pockets[c][iterator[c]],board,comboArray))>bestValue) {
				bestValue = handValue[c]; tie = false;
			}
			else if(handValue[c]==bestValue) tie=true;  //if hand value is same as bestvalue, must be a tie.
		}
		
		//now cycle through again and update ties and wins
		for (int c=0;c<pockets.length;c++) {
			if (handValue[c] == bestValue) {
				if (tie) numTies[c]+=newGames;   //choose here if a tie is a win
				else numWins[c]+=newGames;
			} 
		}
		
		
		
	}
	/**
	 * Helper method to calcEnum for finding the hand value of the current test. Cards still must be extracted from Pocket.
	 * @param pocket
	 * @param board
	 * @param combo
	 * @return Hand value from HandValue static class
	 */
	private int findHandValue(boolean noFlush, Pocket pocket, Card[] board, Card... combo) {
		
		if (board.length + combo.length != 5)  // TODO: extra calc, remove
			throw new IllegalArgumentException ("Problem in findHandValue.");
		
		if (noFlush == false) {
			switch (board.length) { //must be seperated, cannot create new array for speed reasons, (differences seems negligible after tests)
			case 0: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),combo[0],combo[1],combo[2],combo[3],combo[4]);
			case 1: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),board[0],combo[0],combo[1],combo[2],combo[3]);
			case 2: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),board[0],board[1],combo[0],combo[1],combo[2]);
			case 3: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],combo[0],combo[1]);
			case 4: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],board[3],combo[0]);
			case 5: return HandValue.getHandValue(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],board[3],board[4]);
			default: throw new IllegalArgumentException("bad data findHandValue: equityCalc");
			}
		} else {
			switch (board.length) { //must be seperated, cannot create new array for speed reasons, (differences seems negligible after tests)
			case 0: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),combo[0],combo[1],combo[2],combo[3],combo[4]);
			case 1: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),board[0],combo[0],combo[1],combo[2],combo[3]);
			case 2: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),board[0],board[1],combo[0],combo[1],combo[2]);
			case 3: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],combo[0],combo[1]);
			case 4: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],board[3],combo[0]);
			case 5: return HandValue.getHandValueNoFlush(pocket.getCard(0),pocket.getCard(1),board[0],board[1],board[2],board[3],board[4]);
			default: throw new IllegalArgumentException("bad data findHandValue: equityCalc");
			}
		}
		
		//old code, removed the extra array creation
		/*Card[] cards = new Card[(2 + board.length + combo.length)];
		cards[0] = pocket.getCard(0);
		cards[1] = pocket.getCard(1);
		int cardsIndex = 2;
		for (int c = 0; c<board.length; c++) {
			cards[cardsIndex] = board[c];
			cardsIndex++;
		}
		for (int c = 0; c<combo.length;c ++) {
			cards[cardsIndex] = combo[c];
			cardsIndex++;
		}
		if (cards.length == 7) 
			{
			int value = HandValue.getHandValue(cards);
			return value;
			}
		else return -1;*/
	}
	
	private boolean doHeadsupMatrix() {
		long[] tempWins;
		if ((tempWins = getFromMatrix(pockets[0][iterator[0]].normalizeMatchupBits(pockets[1][iterator[1]]))) != null) {					// strange if it equals null but no matter will just continue normally without matrix

			if (pockets[0][iterator[0]].getNum() > pockets[1][iterator[1]].getNum()) {  //TODO: change to proper
				numWins[0] += tempWins[0]; numWins[1] += tempWins[1];
			} else {
				numWins[1] += tempWins[0]; numWins[0] += tempWins[1];
			}
			//debug
			//System.out.printf("M:%s vs %s, %d %d\n",pockets[0][iterator[0]].getString(),pockets[1][iterator[1]].getString(),(numWins[0]-numWins0), (numWins[1]-numWins1));
			numTies[0] += (HUGAMES - tempWins[0] - tempWins[1]);
			numTies[1] += (HUGAMES - tempWins[0] - tempWins[1]);
			numGames += HUGAMES;
			
			return true;
		} else return false;
	}
	
	/**
	 * This method is separated from equityCalc to avoid instantiating a PreflopOddsMatrix if it isn't necessary.
	 * Once this method is called, it will create the class and lookup the key returning the results.
	 * @param key produced from pocket.normalizeMatchupBits
	 * @return numWins for each hand
	 */
	private long[] getFromMatrix(int key) {
		if (preflopOddsMatrix == null)
			preflopOddsMatrix = new PreflopOddsMatrix();
		
		return preflopOddsMatrix.getResults(key);
	}
	
	/**
	 * Setting this to false will tell calcEnum not to try to lookup the values from the PreflopOddsMatrix. Mostly for testing purposes.
	 * Default = true
	 * @param useMatrix
	 */
	public void setUseMatrix(boolean useMatrix) {
		this.useMatrix = useMatrix;
	}
	
	/**
	 * receives an array of cards and removes them from the  deck
	 * (to make equitycalc more readable)
	 */
	private boolean removeFromDeck(Card[] cardsToRemove) {
		for (Card c : cardsToRemove) {
			if (!deck.removeSpecificCard(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Called from calcEnum to update the observers. See EquityCalcObserver for what values it expects.
	 * @param numWins Wins per player
	 * @param numTies Ties per player
	 * @param numGames Number of games processed so far.
	 * @param totalNumGames Total number of games expected to process.
	 * @param force100 boolean true to force progress sent 100
	 */
	private void updateResults(long []numWins, long []numTies, long numGames, long totalNumGames, boolean force100) {
		//TODO: bug here somewhere, after calc complete, sends 75
		float []results = new float[numWins.length];
		for(int c=0;c<numWins.length;c++) {
			results[c] =  ((float)(numWins[c])/(float)numGames)*100;
			
		}
		if (numGames >= totalNumGames || force100==true) resultsObserver.updateEquity(results,100);
		else resultsObserver.updateEquity(results, (int)((double)((double)numGames/(double)totalNumGames)*100));
	}
	
	/**
	 * Usually called by constructor.  TODO: arrange for multiple observers
	 * @param o
	 */
	void setCalcObserver (EquityCalcObserver o) {
		resultsObserver = o;
	}
	
	
	/**
	 * Overloaded for easier calls when board and dead don't exist.
	 * Note: calcEnum should always be called with empty arrays, not null arrays.
	 */
	long [][]calcEnum(Pocket pockets[][]) {
		return calcEnum(pockets,EMPTY_CARD_ARRAY,EMPTY_CARD_ARRAY);
	}
	/**
	 * Overloaded for easier calls when board and dead don't exist.
	 * Note: calcEnum should always be called with empty arrays, not null arrays.
	 */
	
	long [][]calcEnum(Pocket pockets[][], Card board[]) {
		return calcEnum(pockets,board,EMPTY_CARD_ARRAY);
	}
	/**
	 * Overloaded for easier calls when board and dead don't exist.
	 * Note: calcEnum should always be called with empty arrays, not null arrays.
	 */

	long numCalcsEnum(Pocket pockets[][], Card board[]) {
		return numCalcsEnum(pockets,board,EMPTY_CARD_ARRAY);
	}
	
	/**
	 * Overloaded for easier calls when board and dead don't exist.
	 * Note: calcEnum should always be called with empty arrays, not null arrays.
	 */
	long numCalcsEnum(Pocket pockets[][]) {
		return numCalcsEnum(pockets,EMPTY_CARD_ARRAY,EMPTY_CARD_ARRAY);
	}
	
	private double combinations(int num, int size) {
		if (size>num) return 0;
		return factorial(num)/(factorial(num-size)*factorial(size));
	}
	
	private double factorial(int num) {
		double factorial=0;
		for (int c = num;c>0;c--) {
			if (c==num) factorial=num;
			else factorial *= c;
		}
		return factorial;
	}

}
