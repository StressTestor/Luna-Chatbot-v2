package com.luna.chat.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.luna.chat.data.local.entity.ChatMessageEntity;
import com.luna.chat.data.local.entity.UserPreferencesEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatDao_Impl implements ChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatMessageEntity> __insertionAdapterOfChatMessageEntity;

  private final EntityInsertionAdapter<UserPreferencesEntity> __insertionAdapterOfUserPreferencesEntity;

  private final EntityDeletionOrUpdateAdapter<ChatMessageEntity> __updateAdapterOfChatMessageEntity;

  private final EntityDeletionOrUpdateAdapter<UserPreferencesEntity> __updateAdapterOfUserPreferencesEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessage;

  private final SharedSQLiteStatement __preparedStmtOfClearAllMessages;

  private final SharedSQLiteStatement __preparedStmtOfClearSessionMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesOlderThan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExcessiveMessages;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTheme;

  private final SharedSQLiteStatement __preparedStmtOfUpdateApiKeyStatus;

  private final SharedSQLiteStatement __preparedStmtOfMarkNotFirstTimeUser;

  private final SharedSQLiteStatement __preparedStmtOfUpdateParentalControls;

  public ChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatMessageEntity = new EntityInsertionAdapter<ChatMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chat_messages` (`id`,`content`,`is_from_user`,`timestamp`,`session_id`,`status`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getContent());
        final int _tmp = entity.isFromUser() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getSessionId());
        statement.bindString(6, entity.getStatus());
      }
    };
    this.__insertionAdapterOfUserPreferencesEntity = new EntityInsertionAdapter<UserPreferencesEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_preferences` (`id`,`selected_theme`,`parental_controls_enabled`,`api_key_configured`,`first_time_user`,`auto_clear_history_days`,`content_filter_enabled`,`voice_input_enabled`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserPreferencesEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSelectedTheme());
        final int _tmp = entity.getParentalControlsEnabled() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final int _tmp_1 = entity.getApiKeyConfigured() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        final int _tmp_2 = entity.getFirstTimeUser() ? 1 : 0;
        statement.bindLong(5, _tmp_2);
        statement.bindLong(6, entity.getAutoClearHistoryDays());
        final int _tmp_3 = entity.getContentFilterEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_3);
        final int _tmp_4 = entity.getVoiceInputEnabled() ? 1 : 0;
        statement.bindLong(8, _tmp_4);
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getUpdatedAt());
      }
    };
    this.__updateAdapterOfChatMessageEntity = new EntityDeletionOrUpdateAdapter<ChatMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chat_messages` SET `id` = ?,`content` = ?,`is_from_user` = ?,`timestamp` = ?,`session_id` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getContent());
        final int _tmp = entity.isFromUser() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getSessionId());
        statement.bindString(6, entity.getStatus());
        statement.bindString(7, entity.getId());
      }
    };
    this.__updateAdapterOfUserPreferencesEntity = new EntityDeletionOrUpdateAdapter<UserPreferencesEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `user_preferences` SET `id` = ?,`selected_theme` = ?,`parental_controls_enabled` = ?,`api_key_configured` = ?,`first_time_user` = ?,`auto_clear_history_days` = ?,`content_filter_enabled` = ?,`voice_input_enabled` = ?,`created_at` = ?,`updated_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserPreferencesEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSelectedTheme());
        final int _tmp = entity.getParentalControlsEnabled() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final int _tmp_1 = entity.getApiKeyConfigured() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        final int _tmp_2 = entity.getFirstTimeUser() ? 1 : 0;
        statement.bindLong(5, _tmp_2);
        statement.bindLong(6, entity.getAutoClearHistoryDays());
        final int _tmp_3 = entity.getContentFilterEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_3);
        final int _tmp_4 = entity.getVoiceInputEnabled() ? 1 : 0;
        statement.bindLong(8, _tmp_4);
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getUpdatedAt());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chat_messages SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages";
        return _query;
      }
    };
    this.__preparedStmtOfClearSessionMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE session_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessagesOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExcessiveMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE session_id IN (SELECT session_id FROM chat_messages GROUP BY session_id HAVING COUNT(*) > ?)";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTheme = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preferences SET selected_theme = ?, updated_at = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateApiKeyStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preferences SET api_key_configured = ?, updated_at = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfMarkNotFirstTimeUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preferences SET first_time_user = 0, updated_at = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateParentalControls = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preferences SET parental_controls_enabled = ?, updated_at = ? WHERE id = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final ChatMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<ChatMessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertUserPreferences(final UserPreferencesEntity preferences,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserPreferencesEntity.insert(preferences);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final ChatMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChatMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUserPreferences(final UserPreferencesEntity preferences,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserPreferencesEntity.handle(preferences);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessageAndUpdatePreferences(final ChatMessageEntity message,
      final UserPreferencesEntity preferences, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ChatDao.DefaultImpls.insertMessageAndUpdatePreferences(ChatDao_Impl.this, message, preferences, __cont), $completion);
  }

  @Override
  public Object clearAllDataAndResetPreferences(final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ChatDao.DefaultImpls.clearAllDataAndResetPreferences(ChatDao_Impl.this, __cont), $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final String messageId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessage.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllMessages(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllMessages.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearSessionMessages(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSessionMessages.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearSessionMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesOlderThan(final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMessagesOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExcessiveMessages(final int maxMessages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExcessiveMessages.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, maxMessages);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExcessiveMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTheme(final String theme, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTheme.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, theme);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateTheme.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateApiKeyStatus(final boolean configured, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateApiKeyStatus.acquire();
        int _argIndex = 1;
        final int _tmp = configured ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateApiKeyStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markNotFirstTimeUser(final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkNotFirstTimeUser.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAt);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkNotFirstTimeUser.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateParentalControls(final boolean enabled, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateParentalControls.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateParentalControls.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatMessageEntity>> getAllMessages() {
    final String _sql = "SELECT * FROM chat_messages ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_from_user");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new ChatMessageEntity(_tmpId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpSessionId,_tmpStatus);
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

  @Override
  public Flow<List<ChatMessageEntity>> getMessagesBySession(final String sessionId) {
    final String _sql = "SELECT * FROM chat_messages WHERE session_id = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_from_user");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new ChatMessageEntity(_tmpId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpSessionId,_tmpStatus);
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

  @Override
  public Object getMessageById(final String messageId,
      final Continuation<? super ChatMessageEntity> $completion) {
    final String _sql = "SELECT * FROM chat_messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatMessageEntity>() {
      @Override
      @Nullable
      public ChatMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_from_user");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final ChatMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _result = new ChatMessageEntity(_tmpId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpSessionId,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatMessageEntity>> getMessagesByType(final boolean isFromUser) {
    final String _sql = "SELECT * FROM chat_messages WHERE is_from_user = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final int _tmp = isFromUser ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_from_user");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsFromUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp_1 != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new ChatMessageEntity(_tmpId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpSessionId,_tmpStatus);
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

  @Override
  public Flow<List<ChatMessageEntity>> getRecentMessages(final int limit) {
    final String _sql = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessageEntity>>() {
      @Override
      @NonNull
      public List<ChatMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_from_user");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ChatMessageEntity> _result = new ArrayList<ChatMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new ChatMessageEntity(_tmpId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpSessionId,_tmpStatus);
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

  @Override
  public Object getAllSessionIds(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT session_id FROM chat_messages ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMessageCountBySession(final String sessionId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages WHERE session_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalMessageCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserPreferencesEntity> getUserPreferences() {
    final String _sql = "SELECT * FROM user_preferences WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_preferences"}, new Callable<UserPreferencesEntity>() {
      @Override
      @Nullable
      public UserPreferencesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSelectedTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "selected_theme");
          final int _cursorIndexOfParentalControlsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "parental_controls_enabled");
          final int _cursorIndexOfApiKeyConfigured = CursorUtil.getColumnIndexOrThrow(_cursor, "api_key_configured");
          final int _cursorIndexOfFirstTimeUser = CursorUtil.getColumnIndexOrThrow(_cursor, "first_time_user");
          final int _cursorIndexOfAutoClearHistoryDays = CursorUtil.getColumnIndexOrThrow(_cursor, "auto_clear_history_days");
          final int _cursorIndexOfContentFilterEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "content_filter_enabled");
          final int _cursorIndexOfVoiceInputEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_input_enabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final UserPreferencesEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSelectedTheme;
            _tmpSelectedTheme = _cursor.getString(_cursorIndexOfSelectedTheme);
            final boolean _tmpParentalControlsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfParentalControlsEnabled);
            _tmpParentalControlsEnabled = _tmp != 0;
            final boolean _tmpApiKeyConfigured;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfApiKeyConfigured);
            _tmpApiKeyConfigured = _tmp_1 != 0;
            final boolean _tmpFirstTimeUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFirstTimeUser);
            _tmpFirstTimeUser = _tmp_2 != 0;
            final int _tmpAutoClearHistoryDays;
            _tmpAutoClearHistoryDays = _cursor.getInt(_cursorIndexOfAutoClearHistoryDays);
            final boolean _tmpContentFilterEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfContentFilterEnabled);
            _tmpContentFilterEnabled = _tmp_3 != 0;
            final boolean _tmpVoiceInputEnabled;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfVoiceInputEnabled);
            _tmpVoiceInputEnabled = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new UserPreferencesEntity(_tmpId,_tmpSelectedTheme,_tmpParentalControlsEnabled,_tmpApiKeyConfigured,_tmpFirstTimeUser,_tmpAutoClearHistoryDays,_tmpContentFilterEnabled,_tmpVoiceInputEnabled,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
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

  @Override
  public Object getUserPreferencesSync(
      final Continuation<? super UserPreferencesEntity> $completion) {
    final String _sql = "SELECT * FROM user_preferences WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserPreferencesEntity>() {
      @Override
      @Nullable
      public UserPreferencesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSelectedTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "selected_theme");
          final int _cursorIndexOfParentalControlsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "parental_controls_enabled");
          final int _cursorIndexOfApiKeyConfigured = CursorUtil.getColumnIndexOrThrow(_cursor, "api_key_configured");
          final int _cursorIndexOfFirstTimeUser = CursorUtil.getColumnIndexOrThrow(_cursor, "first_time_user");
          final int _cursorIndexOfAutoClearHistoryDays = CursorUtil.getColumnIndexOrThrow(_cursor, "auto_clear_history_days");
          final int _cursorIndexOfContentFilterEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "content_filter_enabled");
          final int _cursorIndexOfVoiceInputEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_input_enabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final UserPreferencesEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSelectedTheme;
            _tmpSelectedTheme = _cursor.getString(_cursorIndexOfSelectedTheme);
            final boolean _tmpParentalControlsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfParentalControlsEnabled);
            _tmpParentalControlsEnabled = _tmp != 0;
            final boolean _tmpApiKeyConfigured;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfApiKeyConfigured);
            _tmpApiKeyConfigured = _tmp_1 != 0;
            final boolean _tmpFirstTimeUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFirstTimeUser);
            _tmpFirstTimeUser = _tmp_2 != 0;
            final int _tmpAutoClearHistoryDays;
            _tmpAutoClearHistoryDays = _cursor.getInt(_cursorIndexOfAutoClearHistoryDays);
            final boolean _tmpContentFilterEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfContentFilterEnabled);
            _tmpContentFilterEnabled = _tmp_3 != 0;
            final boolean _tmpVoiceInputEnabled;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfVoiceInputEnabled);
            _tmpVoiceInputEnabled = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new UserPreferencesEntity(_tmpId,_tmpSelectedTheme,_tmpParentalControlsEnabled,_tmpApiKeyConfigured,_tmpFirstTimeUser,_tmpAutoClearHistoryDays,_tmpContentFilterEnabled,_tmpVoiceInputEnabled,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getChatStatistics(final Continuation<? super ChatStatistics> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) as total_messages,\n"
            + "               SUM(CASE WHEN is_from_user = 1 THEN 1 ELSE 0 END) as user_messages,\n"
            + "               SUM(CASE WHEN is_from_user = 0 THEN 1 ELSE 0 END) as ai_messages,\n"
            + "               MIN(timestamp) as first_message_time,\n"
            + "               MAX(timestamp) as last_message_time\n"
            + "        FROM chat_messages\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatStatistics>() {
      @Override
      @Nullable
      public ChatStatistics call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalMessages = 0;
          final int _cursorIndexOfUserMessages = 1;
          final int _cursorIndexOfAiMessages = 2;
          final int _cursorIndexOfFirstMessageTime = 3;
          final int _cursorIndexOfLastMessageTime = 4;
          final ChatStatistics _result;
          if (_cursor.moveToFirst()) {
            final int _tmpTotalMessages;
            _tmpTotalMessages = _cursor.getInt(_cursorIndexOfTotalMessages);
            final int _tmpUserMessages;
            _tmpUserMessages = _cursor.getInt(_cursorIndexOfUserMessages);
            final int _tmpAiMessages;
            _tmpAiMessages = _cursor.getInt(_cursorIndexOfAiMessages);
            final Long _tmpFirstMessageTime;
            if (_cursor.isNull(_cursorIndexOfFirstMessageTime)) {
              _tmpFirstMessageTime = null;
            } else {
              _tmpFirstMessageTime = _cursor.getLong(_cursorIndexOfFirstMessageTime);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _result = new ChatStatistics(_tmpTotalMessages,_tmpUserMessages,_tmpAiMessages,_tmpFirstMessageTime,_tmpLastMessageTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSessionStatistics(
      final Continuation<? super List<SessionStatistics>> $completion) {
    final String _sql = "SELECT session_id, COUNT(*) as message_count FROM chat_messages GROUP BY session_id ORDER BY message_count DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionStatistics>>() {
      @Override
      @NonNull
      public List<SessionStatistics> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = 0;
          final int _cursorIndexOfMessageCount = 1;
          final List<SessionStatistics> _result = new ArrayList<SessionStatistics>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionStatistics _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpMessageCount;
            _tmpMessageCount = _cursor.getInt(_cursorIndexOfMessageCount);
            _item = new SessionStatistics(_tmpSessionId,_tmpMessageCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
