package library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

public class PokerToolsTestCode implements EquityCalcObserver {

	
	public static void main (String []args) {
		
		//testHoldemStrings();
		//testPanel();
		//testCalcEnum();
		//testCalcEnumTime();
		//testDeck();
		//testMatrixCalcEnum();
		//testHandValue();
		//testMatrixBuilder();
		//testPreFlopOddsMatrix();
		//buildMatrixHash();
		buildHandsHash();
		//testFuckOff();
		//testIsoThing();
		//testDeck();
		testJavaFX();
	}
	
	public static void testJavaFX() {
		MainJavaFX mainJavaFX = new MainJavaFX();
		mainJavaFX.go();
	}
	
	public static void testIsoThing() {
		Deck deck = new Deck();
		deck.drawSpecificCard("Ah");
		deck.drawSpecificCard("Ad");
		deck.drawSpecificCard("As");
		deck.drawSpecificCard("Ks");
		TreeSet<Card[]> map = new TreeSet<Card[]>();
		int numofcombos = 0;
		int fulldeckenum = 0;
		for (int a=0; a < 52; a++) { //full deck iteration
			for (int b = a+1; b < 52; b++) {
				for (int c= b+1; c < 52; c++) {
					for (int d = c+1; d < 52; d++) {
						for (int e = d+1; e < 52; e++) {
							fulldeckenum++;
						}
					}
				}
			}
		} 
		
		//learn this finally
		
		
		
		int numOfXXXXY = 0, numOfXXXYY=0, numOfXXYYZ = 0, numOfXXUYZ=0,numOfXYZTU=0,numOfXXXYZ=0;
		int numOfRankCombos=0;
		int[] rankCount = new int[5];
		int[] rankValue = new int[5];
		Card.Rank ar = Card.Rank.ACE;
		ar = ar.setByValue(2);
		System.out.println(ar.getChar());
		for (Card.Rank a = Card.Rank.TWO;a!=null;a = a.next()) {
			for (Card.Rank b = a;b !=null;b = b.next()) {
				for (Card.Rank c = b; c !=null; c = c.next()) {
					for (Card.Rank d = c; d != null; d = d.next()) {
						for (Card.Rank e = d;e != null; e = e.next()) {
							if (a == b && b==c && c==d && d==e) continue; //5 of one rank
							resetArray(rankCount); resetArray(rankValue);
							
							//fill in arrays
							fillArray(a,rankCount,rankValue);
							fillArray(b,rankCount,rankValue);
							fillArray(c,rankCount,rankValue);
							fillArray(d,rankCount,rankValue);
							fillArray(e,rankCount,rankValue);
							
							//arrange so greater number of ranks first
							arrangeArray(rankCount,rankValue);
							numOfRankCombos++;
							int C=0,D=0;
							if (rankCount[4] > 0) { //XYZTU - 5,4,3
								numOfXYZTU+= (deck.getNumRankLeft(ar.setByValue(rankValue[0])) * deck.getNumRankLeft(ar.setByValue(rankValue[1])) * deck.getNumRankLeft(ar.setByValue(rankValue[2])) * deck.getNumRankLeft(ar.setByValue(rankValue[3])) * deck.getNumRankLeft(ar.setByValue(rankValue[4]))) ;
								
							} else if (rankCount[3] > 0) { //XXUYZ - 4,3
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[0]))) {
								case 0: C=0;
								case 1: C=0;break;
								case 2: C=1;break;
								case 3: C=3;break;
								case 4: C=6;break;
								}
								numOfXXUYZ+=(C * deck.getNumRankLeft(ar.setByValue(rankValue[1])) * deck.getNumRankLeft(ar.setByValue(rankValue[2])) * deck.getNumRankLeft(ar.setByValue(rankValue[3])));
							} else if (rankCount[2] > 0 && rankCount[0]==3) { //XXXYZ - 3 
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[0]))) {
								case 0: 
								case 1: 
								case 2: C=0;break;
								case 3: C=1;break;
								case 4: C=4;break;
								}
								numOfXXXYZ+=(C * deck.getNumRankLeft(ar.setByValue(rankValue[1])) * deck.getNumRankLeft(ar.setByValue(rankValue[2])));
								
							} else if (rankCount[2] > 0 && rankCount[0]==2) { //XXYYZ - 3 
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[0]))) {
								case 0: case 1: C=0; break;
								case 2: C=1; break;
								case 3: C=3; break;
								case 4: C=6; break;
								}
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[1]))) {
								case 0: case 1: D=0; break;
								case 2: D=1; break;
								case 3: D=3; break;
								case 4: D=6; break;
								}
								numOfXXYYZ+=(C * D * deck.getNumRankLeft(ar.setByValue(rankValue[2])));
								
							} else if (rankCount[0] == 3) { //XXXYY - 0
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[0]))) {
								case 0: case 1: C=0; break;
								case 2: C=0; break;
								case 3: C=1; break;
								case 4: C=4; break;
								}
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[1]))) {
								case 0: case 1: D=0; break;
								case 2: D=1; break;
								case 3: D=3; break;
								case 4: D=6; break;
								}
								numOfXXXYY+=(C * D);
								//System.out.println("XXXYY" + " " + C + " " + D + " ");
								
							} else { //XXXXY - 0 
								switch (deck.getNumRankLeft(ar.setByValue(rankValue[0]))) {
								case 4: C=1; break;
								default: C=0;
								}
								numOfXXXXY+=(C * deck.getNumRankLeft(ar.setByValue(rankValue[1])));
								
							}
							/* ways of isomorphic by rank:
							 * XXXXY
							 * XXXYY
							 * XXXYZ
							 * XXYYZ
							 * XXUYZ
							 * XYZTU
							 */
								
						}
					}
				}
			}
		}
		numofcombos += (numOfXYZTU + numOfXXUYZ + numOfXXXYZ + numOfXXYYZ + numOfXXXYY + numOfXXXXY);
		System.out.println("Num of combos: " + numofcombos);
		System.out.println("Num of rank combos: " + numOfRankCombos);
		System.out.println("Full deck enum: " + fulldeckenum);
		System.out.println("XYZTU: " + numOfXYZTU + "\nXXUYZ: " + numOfXXUYZ + "\nXXXYZ: " + numOfXXXYZ + "\nXXYYZ: " + numOfXXYYZ + "\nXXXYY: " + numOfXXXYY + "\nXXXXY: " + numOfXXXXY);
	}
	
	private static void arrangeArray(int[] rankCount, int[] rankValue) {
		int[] newRankCount = new int[5];
		int[] newRankValue = new int[5];
		int p = 0;
		for (int count = 4; count > 0; count--) {
			for (int c=0;c<5;c++) {
				if (rankCount[c] == count) {
					newRankCount[p] = count;
					newRankValue[p] = rankValue[c];
					p++;
				}
			}
		}
		
		for (int c = 0; c< 5;c++) {
			rankCount[c] = newRankCount[c];
			rankValue[c] = newRankValue[c];
		}
		
	}
	
	private static void fillArray(Card.Rank r, int[] rankCount, int[] rankValues) {
		boolean found = false;
		for (int c=0;c<rankCount.length;c++) {
			if (r.getValue() == rankValues[c]) {
				rankCount[c]++; found = true; 
			}
		}
		if (!found) {
			for (int c =0; c <rankCount.length;c++) {
				if (rankValues[c] == 0) {
					rankValues[c] = r.getValue();
					rankCount[c]++;
					break;
				}
			}
		}
	}
	private static void resetArray(int[] a){
		for (int c=0;c<5;c++) {
			a[c]=0;
		}
	}
	
	public static void testFuckOff() {
		int map = (51 << 18); //110011
		System.out.println(Integer.toBinaryString(map));
		int map2 = (47 << 12);
		System.out.println(Integer.toBinaryString(map2));
		int map3 = (50 << 6);  // 110010
		System.out.println(Integer.toBinaryString(map3));
		int map4 = 40; //101000
		int total = map + map2 + map3 + map4;
		System.out.println(Integer.toBinaryString(total));
		
	}

	public static void testPreFlopOddsMatrix() {
		
		
		long[] results = new long[] {1,1};
		PreflopOddsMatrix matrix = new PreflopOddsMatrix();
		results = matrix.getResults(12368324);
		System.out.println(results[0] + " " + results[1]);
	}
	
	
	public static void testMatrixBuilder() {
		Pocket pocket1 = new Pocket("AcQd");
		Pocket pocket2 = new Pocket("Asks");
		
		long map = pocket1.normalizeMatchupBits(pocket2);
		System.out.println(Integer.toBinaryString(pocket1.normalizeMatchupBits(pocket2)));
		System.out.println(pocket1.normalizeMatchup(pocket2));
		
		if (map == new Pocket("QcQd").normalizeMatchupBits(new Pocket("KhAs"))) {
			System.out.println("yeppers");
			System.out.println("Num: " + pocket1.toString() + " " + pocket1.getNum());
			System.out.println("Num: " + pocket2.toString() + " " + pocket2.getNum());
		}
	}
	
	public static void buildMatrixHash() {
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.buildHash();
		
	}
	
	public static void buildHandsHash() {
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.buildHandValueHash();
	}
	
	public static void testHandValue() {
		//Card[] cards = {new Card("Ah"), new Card("kh"), new Card("Qh"), new Card("Jh"), new Card("Tc"), new Card("Td"), new Card("Js"), new Card("Jd")};
		Card[] hand1 = new Card[5];
		Card[] hand2 = new Card[5];
		hand1[0] = new Card("Ah");
		hand1[1] = new Card("Kh");
		hand2[0] = new Card("As");
		hand2[1] = new Card("Qh");
		HandValue.setDebug(false);
		int countOfDups=0;
		for (int c =0; c<1000000; c++) {
			Deck deck = new Deck();
			ArrayList<Card> hand1List = new ArrayList<Card>();
			ArrayList<Card> hand2List = new ArrayList<Card>();
			hand1[0] = deck.drawSpecificCard("Ah");
			hand1[1] = deck.drawSpecificCard("Kh");
			hand2[0] = deck.drawSpecificCard("As");
			hand2[1] = deck.drawSpecificCard("Qh");
			for (int cc=0; cc<3; cc++) {
				hand1[cc+2] = deck.drawRandomCard();
				hand2[cc+2] = deck.drawRandomCard();
				//cards[cc].print(); System.out.print(" ");
			}
			//System.out.println();
			for (int cc=0; cc<5;cc++) {
				hand1List.add(hand1[cc]);
				hand2List.add(hand2[cc]);
			}
			Collections.sort(hand1List);
			Collections.sort(hand2List);
			if(HandValue.getHandValue(hand1) == HandValue.getHandValue(hand2)) {
				countOfDups++;
				for (int cc = 0; cc<5;cc++) {
					hand1List.get(cc).print();
				}
				System.out.print(" == ");
				for (int cc = 0; cc<5;cc++) {
					hand2List.get(cc).print();
				}
				System.out.println(" " + HandValue.getHandValue(hand1));
			}
			
			//SevenCardHand sevencard = new SevenCardHand();
			//sevencard.addCard(cards);
			//if (c%100000 == 0) System.out.println(c);
		}
		System.out.println("dups: " + countOfDups);
	}
	
	public static void testMatrixCalcEnum() {
		
		PokerToolsTestCode jpt = new PokerToolsTestCode();
		EquityCalc equityCalc = new EquityCalc(jpt);
		
		Pocket[][] pockets = new Pocket[2][];
		
		pockets[0] = HoldemStrings.pocketsToArray("TT+,AJs+");
		pockets[1] = HoldemStrings.pocketsToArray("AQo+");
		long[][] results = equityCalc.calcEnum(pockets);
		System.out.printf("\n%d %d %d\n", results[0][0],results[1][0],results[0][2]);
		
		equityCalc.setUseMatrix(false);
		results = equityCalc.calcEnum(pockets);
		System.out.printf("\n%d %d %d\n", results[0][0],results[1][0],results[0][2]);
		
	}
	
	public static void testPanel() {
		
		MainGUI gui = new MainGUI();
		gui.go();
		gui.getCurrentContentPane().updateEquity(new float[] {50,50,50},100);
	}
	
	public static void testCalcEnumTime () {
		Deck deck = new Deck();
		Pocket[][] pocket = new Pocket[2][];
		Pocket[][] pocket3Hands = new Pocket[3][];
		
		pocket3Hands[0] = new Pocket[1]; pocket3Hands[1] = new Pocket[1]; pocket3Hands[2] = new Pocket[1];
		pocket3Hands[0][0] = new Pocket("AhKh"); pocket3Hands[1][0] = new Pocket("KcKd"); pocket3Hands[2][0] = new Pocket("QhJd");
		
		pocket[0] = new Pocket[1];
		pocket[1] = new Pocket[1];
		
		pocket[0][0] = new Pocket("Ahkh");
		pocket[1][0] = new Pocket("QcJd");
		deck.removeSpecificCard(new Card("Ad"));
		deck.removeSpecificCard(new Card("Ah"));
		deck.removeSpecificCard(new Card("As"));
		deck.removeSpecificCard(new Card("Ks"));
		
		long startTime = System.currentTimeMillis();
		
		PokerToolsTestCode jpt = new PokerToolsTestCode();
		EquityCalc equity = new EquityCalc(jpt);
		
		equity.calcEnum(pocket);
		long endTime = System.currentTimeMillis();
		System.out.println("Load Matrix Time: " + (endTime - startTime));
		
		startTime = System.currentTimeMillis();
		

		startTime = System.currentTimeMillis();
		
		equity.setUseMatrix(false);
		long[][] results = equity.calcEnum(pocket);
		endTime = System.currentTimeMillis();
		System.out.println("\nEquitycalc Time: " + (endTime - startTime));
		
		
		System.out.println("R[0][0]: " + results[0][0] + " R[0][1]: " + results[0][1]);
		System.out.println("R[1][0]: " + results[1][0] + " R[1][1]: " + results[1][1]);
		System.out.println("numgames: " + results[0][2]);
		
		startTime = System.currentTimeMillis();
		
		equity.setUseMatrix(false);
		results = equity.calcEnum(pocket3Hands);
		endTime = System.currentTimeMillis();
		System.out.println("\nEquitycalc Time 3 Hands: " + (endTime - startTime));
		
		
		System.out.println("R[0][0]: " + results[0][0] + " R[0][1]: " + results[0][1]);
		System.out.println("R[1][0]: " + results[1][0] + " R[1][1]: " + results[1][1]);
		System.out.println("numgames: " + results[0][2]);

		startTime = System.currentTimeMillis();
		Card[] cardArray = {deck.drawRandomCard(), deck.drawRandomCard(), deck.drawRandomCard(),
				deck.drawRandomCard(), deck.drawRandomCard(),deck.drawRandomCard(),deck.drawRandomCard()
		};
		Card[] card2Array = {deck.drawRandomCard(), deck.drawRandomCard(), deck.drawRandomCard(),
				deck.drawRandomCard(), deck.drawRandomCard(),deck.drawRandomCard(),deck.drawRandomCard()
		};
		deck.setDeck();
		for (int c =0; c < 170200;c++) {
			//deck.setDeck();
			int value = HandValue.getHandValue(cardArray);
			value = HandValue.getHandValue(card2Array);
			cardArray = new Card[] {deck.pickSpecificCard(Card.Rank.ACE,Card.Suit.C), deck.pickSpecificCard(Card.Rank.ACE,Card.Suit.D), deck.pickSpecificCard(Card.Rank.ACE,Card.Suit.H),
					deck.pickSpecificCard(Card.Rank.ACE,Card.Suit.S), deck.pickSpecificCard(Card.Rank.TWO,Card.Suit.C),deck.pickSpecificCard(Card.Rank.THREE,Card.Suit.C),deck.pickSpecificCard(Card.Rank.FOUR,Card.Suit.C)
			};
			card2Array = new Card[] {deck.pickSpecificCard(Card.Rank.SIX,Card.Suit.C), deck.pickSpecificCard(Card.Rank.FIVE,Card.Suit.D), deck.pickSpecificCard(Card.Rank.FIVE,Card.Suit.C),
					deck.pickSpecificCard(Card.Rank.SIX,Card.Suit.D),deck.pickSpecificCard(Card.Rank.SEVEN,Card.Suit.C),deck.pickSpecificCard(Card.Rank.SEVEN,Card.Suit.D),deck.pickSpecificCard(Card.Rank.TEN,Card.Suit.S)
			};
			
			//random numbers
			/*for (int cc=0; cc<5; cc++) {
				Math.random();
			}*/
		}
		endTime = System.currentTimeMillis();
		System.out.println("Hand Value Time: " + (endTime - startTime));
		/*
		 * 
		 * nov 13, 2016
		 * 
		 * headsup w/o matrix: 517
		 * HandValue no draws: 30
		 * HandValue w/ draws: 359
		 * HandValue w/ picks: 142
		 * 
		 * nov 13, 2016 
		 * Removed an array creations in equitycalc.findHandValue
		 * 
		 * headsup w/o matrix: 497
		 * HandValue w/ picks: 151
		 * 
		 * nov 16, 2016
		 * Finished doPreflopEnum()
		 * debugged and seems accurate, now time to look for ways to speed it ip
		 * 
		 * 3 handed: 71
		 * 
		 * nov 17, 2016
		 * 
		 * Fixed the sort algorithm in doPreflopEnum to use less instantiation
		 * 
		 * headsup w/o matrix: 94
		 * 3handed: 62
		 * 
		 * Optimized Deck class
		 * 
		 * headsup w/o matrix: 84
		 * 3handed: 32
		 * Where would more speed possibly come from?
		 * 
		 * Added getRankMap to Card
		 * 
		 * no change
		 * 
		 *  AA vs KK vs QQ: 48m/s
		 *  AKo vs KQo vs QJo: 66m/s
		 *  AKs vs KQs vs QJs: 34m/s
		 */
		
	}
	public static void testCalcEnum () {
		// 	float []calcEnum(Pocket pockets[][], Card board[], Card dead[]) {
		Deck deck = new Deck();
		Pocket[][] pocket = new Pocket[3][];

		pocket[0] = new Pocket[2];
		pocket[1] = new Pocket[2];
		pocket[2] = new Pocket[2];
		
		pocket[0][0] = new Pocket("AhKh");
		pocket[1][0] = new Pocket("AsKs");
		pocket[0][1] = new Pocket("2c2h");
		pocket[1][1] = new Pocket("3c3h");
		pocket[2][0] = new Pocket("5c5h");
		pocket[2][1] = new Pocket("6d6s");
		
		Card[] board = new Card[] {new Card("5h"), new Card("4h"), new Card("4d")};
		
		PokerToolsTestCode jpt = new PokerToolsTestCode();
		EquityCalc equity = new EquityCalc(jpt);
		equity.setUseMatrix(true);
		System.out.println(equity.numCalcsEnum(pocket));
		//System.out.println(results[0][0]);
		//System.out.println(results[1][0]);
	}
	
	
	public void updateEquity(float[] percentage, int progress) {
			/*for (int c=0; c<percentage.length;c++) {
				System.out.println(percentage[c] + "%");
			}*/
	}
	
	public void updateProgress (int percentage) {
		System.out.println("Progress: " + percentage + " %");
	}
	
	public static void testDeck() {
		Deck deck = new Deck();
		deck.print();
		System.out.println("\nDrawing Ac:" + deck.drawSpecificCard("Ac").getCardStr());
	}

	public static void testHoldemStrings() {
		ArrayList<Pocket> testPocket = new ArrayList<Pocket>();
		HashSet<Pocket> testHash = new HashSet<Pocket>();
		String testString = "A3Kh7s";
		Pocket[] testPocketArray;
		testPocket = HoldemStrings.pocketsToArrayList(testString);
		testPocketArray = HoldemStrings.pocketsToArray(testString);
		
		System.out.println("Size of array: " + testPocketArray.length);
		for (Pocket p : testPocketArray) {
			System.out.print (p.toString());
			System.out.print(" " + p.hashCode() + "\n");
		
		}
		System.out.println(testPocket.size());
		
		/*
		ArrayList<Card> testCard = new ArrayList<Card>();
		testCard = HoldemStrings.cardsToArrayList("kd,ah,5c,as");
		
		for (Card c:testCard) {
			System.out.println(c.getCardStr());
		} */
		
		//String cardString = "AcAh,88+,AJo,AJs,T8s+,ATo+,jhjh,A2s+,7h8c,Ts9s,AhJc,9c9y,2c2h";
		
		/*
		ArrayList<String> s = null;
		ArrayList<String> t = null;
		s = HoldemStrings.extractIndividualSpecifics(cardString);
		t = HoldemStrings.extractIndividualHandTypes(cardString);
		
		HoldemStrings.removeUnnecessarySpecifics(t, s);
		System.out.println("printing types: ");
		for (String ss: t ) {
			System.out.println(ss);
		}
		System.out.println("printing specifics:");
		for ( String ss: s) {
			System.out.println(ss);
		}
		
		System.out.println(HoldemStrings.normalizeCaps("TKKkkssSS"));
		*/
		/*
		String cardString = "99";
		System.out.println(HoldemStrings.condenseCardString(cardString));
		String newCardString = HoldemStrings.condenseCardString(cardString);
		cardString = HoldemStrings.addTypetoString(newCardString, "76o");
		System.out.println(cardString);
		*/
		
		System.out.println(HoldemStrings.removeTypeFromString("J5s", "J5s"));
		
	}
	
	public static void testGUI() {
		try {
			MainGUI frame = new MainGUI();
			frame.setVisible(true);
			EvaluatorPanel panel = new EvaluatorPanel();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testPocket() {
		Pocket one = new Pocket("2S2C");
		Pocket two = new Pocket("2D2H");
		Card cone = new Card("2c");
		Card ctwo = new Card("2s");
		
		System.out.printf("%d %d\n",cone.getNum(),ctwo.getNum());
		
		System.out.printf("one: %d two: %d\n",one.getNum(), two.getNum());
		System.out.printf("one: %d two: %d \n", one.hashCode(),two.hashCode());
		System.out.print(one.equals(two));
		System.out.println(one.compareTo(two));
		System.out.println(two.equals(one));
		HashSet<Pocket> testPocket = new HashSet<Pocket>();
		testPocket.add(one);
		testPocket.add(two);
		System.out.println(testPocket.size());
	}

	@Override
	public boolean checkStop() {
		// TODO Auto-generated method stub
		return false;
	}
}
