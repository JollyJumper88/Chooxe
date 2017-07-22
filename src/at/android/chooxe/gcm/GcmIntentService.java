/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.android.chooxe.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import at.android.chooxe.MainActivity;
import at.android.chooxe.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	public static final String TAG_GCM = "Chooxe GCM";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString(), extras);
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification(
						"Deleted messages on server: " + extras.toString(),
						extras);
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				Log.i(TAG_GCM,
						"Completed work @ " + SystemClock.elapsedRealtime());

				// Post notification of received message.
				sendNotification("Received: " + extras.toString(), extras);

				Log.i(TAG_GCM, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg, Bundle bundle) {

		Log.i(TAG_GCM, "Sending Notification: " + msg);

		// System.out.println("message " + msg);

		String url = "http://chooxe.me", title = "", message = "", image = "";

		try {
			if (msg.startsWith("Received")) {

				url = bundle.getString("url");
				message = bundle.getString("message");
				title = bundle.getString("title");
				image = bundle.getString("image");

			} else {
				message = msg;
			}
			
			// System.out.println("image url " + image);
			// System.out.println("test3 " + url);
			// System.out.println("test3 " + title);
			// System.out.println("test3 " + message);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Error parsing notification.", Toast.LENGTH_SHORT).show();
		}
		
		Bitmap test = getBitmapFromURL(image);
		
		
		int height = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
		// int width = (int) getResources().getDimension(android.R.dimen.notification_large_icon_width);
		System.out.println("aaa"+ height);
		Bitmap largeIcon = Bitmap.createScaledBitmap(getBitmapFromURL(image), height, height, false);

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent i = new Intent(this, MainActivity.class);
		// i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		i.putExtra("notificationurl", url);

		// PendingIntent pendingIntent = PendingIntent.getActivity(this,
		// item.getID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentTitle(title).setContentText(message)
				.setSmallIcon(R.drawable.notification_message)
				.setLargeIcon(largeIcon);

		// .setStyle(
		// new NotificationCompat.BigPictureStyle().bigPicture(
		// getBitmapFromURL(image)).setBigContentTitle(
		// title));
		// SetSummaryText

		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	public Bitmap getBitmapFromURL(String strURL) {
		try {
			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// public static Bitmap drawableToBitmap(Drawable drawable) {
	// if (drawable instanceof BitmapDrawable) {
	// return ((BitmapDrawable) drawable).getBitmap();
	// }
	//
	// Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
	// drawable.getIntrinsicHeight(), Config.ARGB_8888);
	// Canvas canvas = new Canvas(bitmap);
	// drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	// drawable.draw(canvas);
	//
	// return bitmap;
	// }
}
