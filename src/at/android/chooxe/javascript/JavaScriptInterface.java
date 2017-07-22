package at.android.chooxe.javascript;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;
import at.android.chooxe.MainActivity;
import at.android.chooxe.R;

public class JavaScriptInterface {
	private MainActivity mActivity;
	private String[] mImageUploadParameters;

	public JavaScriptInterface(MainActivity activiy) {
		this.mActivity = activiy;
	}

	public void doSomething(String string) {

		Toast.makeText(mActivity, "Do Something, ;) " + string,
				Toast.LENGTH_SHORT).show();

	}

	public void setSessionId(int id) {
		SharedPreferences sp = mActivity.getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("sessionid", id);
		editor.commit();

		// System.out.println("JS INTERFACE - SetSessionID WAS CALLED!!!!!!! "
		// + id);
		// Toast.makeText(mActivity, "session is was set=" + id,
		// Toast.LENGTH_LONG)
		// .show();
	}

	public void fileUpload(String type, String users_ID, String table, String id) {
		if (type.equalsIgnoreCase("Image")) {

			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");

			if (mImageUploadParameters == null)
				mImageUploadParameters = new String[4];

			mImageUploadParameters[0] = type; // ("Image"|"File")
			mImageUploadParameters[1] = users_ID;
			mImageUploadParameters[2] = table; // e.g. Channels
			mImageUploadParameters[3] = id;

			mActivity.startActivityForResult(photoPickerIntent, 99);

		} else if (type.equalsIgnoreCase("File")) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			try {
				mActivity.startActivityForResult(Intent.createChooser(intent,
						mActivity.getString(R.string.select_a_file_to_upload)),
						88);
			} catch (android.content.ActivityNotFoundException ex) {
				// Potentially direct the user to the Market with a Dialog
				Toast.makeText(mActivity, "Please install a File Manager.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public String[] getImageUploadParameters() {
		return mImageUploadParameters;
	}

	public void shareAsSMS(String message, String phonenumber) {

		String uri = "sms:" + phonenumber;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(uri));
		intent.putExtra("sms_body", message);

		mActivity.startActivity(intent);
	}

	public void shareAsEmail(String message, String emailAddress, String subject) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// emailIntent.setType("plain/text");
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { emailAddress });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

		mActivity
				.startActivity(Intent.createChooser(emailIntent, "Send eMail"));
	}

	public void setButtonBarVisiblity(boolean visibility) {

		// System.out.println(mActivity.toString());
		if (mActivity != null /* && Build.VERSION.SDK_INT >= 14 */)
			mActivity.setButtonBarVisibility(visibility);
	}

	public void openURLinExternalBrowser(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		mActivity.startActivity(i);

	}
}