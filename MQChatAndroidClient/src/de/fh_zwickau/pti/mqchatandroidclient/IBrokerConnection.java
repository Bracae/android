package de.fh_zwickau.pti.mqchatandroidclient;

/**
 * Dieses Interface definiert die Methoden zur Verbindung mit dem Stomp Message
 * Broker
 * 
 * @author georg beier
 */
public interface IBrokerConnection {

    /**
     * Baue Verbindung zu einem Stomp Message Broker auf.
     * 
     * @param url
     *            broker url
     * @param port
     *            verwendeter port
     * @param user
     *            login name beim broker
     * @param pw
     *            passwort beim broker
     */
    public abstract void connect(String url, int port, String user, String pw);

    /**
     * baue die Verbindung zum Message Broker wieder ab
     */
    public abstract void disconnect();

    /**
     * teste Status der Verbindung zum Message Broker
     * 
     * @return true, wenn verbunden
     */
    public abstract boolean isConnected();
}