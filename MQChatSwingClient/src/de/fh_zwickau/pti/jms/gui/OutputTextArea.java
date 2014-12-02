package de.fh_zwickau.pti.jms.gui;

import java.util.ArrayList;

import javax.swing.JTextArea;

/**
 * Die Klasse erweitert die JTextArea Bei zu vielen Einträgen werden die
 * obersten gelöscht
 */
public class OutputTextArea extends JTextArea {
	private static final long serialVersionUID = 1L;
	private final ArrayList<String> inputList = new ArrayList<String>();
	private final Integer lineNumber;

	/**
	 * @param lineNumber
	 *            lineNumber gibt an, wie viele Strings die TextArea anzeigt.
	 *            Bei zu vielen Strings wird nach dem FIFO-Prinzip gelöscht
	 */
	public OutputTextArea(final Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Die Methode zum ausgeben des Textes
	 */
	public void appendTextToTextArea(final String text) {
		this.setText("");
		if (this.inputList.size() >= this.lineNumber) {
			this.inputList.remove(0);
			this.inputList.add(text);
		} else {
			this.inputList.add(text);
		}
		for (int i = 0; i < this.inputList.size(); i++) {
			this.append(this.inputList.get(i) + "\n");
		}
	}

	public void deleteInputListe() {
		this.inputList.clear();
	}
}