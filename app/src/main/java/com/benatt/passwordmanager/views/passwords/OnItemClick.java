package com.benatt.passwordmanager.views.passwords;

import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.utils.OnActivityResult;

/**
 * @time 23/11/20
 */
public interface OnItemClick {
    void onItemClick(Password password);

    void startKeyguardActivity(OnActivityResult onActivityResult);
}
