package at.android.chooxe;

import java.io.File;
import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.format.DateUtils;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener,
		Preference.OnPreferenceClickListener {

	private String mVersionName = null;
	private long deletedFilesSize = 0;
	private String unit = "";

	private ListPreference mScanners;
	private ListPreference mLightMode;

	private CheckBoxPreference mDebugMode;

	// private int clickCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getTitleName());

		addPreferencesFromResource(R.xml.preferences);

		initializeComponents();

	}

	private void initializeComponents() {
		Preference feedback = (Preference) findPreference("feedback");
		feedback.setOnPreferenceClickListener(this);
		Preference about = (Preference) findPreference("about");
		about.setOnPreferenceClickListener(this);
		Preference clearcache = (Preference) findPreference("clearcache");
		clearcache.setOnPreferenceClickListener(this);
		Preference rate = (Preference) findPreference("rate");
		rate.setOnPreferenceClickListener(this);

		// Disable some Preferences because they are not used currently
		mDebugMode = (CheckBoxPreference) findPreference("debugModeCB");
		PreferenceCategory mCategory = (PreferenceCategory) findPreference("advanced");
		mCategory.removePreference(mDebugMode);
		getPreferenceScreen().removePreference(mDebugMode);
		mCategory = (PreferenceCategory) findPreference("help");
		mCategory.removePreference(about);
		// getPreferenceScreen().removePreference(mDebugMode);

		mScanners = (ListPreference) findPreference("scannerList");
		mLightMode = (ListPreference) findPreference("lightMode");

		mScanners
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						int index = mScanners.findIndexOfValue(newValue
								.toString());
						if (index == 0) // zxing
							mLightMode.setEnabled(true);
						else
							mLightMode.setEnabled(false);
						return true;
					}
				});
	}

	private String getTitleName() {

		try {
			mVersionName = getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Settings " + " (Chooxe v" + mVersionName + ")";
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		// TODO Auto-generated method stub
		// if (arg0.getKey() == "editTextYoffset") {
		// System.out.println(arg1.toString());
		// }
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		if (preference.getKey().contains("about")) {

			Builder aboutDialog = new AlertDialog.Builder(this);
			aboutDialog.setTitle(R.string.about);
			aboutDialog.setMessage("Chooxe	(v" + mVersionName
					+ ")\n\n(c) SK Mobile Development\nAll Rights Reserved");

			aboutDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			aboutDialog.create().show();

			return true;

		} else if (preference.getKey().contains("feedback")) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			// emailIntent.setType("plain/text");
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { "qr.chooxe@gmail.com" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					this.getPackageName());
			startActivity(Intent.createChooser(emailIntent,
					getString(R.string.send_e_mail)));

			return true;

		} else if (preference.getKey().contains("clearcache")) {
			int numDeletedFiles = clearCacheFolder(getApplicationContext()
					.getCacheDir(), 0);

			if (numDeletedFiles != -1) {
				convertUnit();
				Toast.makeText(
						getApplicationContext(),
						String.format("Deleted %d files (%d %s)",
								numDeletedFiles, deletedFilesSize, unit),
						Toast.LENGTH_SHORT).show();
			}
			return true;
		} else if (preference.getKey().contains("rate")) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			String uri = "market://details?id=at.android.chooxe";
			intent.setData(Uri.parse(uri));
			startActivity(intent);
		}
		
		// else if (preference.getKey().contains("debugModeCB")) {
		// System.out.println("FIRE");
		// if (clickCount++ == 2) {
		// mDebugMode.setEnabled(true);
		// return true;
		// }
		// }

		return false;
	}

	private void convertUnit() {
		if (deletedFilesSize > 1024 * 1024) {
			unit = "MB";
			deletedFilesSize /= 1024 * 1024;
		} else if (deletedFilesSize > 1024) {
			unit = "KB";
			deletedFilesSize /= 1024;
		} else {
			unit = "Bytes";
		}
	}

	private int clearCacheFolder(final File dir, final int numDays) {

		int deletedFiles = 0;
		deletedFilesSize = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {

					// first delete subdirectories recursively
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, numDays);
					}

					// then delete the files and subdirectories in this dir
					// only empty directories can be deleted, so subdirs have
					// been done first
					if (child.lastModified() < new Date().getTime() - numDays
							* DateUtils.DAY_IN_MILLIS) {
						deletedFilesSize += child.length();
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Error",
						Toast.LENGTH_SHORT).show();
				System.out.println("error deleting cache");
				return -1;
			}
		}
		return deletedFiles;
	}
}
