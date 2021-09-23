package com.benatt.passwordsmanager.databinding;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class PasswordItemBindingImpl extends PasswordItemBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.btn_decrypt, 3);
    }
    // views
    @NonNull
    private final androidx.cardview.widget.CardView mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public PasswordItemBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 4, sIncludes, sViewsWithIds));
    }
    private PasswordItemBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 2
            , (android.widget.Button) bindings[3]
            , (android.widget.TextView) bindings[1]
            , (android.widget.TextView) bindings[2]
            );
        this.mboundView0 = (androidx.cardview.widget.CardView) bindings[0];
        this.mboundView0.setTag(null);
        this.passwordKey.setTag(null);
        this.passwordValue.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x8L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
        if (BR.passwordItemViewModel == variableId) {
            setPasswordItemViewModel((com.benatt.passwordsmanager.views.passwords.adapter.PasswordItemViewModel) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setPasswordItemViewModel(@Nullable com.benatt.passwordsmanager.views.passwords.adapter.PasswordItemViewModel PasswordItemViewModel) {
        this.mPasswordItemViewModel = PasswordItemViewModel;
        synchronized(this) {
            mDirtyFlags |= 0x4L;
        }
        notifyPropertyChanged(BR.passwordItemViewModel);
        super.requestRebind();
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangePasswordItemViewModelAccountName((androidx.lifecycle.MutableLiveData<java.lang.String>) object, fieldId);
            case 1 :
                return onChangePasswordItemViewModelPasswordText((androidx.lifecycle.MutableLiveData<java.lang.String>) object, fieldId);
        }
        return false;
    }
    private boolean onChangePasswordItemViewModelAccountName(androidx.lifecycle.MutableLiveData<java.lang.String> PasswordItemViewModelAccountName, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
        }
        return false;
    }
    private boolean onChangePasswordItemViewModelPasswordText(androidx.lifecycle.MutableLiveData<java.lang.String> PasswordItemViewModelPasswordText, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x2L;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        androidx.lifecycle.MutableLiveData<java.lang.String> passwordItemViewModelAccountName = null;
        com.benatt.passwordsmanager.views.passwords.adapter.PasswordItemViewModel passwordItemViewModel = mPasswordItemViewModel;
        java.lang.String passwordItemViewModelAccountNameGetValue = null;
        androidx.lifecycle.MutableLiveData<java.lang.String> passwordItemViewModelPasswordText = null;
        java.lang.String passwordItemViewModelPasswordTextGetValue = null;

        if ((dirtyFlags & 0xfL) != 0) {


            if ((dirtyFlags & 0xdL) != 0) {

                    if (passwordItemViewModel != null) {
                        // read passwordItemViewModel.accountName
                        passwordItemViewModelAccountName = passwordItemViewModel.accountName;
                    }
                    updateLiveDataRegistration(0, passwordItemViewModelAccountName);


                    if (passwordItemViewModelAccountName != null) {
                        // read passwordItemViewModel.accountName.getValue()
                        passwordItemViewModelAccountNameGetValue = passwordItemViewModelAccountName.getValue();
                    }
            }
            if ((dirtyFlags & 0xeL) != 0) {

                    if (passwordItemViewModel != null) {
                        // read passwordItemViewModel.passwordText
                        passwordItemViewModelPasswordText = passwordItemViewModel.passwordText;
                    }
                    updateLiveDataRegistration(1, passwordItemViewModelPasswordText);


                    if (passwordItemViewModelPasswordText != null) {
                        // read passwordItemViewModel.passwordText.getValue()
                        passwordItemViewModelPasswordTextGetValue = passwordItemViewModelPasswordText.getValue();
                    }
            }
        }
        // batch finished
        if ((dirtyFlags & 0xdL) != 0) {
            // api target 1

            com.benatt.passwordsmanager.utils.BindingAdapters.setMutableText(this.passwordKey, passwordItemViewModelAccountNameGetValue);
        }
        if ((dirtyFlags & 0xeL) != 0) {
            // api target 1

            com.benatt.passwordsmanager.utils.BindingAdapters.setMutableText(this.passwordValue, passwordItemViewModelPasswordTextGetValue);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): passwordItemViewModel.accountName
        flag 1 (0x2L): passwordItemViewModel.passwordText
        flag 2 (0x3L): passwordItemViewModel
        flag 3 (0x4L): null
    flag mapping end*/
    //end
}