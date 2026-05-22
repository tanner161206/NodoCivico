package com.nodocivico.app.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nodocivico.app.data.local.entity.FollowUpEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FollowUpDao_Impl implements FollowUpDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FollowUpEntity> __insertionAdapterOfFollowUpEntity;

  public FollowUpDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFollowUpEntity = new EntityInsertionAdapter<FollowUpEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `follow_ups` (`id`,`reportId`,`comment`,`createdAt`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FollowUpEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getReportId());
        statement.bindString(3, entity.getComment());
        statement.bindLong(4, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insertFollowUp(final FollowUpEntity followUp,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFollowUpEntity.insertAndReturnId(followUp);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<FollowUpEntity>> getFollowUpsForReport(final long reportId) {
    final String _sql = "SELECT * FROM follow_ups WHERE reportId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, reportId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"follow_ups"}, false, new Callable<List<FollowUpEntity>>() {
      @Override
      @Nullable
      public List<FollowUpEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "reportId");
          final int _cursorIndexOfComment = CursorUtil.getColumnIndexOrThrow(_cursor, "comment");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<FollowUpEntity> _result = new ArrayList<FollowUpEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FollowUpEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpReportId;
            _tmpReportId = _cursor.getLong(_cursorIndexOfReportId);
            final String _tmpComment;
            _tmpComment = _cursor.getString(_cursorIndexOfComment);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new FollowUpEntity(_tmpId,_tmpReportId,_tmpComment,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
