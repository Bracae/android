package de.fh_zwickau.pti.jms.gui;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * the login screen
 * 
 * @author Jose Uhlig
 * 
 */
public class LoginPanel extends JPanel {
	private static final long serialVersionUID = 1222795328472564336L;
	private final JButton btnHelp = new JButton("Help");
	private final JButton btnLogin = new JButton("Login");
	private final JButton btnRegister = new JButton("Register");
	private ChatSwingClient chatSwingClient;
	private final JPasswordField passwordField;
	private final JTextField textField;

	/**
	 * initialise the login screen
	 * 
	 * @param csc
	 *            the chatswingclient
	 */
	public LoginPanel(final ChatSwingClient csc) {
		this.setChatSwingClient(csc);
		this.setLayout(null);
		this.setSize(500, 300);
		final JLabel label = new JLabel("Login Screen");
		label.setBounds(162, 11, 200, 50);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
		this.add(label);
		this.passwordField = new JPasswordField();
		this.passwordField.setBounds(219, 146, 200, 20);
		this.passwordField.setToolTipText("Insert password here");
		this.add(this.passwordField);
		final JLabel label_1 = new JLabel("Name");
		label_1.setBounds(74, 108, 80, 23);
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
		this.add(label_1);
		final JLabel label_2 = new JLabel("Password");
		label_2.setBounds(74, 142, 80, 23);
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
		this.add(label_2);
		this.btnLogin.setBounds(56, 217, 108, 35);
		this.btnLogin.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
		this.add(this.btnLogin);
		this.btnHelp.setBounds(335, 217, 108, 35);
		this.btnHelp.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
		this.add(this.btnHelp);
		this.textField = new JTextField();
		this.textField.setBounds(219, 112, 200, 20);
		this.textField.setColumns(10);
		this.add(this.textField);
		this.btnRegister.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
		this.btnRegister.setBounds(201, 217, 108, 35);
		this.add(this.btnRegister);
	}

	/**
	 * @return the login button
	 */
	public JButton getBtnLogin() {
		return this.btnLogin;
	}

	/**
	 * @return the register button
	 */
	public JButton getBtnRegister() {
		return this.btnRegister;
	}

	/**
	 * @return the chatSwingClient
	 */
	public ChatSwingClient getChatSwingClient() {
		return this.chatSwingClient;
	}

	/**
	 * @return the login name field
	 */
	public JTextField getTxtName() {
		return this.textField;
	}

	/**
	 * @return the password field
	 */
	public JPasswordField getTxtPassword() {
		return this.passwordField;
	}

	/**
	 * lock the buttons and textfields on the login screen
	 */
	public void lockElements() {
		this.textField.setEnabled(false);
		this.passwordField.setEnabled(false);
		this.btnLogin.setEnabled(false);
		this.btnHelp.setEnabled(false);
	}

	/**
	 * unlock the buttons and textfields on the login screen
	 */
	public void relockElements() {
		this.textField.setEnabled(true);
		this.passwordField.setEnabled(true);
		this.btnLogin.setEnabled(true);
		this.btnHelp.setEnabled(true);
	}

	/**
	 * @param chatSwingClient
	 *            the chatSwingClient to set
	 */
	public void setChatSwingClient(final ChatSwingClient chatSwingClient) {
		this.chatSwingClient = chatSwingClient;
	}
}