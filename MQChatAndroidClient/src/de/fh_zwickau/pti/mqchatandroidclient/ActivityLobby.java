package de.fh_zwickau.pti.mqchatandroidclient;

import android.app.Activity;
import android.os.Bundle;
import de.fh_zwickau.pti.jms.chatclient.android.R;

/**
 * @author jose uhlig
 */
public class ActivityLobby extends Activity {
    private ClientStateManager stateManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	this.setContentView(R.layout.activity_lobby);
	this.stateManager = new ClientStateManager(this);
	// this.stateManager.initState();
	// this.stateManager.setState(this.stateManager.getLoggedIn());
	// this.stateManager.getCurrentState().onGetChatrooms();
	// ((TextView)
	// findViewById(R.id.textView1)).setText(this.stateManager.getOutputText().getText());
	//((ListView) findViewById(R.id.listView1)).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[] { "blub" })); //$NON-NLS-1$
    }
}