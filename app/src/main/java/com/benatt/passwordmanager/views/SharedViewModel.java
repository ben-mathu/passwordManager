package com.benatt.passwordmanager.views;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<Boolean> refreshList = new MutableLiveData<>();

    public void refreshList() {
        refreshList.setValue(true);
    }
}
