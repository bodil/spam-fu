package tv.bodil.spamlol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SpamlolActivity extends Activity implements Runnable {

	private static final String BLACKLIST_URL = "http://www.telefonterror.no/uke_alle/terrorliste_navn_nr.txt";
	
	private static Pattern blacklistRe = Pattern.compile("^([0-9]+),(.*)$");
	
	private ProgressDialog progressDialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(refreshButtonListener);
        updateStatusText();
    }
    
    private OnClickListener refreshButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		refresh();
    	}
    };
    
    private void refresh() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.refresh_title));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.setCancelable(true);
		progressDialog.show();
		new Thread(this).start();
    }

	@Override
	public void run() {
		SQLiteDatabase db = new BlacklistOpenHelper(getApplicationContext()).getWritableDatabase();
		db.beginTransaction();
		db.delete(BlacklistOpenHelper.BLACKLIST_TABLE_NAME, null, null);

		try {
			URL url = new URL(BLACKLIST_URL);
			URLConnection c = url.openConnection();
			c.connect();
			Message message = handler.obtainMessage(2);
			message.arg1 = c.getContentLength();
			handler.sendMessage(message);
			
			BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "iso-8859-15"));
			int total = 0;
			String line;
			while ((line = r.readLine()) != null) {
				total += line.length();
				Matcher m = blacklistRe.matcher(line);
				if (m.matches()) {
					ContentValues values = new ContentValues();
					values.put(BlacklistOpenHelper.KEY_NUMBER, m.group(1));
					values.put(BlacklistOpenHelper.KEY_COMPANY, m.group(2));
					db.insert(BlacklistOpenHelper.BLACKLIST_TABLE_NAME, null, values);
				}
				message = handler.obtainMessage(1);
				message.arg1 = total;
				handler.sendMessage(message);
			}
			db.setTransactionSuccessful();
		} catch (IOException e) {
			Log.e("Spamlol", "IO exception when loading blacklist.", e);
			handler.sendEmptyMessage(3);
		} finally {
			db.endTransaction();
		}
		
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 0:
				progressDialog.dismiss();
		        updateStatusText();
				break;
			case 1:
				progressDialog.setProgress(message.arg1);
				break;
			case 2:
				progressDialog.setMax(message.arg1);
				break;
			case 3:
				progressDialog.dismiss();
				Toast.makeText(getBaseContext(), R.string.refresh_io_error, Toast.LENGTH_LONG);
		        updateStatusText();
				break;
			}
		}

	};

	protected void updateStatusText() {
		SQLiteDatabase db = new BlacklistOpenHelper(getApplicationContext()).getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT count(*) FROM " + BlacklistOpenHelper.BLACKLIST_TABLE_NAME, null);
		if (cur.moveToFirst()) {
			int total = cur.getInt(0);
			TextView text = (TextView) findViewById(R.id.status_text);
			text.setText((total > 0) ? getString(R.string.status_text, total) : getString(R.string.status_text_empty));
		}
	}
}
