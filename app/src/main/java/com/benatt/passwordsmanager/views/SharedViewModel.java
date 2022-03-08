package com.benatt.passwordsmanager.views;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public final MutableLiveData<Boolean> refreshListLiveData = new MutableLiveData<>();

    public void refreshList() {
        refreshListLiveData.setValue(true);
    }
}
