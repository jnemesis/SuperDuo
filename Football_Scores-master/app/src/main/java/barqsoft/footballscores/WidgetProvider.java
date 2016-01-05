package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    public static final String TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "WidgetProvider#onUpdate");

        RemoteViews remoteViews = firstMatchRemoteView(context);

        if (appWidgetIds == null || appWidgetIds.length <= 0) {
            return;
        }

        ComponentName componentName = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(componentName, remoteViews);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    private RemoteViews firstMatchRemoteView(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setTextViewText(R.id.homeNameTextView, "N/A");
        remoteViews.setTextViewText(R.id.awayNameTextView, "N/A");

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.homeNameTextView, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.awayNameTextView, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.timeTextView, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.scoreTextView, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent);

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] arg = new String[1];
        arg[0] = format.format(date);
        Cursor cursor = context.getContentResolver().query(
                DatabaseContract.scores_table.buildScoreWithDate(),  // The content URI of the words table
                null,                       // The columns to return for each row
                null,                  // Either null, or the word the user entered
                arg,                    // Either empty, or the string the user entered
                null);                       // The sort order for the returned rows

        if (cursor.moveToFirst()) {
            remoteViews.setTextViewText(R.id.homeNameTextView,
                    cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL)));
            remoteViews.setTextViewText(R.id.awayNameTextView,
                    cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL)));
            remoteViews.setTextViewText(R.id.timeTextView,
                    cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL)));

            String awayGoals = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));
            String homeGoals = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));

            if (homeGoals.equals("-1") || awayGoals.equals("-1")) {
                remoteViews.setTextViewText(R.id.scoreTextView, "not start");
            } else {
                remoteViews.setTextViewText(R.id.scoreTextView, homeGoals + " - " + awayGoals);
            }
        }

        cursor.close();
        return remoteViews;
    }

}
