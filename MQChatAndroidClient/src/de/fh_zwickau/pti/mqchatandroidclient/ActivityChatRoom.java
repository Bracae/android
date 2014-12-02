package de.fh_zwickau.pti.mqchatandroidclient;

import android.app.Activity;
import android.os.Bundle;
import de.fh_zwickau.pti.jms.chatclient.android.R;

public class ActivityChatRoom extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	this.setContentView(R.layout.activity_chat_room);
    }
}
