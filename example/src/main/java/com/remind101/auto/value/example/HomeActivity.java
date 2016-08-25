package com.remind101.auto.value.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.remind101.auto.value.realm.R;

public class HomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        PersistedState state = RealmDatastore.getInstance().getSavedState("MyCounter");
        if (state == null) {
            state = PersistedState.builder().setNumAppLaunches(0).setName("MyCounter").build();
        } else {
            state = state.withNumAppLaunches(state.getNumAppLaunches() + 1);
        }
        RealmDatastore.getInstance().savePersistedState(state);
        ((TextView) findViewById(R.id.counter)).setText(String.valueOf(state.getNumAppLaunches()));
    }
}
