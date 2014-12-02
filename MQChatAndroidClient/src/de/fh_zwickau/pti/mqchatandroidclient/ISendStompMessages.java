package de.fh_zwickau.pti.mqchatandroidclient;

import de.fh_zwickau.android.base.architecture.IBindMessageHandler;
import de.fh_zwickau.informatik.stompj.StompMessage;

/**
 * Dieses Interface definiert die Methoden, über die Stomp Messages über den
 * StompCommunicationService an den Stomp Message Broker gesendet werden können<br/>
 * Eine grundsätzliche Design-Entscheidung ist, ob die Messages im Service oder
 * in der Activity erstellt werden! Die generischste Lösung ist, die Messages in
 * der Activity zu erstellen bzw. zu dekodieren und den Service nur zur
 * Weiterleitung an die Messaging Middleware zu nutzen.
 * 
 * @author georg beier
 */
public interface ISendStompMessages extends IBindMessageHandler,
	IBrokerConnection {

    /**
     * Sende eine Stomp Message. Connection zum Message Broker muss vorher
     * aufgebaut sein.
     * 
     * @param message
     *            eine Stomp Message
     * @param destination
     *            (optional) sende Message an gegebene Stomp Destination. Wenn
     *            nicht angegeben, wird die Destination aus der Message genommen
     */
    void sendMessage(StompMessage message, String... destination);
}