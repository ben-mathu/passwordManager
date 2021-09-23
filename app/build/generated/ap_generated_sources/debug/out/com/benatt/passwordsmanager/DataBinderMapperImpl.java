package com.benatt.passwordsmanager;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.benatt.passwordsmanager.databinding.ActivityMainBindingImpl;
import com.benatt.passwordsmanager.databinding.FragmentAddPasswordBindingImpl;
import com.benatt.passwordsmanager.databinding.FragmentAuthBindingImpl;
import com.benatt.passwordsmanager.databinding.FragmentPasswordsBindingImpl;
import com.benatt.passwordsmanager.databinding.PasswordItemBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYMAIN = 1;

  private static final int LAYOUT_FRAGMENTADDPASSWORD = 2;

  private static final int LAYOUT_FRAGMENTAUTH = 3;

  private static final int LAYOUT_FRAGMENTPASSWORDS = 4;

  private static final int LAYOUT_PASSWORDITEM = 5;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(5);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.benatt.passwordsmanager.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.benatt.passwordsmanager.R.layout.fragment_add_password, LAYOUT_FRAGMENTADDPASSWORD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.benatt.passwordsmanager.R.layout.fragment_auth, LAYOUT_FRAGMENTAUTH);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.benatt.passwordsmanager.R.layout.fragment_passwords, LAYOUT_FRAGMENTPASSWORDS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.benatt.passwordsmanager.R.layout.password_item, LAYOUT_PASSWORDITEM);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTADDPASSWORD: {
          if ("layout/fragment_add_password_0".equals(tag)) {
            return new FragmentAddPasswordBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_add_password is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTAUTH: {
          if ("layout/fragment_auth_0".equals(tag)) {
            return new FragmentAuthBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_auth is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTPASSWORDS: {
          if ("layout/fragment_passwords_0".equals(tag)) {
            return new FragmentPasswordsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_passwords is invalid. Received: " + tag);
        }
        case  LAYOUT_PASSWORDITEM: {
          if ("layout/password_item_0".equals(tag)) {
            return new PasswordItemBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for password_item is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(7);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "addPasswordViewModel");
      sKeys.put(2, "authViewModel");
      sKeys.put(3, "mainViewModel");
      sKeys.put(4, "password");
      sKeys.put(5, "passwordItemViewModel");
      sKeys.put(6, "passwordsViewModel");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(5);

    static {
      sKeys.put("layout/activity_main_0", com.benatt.passwordsmanager.R.layout.activity_main);
      sKeys.put("layout/fragment_add_password_0", com.benatt.passwordsmanager.R.layout.fragment_add_password);
      sKeys.put("layout/fragment_auth_0", com.benatt.passwordsmanager.R.layout.fragment_auth);
      sKeys.put("layout/fragment_passwords_0", com.benatt.passwordsmanager.R.layout.fragment_passwords);
      sKeys.put("layout/password_item_0", com.benatt.passwordsmanager.R.layout.password_item);
    }
  }
}
