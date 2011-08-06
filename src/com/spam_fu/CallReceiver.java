package com.spam_fu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, SpamfuService.class);
		if (intent.getStringExtra("state").equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
			Log.d("Spam-FU", "Call received: " + intent.getStringExtra("incoming_number"));
			service.putExtra("action", SpamfuService.VERIFY_CALL);
			service.putExtra("incoming_number", intent.getStringExtra("incoming_number"));
		} else {
			service.putExtra("action", SpamfuService.END_CALL);
		}
		context.startService(service);
	}

}
