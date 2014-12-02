package de.fh_zwickau.pti.mqgamecommon;

public interface ChatServerMessageProducer {
	// Beantragte Teilnahme eines anderen Chatters akzeptieren
	void onAccept(String refId) throws Exception;

	// Einladung annehmen
	void onAcceptionInvitation() throws Exception;

	// Beantragte Teilnahme wieder zur�ckziehen
	void onCancel() throws Exception;

	// Nachricht an Chatroom senden
	void onChat(String text) throws Exception;

	// Abfrage nach aktiven Chats
	void onChats() throws Exception;

	// Abfrage nach verf�gbaren Chattern
	void onChatters() throws Exception;

	// Chat schlie�en
	void onClose() throws Exception;

	// Einladung ablehnen
	void onDeny() throws Exception;

	// Client einladen
	void onInvite(String refId) throws Exception;

	// Aktuellen Chatroom verlassen
	void onLeave() throws Exception;

	// Login abschicken
	void onLogin(String uname, String pword) throws Exception;

	// Logout abschicken
	void onLogout() throws Exception;

	// Registrierung abschicken
	void onRegister(String uname, String pword) throws Exception;

	// Beantragte Teilnahme eines anderen Chatters ablehnen
	void onReject(String refId) throws Exception;

	// Teilnahme beantragen
	void onRequestParticipation(String chatroomId) throws Exception;

	// Neuen Chatroom erstellen
	void onStartChat(String chatroomName) throws Exception;

	void setMessageReceiver(ChatServerMessageReceiver messageReceiver);
}