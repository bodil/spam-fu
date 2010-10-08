package tv.bodil.spamlol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, SpamlolService.class);
		if (intent.getStringExtra("state").equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
			Log.d("Spamlol", "Call received: " + intent.getStringExtra("incoming_number"));
			service.putExtra("action", SpamlolService.VERIFY_CALL);
			service.putExtra("incoming_number", intent.getStringExtra("incoming_number"));
		} else {
			service.putExtra("action", SpamlolService.END_CALL);
		}
		context.startService(service);
	}

}
