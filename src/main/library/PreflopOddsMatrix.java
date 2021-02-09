package library;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Stores the pre-calculated values of all Holdem Preflop headsup matchups. This class is meant to be
 * a helper class for equityCalc. Upon initialization, it is assumed it will be used at least once so it
 * builds the HashMap with the preflop odds and stores it in HashMap<Integer, long[]> matrix.
 * See Pockets.normalizematchupbits for explanation on the Key.
 * @author sacred
 *
 */
public class PreflopOddsMatrix {
	HashMap<Integer, long[]> matrix = null;
	String matrixFilename = "/matrix.ser";  
	
	public PreflopOddsMatrix () {
		buildMatrix();
	}

	/**
	 * separating from constructor. This will read the matrix HashMap from the serial object.
	 */
	@SuppressWarnings("unchecked")
	private void buildMatrix () {
		ObjectInputStream objInput;
		try  {
			objInput = new ObjectInputStream(getClass().getResourceAsStream(matrixFilename));
			matrix = (HashMap<Integer, long[]>) objInput.readObject();
			objInput.close();
		} catch (IOException e) {
			//can't find the file, we just won't have a matrix
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO: in production we can just notify user we couldn't open the matrix
			e.printStackTrace();
		}
		
	}
	/**
	 * Simple get to look for the normalized matchup bitmap. While we only have headsup matchups, the return value is a long[2] holding
	 * wins for each hand. Equity Calc will know the number of games and ties. Later if we put 3 hand matchups in there, will have to return long[6]
	 * which holds wins and ties for each hand.
	 * @param key normalized matchup bitmap
	 * @return long[2] holding wins for each hand, null if not found
	 */
	public long[] getResults(int key) {
		if (matrix == null) return null; //should only happen if we can't find the file
		
		if (!matrix.containsKey(key)) return null; //TODO: do we really want to not throw exception here?
		
		return matrix.get(key);
	}
	
	
	
	
	
	
}
