package de.fh_zwickau.pti.jms.gui;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * the panel for the lobby and the chatroomlist
 * 
 * @author Jose Uhlig
 * 
 */
public class LobbyPanel extends JPanel {
	private static final long serialVersionUID = 474333634304022242L;
	private final JButton btnLogout = new JButton("Logout");
	private final JButton btnNewRoom = new JButton("New Room");
	private ChatSwingClient chatSwingClient;
	private final JLabel lbluser = new JLabel("username");
	private final DefaultTableModel modelChatRoomList = new DefaultTableModel();
	private JTable tblRooms, tblLobby;

	/**
	 * Create the panel.
	 */
	public LobbyPanel(final String username, final ChatSwingClient csc) {
		this.setChatSwingClient(csc);
		this.setLayout(null);
		this.setSize(500, 300);
		this.lbluser.setHorizontalAlignment(SwingConstants.LEFT);
		this.lbluser.setBounds(10, 11, 174, 30);
		this.lbluser.setText(username);
		this.add(this.lbluser);
		this.modelChatRoomList.addColumn("current chatrooms");
		this.tblRooms = new JTable(this.modelChatRoomList) {
			private static final long serialVersionUID = 3084738690908991446L;

			@Override
			public boolean isCellEditable(final int x, final int y) {
				return false;
			}
		};
		this.tblRooms.setColumnSelectionAllowed(true);
		this.tblRooms.setCellSelectionEnabled(true);
		this.tblRooms.setFillsViewportHeight(true);
		this.tblRooms.setRowHeight(35);
		this.tblRooms.setShowVerticalLines(false);
		this.tblRooms.setShowHorizontalLines(false);
		this.tblRooms
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.tblRooms.setBounds(285, 86, 205, 140);
		this.add(this.tblRooms);
		this.btnNewRoom.setBounds(355, 228, 89, 23);
		this.add(this.btnNewRoom);
		this.btnLogout.setBounds(68, 228, 89, 23);
		this.add(this.btnLogout);
		this.setTblLobby(new JTable());
		this.getTblLobby().setShowVerticalLines(false);
		this.getTblLobby().setShowHorizontalLines(false);
		this.getTblLobby().setShowGrid(false);
		this.getTblLobby().setRowSelectionAllowed(false);
		this.getTblLobby().setBounds(10, 87, 205, 140);
		this.add(this.getTblLobby());
		final JLabel lblNewLabel_1 = new JLabel("Whistlebuster");
		lblNewLabel_1.setFont(new Font("Comic Sans MS", Font.BOLD, 22));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(268, 0, 222, 33);
		this.add(lblNewLabel_1);
		final JLabel lblNewLabel_2 = new JLabel("Lobby");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(10, 56, 205, 30);
		this.add(lblNewLabel_2);
		final JLabel lblNewLabel_3 = new JLabel("Chat Rooms");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setBounds(285, 56, 205, 30);
		this.add(lblNewLabel_3);
	}

	public JButton getBtnNewChat() {
		return this.btnNewRoom;
	}

	public JTable getChatRoomTable() {
		return this.tblRooms;
	}

	/**
	 * @return the chatSwingClient
	 */
	public ChatSwingClient getChatSwingClient() {
		return this.chatSwingClient;
	}

	public JButton getLogoutButton() {
		return this.btnLogout;
	}

	/**
	 * @return the tblLobby
	 */
	public JTable getTblLobby() {
		return this.tblLobby;
	}

	public void setActiveChatRooms(final String chatRooms) {
		for (int i = this.modelChatRoomList.getRowCount() - 1; i >= 0; i--) {
			this.modelChatRoomList.removeRow(i);
		}
		final String[] chatRoomsStr = chatRooms.split("\n");
		for (final String chatter : chatRoomsStr) {
			this.modelChatRoomList.addRow(new Object[] { chatter });
		}
	}

	/**
	 * @param chatSwingClient
	 *            the chatSwingClient to set
	 */
	public void setChatSwingClient(final ChatSwingClient chatSwingClient) {
		this.chatSwingClient = chatSwingClient;
	}

	/**
	 * @param tblLobby
	 *            the tblLobby to set
	 */
	public void setTblLobby(final JTable tblLobby) {
		this.tblLobby = tblLobby;
	}

	/**
	 * Wird eine Einladung durch gotInvite(String text) empfangen, ruft diese
	 * implizit diese Methode auf welche sich um das Anzeigen des Dialogfeldes
	 * kümmert
	 */
	public void showInvite(final String refId) {
		if (JOptionPane
				.showConfirmDialog(
						null,
						"Sie wurden von "
								+ refId
								+ "zu einem Chat eingeladen.\nWollen Sie die Einladung annehmen?",
						"Einladung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		}
	}

	/**
	 * Wird von gotRejected() aufgerufen, um in einem Dialogfeld ersichtlich zu
	 * machen das die Anfrag an einen Chatroom abgelehnt wurde
	 */
	public void showRejected() {
		JOptionPane
				.showMessageDialog(
						null,
						"Ihre Anfrage an dem Chatroom teilnehmen zu dürfen, wurde abgelehnt!",
						"Hinweis", JOptionPane.PLAIN_MESSAGE);
	}
}