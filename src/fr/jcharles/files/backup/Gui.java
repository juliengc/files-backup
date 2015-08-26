package fr.jcharles.files.backup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Gui {
	private JFrame frame = new JFrame("Sauvegarde");
	private final JPanel config = new JPanel();
	public final TargetPanel target;
	public TargetPanel out;
	public final LogWindow err;
	
	private JButton logBtn = new JButton("Journal d'erreurs");
	private JButton cancelBtn = new JButton("Annuler");
	private JButton quitBtn = new JButton("Quitter");
	private boolean bHasCanceled = false;
	
	public Gui() {
		target = new TargetPanel(this);
		err = new LogWindow(this);
		out = target;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setPreferredSize(new Dimension(500, 330));
		init();
	}
	
	private void init() {
		config.add(new JLabel("Configuration..."));
		frame.pack();
		frame.setLocationRelativeTo(null);
		logBtn.setEnabled(false);
		logBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				err.setVisible(true);
			}
		});
		
		cancelBtn.setEnabled(true);
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancelBtn.setEnabled(false);
//				quitBtn.setEnabled(true);
				bHasCanceled = true;
				
			}
			
		});

		quitBtn.setEnabled(false);
		quitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
			
		});
		
	}
	

	public void showConfigFileWindow() {
		frame.getContentPane().removeAll();
		frame.add(config);
		update();
	}

	public void showTargets(List<Target> targets) {
		frame.setVisible(false);
		frame.getContentPane().removeAll();
		frame.setLayout(new BorderLayout());
		frame.add(target.getPane(), BorderLayout.CENTER);
		if (targets.size() == 1) {
			frame.setTitle("Sauvegarde (" + targets.size() + " cible trouvée)");
		}
		else {
			frame.setTitle("Sauvegarde (" + targets.size() + " cibles trouvées)");
		}
		out = target;
		JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPane.add(logBtn);
		btnPane.add(cancelBtn);
		btnPane.add(quitBtn);
		
		frame.add(btnPane, BorderLayout.SOUTH);
		
		update();
	}

	public boolean hasCanceled() {
		return bHasCanceled;
	}
	public void update() {
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void showQuit() {
		cancelBtn.setEnabled(false);
		quitBtn.setEnabled(true);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setErrorLogBtnEnabled(boolean e) {
		logBtn.setEnabled(e);
	}

	public boolean askIfUpdate(List<RelativeFile> update) {
		JPanel pane = new JPanel(new BorderLayout(5,5));
		JPanel txt = new JPanel(new GridLayout(2, 1));
		if (update.size() == 1) {
			txt.add(new JLabel("Depuis la dernière sauvegarde " + update.size() + 
					" fichier a été modifié."));
			txt.add(new JLabel("Etes-vous sûr de vouloir l'écraser?"));
		}
		else {
			txt.add(new JLabel("Depuis la dernière sauvegarde " + update.size() + 
					" fichiers ont été modifiés."));
			txt.add(new JLabel("Etes-vous sûr de vouloir les écraser?"));
		}
		JTextArea area = new JTextArea();
		String list = "";
		for (RelativeFile rf:update) {
			list += rf.getRelativePath() + "\n";
		}
		area.setText(list);
		area.setEditable(false);
		JScrollPane scroll = new JScrollPane(area);
		scroll.setPreferredSize(new Dimension(450, 120));
		pane.add(scroll);
		
		pane.add(txt, BorderLayout.NORTH);
		int response = JOptionPane.showConfirmDialog(frame, pane, "Mise à jour",
		        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	    if (response == JOptionPane.YES_OPTION) {
	    	return true;
	    } 
		return false;
	}
}
