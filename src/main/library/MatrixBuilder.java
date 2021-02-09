package library;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class MatrixBuilder implements EquityCalcObserver {
	EquityCalc equityCalc;
	DataOutputStream binOutput; // used to writing to binary file
	
	public MatrixBuilder () {
		equityCalc = new EquityCalc(this);
		equityCalc.setUseMatrix(false);
	}
	
	public void buildHandValueHash() {
		HashMap<Long,Integer> lookup = new HashMap<Long, Integer>();
		Deck deck = new Deck();
		
		long ranksMap = 0;
		
		//loop variables
		int comboL = 5;
		
		Card cardArray[] = deck.getDeckArray();
		Card comboArray[] = new Card[comboL];
		int deckL = cardArray.length;
		
		for (comboL=5; comboL < 8; comboL+=2) { //loop for 7 cards and 5 cards
			int deckIterator[] = new int[comboL];
			int cc=0;   	//cc manages deckIterator, ii manages cardArray
			int ii=0;
			
			while (cc>=0) {
				
				if (ii <= (deckL + (cc - comboL))) {
					deckIterator[cc] = ii;
					if (cc == comboL-1) {
						ii++;
						//work here
						ranksMap=0L;
						for (int d : deckIterator) {
							//build a ranksMap from deckIterator
							ranksMap += cardArray[d].getRankMap();
						}
						
						if (!lookup.containsKey(ranksMap)) {
							//build an array for HandValue
							comboArray =  new Card[comboL];
							for (int dd=0; dd<comboL; dd++) {
								comboArray[dd] = cardArray[deckIterator[dd]];
							}
						
							lookup.put(new Long(ranksMap), new Integer(HandValue.getHandValue(comboArray)));
						}
					} else {
						ii = deckIterator[cc]+1;
						cc++;
					}
				} else {
					cc--;
					if (cc>0) ii = deckIterator[cc]+1;
					else ii = deckIterator[0]+1;
				}
			}
		} //end comboL loop
		
		try {
			writeHandsHashObject(lookup);
		} catch (IOException i) {
			System.out.println("Unable to write HandValues");
		}

	}
	
	public void buildHash () {
		HashMap<Integer,long[]> matrix = new HashMap<Integer,long[]>();
		HashMap<Integer,String> matrixString = new HashMap<Integer,String>(); //temp
		Pocket[][] pockets = new Pocket[2][1];
		long[] matchupResults = new long[2];
		int matchups = 0;
		int maxMatchups = Integer.MAX_VALUE;
		long startTime = System.currentTimeMillis();
		for(int c = 0; c<52 && matchups < maxMatchups; c++) {
			for (int cc=c+1;cc <52 && matchups < maxMatchups; cc++) {
				for (int ccc=c+1;ccc<52 && matchups < maxMatchups; ccc++) {
					for (int cccc=ccc+1;cccc<52 && matchups < maxMatchups;cccc++) {
						if (ccc==cc || cccc==cc) continue; //Since the second hand has to start at the card lower than the top card of the first hand, checking here for duplicates. not elegant but w/e
						System.out.printf("%d %d %d %d\n", c,cc,ccc,cccc);
						pockets[0][0] = new Pocket(new Card(c), new Card(cc));
						pockets[1][0] = new Pocket(new Card(ccc), new Card(cccc));
						if(matrix.containsKey(pockets[0][0].normalizeMatchupBits(pockets[1][0]))) continue;
						long[][] results = equityCalc.calcEnum(pockets); 
						if (results[0][0] == 0 && results[1][0]==0) continue; //duplicate
						//below is to count matchups, remove above when finished
						//long [][]results = new long[2][3];
						//results[0][0] = results[0][1] =results[0][2]= 0;
						//results[1][0] = results[1][1] =results[1][2]= 0;
						//end count matchups
						System.out.print(Integer.toBinaryString(pockets[0][0].normalizeMatchupBits(pockets[1][0])) + " " + pockets[0][0].normalizeMatchup(pockets[1][0]));
						System.out.println(pockets[0][0].toString() + " vs " + pockets[1][0].toString() + " " + results[0][0] + " " + results[1][0] + " " + results[0][1] + " " + results[1][1] + " numgames: " + results[0][2]);
						matchups++;
						matchupResults = new long[3];
						if(pockets[0][0].getNum() > pockets[1][0].getNum()) {
							matchupResults[0] = results[0][0];
							matchupResults[1] = results[1][0];
						}
						else {
							matchupResults[0] = results[1][0];
							matchupResults[1] = results[0][0];
						}
						matchupResults[2] = results[0][1]; //ties just one needed
						matrix.put(pockets[0][0].normalizeMatchupBits(pockets[1][0]), matchupResults);
						matrixString.put(pockets[0][0].normalizeMatchupBits(pockets[1][0]), pockets[0][0].normalizeMatchup(pockets[1][0]));
					}
				}
			}
		}
		System.out.println(matchups);
		for ( Integer i : matrix.keySet()) {
			System.out.print(i + ": " );
			System.out.print(matrix.get(i)[0] + " " + matrix.get(i)[1]);
			System.out.println();
		}
	System.out.println("Number of keys: " + matrix.size());
	long endTime = System.currentTimeMillis();
	System.out.println("Time Taken " + (endTime-startTime) + "ms");
	writeCommaDelimitedFile(matrix,matrixString);
	writeJavaMatrixCode(matrix);
	writeBinaryFile(matrix,matrixString);
	try {
		writeHashObject(matrix);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	private void writeJavaMatrixCode(HashMap<Integer,long[]> matrix) {
		String fileName = "matrix.code";
		File matrixOutputFile = new File(fileName);
		PrintWriter output;
		try { output = new PrintWriter(matrixOutputFile);
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		
		for (int m : matrix.keySet()) {
			output.print("p(");
			output.print(m + ",");
			output.print(matrix.get(m)[0] + ",");
			output.print(matrix.get(m)[1]);
			output.print(");");
		}
		
		output.close();
	}
	private void writeCommaDelimitedFile(HashMap<Integer,long[]> matrix, HashMap<Integer,String> matrixString)  {
		String fileName = "matrix.txt";
		
		File matrixOutputFile = new File(fileName);
		PrintWriter output;
		try {
			output = new PrintWriter(matrixOutputFile);
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		
		for (int m : matrix.keySet()) {
			output.print(m + ",");
			output.print(matrixString.get(m) + ",");
			for (long r : matrix.get(m)) {
				output.print(r + ",");
			}
			output.println();
		}
		
		output.close();
	}
	
	/**
	 * Called from buildhash to write a binary data file with the matrix. The binary data file will hold int long long for each result
	 * @param matrix
	 * @param matrixString
	 */
	private void writeBinaryFile(HashMap<Integer,long[]> matrix, HashMap<Integer,String> matrixString) {
		try {
			binOutput = new DataOutputStream(new FileOutputStream("matrix.dat"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int m: matrix.keySet()) {
			try {
				binOutput.writeInt(m);
				for (long r : matrix.get(m)) {
					binOutput.writeLong(r);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		try {
			binOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Writes the whole hashmap to a file. This will probably be the easiest way to import the values.
	 * @param matrix
	 * @throws IOException 
	 */
	private void writeHashObject(HashMap<Integer,long[]> matrix) throws IOException {
		ObjectOutputStream objOutput;
		objOutput = new ObjectOutputStream(new FileOutputStream("matrix.ser"));
		
		objOutput.writeObject(matrix);
		
		objOutput.close();
	}
	
	/**
	 * Writes whole hashmap to a file.
	 */
	private void writeHandsHashObject(HashMap<Long,Integer> matrix) throws IOException {
		ObjectOutputStream objOutput;
		objOutput = new ObjectOutputStream(new FileOutputStream("HandValues.ser"));
		
		objOutput.writeObject(matrix);
		
		objOutput.close();

	}
	
	public void go () {
		//Deck deck = new Deck();
		int matchups=0;
		//Card[] cardArray = deck.getDeckArray();
		/*for(int c=0; c<52; c++) {
			for (int cc=c+1; cc<52;cc++) {
				for (int ccc=cc+1; ccc<52; ccc++) {
					for (int cccc=ccc+1; cccc<52; cccc++) {
						//System.out.printf("%s%s vs %s%s\n",cardArray[c].getCardStr(),cardArray[cc].getCardStr(),cardArray[ccc].getCardStr(),cardArray[cccc].getCardStr());
						matchups++;
					}
				}
			}
		}
		System.out.println("Matchups: " + matchups);*/
		
		Card.Rank one1 = Card.Rank.FOUR;
		Card.Rank one2 = Card.Rank.THREE;
		Card.Rank two1 = Card.Rank.ACE;
		Card.Rank two2 = Card.Rank.FIVE;
		HashSet<Long> resultsList1 = new HashSet<Long>();
		HashSet<Long> resultsList2 = new HashSet<Long>();
		HashSet<Integer> matchupsList = new HashSet<Integer>();
		HashSet<Long> ties = new HashSet<Long>();
		Pocket[][] pockets = new Pocket[2][1];
		top:
		for (Card.Suit ones1 : Card.Suit.values()) {
			if (ones1 == Card.Suit.C || ones1 == Card.Suit.D) continue;
			for (Card.Suit ones2 : Card.Suit.values()) {
				if (ones2 == Card.Suit.C || ones2 == Card.Suit.D) continue;
				for (Card.Suit twos1 : Card.Suit.values()) {
					for (Card.Suit twos2 : Card.Suit.values()) {
						//if (ones2 == twos1) continue;
						pockets[0][0] = new Pocket(new Card(one1,ones1),new Card(one2,ones2));
						pockets[1][0] = new Pocket(new Card(two1,twos1),new Card(two2,twos2));
						long[][] results = equityCalc.calcEnum(pockets);
						System.out.println(Integer.toBinaryString(pockets[0][0].normalizeMatchupBits(pockets[1][0])));
						System.out.println(pockets[0][0].toString() + " vs " + pockets[1][0].toString() + " " + results[0][0] + " " + results[1][0] + " " + results[0][1] + " " + results[1][1] + " numgames: " + results[0][2]);
						matchups++; 
						resultsList1.add(results[0][0]);
						resultsList2.add(results[1][0]);
						matchupsList.add(pockets[0][0].normalizeMatchupBits(pockets[1][0]));
						ties.add(results[0][1]);
					}
				}
			}
		}
		System.out.println("Matchups: " + matchups);
		System.out.println("Normalized Matchups: " + matchupsList.size());
		System.out.println(matchupsList);
		System.out.println("Sizes: " + resultsList1.size() + " " + resultsList2.size());
		System.out.println(resultsList1);
		System.out.println(resultsList2);
		System.out.println("Ties: " + ties.size());
		System.out.println(ties);
		System.out.println(Integer.toBinaryString(51<<18));
		System.out.println(Integer.toBinaryString(51));
		
	}
	
	public void buildBinaryFile() {
		try {
			binOutput = new DataOutputStream(new FileOutputStream("matrix.dat"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			bigP();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			binOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Holds all the p(int,int,int) calls generated from writeJavaMatrixCode()
	 * @throws IOException if a binary write cannot be performed, passes it back
	 */
	private void bigP() throws IOException {
	}
	
	private void p(int index, long win1, long win2) throws IOException {
		binOutput.writeInt(index);
		binOutput.writeLong(win1);
		binOutput.writeLong(win2);
	}

	@Override
	public void updateEquity(float[] percentage, int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkStop() {
		// TODO Auto-generated method stub
		return false;
	}

}
