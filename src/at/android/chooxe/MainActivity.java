package at.android.chooxe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sourceforge.zbar.Symbol;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import at.android.chooxe.database.HistoryDatabaseHandler;
import at.android.chooxe.database.HistoryItem;
import at.android.chooxe.javascript.JavaScriptInterface;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity implements OnClickListener,
		Camera.PictureCallback {

	private static final String START_PAGE = "http://chooxe.me/";
	private boolean mDebugMode;
	private static final boolean debugModeStartup = false;
	private static final boolean debugModeCamera = false;
	private static final boolean debugModeGCM = false;

	private static final boolean videoAudioRecording = false;

	private ImageButton mButtonScan;
	private Builder mScannerNotInstalledDialog;

	private boolean mAutoScan;
	private WebView mWebView;
	private boolean mOnCreateCalled = false;
	private HistoryDatabaseHandler historyDatabaseHandler;
	private ValueCallback<Uri> mUploadMessage;
	private boolean mShareLocation;
	private ImageButton mButtonActionMode;
	private SurfaceView mSurfaceView = null;
	private Camera mCamera = null;
	private boolean mInCameraPreview = false;
	private boolean cameraConfigured = false;
	private ImageButton mButtonPhotos;
	private Button mButtonVideo;
	private boolean mBundleDataExists = false;
	private Activity mActivity = this;
	private String mScanner;
	private LinearLayout mButtonBarLinearLayout;
	private Handler mProgressHandler;
	// private boolean mIsPaused = false;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "370928762040";

	public static final String KEY_FRONT_LIGHT_MODE = "preferences_front_light_mode";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String regid;
	private boolean doSendRegIdToBackend = false;
	private ProgressBar Pbar;
	private boolean finishChooxeWithToast = false;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	private boolean mVideoRecordingInProgress = false;
	private Button mButtonAudio;
	private boolean mAudioRecordingInProgress = false;
	private String mFileName;
	private JavaScriptInterface jsInterface;
	private FrameLayout mWebViewPlaceholder;
	private ImageView mImageSplashScreen;
	protected boolean mDoHideSplashScreen = true;
	private boolean mActionModeEnabled = false;
	private boolean mIsInOfflineState = false;
	protected SslErrorHandler handlerSsl;
	private String mRotationFromPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOnCreateCalled = true;

		// TODO: Check removed from here because it should only be sent to the
		// server once
		doSendRegIdToBackend = true; // if called to often move to bundle

		context = getApplicationContext();

		setContentLayout();
		loadPreferences();
		initializeDatabase();
		initializeComponents();
		initializeWebView();

		// Check device for Play Services APK.
		if (checkPlayServices()) {
			if (debugModeGCM)
				System.out.println("Found valid Google Play Service APK");

			gcm = GoogleCloudMessaging.getInstance(this);

			// get the registration id from the property storage of the device
			regid = getRegistrationId(context);

			if (TextUtils.isEmpty(regid)) {
				if (debugModeGCM) {
					System.out
							.println("registration id not found -> we need to register");
				}
				registerInBackground();
			}

		} else {
			if (debugModeGCM)
				System.out.println("No valid Google Play Services APK found.");
		}

		// first application start
		if (savedInstanceState == null) {
			// doSendRegIdToBackend = true;

			if (mAutoScan)
				openQRCodeScanner();

			// Show the loading Screen at first app start
			// mWebView.setBackgroundColor(getResources().getColor(
			// R.color.splashBackground));

			// Show splash screen
			mImageSplashScreen.setVisibility(View.VISIBLE);
			mImageSplashScreen.setBackgroundColor(getResources().getColor(
					R.color.splashBackground));

		} else {
			if (debugModeStartup)
				System.out.println(savedInstanceState.toString());

			// Resuming the application

			// Restore WebView State
			mWebView.restoreState(savedInstanceState);

			// if we restore the state of the webview, we also have to create a
			// new
			// javascript interface object to call methods from within the
			// webpage
			mWebView.getSettings().setJavaScriptEnabled(true);
			jsInterface = new JavaScriptInterface(this);
			mWebView.addJavascriptInterface(jsInterface, "JSInterface");

			if (debugModeStartup)
				System.out.println("OPEN URL restoreState");
			mBundleDataExists = true;
		}

		if (debugModeStartup)
			System.out.println("OnCreate() called");

		if (mDebugMode) {
			if (debugModeGCM)
				System.out.println("Saving RegID to file in GCM folder");
			writeToLog(regid, "RegistrationID.txt", "GoogleCloudMessage");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		// show error dialog if problem occurs
		checkPlayServices();

		enableActionMode(false);

		// come back to this activity put code here
		if (!mOnCreateCalled) {
			loadPreferences();
			if (debugModeStartup)
				System.out.println("Main: onResume()");
		}

		// resume flash and javascript etc
		callHiddenWebViewMethod(mWebView, "onResume");
		mWebView.resumeTimers();

		// not needed anymore because no actionbar is available
		// ActivityCompat.invalidateOptionsMenu(this);

		if (debugModeStartup)
			System.out.println("Main: onResume() everytime");

		mOnCreateCalled = false;

		// INITIAL loading of WebView content
		if (mWebView != null && !mBundleDataExists && isOnline()) {

			Uri data = getIntent().getData();
			// loading from Intent Filter from e.g. Google Search or Browser
			if (data != null) {
				System.out.println("test " + data.toString());
				if (data.toString().contains("chooxe")) {
					openInWebView(data.toString());
					// System.out.println("test open special");
				}

			} else { // default loading of START_Page OR
						// from NOTIDICATION when
						// App was closed (If not closed OnNewIntent() is
						// called)
						// or SHARE MENU

				String urlFromNotification = getIntent().getStringExtra(
						"notificationurl");

				String action = getIntent().getAction();

				// NOTIFICATION
				if (urlFromNotification != null
						&& urlFromNotification.contains("chooxe"))
					openInWebView(urlFromNotification);

				// SHARE MENU (android.intent.action.SEND)
				else if (action != null && action.equals(Intent.ACTION_SEND)) {
					String shareUrl = START_PAGE;
					Bundle extras = getIntent().getExtras();

					// IMAGE - VIDEO
					if (extras.containsKey(Intent.EXTRA_STREAM)) {
						// Get resource path
						// video temporarily deactivated in manifest file
						if (debugModeStartup)
							System.out.println("test image or video received "
									+ extras.toString());

						// System.out.println("test locations:"
						// + extras.getString(Intent.EXTRA_STREAM
						// .toString()));

						Uri imageUri = (Uri) getIntent().getExtras().get(
								Intent.EXTRA_STREAM);

						String filePath = imageUri.toString();
						// String s2 = imageUri.getPath();

						int sessionId = getSessionId();

						if (sessionId > 0 && filePath.length() > 0) {
							// Filepath as string
							// type; // ("Image"|"File")
							// users_ID;
							// table; // e.g. Channels
							// id;
							try {
								uploadImageInBackground(filePath, "Image",
										String.valueOf(sessionId), "", "");

								shareUrl = "https://chooxe.me/?appShare";

							} catch (Exception e) {
								e.printStackTrace();

								Toast.makeText(this,
										"error: uploadImageInBackground",
										Toast.LENGTH_SHORT).show();

							}
						}

						// YOUTUBE
					} else if (extras.containsKey(Intent.EXTRA_TEXT)) {
						System.out.println("test youtube TEXT received "
								+ extras.toString());
						System.out
								.println("test link:"
										+ extras.getString(Intent.EXTRA_TEXT
												.toString()));

					}

					openInWebView(shareUrl);

				} else {
					// DEFAULT PAGE
					if (debugModeStartup)
						System.out.println("load default page");

					openInWebView(START_PAGE);
				}
			}

		} else if (!isOnline()) {
			Toast.makeText(this, R.string.not_online, Toast.LENGTH_SHORT)
					.show();
			mIsInOfflineState = true;

		}

		// registering the NetworkChangeReceiver for connection changes
		NetworkChangeReceiver ncr = new NetworkChangeReceiver(this);
		registerReceiver(ncr, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// This is called when the activity is started.
		// From Notification or From Launcher Icon
		String urlFromNotification = intent.getStringExtra("notificationurl");

		if (urlFromNotification != null
				&& urlFromNotification.contains("chooxe.me")) {
			System.out.println("test6 open from notificaton"
					+ urlFromNotification);
			openInWebView(urlFromNotification);
		}
		super.onNewIntent(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (debugModeStartup)
			System.out.println("onConfigurationChanged(Configuration)");

		if (mWebView != null) {
			// Remove the WebView from the old placeholder
			mWebViewPlaceholder.removeView(mWebView);
		}

		// here we save back if action mode was enabled before orientation
		// change
		boolean reEnable = mActionModeEnabled;

		// for safety reason we disable the preview
		enableActionMode(false);

		// here the actual rotation is triggered.
		super.onConfigurationChanged(newConfig);

		// Load the layout resource for the new configuration
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			setContentView(R.layout.activity_main);
		else
			setContentView(R.layout.activity_main_land);

		// Reinitialize the UI
		initializeComponents();
		initializeWebView();

		// we have to set
		enableActionMode(reEnable);

	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();

			} else {
				Toast.makeText(this, "GCM - This device is not supported.",
						Toast.LENGTH_SHORT).show();
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			if (debugModeGCM)
				System.out.println("Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			if (debugModeGCM)
				System.out.println("App version changed.");
			return "";
		}
		if (debugModeGCM)
			System.out.println("Existing Reg ID found: "/* + registrationId */);
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private int getSessionId() {
		SharedPreferences sp = getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);

		return sp.getInt("sessionid", 0);

	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		SSLSocketFactory sf = null;
		try {
			KeyStore trustStore = null;
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sf = new MySSLSocketFactory(trustStore);

		} catch (Exception e) {
			e.printStackTrace();
		}

		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schReg.register(new Scheme("https", sf, 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

		Matrix matrix = new Matrix();
		switch (orientation) {
		case ExifInterface.ORIENTATION_NORMAL:
			return bitmap;
		case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
			matrix.setScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.setRotate(180);
			break;
		case ExifInterface.ORIENTATION_FLIP_VERTICAL:
			matrix.setRotate(180);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_TRANSPOSE:
			matrix.setRotate(90);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.setRotate(90);
			break;
		case ExifInterface.ORIENTATION_TRANSVERSE:
			matrix.setRotate(-90);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.setRotate(-90);
			break;
		default:
			return bitmap;
		}
		try {
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * uploadImageInBackground is used for Image file upload
	 * 
	 * @param filePath
	 * @param string4
	 * @param string3
	 * @param users_ID
	 * @param type
	 */
	private void uploadImageInBackground(String filePath, String type,
			String users_ID, String table, String id) {

		// sendNotification("upload", getString(R.string.image_upload),
		// getString(R.string.upload_started_in_background));

		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... arguments) {

				String url = "https://chooxe.me/services/file?method=uploadApp";

				try {
					String filePath = arguments[0];
					int fileLength = 0;

					runOnUiThread(new Runnable() {
						public void run() {
							if (mWebView != null)
								mWebView.loadUrl("javascript:appUploadStart()");
						}
					});

					// HttpClient httpclient = new DefaultHttpClient();
					// we need our own httpClient because ssl certificates were
					// not accepted...
					HttpClient httpclient = createHttpClient();
					HttpPost httpPost = new HttpPost(url);

					MultipartEntityBuilder builder = MultipartEntityBuilder
							.create();
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

					if (filePath != null) {
						Bitmap bitmap = null;
						Bitmap finalBitmap = null;
						File file = null;
						File newFile = null;
						final int maxSize = 1024;

						// Uri is passed
						if (filePath.startsWith("content")) {
							Uri imageUri = Uri.parse(filePath);
							// Uri imageUri = data.getData();
							// bitmap = MediaStore.Images.Media.getBitmap(
							// mActivity.getContentResolver(), imageUri);

							// if problems with getting BIG images - use this
							bitmap = getThumbnail(imageUri, maxSize);
						} else {
							file = new File(filePath);

							// get bitmap from file
							BitmapFactory.Options bmOptions = new BitmapFactory.Options();
							bitmap = BitmapFactory.decodeFile(
									file.getAbsolutePath(), bmOptions);
						}

						int outWidth;
						int outHeight;
						int inWidth = bitmap.getWidth();
						int inHeight = bitmap.getHeight();

						// we only scale if the the image is bigger than 1024px
						if (inWidth > maxSize || inHeight > maxSize) {

							if (inWidth > inHeight) {
								outWidth = maxSize;
								outHeight = (inHeight * maxSize) / inWidth;
							} else {
								outHeight = maxSize;
								outWidth = (inWidth * maxSize) / inHeight;
							}

							finalBitmap = Bitmap.createScaledBitmap(bitmap,
									outWidth, outHeight, true);

							// // we evaluate the orientation
							// ExifInterface exif = null;
							// try {
							// exif = new ExifInterface(filePath);
							// } catch (IOException e) {
							// String stackTrace = Log.getStackTraceString(e);
							// // e.printStackTrace();
							// return stackTrace;
							// }
							// int orientation = exif.getAttributeInt(
							// ExifInterface.TAG_ORIENTATION,
							// ExifInterface.ORIENTATION_NORMAL);
							//
							// // rotate the image here
							// finalBitmap = rotateBitmap(bitmap, orientation);
						} else {
							finalBitmap = bitmap;
						}

						// compress bitmap to file
						if (finalBitmap != null
								&& finalBitmap.getByteCount() > 0) {

							String file_path = Environment
									.getExternalStorageDirectory()
									.getAbsolutePath()
									+ "/ChooxeImages";

							File dir = new File(file_path);
							if (!dir.exists())
								dir.mkdirs();

							String fileName;
							if (file != null)
								fileName = file.getName();
							else
								fileName = "AppShare" + new Date().getTime()
										+ ".jpg";

							newFile = new File(dir, fileName);

							FileOutputStream fOut = new FileOutputStream(
									newFile);

							finalBitmap.compress(Bitmap.CompressFormat.JPEG,
									100, fOut);
							finalBitmap.recycle();

							fOut.flush();
							fOut.close();

							bitmap.recycle();

							fileLength = (int) newFile.length() / 1024;

						}

						if (newFile != null) {
							builder.addTextBody("type", arguments[1]);
							builder.addTextBody("users_ID", arguments[2]);
							builder.addTextBody("table", arguments[3]);
							builder.addTextBody("id", arguments[4]);
							builder.addBinaryBody("File", newFile);

							HttpEntity entity = builder.build();
							httpPost.setEntity(entity);
						} else {
							return "Error: newFile == null";
						}
					}

					HttpResponse response = httpclient.execute(httpPost);

					HttpEntity res = response.getEntity();
					String result = EntityUtils.toString(res);
					int code = response.getStatusLine().getStatusCode();

					httpclient.getConnectionManager().shutdown();

					return getString(R.string.finished_) + " " + fileLength
							+ "KB" + " Code:" + code + " Result:" + result;

				} catch (Exception e) {
					String stackTrace = Log.getStackTraceString(e);
					// e.printStackTrace();
					return stackTrace;
				}

				// return "Error";
			}

			protected void onProgressUpdate(Integer... progress) {
				// setProgressPercent(progress[0]);
			}

			protected void onPostExecute(String resultString) {

				// runs on UI Thread!

				if (mWebView != null) {
					mWebView.loadUrl("javascript:appUploadComplete()");
				}

				if (mDebugMode) {
					writeToLog(resultString, "debug.txt", "debug");
					// sendNotification("upload",
					// getString(R.string.image_upload),
					// resultString);
					
					// Toast.makeText(mActivity, result,
					// Toast.LENGTH_LONG).show();
				}

			}
		}.execute(filePath, type, users_ID, table, id);
	}

	// we need this to avoid out of memory error
	public Bitmap getThumbnail(Uri uri, int maxSize)
			throws FileNotFoundException, IOException {

		int THUMBNAIL_SIZE = maxSize;
		InputStream input = this.getContentResolver().openInputStream(uri);

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither = true;// optional
		onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		input.close();
		if ((onlyBoundsOptions.outWidth == -1)
				|| (onlyBoundsOptions.outHeight == -1))
			return null;

		int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
				: onlyBoundsOptions.outWidth;

		double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE)
				: 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true;// optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		input = this.getContentResolver().openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		input.close();
		return bitmap;
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0)
			return 1;
		else
			return k;
	}

	/**
	 * uploadInBackground is used for Audio and Video file upload
	 * 
	 * @param args
	 */
	private void uploadInBackground(String args) {

		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... arguments) {

				String url = "http://chooxe.me/services/media?method=upload";

				try {
					String filename = arguments[0].toString();
					File file = new File(filename);

					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(url);

					MultipartEntityBuilder builder = MultipartEntityBuilder
							.create();
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

					builder.addBinaryBody("file", file);
					builder.addTextBody("filename", filename);
					builder.addTextBody("type", "");

					HttpEntity entity = builder.build();
					httpPost.setEntity(entity);

					HttpResponse response = httpclient.execute(httpPost);

					int code = response.getStatusLine().getStatusCode();

					return String.valueOf(code);
					// Do something with response...

				} catch (Exception e) {
					e.printStackTrace();
				}

				return "error (exception handling)";
			}

			protected void onProgressUpdate(Integer... progress) {
				// setProgressPercent(progress[0]);
			}

			protected void onPostExecute(String result) {
				// System.out.println("async " + result);
				// run on UI Thread

				Toast.makeText(mActivity, "Code: " + result, Toast.LENGTH_SHORT)
						.show();
			}
		}.execute(args, null, null);
	}

	public void sendNotification(String type, String title, String message) {

		int NOTIFICATION_ID;
		int icon;

		if (type.compareToIgnoreCase("upload") == 0) {
			NOTIFICATION_ID = 8;
			icon = R.drawable.ic_menu_upload_you_tube;
		} else {
			return;
		}
		// R.drawable.ic_menu_start_conversation

		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				this.getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

		// TODO: update code for notification from GcmIntentService...
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(icon)
				.setContentTitle(title)
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(message))
				// .setStyle(
				// new NotificationCompat.BigPictureStyle()
				// .bigPicture(drawableToBitmap(getResources()
				// .getDrawable(R.drawable.ic_menu_gallery))))
				.setContentText(message);

		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.

					doSendRegIdToBackend = true;

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// System.out.println(msg);

					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(context,
									"GCM: Service currently not available",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (debugModeGCM)
					System.out.println(msg + "\n");
			}
		}.execute(null, null, null);

	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {

		try {
			if (!TextUtils.isEmpty(regid)
					&& mWebView.getUrl().contains("chooxe")) {

				mWebView.loadUrl("javascript:setAndroidRegistrationId('"
						+ regid + "')");
				if (debugModeGCM)
					System.out.println("Send Reg ID to Backend");

			} else {
				if (debugModeGCM)
					System.out
							.println("Failed sending reg ID because it is empty ("
									+ regid
									+ ") or no chooxe url loaded ("
									+ mWebView.getUrl() + ")");
			}

		} catch (Exception e) {
			if (debugModeGCM)
				System.out.println("Failed sending Reg ID to Backend");
		}

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		if (debugModeGCM)
			System.out.println("Saving regId on app version " + appVersion);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	@Override
	public void onPause() {
		if (mInCameraPreview) {
			mCamera.stopPreview();
			mInCameraPreview = false;
		}
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
			mInCameraPreview = false;
		}

		if (mMediaRecorder != null) {
			mMediaRecorder.release();
			mMediaRecorder = null;
		}

		// pause flash and javascript etc
		callHiddenWebViewMethod(mWebView, "onPause");
		mWebView.pauseTimers();

		// System.out.println("pause webview");

		// We need this so that the startpage is not loaded if we leave
		// the activity for settings and come back afterwards
		mBundleDataExists = true;
		super.onPause();

		if (debugModeStartup)
			System.out.println("OnPause() called");
	}

	private void callHiddenWebViewMethod(final WebView webview,
			final String name) {
		try {
			final Method method = WebView.class.getMethod(name);
			method.invoke(webview);

		} catch (final Exception e) {
		}
	}

	@Override
	public void onBackPressed() {

		// back might have closed the keyboard so we always enable the buttonbar
		setButtonBarVisibility(true);

		if (mWebView.canGoBack()) {
			mWebView.goBack();
			finishChooxeWithToast = false;

		} else if (!isOnline()) {
			finishChooxe();

		} else {
			// finishChooxe();
			finishChooxeWithTaost(finishChooxeWithToast);
			// super.onBackPressed(); // will close the application...
		}

	}

	private void finishChooxeWithTaost(boolean quit) {
		if (quit)
			this.finish();
		else
			Toast.makeText(context, R.string.press_back_again_to_quit_chooxe_,
					Toast.LENGTH_SHORT).show();

		finishChooxeWithToast = true;
	}

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	private void initializeDatabase() {
		historyDatabaseHandler = new HistoryDatabaseHandler(this);

	}

	private void finishChooxe() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Chooxe!");
		builder.setMessage(R.string.do_you_really_want_to_quit_)
				.setCancelable(true)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mActivity.finish();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mWebView.reload();
								dialog.dismiss();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private String getPreviousURLfromHistory() {
		WebBackForwardList mWebBackForwardList = mWebView.copyBackForwardList();

		if (mWebBackForwardList.getCurrentIndex() - 1 >= 0)
			return mWebBackForwardList.getItemAtIndex(
					mWebBackForwardList.getCurrentIndex() - 1).getUrl();
		else
			return "cannotGoBack";
	}

	// Screen Rotation - Home Button pressed - free MEM
	// Saving the Web View data and restore it in OnCreate()
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if (debugModeStartup)
			System.out.println("onSaveInstanceState");

		super.onSaveInstanceState(savedInstanceState);
		mWebView.saveState(savedInstanceState);

		// savedInstanceState.putInt("last_orientation", getOrientation());
	}

	// Bundle is also accessible in onCreate()
	// @Override
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// super.onRestoreInstanceState(savedInstanceState);
	// System.out.println("onRestoreInstanceState");
	// }

	private void setContentLayout() {
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

		WindowManager winMan = (WindowManager) getBaseContext()
				.getSystemService(Context.WINDOW_SERVICE);

		if (winMan.getDefaultDisplay().getRotation() == 0) // Portrait
			setContentView(R.layout.activity_main);
		else
			setContentView(R.layout.activity_main_land);

	}

	private int getRotation() {

		WindowManager winMan1 = (WindowManager) getBaseContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int orientation = winMan1.getDefaultDisplay().getRotation();

		// System.out.println("initial:"+mInitialOrientation+" new:"+orientation);
		return orientation;

	}

	private void initializeComponents() {

		mButtonBarLinearLayout = (LinearLayout) findViewById(R.id.LinearLayoutButtonBar);

		mImageSplashScreen = (ImageView) findViewById(R.id.imageViewSplash);

		mButtonScan = (ImageButton) findViewById(R.id.ButtonScan);
		mButtonScan.setOnClickListener(this);

		mButtonActionMode = (ImageButton) findViewById(R.id.ButtonActionMode);
		mButtonActionMode.setOnClickListener(this);

		mButtonPhotos = (ImageButton) findViewById(R.id.ButtonPhotos);
		mButtonPhotos.setOnClickListener(this);

		mButtonVideo = (Button) findViewById(R.id.ButtonVideo);
		mButtonVideo.setOnClickListener(this);

		mButtonAudio = (Button) findViewById(R.id.ButtonAudio);
		mButtonAudio.setOnClickListener(this);

		Pbar = (ProgressBar) findViewById(R.id.progressbar);

		// mWebView = (WebView) findViewById(R.id.webView1);
		mWebViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(surfaceCallback);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		if (debugModeCamera)
			System.out.println("### init components camerasurface:"
					+ mSurfaceView + " holder:" + mSurfaceHolder
					+ " surfacecallback:" + surfaceCallback);
	}

	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		mAutoScan = prefs.getBoolean("autoScanCB", false);
		mScanner = prefs.getString("scannerList", "zxing");
		if (Build.MODEL.equals("Nexus 5X")) {
			mRotationFromPrefs = prefs.getString("rotationList", "180");
		} else {
			mRotationFromPrefs = prefs.getString("rotationList", "90");
		}
		mShareLocation = prefs.getBoolean("shareLocationCB", true);
		mDebugMode = prefs.getBoolean("debugModeCB", false);

		// get the lightMode and write it to the preference of the zxing
		// scanner
		String lightMode = prefs.getString("lightMode", "OFF");
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_FRONT_LIGHT_MODE, lightMode);
		editor.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		enableActionMode(false);

		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent prefActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(prefActivity);
			break;
		case R.id.menu_exit:
			this.finish();
			break;

		case R.id.menu_history:
			// shareAsSMS("testmessage ole", "200380");
			// shareAsEmail("testmail\nssss", "hall@hallo", "testsubj");
			Intent historyActivity = new Intent(getBaseContext(), History.class);
			startActivityForResult(historyActivity, 1);
			break;

		case R.id.menu_gallery:
			// Intent intent = new Intent();
			// intent.setType("image/*");
			// intent.setAction(Intent.ACTION_GET_CONTENT);
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("content://media/internal/images/media"));
			startActivity(intent);
			// startActivity(Intent.createChooser(intent, "Select Picture"));
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ButtonScan:
			openQRCodeScanner();
			break;

		case R.id.ButtonActionMode:
			enableActionMode(true);
			break;

		case R.id.ButtonPhotos:
			// get camera sensor orientation

			// camera.setDisplayOrientation(90); this is just for the surface

			// if (getRotation() == 0) {// Portrait
			// Camera.Parameters parameters = mCamera.getParameters();
			// parameters.setRotation(90);
			// mCamera.setParameters(parameters);
			// }

			mCamera.takePicture(null, null, this);
			break;

		case R.id.ButtonVideo:
			if (mVideoRecordingInProgress) {

				if (mMediaRecorder != null)
					mMediaRecorder.stop();

				mVideoRecordingInProgress = false;

				// Let's initRecorder so we can record again
				// initRecorder();
				// prepareAndStartVideoRecorder();

				// TODO: somehow we have to resume here!!?!
				// try {
				// if (mCamera != null)
				// mCamera.setPreviewDisplay(mSurfaceHolder);
				// startPreview();
				// } catch (Exception e) {
				// }

			} else {
				mVideoRecordingInProgress = true;
				initVideoRecorder();
				prepareAndStartRecorder();
			}

			break;

		case R.id.ButtonAudio:
			if (mAudioRecordingInProgress) {
				Toast.makeText(getApplicationContext(),
						"Audio recording completed.", Toast.LENGTH_SHORT)
						.show();

				if (mMediaRecorder != null)
					mMediaRecorder.stop();

				mAudioRecordingInProgress = false;
				toggleOtherButtons(mButtonAudio, true);

				// now we can upload the recorded audio data
				uploadInBackground(mFileName);

			} else {
				Toast.makeText(getApplicationContext(),
						"Audio recording started.", Toast.LENGTH_SHORT).show();

				mAudioRecordingInProgress = true;
				toggleOtherButtons(mButtonAudio, false);

				initAudioRecorder();
				prepareAndStartRecorder();
			}

			break;

		// case R.id.ButtonHistory:
		// Intent historyActivity = new Intent(getBaseContext(), History.class);
		// startActivityForResult(historyActivity, 1);
		// break;

		case R.id.surfaceView1:
			enableActionMode(false);
			break;

		default:
			break;
		}

	}

	private void initAudioRecorder() {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

		File folder = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath(), "/_chooooxe");
		folder.mkdir();

		long timeDate = new Date().getTime();
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/_chooooxe/audiocapture" + timeDate + ".3gp";
		mMediaRecorder.setOutputFile(mFileName);

		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	}

	private void initVideoRecorder() {
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();

		if (mInCameraPreview) {
			mCamera.stopPreview();
			mInCameraPreview = false;
		}

		mCamera.unlock();

		mMediaRecorder.setCamera(mCamera);

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

		File folder = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath(), "/_chooooxe");
		folder.mkdir();

		long timeDate = new Date().getTime();
		String fileName = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		fileName += "/_chooooxe/videocapture" + timeDate + ".mp4";
		mMediaRecorder.setOutputFile(fileName);

		// mMediaRecorder.setProfile(CamcorderProfile
		// .get(CamcorderProfile.QUALITY_QCIF));

		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);

		// mMediaRecorder.setMaxDuration(10000); // 50 seconds
		// mMediaRecorder.setVideoFrameRate(20);
		// mMediaRecorder.setMaxFileSize(5000000); // Approximately 5 megabytes

		// mMediaRecorder.setVideoSize(160,240);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
	}

	private void prepareAndStartRecorder() {

		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void toggleOtherButtons(Button current, boolean enabled) {

		ArrayList<Button> buttons = new ArrayList<Button>();
		buttons.add(mButtonAudio);
		// buttons.add(mButtonPhotos);
		// buttons.add(mButtonScan);
		buttons.add(mButtonVideo);
		// buttons.add(mButtonActionMode);

		Iterator<Button> i = buttons.iterator();
		while (i.hasNext()) {
			Button b = i.next();
			if (!b.equals(current)) {
				b.setEnabled(enabled);
			}
		}

		mSurfaceView.setClickable(enabled);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// System.out.println(requestCode);
		switch (requestCode) {
		case 0: // QR Code Scanner Result
			if (resultCode == RESULT_OK) {
				String url = data.getStringExtra("SCAN_RESULT"); // this is
																	// the
																	// result
				url = checkScannerResultURL(url);

				openInWebView(url);

				// get current date and time
				String mydate = java.text.DateFormat.getDateTimeInstance()
						.format(Calendar.getInstance().getTime());

				// add new history item with scan result
				historyDatabaseHandler.addHistoryItem(new HistoryItem(
						"empty_name", url, mydate));

			} else if (resultCode == RESULT_CANCELED) {
				// Toast.makeText(this, "Cancelled",
				// Toast.LENGTH_SHORT).show();

				// TODO: removed this call
				// openInWebView(START_PAGE);
			}
			break;
		case 1: // History Selection Result
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Bundle b = data.getExtras();
					if (b != null) {
						openInWebView(b.getString("url"));
					}
				}
			}
			break;
		case 2:
			if (null == mUploadMessage)
				return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data
					.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
			break;

		case 88: // file uplaod async
		case 99: // photo upload async
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String filePath = getPath(selectedImage);
				// System.out.println("filepath " + filePath);

				String[] imageUploadParameters = jsInterface
						.getImageUploadParameters();

				if (imageUploadParameters != null
						&& imageUploadParameters.length == 4) {

					// mImageUploadParameters[0] = type; // ("Image"|"File")
					// mImageUploadParameters[1] = users_ID;
					// mImageUploadParameters[2] = table; // e.g. Channels
					// mImageUploadParameters[3] = id;
					try {
						uploadImageInBackground(filePath,
								imageUploadParameters[0],
								imageUploadParameters[1],
								imageUploadParameters[2],
								imageUploadParameters[3]);

					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(this,
								"Error: uploading image in background.",
								Toast.LENGTH_SHORT).show();

					}
				} else
					// TODO: remove this
					Toast.makeText(this, "Error: upload parameters empty.",
							Toast.LENGTH_SHORT).show();

			} else {
				// TODO: reset parameters object?
				// System.out.println("abc nothing recieved");
			}

			break;
		default:
			break;
		}

	}

	public String getPath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 140;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

	}

	private String checkScannerResultURL(String url) {
		if (url.contains("chooxe.me") && !url.contains("http://"))
			return "http://" + url;
		return url;
	}

	private void enableActionMode(boolean enable) {

		mActionModeEnabled = enable;

		if (enable) { // ON
			mButtonPhotos.setVisibility(View.VISIBLE);
			if (videoAudioRecording) {
				mButtonVideo.setVisibility(View.VISIBLE);
				mButtonAudio.setVisibility(View.VISIBLE);
			} else {
				mButtonVideo.setVisibility(View.GONE);
				mButtonAudio.setVisibility(View.GONE);
			}
			mButtonScan.setVisibility(View.VISIBLE);
			mButtonActionMode.setVisibility(View.GONE);
			mSurfaceView.setVisibility(View.VISIBLE);
			findViewById(R.id.closeactionmode).setVisibility(View.VISIBLE);

			startPreview();

		} else { // OFF

			// we have to set to false that next time when enabled camera will
			// get new configuration
			cameraConfigured = false;

			// if (getRotation() == 0) {// Portrait
			mButtonPhotos.setVisibility(View.GONE);
			mButtonVideo.setVisibility(View.GONE);
			mButtonAudio.setVisibility(View.GONE);

			// } else {
			// mButtonPhotos.setVisibility(View.VISIBLE);
			// }
			mButtonScan.setVisibility(View.VISIBLE);
			mButtonActionMode.setVisibility(View.VISIBLE);
			mSurfaceView.setVisibility(View.GONE);
			findViewById(R.id.closeactionmode).setVisibility(View.GONE);

			if (mInCameraPreview) {
				mCamera.stopPreview();
				mInCameraPreview = false;
			}
		}

	}

	// private int getPixelValueFromDP(int dp) {
	// return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
	// getResources().getDisplayMetrics());
	// }

	private void openQRCodeScanner() {
		if (isCameraAvailable()) {
			if (mScanner.equalsIgnoreCase("zxing")) { // use bulit-in scanner

				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN_intern");
				// intent = new Intent(this, CaptureActivity.class);
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				intent.putExtra("SAVE_HISTORY", false);
				startActivityForResult(intent, 0); // 0..Result code for Scanner

			} else if (mScanner.equalsIgnoreCase("zbar")) {
				Intent intent = new Intent(this, ZBarScannerActivity.class);
				intent.putExtra(ZBarConstants.SCAN_MODES,
						new int[] { Symbol.QRCODE });
				startActivityForResult(intent, 0); // 0..Result code for Scanner

			} else { // external scanner
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // or
																	// "PRODUCT_MODE"
					intent.putExtra("SAVE_HISTORY", false);

					startActivityForResult(intent, 0); // 0..Result code for
														// Scanner

				} catch (Exception e) {
					showDialogScannerNotInstalled();
				}
			}
		} else {
			Toast.makeText(this, "Rear Facing Camera Unavailable",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void openInWebView(String url) {

		mWebView.loadUrl(url);

		// System.out.println("OPEN URL " + url);

	}

	public void setButtonBarVisibility(boolean visibility) {
		// System.out.println("layout SetVisbility called:" + visibility);
		// Maybe requestLayout() on the RelativeLayout is needed here to update
		// the whole root layout

		if (mButtonBarLinearLayout != null) {
			if (visibility) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// // hide the keyboard in order to avoid
						// getTextBeforeCursor on inactive InputConnection
						// InputMethodManager inputMethodManager =
						// (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						// inputMethodManager.hideSoftInputFromWindow(mWebView.getWindowToken(),
						// 0);

						// hideKeyboard();

						if (mButtonBarLinearLayout.getVisibility() != View.VISIBLE) {
							mButtonBarLinearLayout.setVisibility(View.VISIBLE);
						}
					}
				});

			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// InputMethodManager inputMethodManager =
						// (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						// inputMethodManager.showSoftInputFromInputMethod(mWebView.getWindowToken(),
						// 0);
						if (mButtonBarLinearLayout.getVisibility() != View.GONE) {
							mButtonBarLinearLayout.setVisibility(View.GONE);
						}
					}
				});
			}
		}
	}

	private void fadeOutAndHideImage(final ImageView img) {

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(400);

		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				img.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		img.startAnimation(fadeOut);
	}

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void initializeWebView() {

		if (mWebView == null) {

			mWebView = new WebView(context);

			// every view need an id so that focus can be saved
			mWebView.setId(8998989);

			mWebView.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			mWebView.getSettings().setJavaScriptEnabled(true);
			jsInterface = new JavaScriptInterface(this);
			mWebView.addJavascriptInterface(jsInterface, "JSInterface");
			mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

			mWebView.setFocusable(true);
			mWebView.setFocusableInTouchMode(true);

			// mWebView.setInitialScale(100);
			// mWebView.clearCache(true);
			// mWebView.clearFormData();

			// Settings //
			WebSettings ws = mWebView.getSettings();
			ws.setBuiltInZoomControls(true);
			ws.setSupportZoom(true);
			ws.setRenderPriority(RenderPriority.HIGH);

			if (Build.VERSION.SDK_INT >= 11) {
				ws.setEnableSmoothTransition(true);
				ws.setDisplayZoomControls(false);
			}

			// if (Build.VERSION.SDK_INT >= 19) {
			// mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			// } else {
			// mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			// }

			ws.setUserAgentString(getUserAgentString());
			ws.setAppCacheEnabled(true);
			ws.setAppCachePath(getFilesDir().getPath());

			ws.setDomStorageEnabled(true);
			ws.setGeolocationEnabled(true);
			ws.setGeolocationDatabasePath(getFilesDir().getPath());

			// ws.setDatabaseEnabled(true);
			// ws.setDatabasePath(getFilesDir().getPath());
			// if (Build.VERSION.SDK_INT >= 8)
			// ws.setPluginsEnabled(true);
			// mWebView.getSettings().setPluginState(PluginState.ON); // 8-18

			final Activity activity = this;

			mWebView.setWebChromeClient(new WebChromeClient() {
				public void onGeolocationPermissionsShowPrompt(String origin,
						GeolocationPermissions.Callback callback) {
					final boolean remember = false;

					if (mShareLocation)
						callback.invoke(origin, true, remember);
					else
						callback.invoke(origin, false, remember);

				}

				public void onProgressChanged(WebView view, int progress) {
					// System.out.println("abc progress" + progress);

					if (progress < 100
							&& Pbar.getVisibility() == ProgressBar.GONE) {
						Pbar.setVisibility(ProgressBar.VISIBLE);
					}
					Pbar.setProgress(progress);

					if (progress == 100) {
						mProgressHandler = new Handler();
						Runnable r = new Runnable() {
							public void run() {
								Pbar.setVisibility(ProgressBar.GONE);
							}
						};
						mProgressHandler.postDelayed(r, 1000);

						if (mDoHideSplashScreen) {
							fadeOutAndHideImage(mImageSplashScreen);
							mDoHideSplashScreen = false;
						}
					}
				}

				// Since we have async upload we do not need this anymore

				// @SuppressWarnings("unused")
				// public void openFileChooser(ValueCallback<Uri> uploadMsg,
				// String acceptType, String capture) {
				// openFileChooser(uploadMsg);
				// }
				//
				// // For Android > 3.x
				// @SuppressWarnings("unused")
				// public void openFileChooser(ValueCallback<Uri> uploadMsg,
				// String acceptType) {
				// mUploadMessage = uploadMsg;
				// Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				// i.addCategory(Intent.CATEGORY_OPENABLE);
				// i.setType("image/*");
				//
				// MainActivity.this.startActivityForResult(Intent
				// .createChooser(i, "Choose type of attachment"), 2);
				//
				// }
				//
				// // For Android < 3.x
				// @SuppressWarnings("unused")
				// public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				// mUploadMessage = uploadMsg;
				// Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				// i.addCategory(Intent.CATEGORY_OPENABLE);
				// i.setType("image/*");
				// MainActivity.this.startActivityForResult(Intent
				// .createChooser(i, "Choose type of attachment"), 2);
				// }

			});

			mWebView.setWebViewClient(new WebViewClient() {

				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {

					super.onPageStarted(view, url, favicon);

				}

				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					Toast.makeText(activity, " " + description,
							Toast.LENGTH_SHORT).show();
				}

				public void onReceivedSslError(WebView view,
						SslErrorHandler handler, SslError error) {
					AlertDialog.Builder builder = new AlertDialog.Builder(view
							.getContext());
					handlerSsl = handler;
					String message = "SSL Certificate error.";
					switch (error.getPrimaryError()) {
					case SslError.SSL_UNTRUSTED:
						message = "The certificate authority is not trusted.";
						break;
					case SslError.SSL_EXPIRED:
						message = "The certificate has expired.";
						break;
					case SslError.SSL_IDMISMATCH:
						message = "The certificate Hostname mismatch.";
						break;
					case SslError.SSL_NOTYETVALID:
						message = "The certificate is not yet valid.";
						break;
					}
					message += " Do you want to continue anyway?";

					builder.setTitle("SSL Certificate Error");
					builder.setMessage(message);
					builder.setPositiveButton("continue",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									handlerSsl.proceed();
								}
							});
					builder.setNegativeButton("cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									handlerSsl.cancel();
								}
							});
					final AlertDialog dialog = builder.create();
					dialog.show();
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					if (debugModeStartup)
						System.out.println("LOAD FINISHED of " + url);

					if (doSendRegIdToBackend) {
						sendRegistrationIdToBackend();
						doSendRegIdToBackend = false;
					}

					super.onPageFinished(view, url);

				}

				@Override
				public void onReceivedHttpAuthRequest(WebView view,
						HttpAuthHandler handler, String host, String realm) {
					// super.onReceivedHttpAuthRequest(view, handler, host,
					// realm);
					handler.proceed("chooxe", "exoohc");
				}

				// Handle email links and open email client

				// If WebViewClient is provided, return true means the host
				// application handles the url, while return false means the
				// current
				// WebView handles the url. This method is not called for
				// requests
				// using the POST "method".
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// System.out.println("URLABC " + url);

					if (url.startsWith("mailto:")) {
						MailTo mt = MailTo.parse(url);
						// mt.getSubject(), mt.getBody(), mt.getCc());

						Intent emailIntent = new Intent(Intent.ACTION_SEND);
						emailIntent.setType("message/rfc822");
						emailIntent.putExtra(
								android.content.Intent.EXTRA_EMAIL,
								new String[] { mt.getTo() });
						emailIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								activity.getPackageName());
						startActivity(Intent.createChooser(emailIntent,
								"Send mail..."));
						return true; // URL not handled by WebView
					}

					// Not Online
					// TODO: what for do we need this?? already handled search
					// for:
					// not_online
					if (!isOnline()) {
						// System.out.println("SHOULDOVERRIEDE URL not online!!!");
						Toast.makeText(mActivity,
								"You are currently not online!",
								Toast.LENGTH_SHORT).show();

						return true; // URL not handled by WebView

					} else if (!url.contains("chooxe")) {
						// External URL in Browser (handle everything than
						// mailto and not on in browser)
						Intent i = new Intent(Intent.ACTION_VIEW, Uri
								.parse(url));
						mActivity.startActivity(i);
						return true;
					}
					return false;
				}

			});
			/*
			 * mWebView.setOnTouchListener(new View.OnTouchListener() {
			 * 
			 * @Override public boolean onTouch(View v, MotionEvent event) { //
			 * This is useful to make the text input work on text fields switch
			 * (event.getAction()) { case MotionEvent.ACTION_DOWN: case
			 * MotionEvent.ACTION_UP: // v.clearFocus(); if (!v.hasFocus()) {
			 * v.requestFocus(); System.out.println("request focus"); } break; }
			 * return false; } });
			 */
		}

		// Attach the WebView to its placeholder
		mWebViewPlaceholder.addView(mWebView);
		// mWebView.requestFocus(View.FOCUS_DOWN);
		// mWebView.requestFocusFromTouch();
	}

	public void hideKeyboard() {
		// hide the keyboard in order to avoid getTextBeforeCursor on inactive
		// InputConnection
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.hideSoftInputFromWindow(mActivity.getCurrentFocus()
				.getWindowToken(), 0);

		System.out.println("hide keyboard");
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private void showDialogScannerNotInstalled() {
		mScannerNotInstalledDialog = new AlertDialog.Builder(this);
		mScannerNotInstalledDialog.setTitle("Info");
		mScannerNotInstalledDialog
				.setMessage("No external Scanner found! Would you like to download Google Goggles from the Play Store?");
		mScannerNotInstalledDialog.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		mScannerNotInstalledDialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri marketUri = Uri
								.parse("market://details?id=com.google.android.apps.unveil");
						// id=com.google.zxing.client.android
						Intent marketIntent = new Intent(Intent.ACTION_VIEW,
								marketUri);
						startActivity(marketIntent);
						dialog.dismiss();
					}
				});
		mScannerNotInstalledDialog.create().show();
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			if (debugModeCamera)
				System.out.println("### surface changed/ holder:" + holder
						+ "wh:" + width + " " + height);

			initPreview(width, height, holder);
			startPreview();

		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			if (debugModeCamera)
				System.out.println("destroy!!");
		}
	};

	private void initPreview(int width, int height, SurfaceHolder previewHolder) {

		if (debugModeCamera)
			System.out.println("### initpreview: surfaceholder:"
					+ previewHolder + " camera:" + mCamera + " surface:"
					+ previewHolder.getSurface());

		if (mCamera == null)
			mCamera = Camera.open();
		// TODO else camera.unlock(); ??

		if (mCamera != null && previewHolder.getSurface() != null) {

			int rotation = 90;
			if (mRotationFromPrefs.equalsIgnoreCase("90")) {
				switch (getRotation()) {
				case Surface.ROTATION_0: // 0 Portrait
				case Surface.ROTATION_180:// 2
					rotation = 90;
					break;
				case Surface.ROTATION_90: // 1 Landscape right
					rotation = 0;
					break;
				case Surface.ROTATION_270: // 3 Landscape left
					rotation = 180;
					break;
				}
			} else if (mRotationFromPrefs.equalsIgnoreCase("0")) {
				switch (getRotation()) {
				case Surface.ROTATION_0: // portrait
				case Surface.ROTATION_180:
					rotation = 0;
					break;
				case Surface.ROTATION_90: // links
					rotation = 270;
					break;
				case Surface.ROTATION_270: // rechts
					rotation = 90;
					break;
				}
			} else if (mRotationFromPrefs.equalsIgnoreCase("180")) {
				switch (getRotation()) {
				case Surface.ROTATION_0: // portrait
				case Surface.ROTATION_180:
					rotation = 270;
					break;
				case Surface.ROTATION_90: // links
					rotation = 180;
					break;
				case Surface.ROTATION_270: // rechts
					rotation = 0;
					break;
				}
			} else if (mRotationFromPrefs.equalsIgnoreCase("270")) {
				switch (getRotation()) {
				case Surface.ROTATION_0: // portrait
				case Surface.ROTATION_180:
					rotation = 180;
					break;
				case Surface.ROTATION_90: // links
					rotation = 90;
					break;
				case Surface.ROTATION_270: // rechts
					rotation = 270;
					break;
				}
			}

			try {
				if (debugModeCamera)
					System.out
							.println("### initpreivew: setdisplaypreview/ camera:"
									+ mCamera
									+ " prevholder:"
									+ previewHolder
									+ " surface:" + previewHolder.getSurface());
				mCamera.setPreviewDisplay(previewHolder);

				// Requires Api Level 8
				mCamera.setDisplayOrientation(rotation);

				if (debugModeCamera)
					System.out.println("initpreview: setDisplayOrientation "
							+ rotation + "(" + getRotation() + ")");
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = mCamera.getParameters();

				// added this, dont know what it does
				// parameters.setRotation(rotation);
				// parameters.set("orientation","portrait");
				// parameters.set("orientation", "landscape");

				Camera.Size size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);

					// used for samsung devices green
					// parameters.set("cam_mode", 1);

					mCamera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;
		String supportedResolutions = null;

		if (mDebugMode) {
			supportedResolutions = "Supported Resolutions for Camera Preview\n\nDensitiy: "
					+ getResources().getDisplayMetrics().toString() + "\n\n";
		}

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (mDebugMode) {
				supportedResolutions += size.width + " x " + size.height + "\n";
			}
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}
		if (mDebugMode)
			writeToLog(supportedResolutions, "supportedResolutions.txt",
					"Camera");

		return (result);
	}

	private void writeToLog(String text, String filename, String foldername) {

		File root = Environment.getExternalStorageDirectory();
		// System.out.println(Environment.getExternalStorageState());
		if (root.canWrite()) {
			String path = String.format("/Chooxe/%s/", foldername);

			File folder = new File(root, path);
			folder.mkdirs();

			File file = new File(folder, filename);

			try {
				if (root.canWrite()) {

					FileWriter fw = new FileWriter(file);
					BufferedWriter out = new BufferedWriter(fw);
					out.write(text);
					out.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && mCamera != null) {

			// only api level 14
			// Camera.Parameters p = mCamera.getParameters();
			// p.setRecordingHint(true);
			// mCamera.setParameters(p);

			if (!mInCameraPreview) {
				if (debugModeCamera)
					System.out.println("startPreview!!!");
				mCamera.startPreview();
				mInCameraPreview = true;
			}
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		Uri imageFileUri = getContentResolver().insert(
				Media.EXTERNAL_CONTENT_URI, new ContentValues());

		/*
		 * ExifInterface exif; try { exif = new
		 * ExifInterface(imageFileUri.toString()); String exifOrientation =
		 * exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		 * System.out.println("exif info="+ exifOrientation + " getrotation" +
		 * getRotation()); } catch (IOException e1) { e1.printStackTrace(); }
		 */

		try {
			// write data to file
			OutputStream imageFileOS = getContentResolver().openOutputStream(
					imageFileUri);
			imageFileOS.write(data);
			imageFileOS.flush();
			imageFileOS.close();

			// upload data and open share URL overlay
			try {
				int sessionId = getSessionId();
				uploadImageInBackground(imageFileUri.toString(), "Image",
						String.valueOf(sessionId), "", "");

				runOnUiThread(new Runnable() {
					public void run() {

						// hide the action bar
						enableActionMode(false);

						// java script share overlay
						if (mWebView != null)
							mWebView.loadUrl("javascript:loadOverlay('/overlay.appShare')");
					}
				});

			} catch (Exception e) {
				Toast.makeText(
						this,
						"Error during upload the picture to the server."
								+ e.getMessage(), Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
			t.show();
		}

		// THIS WAS REMOVED - ENABLE IF PROBLEMS OCCUR
		// camera.startPreview();
	}

	public void writeLogToSd() {
		try {
			File filename = new File(Environment.getExternalStorageDirectory()
					+ "/logfile.log");
			filename.createNewFile();
			String cmd = "logcat -d -f " + filename.getAbsolutePath();
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDeviceName() {
		try {
			String manufacturer = Build.MANUFACTURER;
			String model = Build.MODEL;
			if (model.startsWith(manufacturer)) {
				return capitalize(model).replace(" ", "_");
			} else {
				return (capitalize(manufacturer) + " " + model).replace(" ",
						"_");
			}
		} catch (Exception e) {
			return "";
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	private String getAppVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
			return "";
		}
	}

	private String getSdkVersion() {

		try {
			// System.out.println(Build.VERSION.RELEASE);
			// System.out.println(Build.VERSION.SDK);
			// System.out.println(Build.VERSION.SDK_INT);
			return Build.VERSION.RELEASE;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private String getUserAgentString() {

		String userAgentString = String.format("App%s-Android%s-%s",
				getAppVersion(), getSdkVersion(), getDeviceName());
		// System.out.println(userAgentString);
		return userAgentString;
	}

	protected void checkConnectionStateAndReload() {

		if (isOnline()) {
			if (mWebView != null && mIsInOfflineState) {
				if (mWebView.getUrl() == null) { // START_PAGE

					if (debugModeStartup)
						System.out
								.println("NetworkChangeReceiver: loading start page, last url= "
										+ mWebView.getUrl());

					openInWebView(START_PAGE);
				}

				// Reload last url when device was not connected
				// else {
				// System.out.println("page not null, last url was="
				// + mWebView.getUrl());
				//
				// openInWebView(mWebView.getUrl());
				// }

				mIsInOfflineState = false;
			}

		} else {
			Toast.makeText(this, R.string.not_online, Toast.LENGTH_SHORT)
					.show();
			mIsInOfflineState = true;
		}

	}
}

class MySSLSocketFactory extends SSLSocketFactory {
	SSLContext sslContext = SSLContext.getInstance("TLS");

	public MySSLSocketFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}

class NetworkChangeReceiver extends BroadcastReceiver {

	MainActivity mMainActivity = null;

	public NetworkChangeReceiver(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {

		// System.out.println("NetworkChangeReceiver: statechagned");
		mMainActivity.checkConnectionStateAndReload();

	}
}
