package com.mysentosa.android.sg.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Gravity;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.TicketsActivity;
import com.mysentosa.android.sg.custom_views.CustomMenu;

public class AlertHelper {
	public static boolean isKillSwitchAlertShowing = false;
	
	public static void showAlert(Context context, String title, String message,
			String button1, OnClickListener button1Click, String button2,
			OnClickListener button2Click) {
		if (((Activity) context).isFinishing())
			return;

		final AlertDialog ad = new AlertDialog.Builder(context).create();
		ad.setCancelable(false);

		if (button1 == null) {
			button1 = "OK";
		}

		if (title != null) {
			ad.setTitle(title);
		}
		if (message != null) {
			ad.setMessage(message);
		}

		if (button1Click == null) {
			button1Click = new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// isShowingDialog = false;
					ad.dismiss();

				}
			};
		}

		ad.setButton(AlertDialog.BUTTON_POSITIVE, button1, button1Click);

		if (button2 != null && button2Click == null) {
			button2Click = new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// isShowingDialog = false;
					ad.dismiss();

				}
			};
		}

		if (button2 != null) {
			ad.setButton(AlertDialog.BUTTON_NEGATIVE, button2, button2Click);
		}

		ad.show();
		TextView messageText = (TextView) ad
				.findViewById(android.R.id.message);
		messageText.setGravity(Gravity.CENTER);
	}

	public static void showErrorPopup(Context context,
			OnClickListener button1Click) {
		showAlert(context, null,
				context.getString(R.string.ticket_error_message),
				context.getString(R.string.ok), button1Click, null, null);
	}

	public static void showSuccessPopup(Context context,
			OnClickListener button1Click) {
		showAlert(context, null,
				context.getString(R.string.ticket_success_message),
				context.getString(R.string.tcontinue), button1Click, null, null);
	}

	public static void showPopup(Context context,
			OnClickListener button1Click, String message) {
		showAlert(context, null, message, context.getString(R.string.ok),
				button1Click, null, null);
    }

    public static void showNotificationAlert(Context context, OnClickListener button1Click) {
        showAlert(context, context.getString(R.string.notification_alert_title),
                context.getString(R.string.notification_alert_detail), context.getString(R.string.ok), button1Click,
                null, null);
    }
    
	/**
	 * Show a dialog with message warning kill-switch is ON.
	 * @param activity Activity to show the dialog on.
	 * @param resMsgId Resource id of the message string to show in dialog content.
	 */
	public static void showKillSwitchAlert(Activity activity, int resMsgId) {
		if (isKillSwitchAlertShowing)
			return;
		
		isKillSwitchAlertShowing = true;
		
		final boolean isTicketActivity = activity instanceof TicketsActivity;
		showAlert(activity, null, activity.getString(resMsgId), "OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (isTicketActivity == false) {
					Intent intent = CustomMenu.createNavigatingIntent(((AlertDialog)dialog).getContext(), TicketsActivity.class.getName());
					((AlertDialog)dialog).getContext().startActivity(intent);
				}
				isKillSwitchAlertShowing = false;
			}
		}, null, null);
	}
}
