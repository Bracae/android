package de.fh_zwickau.pti.jms.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RequestPanel extends JPanel {
	private static final long serialVersionUID = -7074472941794999394L;
	private JButton btnAbort = new JButton("Abbrechen");
	private JLabel labelMessage = new JLabel();
	private int x, y;

	/**
	 * initialise the request panel
	 * 
	 * @param x
	 *            the width of the panel
	 * @param y
	 *            the heigth of the panel
	 */
	public RequestPanel(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.labelMessage.setSize(200, 60);
		this.labelMessage.setLocation(50, (this.y / 2) - 200);
		this.btnAbort.setSize(120, 40);
		this.btnAbort.setLocation((this.x / 2) - 60, (this.y / 2) - 90);
		this.add(this.labelMessage);
		this.add(this.btnAbort);
		this.setLocation(0, 0);
		this.setSize(x, y);
		this.setLayout(null);
		this.setVisible(true);
	}

	/**
	 * @return the btnAbort
	 */
	public JButton getBtnAbort() {
		return this.btnAbort;
	}

	/**
	 * @return the labelMessage
	 */
	public JLabel getLabelMessage() {
		return this.labelMessage;
	}

	/**
	 * @return the x
	 */
	@Override
	public int getX() {
		return this.x;
	}

	/**
	 * @return the y
	 */
	@Override
	public int getY() {
		return this.y;
	}

	/**
	 * @param btnAbort
	 *            the btnAbort to set
	 */
	public void setBtnAbort(final JButton btnAbort) {
		this.btnAbort = btnAbort;
	}

	/**
	 * @param labelMessage
	 *            the labelMessage to set
	 */
	public void setLabelMessage(final JLabel labelMessage) {
		this.labelMessage = labelMessage;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(final int x) {
		this.x = x;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(final int y) {
		this.y = y;
	}
}