package de.fh_zwickau.pti.jms.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;

/**
 * Klasse stellt ein Chatfenster dar. Es repräsentiert den Zustand "InOwnChat"
 * bzw. "InOtherChat"
 * 
 * @author Rene Fritzsch
 */
public class ChatFrame extends JFrame {
	private static final long serialVersionUID = -6916566711807104152L;
	private final JButton btnLeaveChatroom = new JButton(
			"<html><div align=\"center\">Chatroom<br>verlassen</div></html>");
	private final JButton btnRequests = new JButton(
			"<html><div align=\"center\">Requests</div></html>");
	private final OutputTextArea chatBox = new OutputTextArea(20); //
	private ChatSwingClient chatSwingClient;
	private final JButton closeButton = new JButton(
			"<html><div align=\"center\">X</div></html>");
	private final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	private final JButton helpButton = new JButton(
			"<html><div align=\"center\">?</div></html>");
	private final JButton inviteButton = new JButton(
			"<html><div align=\"center\">+</div></html>");
	private final DefaultTableModel modelChatterList = new DefaultTableModel();
	private final DefaultTableModel modelRequesterList = new DefaultTableModel();
	private JScrollPane receivedAreaScrollpanel, sendAreaScrollpanel, userList,
			requesterList;
	private final JButton sendButton = new JButton("Send");
	private JTable tableChatterList, tableRequesterList;
	private final JTextArea writeBox = new JTextArea();

	/**
	 * Konstruktor der Klasse
	 */
	public ChatFrame(final ChatSwingClient chatSwingClient) {
		super("Chat");
		this.setChatSwingClient(chatSwingClient);
		this.setSize(550, 350);
		this.setLocation((this.d.width / 2) - 50, (this.d.height / 2)
				- (500 / 2));
		this.setResizable(false);
		this.setLayout(null);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.initElements();
		this.initTables();
	}

	public void addRequester(final String requester) {
		this.modelRequesterList.addRow(new Object[] { requester });
	}

	/*
	 * private void exitApp() { if (JOptionPane.showConfirmDialog(null,
	 * "Quit Chat?", "Quit", JOptionPane.YES_NO_OPTION) ==
	 * JOptionPane.YES_OPTION) { System.exit(1); } }
	 */

	public void clearAndShowChattingFrame(final boolean isOwnChat) {
		this.setVisible(true);
		this.setTitle("Host");

		if (isOwnChat == false) {
			this.inviteButton.setVisible(false);
			this.closeButton.setVisible(false);
			this.btnLeaveChatroom.setVisible(true);
			this.btnRequests.setVisible(false);
			this.setTitle("User");
		} else {
			this.inviteButton.setVisible(true);
			this.closeButton.setVisible(true);
			this.btnLeaveChatroom.setVisible(false);
			this.btnRequests.setVisible(true);
			this.userList.setVisible(true);
			this.requesterList.setVisible(true);
		}
		this.writeBox.setText("");
		this.chatBox.setText("");
		this.chatBox.deleteInputListe();
		for (int i = this.modelChatterList.getRowCount() - 1; i >= 0; i--) {
			this.modelChatterList.removeRow(i);
		}
		for (int i = this.modelRequesterList.getRowCount() - 1; i >= 0; i--) {
			this.modelRequesterList.removeRow(i);
		}
	}

	public void clearAndShowTableChatterList() {
		for (int i = this.modelChatterList.getRowCount() - 1; i >= 0; i--) {
			this.modelChatterList.removeRow(i);
		}
		this.requesterList.setVisible(false);
		this.userList.setVisible(true);
	}

	public JButton getBtnCloseChatroom() {
		return this.closeButton;
	}

	public JButton getBtnInvite() {
		return this.inviteButton;
	}

	public JButton getBtnLeaveChatroom() {
		return this.btnLeaveChatroom;
	}

	public JButton getBtnRequests() {
		return this.btnRequests;
	}

	public JButton getBtnSend() {
		return this.sendButton;
	}

	/**
	 * @return the chatSwingClient
	 */
	public ChatSwingClient getChatSwingClient() {
		return this.chatSwingClient;
	}

	public JTable getTableChatterList() {
		return this.tableChatterList;
	}

	public JTable getTableRequesterList() {
		return this.tableRequesterList;
	}

	public JTextArea getTxtAreaMessageInput() {
		return this.writeBox;
	}

	public void hideChattingFrame() {
		this.setVisible(false);
	}

