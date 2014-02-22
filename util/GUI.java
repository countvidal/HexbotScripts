package util;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JComboBox;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.DefaultComboBoxModel;


public class GUI extends JFrame {
	public String bankArea;
	public String treeArea;
	public String treeName;
	public String axeName;
	public boolean startScript = false;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 348);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLabel lblVcut = new JLabel("VCut - The AIO Woodcutter");
		lblVcut.setFont(new Font("Tempus Sans ITC", Font.PLAIN, 30));
		contentPane.add(lblVcut, BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Settings", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(4, 2, 0, 0));
		
		JLabel lblDesiredCuttingArea = new JLabel("Desired Cutting Area");
		panel_2.add(lblDesiredCuttingArea);
		
		final JComboBox<String> comboBox_1 = new JComboBox<String>();
		comboBox_1.setModel(new DefaultComboBoxModel<String>(new String[] {"draynor"}));
		panel_2.add(comboBox_1);
		
		JLabel lblDesiredBankingArea = new JLabel("Desired Banking Area");
		panel_2.add(lblDesiredBankingArea);
		
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"draynor"}));
		panel_2.add(comboBox);
		
		JLabel lblNewLabel = new JLabel("Tree Name");
		panel_2.add(lblNewLabel);
		
		final JComboBox<String> comboBox_2 = new JComboBox<String>();
		comboBox_2.setModel(new DefaultComboBoxModel<String>(new String[] {"Willow"}));
		panel_2.add(comboBox_2);
		
		JLabel lblAxeName = new JLabel("Axe Name");
		panel_2.add(lblAxeName);
		
		final JComboBox<String> comboBox_3 = new JComboBox<String>();
		comboBox_3.setModel(new DefaultComboBoxModel<String>(new String[] {"Rune axe"}));
		panel_2.add(comboBox_3);
		
		JButton btnStartScript = new JButton("START SCRIPT");
		btnStartScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
				treeArea = comboBox.getSelectedItem().toString();
				bankArea = comboBox_1.getSelectedItem().toString();
				treeName = comboBox_2.getSelectedItem().toString();
				axeName = comboBox_3.getSelectedItem().toString();
				startScript = true;
				System.out.println("GUI Closed"); // TODO add your handling code
													// here:
			}
		});
		contentPane.add(btnStartScript, BorderLayout.SOUTH);
	}
	
	

}
