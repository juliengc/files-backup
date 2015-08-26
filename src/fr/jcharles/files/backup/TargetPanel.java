package fr.jcharles.files.backup;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class TargetPanel {
	private final JPanel pane = new JPanel();
	private final JPanel target = new JPanel();
	private final JPanel progress = new JPanel();
	private final JProgressBar fileProgress = new JProgressBar(0, 10000);
	private final JLabel fileProgressLbl = new JLabel("000,00%");
	private final JProgressBar totalProgress = new JProgressBar(0, 10000);
	private final JLabel totalProgressLbl = new JLabel("000,00%");
	private final JLabel actionLbl = new JLabel("Inspection...");
	private final JLabel speedLbl = new JLabel("0,00 o/s");
	private final Gui parent;
	private JPanel progressBorder = new JPanel(new CardLayout());

	
	public TargetPanel (Gui parent) {
		this.parent = parent;
		target.setLayout(new GridLayout(7, 1));
		target.add(new JLabel());
		target.add(new JLabel());
		target.add(new JLabel());
		target.add(new JLabel());
		target.add(new JLabel());
		target.add(new JLabel());
		target.add(new JLabel());
		JPanel tmp = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tmp.add(target);
		pane.setLayout(new BorderLayout());
		pane.add(tmp, BorderLayout.NORTH);
		
		progressBorder.add(progress);
		pane.add(progressBorder, BorderLayout.CENTER);
		
		progress.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridLayout gl = new GridLayout(5, 1);
		gl.setVgap(5);
		progress.setLayout(gl);
		progress.add(actionLbl);
		JPanel progress1 = new JPanel(new BorderLayout(5, 5));
		progress1.add(fileProgress, BorderLayout.CENTER);
		progress1.add(fileProgressLbl, BorderLayout.EAST);
		progress.add(progress1);
		progress.add(new JLabel(""));
		tmp = new JPanel();
		tmp.add(new JLabel("Progression totale:"));
		tmp.add(speedLbl);
		progress.add(tmp);

		JPanel progress2 = new JPanel(new BorderLayout(5, 5));

		progress2.add(totalProgress, BorderLayout.CENTER);
		progress2.add(totalProgressLbl, BorderLayout.EAST);
		progress.add(progress2);
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
	}
	
	public JPanel getPane() {
		return pane;
	}



	public void inspectTarget(Target t, int idx) {
		JLabel lbl = (JLabel) target.getComponent(0);
		lbl.setText("Traitement de la cible " + idx +": ");
		lbl = (JLabel) target.getComponent(1);
		lbl.setText("  src:  " + t.getSrc());
		lbl = (JLabel) target.getComponent(2);
		lbl.setText("  dest: " + t.getDest());
		parent.update();
	}

	public void setElementsCount(int size) {
		JLabel lbl = (JLabel) target.getComponent(3);
		if (size > 0) {
			lbl.setText(size + " éléments au total.");
		}
		else {
			lbl.setText("");
		}
		parent.update();
	}
	
	public void setActionsCount(int dirs, int backup, long backupsize, 
			int update, long updatesize) {
		int idx = 4;
		JLabel lbl = (JLabel) target.getComponent(idx);
		if (dirs > 0) {
			lbl.setText(dirs +  " répertoires à créer.");
			idx++;
		}
		else {
			lbl.setText("");
		}
		
		lbl = (JLabel) target.getComponent(idx);
		if (backup > 0) {
			lbl.setText(formatFileSize(backupsize) + " à sauvegarder dans " + backup + " fichiers.");
			idx++;
		}
		else {
			lbl.setText("");
		}
		
		lbl = (JLabel) target.getComponent(idx);
		if (update > 0) {
			lbl.setText(formatFileSize(updatesize) + " à sauvegarder dans " + update + " fichiers.");
			idx++;
		}
		else {
			lbl.setText("");
		}
		
		for (int i = idx; i < 8; i++) {
			lbl = (JLabel) target.getComponent(idx);
			lbl.setText("");
		}
	}
	
	private String formatFileSize(long size) {
		String res = "";
		final double ko = 1024L;
		final double mo = ko * 1024;
		final double go = mo * 1024;
		double ds = 0;
		if ((ds = (size / go)) > 1) {
			res = String.format("%.2f Go", ds);
		}
		else if ((ds = (size / mo)) > 1) {
			res = String.format("%.2f Mo", ds);
		}
		else if ((ds = (size / ko)) > 1) {
			res = String.format("%.2f Ko", ds);
		}
		else {
			res = size + " o";
		}
		return res;
	}

	public void progress(int percent, File file, String msg) {
		actionLbl.setText(String.format("%s '%s'.", msg, file));
		totalProgress.setValue(percent);
		totalProgressLbl.setText(String.format("%6.2f%%", percent / 100f));	

	}

	public void progress(int percent, int filepercent, File file, String msg) {

		actionLbl.setText(String.format("%s '%s'.", msg, file));
		totalProgress.setValue(percent);
		totalProgressLbl.setText(String.format("%6.2f%%", percent / 100f));	
		
		fileProgress.setValue(filepercent);
		fileProgressLbl.setText(String.format("%6.2f%%", filepercent / 100f));	
	}

	public void setSrcInspectMode() {
		progressBorder.setBorder(new TitledBorder("Inspection des fichiers sources"));
		


		fileProgress.setValue(10000);
		fileProgressLbl.setText(String.format("%6.2f%%", 100f));
		fileProgress.setEnabled(false);
		fileProgressLbl.setEnabled(false);

		parent.update();
	}
	
	public void setDirMode() {
		progressBorder.setBorder(new TitledBorder("Création des répertoires:"));



		fileProgress.setValue(10000);
		fileProgressLbl.setText(String.format("%6.2f%%", 100f));
		fileProgress.setEnabled(false);
		fileProgressLbl.setEnabled(false);

		parent.update();
	}

	public void setUpdateMode() {

		progressBorder.setBorder(new TitledBorder("Mise à jour des fichiers:"));

		fileProgress.setEnabled(true);
		fileProgressLbl.setEnabled(true);

		parent.update();
	}
	public void setBackupMode() {
		progressBorder.setBorder(new TitledBorder("Sauvegarde des fichiers:"));

		fileProgress.setEnabled(true);
		fileProgressLbl.setEnabled(true);
		parent.update();
	}
	
	public void updateSpeed(double speed, long left) {
		speedLbl.setText(formatTime((long) (left/speed)) + " restantes (" + formatFileSize((long)speed) + "/s)");
		
	}

	private String formatTime(long l) {
		String res;
		res = l % 60 + "s";
		l /= 60;
		if (l > 0) {
			res = l % 60 + "min " + res;
			l/=60;
			if (l > 0) {
				res = l % 60 + "h " + res;
			}
		}
		
		return res;
	}
}
