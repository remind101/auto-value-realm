package com.remind101.auto.value.example;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmDatastore {
    private static RealmDatastore instance;

    public static synchronized RealmDatastore getInstance() {
        if (instance == null) {
            instance = new RealmDatastore();
        }
        return instance;
    }

    private RealmDatastore() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MyApp.getInstance()).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    @Nullable
    public PersistedState getSavedState() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<$RealmPersistedState> res = realm.where($RealmPersistedState.class).findAll();
        if (res.isEmpty()) {
            return null;
        }
        return res.first().toModel();
    }

    public void savePersistedState(PersistedState state) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(state.toRealmObject());
        realm.commitTransaction();
    }
}
