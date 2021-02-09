package library;

/*
 * Panel fits inside of MainGUI
 * 
 * May want to pull some of the universal formatting and such into a super class that can be extended 
 * by any other poker tools I put in this panel (which would then be interchangable by user
 * 
 * This class handles getting hands from the user and passing them to EquityCalc. Will need to instantiate 
 * classes for the results panel and progress panel to be passed to EquityCalc.
 */

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.BevelBorder;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;

public class EvaluatorPanel extends JPanel implements EquityCalcObserver {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2657356165397134059L;
	private JTextField boardField;
	private JTextField deadField;

	private JPanel []handContainer = new JPanel[6];
	private JLabel []handTitleLabel = new JLabel[6];
	private JButton []handKeyButton = new JButton[6];
	private JTextField []handField = new JTextField[6];
	private JButton []handClearButton = new JButton[6];
	private JLabel []handResultLabel = new JLabel[6];
	String handString = "HAND ";  //title of each hand  %s %d where %d is number of hand

	JPanel centerContainer = new JPanel();
	JPanel enumContainer = new JPanel();
	JLabel enumTitleLbl = new JLabel("Calculate Full:");
	JProgressBar enumProgressBar;
	JButton enumGoBtn = new JButton("GO");
	
	JLabel enumEstimateLbl = new JLabel("Estimated Time:");
	JPanel enumEstimateContainer = new JPanel();
	JLabel enumCalcsLbl = new JLabel("1M");
	JLabel enumTimeLbl = new JLabel(":45");
	
	
	private JButton boardClearBtn = new JButton("X");
	private JButton deadClearBtn = new JButton("X");
	
	/* State Global Variables
	 * 
	 */
	
	EnumInProgress enumInProgress;
	
