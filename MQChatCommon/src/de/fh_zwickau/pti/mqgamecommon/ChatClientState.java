package de.fh_zwickau.pti.mqgamecommon;

public abstract class ChatClientState {
	protected String name;

	public ChatClientState() {
		this("");
	}

	public ChatClientState(final String stateName) {
		this.name = stateName;
	}

	public void gotAccepted() {
		System.err.println("unexpected event - gotAccepted");
	}

	public void gotAnswerChats(final String text) {
		System.err.println("unexpected event - gotAnswerChats");
	}

	public void gotAnswerChatters(final String text) {
		System.err.println("unexpected event - gotAnswerChatters");
	}

	public void gotChatClosed() {
		System.err.println("unexpected event - gotChatClosed");
	}

	public void gotChatStarted() {
		System.err.println("unexpected event - gotChatStarted");
	}

	public void gotDenied() {
		System.err.println("unexpected event - gotDenied");
	}

	public void gotFail(final String text) {
		System.err.println("unexpected event - gotFail");
	}

	public void gotInvitation(final String text) {
		System.err.println("unexpected event - gotInvitation");
	}

	public void gotLogout() {
		System.err.println("unexpected event - gotLogout");
	}

	public void gotNewChat(final String text) {
		System.err.println("unexpected event - gotNewChat");
	}

	public void gotParticipantEntered(final String participant) {
		System.err.println("unexpected event - gotParticipantEntered");
	}

	public void gotParticipantLeft(final String participant) {
		System.err.println("unexpected event - gotParticipantLeft");
	}

	public void gotParticipating() {
		System.err.println("unexpected event - gotParticipating");
	}

	public void gotRejected() {
		System.err.println("unexpected event - gotRejected");
	}

	public void gotRequest(final String refId) {
		System.err.println("unexpected event - gotRequest");
	}

	public void gotRequestCancelled(final String requestor) {
		System.err.println("unexpected event - gotRequestCancelled");
	}

	public void gotSuccess() {
		System.err.println("unexpected event - gotSuccess");
	}

	public void onAccept(final String refId) {
		System.err.println("unexpected event - onAccept");
	}

	public void onAcceptInvitation() {
		System.err.println("unexpected event - onAcceptInvitation");
	}

	public void onCancel() {
		System.err.println("unexpected event - onCancel");
	}

	public void onChat() {
		System.err.println("unexpected event - onChat");
	}

	public void onChats() {
		System.err.println("unexpected event - onChats");
	}

	public void onChatters() {
		System.err.println("unexpected event - onChatters");
	}

	public void onClose() {
		System.err.println("unexpected event - onClose");
	}

	public void onDeny() {
		System.err.println("unexpected event - onDeny");
	}

	public void onInvite(final String refId) {
		System.err.println("unexpected event - onInvite");
	}

	public void onLeave() {
		System.err.println("unexpected event - onLeave");
	}

	public void onLogin() {
		System.err.println("unexpected event - onLogin");
	}

	public void onLogout() {
		System.err.println("unexpected event - onLogout");
	}

	public void onRegister() {
		System.err.println("unexpected event - onRegister");
	}

	public void onReject(final String refId) {
		System.err.println("unexpected event - onReject");
	}

	public void onRequest(final String chatroomId) {
		System.err.println("unexpected event - onRequest");
	}

	public void onStartChat(final String chatName) {
		System.err.println("unexpected event - onStartChat");
	}
}