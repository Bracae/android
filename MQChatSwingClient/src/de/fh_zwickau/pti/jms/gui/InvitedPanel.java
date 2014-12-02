package de.fh_zwickau.pti.jms.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class InvitedPanel extends JPanel {
	private static final long serialVersionUID = -1480908633763373732L;
	private final JButton btnAccept = new JButton("Accept");
	private final JButton btnDeny = new JButton("Deny");
	private final JLabel labelMessage = new JLabel();
	private final int x, y;

	/**
	 * initialise the jpanel
	 * 
	 * @param x
	 *            the width of the panel
	 * @param y
	 *            the height of the panel
	 */
	public InvitedPanel(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.labelMessage.setSize(200, 30);
		this.labelMessage.setLocation(80, (this.y / 2) - 200);
		this.btnAccept.setSize(100, 40);
		this.btnAccept.setLocation((this.x / 2) - 50, (this.y / 2) - 90);
		this.btnDeny.setSize(100, 40);
		this.btnDeny.setLocation((this.x / 2) - 50, (this.y / 2) - 40);
		this.add(this.labelMessage);
		this.add(this.btnAccept);
		this.add(this.btnDeny);
		this.setLocation(0, 0);
		this.setSize(x, y);
		this.setLayout(null);
		this.setVisible(true);
	}

	public JButton getBtnAccept() {
		return this.btnAccept;
	}

	public JButton getBtnDeny() {
		return this.btnDeny;
	}

	public JLabel getLabelMessage() {
		return this.labelMessage;
	}
}