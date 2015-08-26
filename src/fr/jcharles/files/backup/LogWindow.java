package fr.jcharles.files.backup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class LogWindow {
	private final JFrame win;
	private final JTextArea text = new JTextArea();
	private final Gui gui;
	public LogWindow(final Gui gui) {
		this.gui = gui;
		win = new JFrame("Journal d'erreurs");
		win.setPreferredSize(new Dimension(400, 300));
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout(5, 5));
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		pane.add(new JLabel("Erreurs:"), BorderLayout.NORTH);
		pane.add(new JScrollPane(text), BorderLayout.CENTER);
		text.setText("");
		win.add(pane);
		win.pack();
		win.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				gui.setErrorLogBtnEnabled(true);
				
			}
		});
	}

	public void println(String msg) {
		System.err.println(msg);
		gui.setErrorLogBtnEnabled(!win.isVisible());
		Document doc = text.getDocument();
		try {
			doc.insertString(doc.getEndPosition().getOffset() -1, msg + "\n", null);
		} catch (BadLocationException e) {
			System.err.println(e.getMessage());
		}
		
	}

	public boolean isEmpty() {
		return text.getDocument().getLength() == 0;
	}

	public void setVisible(boolean b) {
		win.setVisible(true);
		gui.setErrorLogBtnEnabled(false);
	}
}
