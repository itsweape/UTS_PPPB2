package com.example.runnertracker;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class RunTrackerWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        ComponentName thisWidget = new ComponentName(context, RunTrackerWidget.class);
        int[] appWidgetID = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetID : appWidgetID){

            //button di klik akan mengarah ke recordtrack activity
            Intent intent = new Intent(context, RecordTrack.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Construct the RemoteViews object
            @SuppressLint("RemoteViewLayout")
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.run_tracker_widget);

            views.setOnClickPendingIntent(R.id.btnStart, pendingIntent);

            String dateString = DateFormat.getDateInstance().format(new Date());
            views.setTextViewText(R.id.date, dateString);

            String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
            views.setTextViewText(R.id.times, timeString);

            //update widget
            Intent intentUpdate = new Intent(context, RunTrackerWidget.class);
            intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] idArray = new int[]{appWidgetId};
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

            PendingIntent pendingIntents = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            views.setOnClickPendingIntent(R.id.refresh, pendingIntents);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}