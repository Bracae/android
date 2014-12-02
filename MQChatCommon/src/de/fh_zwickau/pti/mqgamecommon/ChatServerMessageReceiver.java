package de.fh_zwickau.pti.mqgamecommon;

public interface ChatServerMessageReceiver {
	// Meine versandte Einladung wurde akzeptiert
	void gotAccepted();

	// Liste der aktiven Chats und ihrer Initiatoren im Body der Textmessage
	void gotAnswerChats(String text);

	// Lister der verf�gbaren Chatter im Body der Textmessage
	void gotAnswerChatters(String text);

	// Aktueller Chatroom wurde geschlossen
	void gotChatClosed();

	// Chatroom wurde erstellt
	void gotChatStarted();

	// Meine versandte Einladung wurde abgelehnt
	void gotDenied();

	// Login bzw. Registrierung ist fehlgeschlagen
	void gotFail(String text);

	// Einladung zu einem Chatroom erhalten
	void gotInvite(String text);

	// Erfolgreicher Logout
	void gotLogout();

	// Eingehende Nachricht
	void gotNewChat(String text);

	// Neuer Teilnehmer beigetreten
	void gotParticipantEntered(String text);

	// Teilnehmer hat den Chat verlassen
	void gotParticipantLeft(String text);

	// Beantragte Teilnahme wurde akzeptiert
	void gotParticipating();

	// Beantragte Teilnahme wurde abgelehnt
	void gotRejected();

	// Neuer Antragsteller
	void gotRequest(String refId);

	// Antragsteller zieht Antrag zur�ck
	void gotRequestCancelled(String requestor);

	// Erfolgreicher Login bzw. Registrierung
	void gotSuccess();
}