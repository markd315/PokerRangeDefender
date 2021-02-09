package library;

public interface EquityCalcObserver {

	void updateEquity (float[] percentage,int progress);
	boolean checkStop();
	
}