	/**
	 * Create the panel.
	 */
	public EvaluatorPanel() {
		
		enumInProgress = EnumInProgress.NO;
		
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JPanel playerContainer = new JPanel();
		add(playerContainer, BorderLayout.WEST);
		playerContainer.setLayout(new GridLayout(6, 1, 5, 5));
		
		for (int cc=0; cc<6; cc++) {  //set up each hand using class arrays
		
			handContainer[cc] = new JPanel();
			handContainer[cc].setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
			handContainer[cc].setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(handContainer[cc]);
			
			
			handTitleLabel[cc] = new JLabel(String.format("%s%d", handString,cc+1));
			handTitleLabel[cc].setFont(new Font("Times New Roman", Font.PLAIN, 12));
			handContainer[cc].add(handTitleLabel[cc]);
			
			handKeyButton[cc] = new JButton("K");
			handKeyButton[cc].setFont(new Font("Times New Roman", Font.PLAIN, 8));
			handContainer[cc].add(handKeyButton[cc]);
			
			handField[cc] = new JTextField();
			handField[cc].setFont(new Font("Times New Roman", Font.PLAIN, 12));
			handContainer[cc].add(handField[cc]);
			handField[cc].setColumns(12);
			
			handClearButton[cc] = new JButton("X");
			handClearButton[cc].setFont(new Font("Times New Roman", Font.PLAIN, 8));
			handClearButton[cc].addActionListener(new ClearBtnListener());
			handContainer[cc].add(handClearButton[cc]);
			
			handResultLabel[cc] = new JLabel();
			handContainer[cc].add(handResultLabel[cc]);
			
			//eclipse stuff
			/*JPanel player2Container = new JPanel();
			player2Container.setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(player2Container);
			
			JLabel player2TitleLbl = new JLabel("HAND 2");
			player2TitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player2Container.add(player2TitleLbl);
			
			JButton player2KeyBtn = new JButton("K");
			player2KeyBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player2Container.add(player2KeyBtn);
			
			player2HandField = new JTextField();
			player2HandField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player2HandField.setColumns(12);
			player2Container.add(player2HandField);
			
			player2ClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player2ClearBtn.addActionListener(new ClearBtnListener());
			player2Container.add(player2ClearBtn);
			
			JPanel player2ResultPanel = new JPanel();
			player2Container.add(player2ResultPanel);
			
			JPanel player3Container = new JPanel();
			player3Container.setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(player3Container);
			
			JLabel player3TitleLbl = new JLabel("HAND 3");
			player3TitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player3Container.add(player3TitleLbl);
			
			JButton player3KeyboardBtn = new JButton("K");
			player3KeyboardBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player3Container.add(player3KeyboardBtn);
			
			player3HandField = new JTextField();
			player3HandField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player3HandField.setColumns(12);
			player3Container.add(player3HandField);
			
			player3ClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player3ClearBtn.addActionListener(new ClearBtnListener());
			player3Container.add(player3ClearBtn);
			
			JPanel player3ResultPanel = new JPanel();
			player3Container.add(player3ResultPanel);
			
			JPanel player4Container = new JPanel();
			player4Container.setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(player4Container);
			
			JLabel player4TitleLbl = new JLabel("HAND 4");
			player4TitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player4Container.add(player4TitleLbl);
			
			JButton player4KeyboardBtn = new JButton("K");
			player4KeyboardBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player4Container.add(player4KeyboardBtn);
			
			player4HandField = new JTextField();
			player4HandField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player4HandField.setColumns(12);
			player4Container.add(player4HandField);
			
			player4ClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player4ClearBtn.addActionListener(new ClearBtnListener());
			player4Container.add(player4ClearBtn);
			
			JPanel player4ResultPanel = new JPanel();
			player4Container.add(player4ResultPanel);
			
			JPanel player5Container = new JPanel();
			player5Container.setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(player5Container);
			
			JLabel player5TitleLbl = new JLabel("HAND 5");
			player5TitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player5Container.add(player5TitleLbl);
			
			JButton player5KeyboardBtn = new JButton("K");
			player5KeyboardBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player5Container.add(player5KeyboardBtn);
			
			player5HandField = new JTextField();
			player5HandField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player5HandField.setColumns(12);
			player5Container.add(player5HandField);
			
			player5ClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player5ClearBtn.addActionListener(new ClearBtnListener());
			player5Container.add(player5ClearBtn);
			
			JPanel player5ResultPanel = new JPanel();
			player5Container.add(player5ResultPanel);
			
			JPanel player6Container = new JPanel();
			player6Container.setBorder(new LineBorder(Color.WHITE, 1, true));
			playerContainer.add(player6Container);
			
			JLabel player6TitleLbl = new JLabel("HAND 6");
			player6TitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player6Container.add(player6TitleLbl);
			
			JButton player6KeyboardBtn = new JButton("K");
			player6KeyboardBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player6Container.add(player6KeyboardBtn);
			
			player6HandField = new JTextField();
			player6HandField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			player6HandField.setColumns(12);
			player6Container.add(player6HandField);
			
			player6ClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			player6ClearBtn.addActionListener(new ClearBtnListener());
			player6Container.add(player6ClearBtn);
			
			JPanel player6ResultPanel = new JPanel();
			player6Container.add(player6ResultPanel); */
		
		}
			

		add(centerContainer, BorderLayout.CENTER);
		centerContainer.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel communityContainer = new JPanel();
		centerContainer.add(communityContainer);
		communityContainer.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel boardContainer = new JPanel();
		communityContainer.add(boardContainer);
		boardContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel boardTitleLbl = new JLabel("Board:");
		boardTitleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		boardTitleLbl.setVerticalAlignment(SwingConstants.TOP);
		boardContainer.add(boardTitleLbl);
		boardTitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		
		boardField = new JTextField();
		boardContainer.add(boardField);
		boardField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		boardField.setColumns(14);
		
		boardClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
		boardClearBtn.addActionListener(new ClearBtnListener());
		boardContainer.add(boardClearBtn);
		
		
		JPanel deadContainer = new JPanel();
		communityContainer.add(deadContainer);
		deadContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel deadTitleLbl = new JLabel("Dead:");
		deadTitleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		deadTitleLbl.setVerticalAlignment(SwingConstants.TOP);
		deadTitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		deadContainer.add(deadTitleLbl);
		
		deadField = new JTextField();
		deadField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		deadField.setColumns(14);
		deadContainer.add(deadField);
		
		deadClearBtn.setFont(new Font("Times New Roman", Font.PLAIN, 8));
		deadClearBtn.addActionListener(new ClearBtnListener());
		deadContainer.add(deadClearBtn);
		
		buildEnumStart();
		centerContainer.add(enumContainer);

	}
	
	class ClearBtnListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			
			
