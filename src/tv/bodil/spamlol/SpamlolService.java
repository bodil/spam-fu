package tv.bodil.spamlol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SpamlolService extends Service {
	
	public static final String VERIFY_CALL = "verify_call";
	public static final String END_CALL = "end_call";
	public static final String REFRESH = "refresh";
	private static final String KEEPALIVE = "keepalive";
	
	private Toast toast = null;
	
	@Override
	public void onCreate() {
		Log.d("Spamlol", "Service.onCreate");
	}
	
	@Override
	public void onDestroy() {
		Log.d("Spamlol", "Service.onDestroy");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Context context = getApplicationContext();
		String action = intent.getStringExtra("action");
		
		if (action.equals(KEEPALIVE)) {
			keepAlive(context);
		} else if (action.equals(VERIFY_CALL)) {
			String number = intent.getStringExtra("incoming_number");
			
			SQLiteDatabase db = new BlacklistOpenHelper(context).getReadableDatabase();
			Cursor cur = db.query(BlacklistOpenHelper.BLACKLIST_TABLE_NAME,
					new String[]{ BlacklistOpenHelper.KEY_COMPANY },
					BlacklistOpenHelper.KEY_NUMBER + " = ?", new String[]{ number },
					null, null, null);
			if (cur.moveToFirst()) {
				String company = cur.getString(0);
				Log.d("Spamlol", "Service.onStartcommand: query returned company \"" + company + "\"");
				clearToast();
				
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.toast, null);
				TextView text = (TextView) layout.findViewById(R.id.toast_text);
				text.setText(getString(R.string.spam_warning, company));
				
				toast = new Toast(context);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.setView(layout);
				toast.setDuration(Toast.LENGTH_LONG);
				keepAlive(context);
			} else {
				Log.d("Spamlol", "Service.onStartCommand: query returned no results");
			}
		} else if (action.equals(END_CALL)) {
			clearToast();
		}
		
		return START_NOT_STICKY;
	}
	
	private void clearToast() {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
	}
	
	private void keepAlive(Context context) {
		if (toast == null) {
			return;
		}
		toast.show();
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, SpamlolService.class);
		i.putExtra("action", KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		long t = SystemClock.elapsedRealtime() + 1000; // one second interval
		mgr.set(AlarmManager.ELAPSED_REALTIME, t, pi);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
