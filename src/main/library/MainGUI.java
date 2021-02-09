package library;

/*
 * Main Frame for PokerTools software. The panel is switched out for the different tools. 
 * for now only evaluator
 * 
 * CopyRight Nathan Dunn
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

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager.*;
import javax.swing.UIManager;

public class MainGUI extends JFrame {

	private EvaluatorPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainGUI() {
		setTitle("Poker Tools   idontchop.com");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 300);
		
		//nimbus
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			//no nimbus, other?
		}
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		contentPane = new EvaluatorPanel();
		setContentPane(contentPane);
	}

	EvaluatorPanel getCurrentContentPane() {
		return contentPane;
	}
	
	public void go() {
		this.setVisible(true);
	}
}
