package rehanced.com.simplenukowallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rehanced.com.simplenukowallet.services.NotificationLauncher;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationLauncher.getInstance().start(context);
    }

}