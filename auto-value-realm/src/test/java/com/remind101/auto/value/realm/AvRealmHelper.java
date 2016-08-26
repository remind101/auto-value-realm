package com.remind101.auto.value.realm;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmModel;

public class AvRealmHelper {
    public static <T extends AvModel<R>, R extends AvRealmModel<T>> List<T> fromRealmModels(List<R> realmModels) {
        return null;
    }

    public static <T extends AvModel<R>, R extends AvRealmModel<T> & RealmModel> RealmList<R> toRealmModels(List<T> models) {
        return null;
    }
}