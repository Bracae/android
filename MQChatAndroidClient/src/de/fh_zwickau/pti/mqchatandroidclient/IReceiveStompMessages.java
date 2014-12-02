package de.fh_zwickau.pti.mqchatandroidclient;

import java.io.Serializable;
import de.fh_zwickau.android.base.architecture.IBindingCallbacks;

/**
 * Dieses Interface definiert die Message Callback Methoden, über die Messages
 * vom Stomp Message Broker an die Activity gesendet werden. <br/>
 * Eine grundsätzliche Design-Entscheidung ist, ob die Messages im Service oder
 * in der Activity erstellt werden! Die generischste Lösung ist, die Messages in
 * der Activity zu erstellen bzw. zu dekodieren und den Service nur zur
 * Weiterleitung an die Messaging Middleware zu nutzen.
 * 
 * @author georg beier
 */
public interface IReceiveStompMessages extends IBindingCallbacks {

    /**
     * callback nach Verbindungsaufbau
     * 
     * @param success
     *            true, wenn Verbindung zum Message Broker aufgebaut werden
     *            konnte
     */
    void onConnection(boolean success);

    /**
     * Error callback bei Message Fehlern
     * 
     * @param error
     *            Fehlerbeschreibung
     */
    void onError(String error);

    /**
     * Eingehende Message wird übermittelt
     * 
     * @param message
     */
    void onStompMessage(Serializable message);

}