			for (int cc = 0; cc<handClearButton.length;cc++) {
				if (event.getSource() == handClearButton[cc] ) {
					handField[cc].setText("");
					break;
				}
			}
			if (event.getSource() == boardClearBtn) {
				boardField.setText("");
			} else if (event.getSource() == deadClearBtn) {
				deadField.setText("");
			}
			
		}
		
	}
	
	private enum EnumInProgress {
		/*
		 * Global variable enumInProgress will store this value. The panel should always be the same as this
		 * also, checkStop will use this
		 */
		YES,
		NO
	}
	
	public void verifyEnumPanel () {
		/*
		 * TODO: fix this mess
		 */
		if (enumInProgress == EnumInProgress.NO) buildEnumStart();
		else buildEnumInProgress();
	}
	
	private void buildEnumStart () {
		/*
		 * This function will reset the panel for starting an enumeration
		 */

		enumContainer.removeAll();
		enumTitleLbl = new JLabel("Calculate Full:");
		enumGoBtn = new JButton("GO");
		
		enumEstimateLbl = new JLabel("Estimated Time:");
		enumEstimateContainer = new JPanel();
		enumCalcsLbl = new JLabel("1M");
		enumTimeLbl = new JLabel(":45");

		enumContainer.setBorder(new SoftBevelBorder(BevelBorder.RAISED, Color.WHITE, null, null, null));

		enumContainer.setLayout(new GridLayout(2, 2, 0, 0));
		

		enumTitleLbl.setHorizontalAlignment(SwingConstants.CENTER);
		enumTitleLbl.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		enumContainer.add(enumTitleLbl);
		
		enumGoBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));
		enumGoBtn.addActionListener(new EnumGo(this));
		enumContainer.add(enumGoBtn);
		enumEstimateLbl.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		enumEstimateLbl.setHorizontalAlignment(SwingConstants.CENTER);
		enumContainer.add(enumEstimateLbl);
		
		enumContainer.add(enumEstimateContainer);
		enumEstimateContainer.setLayout(new GridLayout(2, 0, 0, 0));
		
		enumCalcsLbl.setHorizontalAlignment(SwingConstants.CENTER);
		enumCalcsLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		enumEstimateContainer.add(enumCalcsLbl);
		
		enumTimeLbl.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		enumTimeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		enumEstimateContainer.add(enumTimeLbl);
		
		enumContainer.repaint();


	}
	
	public void buildEnumInProgress () {
		/*
		 * This function builds the panel while an enumeration is in progress.
		 * TODO: make prettier
		 */
		enumContainer.removeAll();
		enumProgressBar = new JProgressBar();
		enumProgressBar.setValue(0);
		enumProgressBar.setStringPainted(true);
		enumContainer.add(enumProgressBar);
		
		enumGoBtn = new JButton("STOP");
		enumGoBtn.setFont(new Font("Times New Roman", Font.BOLD,14));
		enumGoBtn.addActionListener(new EnumStop());
		enumContainer.add(enumGoBtn);
		enumContainer.repaint();
		
		
	
	}
	
	class EnumStop implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			enumInProgress = EnumInProgress.NO;
			verifyEnumPanel();
		}
	}
	
	class EnumGo implements ActionListener,Runnable {
		EquityCalcObserver observer;
		EquityCalc equityCalc;
		Pocket [][]pockets;
		Card []board;
		Card []dead;

		
		public EnumGo (EquityCalcObserver observer) {
			this.observer = observer;
		}
		
		public void actionPerformed (ActionEvent event) {
			/*
			 * When the go button is pressed, we have to get the strings from the hands and board and pass them to HoldemStrings and then to equitycalc
			 * then we'll just punt this class off to a thread?
			 */
			
			/*
			 * TODO: figure out how to update the proper hand if the moron user skipped a hand.
			 */
			
			int size=0;
			
			
			for (int cc=0; cc<handContainer.length;cc++) {
				if (handField[cc].getText().length() > 1 && HoldemStrings.pocketsToArray(handField[cc].getText()).length!=0) size++;
			}
			
			if (size<2) return;
			pockets = new Pocket[size][];
			
			for (int cc = 0; cc<handContainer.length;cc++) {
				if (handField[cc].getText().length() > 1) { //we have at least two characters so should be able to find pockets
					
					if (HoldemStrings.pocketsToArray(handField[cc].getText()).length > 0) {
					pockets[cc] = HoldemStrings.pocketsToArray(handField[cc].getText());
					}
				}
			}
			
			board = HoldemStrings.cardsToArray(boardField.getText());
			dead = HoldemStrings.cardsToArray(deadField.getText());

			equityCalc = new EquityCalc(observer);
			
			Thread myThread = new Thread(this);
			enumInProgress = EnumInProgress.YES;
			myThread.start();
			verifyEnumPanel();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (board.length>0 && dead.length>0) {
				equityCalc.calcEnum(pockets,board,dead);
			} else if (board.length>0) {
				equityCalc.calcEnum(pockets,board);
			} else if (dead.length>0) {
				equityCalc.calcEnum(pockets,board,dead);
			} else equityCalc.calcEnum(pockets);
	
		}
	}

	@Override
	public void updateEquity(float[] percentage, int progress) {

		/*
		 * TODO: colors for whether in progress or completed.  progress bar.
		 */
		for (int cc =0;cc<handResultLabel.length;cc++) {
			if (cc >= percentage.length) {
				handResultLabel[cc].setText(""); handResultLabel[cc].repaint();
			} else 	handResultLabel[cc].setText(String.format("%.2f",percentage[cc]));
		}
		
		if(enumProgressBar!=null && enumInProgress == EnumInProgress.YES) {enumProgressBar.setValue(progress); enumProgressBar.repaint();}
		if (progress == 100 ) { enumInProgress = EnumInProgress.NO; verifyEnumPanel(); }
	}
	
	@Override
	public boolean checkStop() {
		if (enumInProgress == EnumInProgress.NO) return true;
		else return false;
	}


}
