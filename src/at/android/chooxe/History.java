package at.android.chooxe;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import at.android.chooxe.database.HistoryDatabaseHandler;
import at.android.chooxe.database.HistoryItem;

public class History extends ListActivity {

	private HistoryDatabaseHandler historyDatabaseHandler;
	private ArrayAdapter<HistoryItem> adapter;
	private List<HistoryItem> values;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		historyDatabaseHandler = new HistoryDatabaseHandler(this);
		values = historyDatabaseHandler.getAllHistoryItems();

		// adapter = new ArrayAdapter<HistoryItem>(this,
		// android.R.layout.simple_list_item_1, values);

		adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2,
				values) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem row;
				if (convertView == null) {
					LayoutInflater inflater = (LayoutInflater) getApplicationContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					row = (TwoLineListItem) inflater.inflate(
							android.R.layout.simple_list_item_2, null);
				} else {
					row = (TwoLineListItem) convertView;
				}

				// postion = row number
				row.getText1().setText(values.get(position).getUrl());
				row.getText2().setText(values.get(position).getDateTime()); 

				return row;
			}
		};

		setListAdapter(adapter);
		// adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clearall:
			historyDatabaseHandler = new HistoryDatabaseHandler(this);
			historyDatabaseHandler.clearAll();

			values = historyDatabaseHandler.getAllHistoryItems();
			adapter = new ArrayAdapter<HistoryItem>(this,
					android.R.layout.simple_list_item_1, values);
			setListAdapter(adapter);

			Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		HistoryItem hi = (HistoryItem) l.getItemAtPosition(position);
		// System.out.println(hi.getUrl());
		setResult(RESULT_OK, new Intent().putExtra("url", hi.getUrl()));
		finish();

	}

}
