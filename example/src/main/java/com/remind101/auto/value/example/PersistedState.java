package com.remind101.auto.value.example;

import com.google.auto.value.AutoValue;
import com.remind101.auto.value.realm.AvModel;
import com.remind101.auto.value.realm.AvPrimaryKey;

@AutoValue
public abstract class PersistedState implements AvModel<$RealmPersistedState> {
    @AvPrimaryKey
    public abstract String getName();

    public abstract int getNumAppLaunches();

    @Override
    public abstract $RealmPersistedState toRealmObject();

    public static Builder builder() {
        return new AutoValue_PersistedState.Builder();
    }

    abstract Builder toBuilder();

    public PersistedState withNumAppLaunches(int numAppLaunches) {
        return toBuilder().setNumAppLaunches(numAppLaunches).build();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setName(String name);

        public abstract Builder setNumAppLaunches(int numAppLaunches);

        public abstract PersistedState build();
    }
}
