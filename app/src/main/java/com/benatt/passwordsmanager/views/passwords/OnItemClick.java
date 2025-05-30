package com.benatt.passwordsmanager.views.passwords;

import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.utils.OnActivityResult;

/**
 * @author ben-mathu 23/11/20
 */
public interface OnItemClick {
    void onItemClick(Password password);

    void startKeyguardActivity(OnActivityResult onActivityResult, int requestCode);
}