	public void hideTableChatterList() {
		this.userList.setVisible(false);
	}

	public void hideTableRequesterList() {
		this.requesterList.setVisible(false);
	}

	/**
	 * Wird von der Methode gotNewChat(String text) genutzt, um die Nachrichten
	 * im Empfangsfeld (receivedArea) hinzuzuf�gen
	 */
	public void print(final String string) {
		this.chatBox.appendTextToTextArea(string);
		final DefaultCaret caret = (DefaultCaret) this.chatBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}

	public void removeRequester(final String pRequester) {
		String requestors = "";
		for (int i = 0; i < this.tableRequesterList.getRowCount(); i++) {
			requestors = requestors
					+ this.tableRequesterList.getValueAt(i, 0).toString()
					+ "\n";
		}
		for (int i = this.modelRequesterList.getRowCount() - 1; i >= 0; i--) {
			this.modelRequesterList.removeRow(i);
		}
		final String[] aRequestors = requestors.split("\n");
		for (final String requestor : aRequestors) {
			if (!requestor.equals(pRequester)) {
				this.modelRequesterList.addRow(new Object[] { requestor });
			}
		}
	}

	public void setActiveChatters(final String chatters) {
		final String[] aChatters = chatters.split("\n");
		for (final String chatter : aChatters) {
			this.modelChatterList.addRow(new Object[] { chatter });
		}
	}

	/**
	 * @param chatSwingClient
	 *            the chatSwingClient to set
	 */
	public void setChatSwingClient(final ChatSwingClient chatSwingClient) {
		this.chatSwingClient = chatSwingClient;
	}

	public void showTableRequesterList() {
		this.userList.setVisible(false);
		this.requesterList.setVisible(true);
	}

	/**
	 * Initialisiert die Elemente der Benutzeroberfl�che
	 */
	private void initElements() {
		this.chatBox.setEditable(false);
		this.receivedAreaScrollpanel = new JScrollPane(this.chatBox);
		this.receivedAreaScrollpanel.setSize(269, 136);
		this.receivedAreaScrollpanel.setLocation(10, 10);
		this.writeBox.setFont(new Font("Serif", Font.BOLD, 15));
		this.sendAreaScrollpanel = new JScrollPane(this.writeBox);
		this.sendAreaScrollpanel.setSize(269, 87);
		this.sendAreaScrollpanel.setLocation(10, 174);
		this.sendButton.setLocation(309, 209);
		this.sendButton.setSize(117, 50);
		this.inviteButton.setLocation(309, 174);
		this.inviteButton.setSize(27, 23);
		this.helpButton.setLocation(354, 174);
		this.helpButton.setSize(27, 23);
		this.closeButton.setLocation(399, 174);
		this.closeButton.setSize(27, 23);
		this.btnRequests.setLocation(530, 10);
		this.btnRequests.setSize(100, 80);
		this.add(this.receivedAreaScrollpanel);
		this.add(this.sendAreaScrollpanel);
		this.add(this.sendButton);
		this.add(this.inviteButton);
		this.add(this.closeButton);
		this.add(this.helpButton);
		this.add(this.btnLeaveChatroom);
	}

	private void initTables() {
		this.tableChatterList = new JTable(this.modelChatterList) {
			private static final long serialVersionUID = 34436871469607262L;

			@Override
			public boolean isCellEditable(final int x, final int y) {
				return false;
			}
		};
		this.tableChatterList.setFillsViewportHeight(true);
		this.tableChatterList.setRowHeight(35);
		this.modelChatterList.addColumn("send request");
		this.userList = new JScrollPane(this.tableChatterList);
		this.userList.setSize(100, 136);
		this.userList.setLocation(326, 10);
		this.add(this.userList);
		this.userList.setVisible(false);
		this.tableRequesterList = new JTable(this.modelRequesterList) {
			private static final long serialVersionUID = 34436871469607262L;

			@Override
			public boolean isCellEditable(final int x, final int y) {
				return false;
			}
		};
		this.tableRequesterList.setFillsViewportHeight(true);
		this.tableRequesterList.setRowHeight(35);
		this.modelRequesterList.addColumn("Requests");
		this.requesterList = new JScrollPane(this.tableRequesterList);
		this.requesterList.setSize(100, 136);
		this.requesterList.setLocation(430, 10);
		this.add(this.requesterList);
		this.requesterList.setVisible(false);
	}
}