package de.fh_zwickau.pti.mqgamecommon;

/**
 * @author georg beier
 * 
 */
public enum MessageKind {
	authenticated, chatCancelRequest, chatChat, chatClose, // messages from
															// chatter to
															// chatroom
	chatCreate, chatLeave, chatNewParticipant, chatParticipationRequest, chatterAccepted, chatterChatCreated, chatterClosed, chatterDenied, // messages
																																			// from
																																			// chatroom
																																			// or
																																			// peer
																																			// chatter
																																			// to
																																			// chatter
	chatterInvited, chatterMsgAccept, chatterMsgAcceptInvitation, chatterMsgCancel, chatterMsgChat, chatterMsgChats, chatterMsgChatters, chatterMsgClose, // messages
																																							// from
																																							// client
																																							// to
																																							// chatter
	chatterMsgDeny, chatterMsgInvite, chatterMsgLeave, chatterMsgReject, chatterMsgRequestParticipation, chatterMsgStartChat, chatterNewChat, chatterParticipantEntered, chatterParticipantLeft, chatterParticipationRequest, chatterReject, chatterRequestCanceled, clientAccepted, clientAnswerChat, clientAnswerChatters, clientChatClosed, // messages
																																																																																				// from
																																																																																				// chatter
																																																																																				// to
																																																																																				// client
	clientChatStarted, clientDenied, clientInvitation, clientNewChat, clientParticipantEntered, clientParticipantLeft, clientParticipating, clientRejected, clientRequest, clientRequestCancelled, failed, loggedOut, // basic
																																																						// for
																																																						// session
																																																						// management
	login, logout, register
}