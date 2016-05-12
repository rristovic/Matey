package com.mateyinc.marko.matey.inall;

import android.view.Menu;
import android.view.MenuItem;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;

/**
 * Created by M4rk0 on 5/12/2016.
 */
abstract public class InsideActivity extends MotherActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LogoutAs logoutAs = new LogoutAs(InsideActivity.this);
            logoutAs.execute(securePreferences.getString("email"),
                    securePreferences.getString("uid"),
                    securePreferences.getString("device_id"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
