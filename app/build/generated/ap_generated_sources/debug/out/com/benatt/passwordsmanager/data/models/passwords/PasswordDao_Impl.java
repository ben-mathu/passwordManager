package com.benatt.passwordsmanager.data.models.passwords;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.RxRoom;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.Void;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PasswordDao_Impl implements PasswordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Password> __insertionAdapterOfPassword;

  private final EntityDeletionOrUpdateAdapter<Password> __deletionAdapterOfPassword;

  public PasswordDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPassword = new EntityInsertionAdapter<Password>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `passwords` (`id`,`cipher`,`accountName`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Password value) {
        stmt.bindLong(1, value.getId());
        if (value.getCipher() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getCipher());
        }
        if (value.getAccountName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAccountName());
        }
      }
    };
    this.__deletionAdapterOfPassword = new EntityDeletionOrUpdateAdapter<Password>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `passwords` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Password value) {
        stmt.bindLong(1, value.getId());
      }
    };
  }

  @Override
  public void save(final Password password) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPassword.insert(password);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void saveAll(final List<Password> passwords) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPassword.insert(passwords);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Completable delete(final Password password) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPassword.handle(password);
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
        }
      }
    });
  }

  @Override
  public Observable<List<Password>> getAll() {
    final String _sql = "SELECT * FROM passwords";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return RxRoom.createObservable(__db, false, new String[]{"passwords"}, new Callable<List<Password>>() {
      @Override
      public List<Password> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCipher = CursorUtil.getColumnIndexOrThrow(_cursor, "cipher");
          final int _cursorIndexOfAccountName = CursorUtil.getColumnIndexOrThrow(_cursor, "accountName");
          final List<Password> _result = new ArrayList<Password>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Password _item;
            _item = new Password();
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpCipher;
            if (_cursor.isNull(_cursorIndexOfCipher)) {
              _tmpCipher = null;
            } else {
              _tmpCipher = _cursor.getString(_cursorIndexOfCipher);
            }
            _item.setCipher(_tmpCipher);
            final String _tmpAccountName;
            if (_cursor.isNull(_cursorIndexOfAccountName)) {
              _tmpAccountName = null;
            } else {
              _tmpAccountName = _cursor.getString(_cursorIndexOfAccountName);
            }
            _item.setAccountName(_tmpAccountName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
