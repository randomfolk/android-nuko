package rehanced.com.simplenukowallet.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        // close splash activity
        finish();
    }

}
