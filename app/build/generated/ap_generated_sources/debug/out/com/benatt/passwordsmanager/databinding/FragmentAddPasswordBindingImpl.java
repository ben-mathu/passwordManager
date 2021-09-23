package com.benatt.passwordsmanager.databinding;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class FragmentAddPasswordBindingImpl extends FragmentAddPasswordBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.ll_alias, 3);
        sViewsWithIds.put(R.id.tv_account_name, 4);
        sViewsWithIds.put(R.id.ll_password, 5);
        sViewsWithIds.put(R.id.tv_password, 6);
        sViewsWithIds.put(R.id.ll_preferences, 7);
        sViewsWithIds.put(R.id.cb_alphabets, 8);
        sViewsWithIds.put(R.id.cb_digits, 9);
        sViewsWithIds.put(R.id.cb_special, 10);
        sViewsWithIds.put(R.id.tv_length, 11);
        sViewsWithIds.put(R.id.edt_length, 12);
        sViewsWithIds.put(R.id.btn_set_password, 13);
        sViewsWithIds.put(R.id.btn_show_prefs, 14);
        sViewsWithIds.put(R.id.btn_submit_password, 15);
        sViewsWithIds.put(R.id.btn_delete_password, 16);
    }
    // views
    @NonNull
    private final android.widget.ScrollView mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers
    private androidx.databinding.InverseBindingListener edtAccountNameandroidTextAttrChanged = new androidx.databinding.InverseBindingListener() {
        @Override
        public void onChange() {
            // Inverse of password.accountName
            //         is password.setAccountName((java.lang.String) callbackArg_0)
            java.lang.String callbackArg_0 = androidx.databinding.adapters.TextViewBindingAdapter.getTextString(edtAccountName);
            // localize variables for thread safety
            // password
            com.benatt.passwordsmanager.data.models.passwords.model.Password password = mPassword;
            // password.accountName
            java.lang.String passwordAccountName = null;
            // password != null
            boolean passwordJavaLangObjectNull = false;



            passwordJavaLangObjectNull = (password) != (null);
            if (passwordJavaLangObjectNull) {




                password.setAccountName(((java.lang.String) (callbackArg_0)));
            }
        }
    };
    private androidx.databinding.InverseBindingListener edtPasswordandroidTextAttrChanged = new androidx.databinding.InverseBindingListener() {
        @Override
        public void onChange() {
            // Inverse of password.cipher
            //         is password.setCipher((java.lang.String) callbackArg_0)
            java.lang.String callbackArg_0 = androidx.databinding.adapters.TextViewBindingAdapter.getTextString(edtPassword);
            // localize variables for thread safety
            // password
            com.benatt.passwordsmanager.data.models.passwords.model.Password password = mPassword;
            // password.cipher
            java.lang.String passwordCipher = null;
            // password != null
            boolean passwordJavaLangObjectNull = false;



            passwordJavaLangObjectNull = (password) != (null);
            if (passwordJavaLangObjectNull) {




                password.setCipher(((java.lang.String) (callbackArg_0)));
            }
        }
    };

    public FragmentAddPasswordBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 17, sIncludes, sViewsWithIds));
    }
    private FragmentAddPasswordBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.Button) bindings[16]
            , (android.widget.Button) bindings[13]
            , (android.widget.Button) bindings[14]
            , (android.widget.Button) bindings[15]
            , (com.google.android.material.checkbox.MaterialCheckBox) bindings[8]
            , (com.google.android.material.checkbox.MaterialCheckBox) bindings[9]
            , (com.google.android.material.checkbox.MaterialCheckBox) bindings[10]
            , (android.widget.EditText) bindings[1]
            , (android.widget.EditText) bindings[12]
            , (android.widget.EditText) bindings[2]
            , (android.widget.LinearLayout) bindings[3]
            , (android.widget.LinearLayout) bindings[5]
            , (android.widget.LinearLayout) bindings[7]
            , (android.widget.TextView) bindings[4]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[6]
            );
        this.edtAccountName.setTag(null);
        this.edtPassword.setTag(null);
        this.mboundView0 = (android.widget.ScrollView) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x4L;
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
        if (BR.addPasswordViewModel == variableId) {
            setAddPasswordViewModel((com.benatt.passwordsmanager.views.addpassword.AddPasswordViewModel) variable);
        }
        else if (BR.password == variableId) {
            setPassword((com.benatt.passwordsmanager.data.models.passwords.model.Password) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setAddPasswordViewModel(@Nullable com.benatt.passwordsmanager.views.addpassword.AddPasswordViewModel AddPasswordViewModel) {
        this.mAddPasswordViewModel = AddPasswordViewModel;
    }
    public void setPassword(@Nullable com.benatt.passwordsmanager.data.models.passwords.model.Password Password) {
        this.mPassword = Password;
        synchronized(this) {
            mDirtyFlags |= 0x2L;
        }
        notifyPropertyChanged(BR.password);
        super.requestRebind();
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
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
        java.lang.String passwordAccountName = null;
        java.lang.String passwordCipher = null;
        com.benatt.passwordsmanager.data.models.passwords.model.Password password = mPassword;

        if ((dirtyFlags & 0x6L) != 0) {



                if (password != null) {
                    // read password.accountName
                    passwordAccountName = password.getAccountName();
                    // read password.cipher
                    passwordCipher = password.getCipher();
                }
        }
        // batch finished
        if ((dirtyFlags & 0x6L) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setText(this.edtAccountName, passwordAccountName);
            androidx.databinding.adapters.TextViewBindingAdapter.setText(this.edtPassword, passwordCipher);
        }
        if ((dirtyFlags & 0x4L) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(this.edtAccountName, (androidx.databinding.adapters.TextViewBindingAdapter.BeforeTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged)null, edtAccountNameandroidTextAttrChanged);
            androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(this.edtPassword, (androidx.databinding.adapters.TextViewBindingAdapter.BeforeTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged)null, edtPasswordandroidTextAttrChanged);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): addPasswordViewModel
        flag 1 (0x2L): password
        flag 2 (0x3L): null
    flag mapping end*/
    //end
}