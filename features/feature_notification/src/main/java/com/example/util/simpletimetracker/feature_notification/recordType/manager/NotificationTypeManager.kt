package com.example.util.simpletimetracker.feature_notification.recordType.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val router: Router
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private val iconView =
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }

    fun show(params: NotificationTypeParams) {
        val notification: Notification = buildNotification(params)
        createAndroidNotificationChannel()
        notificationManager.notify(params.id.toInt(), notification)
    }

    fun hide(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(params: NotificationTypeParams): Notification {
        val notificationLayout = prepareView(params)

        val startIntent = router.getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            startIntent,
            PendingIntents.getFlags(),
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCustomContentView(notificationLayout)
            .addAction(0, params.stopButton, getPendingSelfIntent(context, params.id))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW) // no sound
        return builder.build().apply {
            flags = flags or Notification.FLAG_NO_CLEAR
        }
    }

    private fun createAndroidNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // no sound
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun prepareView(params: NotificationTypeParams): RemoteViews {
        val iconBitmap = iconView.apply {
            itemIcon = params.icon
            itemColor = params.color
            measureExactly(iconSize)
        }.getBitmapFromView()

        return RemoteViews(context.packageName, R.layout.notification_record_layout).apply {
            setTextViewText(R.id.tvNotificationText, params.text)
            setTextViewText(R.id.tvNotificationTimeStarted, params.timeStarted)
            setTextViewText(R.id.tvNotificationGoalTime, params.goalTime)
            setImageViewBitmap(R.id.ivNotificationIcon, iconBitmap)
            val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - params.startedTimeStamp)
            setChronometer(R.id.timerNotification, base, null, true)
        }
    }

    private fun getPendingSelfIntent(
        context: Context,
        recordTypeId: Long
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = ACTION_NOTIFICATION_STOP
        intent.putExtra(ARGS_RECORD_TYPE_ID, recordTypeId)
        return PendingIntent.getBroadcast(context, recordTypeId.toInt(), intent, PendingIntents.getFlags())
    }

    companion object {
        const val ACTION_NOTIFICATION_STOP =
            "com.example.util.simpletimetracker.feature_notification.recordType.onStopClick"
        const val ARGS_RECORD_TYPE_ID = "recordTypeId"

        private const val NOTIFICATIONS_CHANNEL_ID = "NOTIFICATIONS"
        private const val NOTIFICATIONS_CHANNEL_NAME = "Notifications"
    }
}