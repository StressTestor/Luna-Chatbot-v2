package com.luna.chat.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.luna.chat.data.local.dao.ChatDao;
import com.luna.chat.data.local.dao.ChatDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LunaDatabase_Impl extends LunaDatabase {
  private volatile ChatDao _chatDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `content` TEXT NOT NULL, `is_from_user` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `session_id` TEXT NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_session_id` ON `chat_messages` (`session_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_timestamp` ON `chat_messages` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_is_from_user` ON `chat_messages` (`is_from_user`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_preferences` (`id` INTEGER NOT NULL, `selected_theme` TEXT NOT NULL, `parental_controls_enabled` INTEGER NOT NULL, `api_key_configured` INTEGER NOT NULL, `first_time_user` INTEGER NOT NULL, `auto_clear_history_days` INTEGER NOT NULL, `content_filter_enabled` INTEGER NOT NULL, `voice_input_enabled` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd57bfbf81c639c9958ac1b08ec8ebb92')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `chat_messages`");
        db.execSQL("DROP TABLE IF EXISTS `user_preferences`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsChatMessages = new HashMap<String, TableInfo.Column>(6);
        _columnsChatMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("is_from_user", new TableInfo.Column("is_from_user", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("session_id", new TableInfo.Column("session_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChatMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChatMessages = new HashSet<TableInfo.Index>(3);
        _indicesChatMessages.add(new TableInfo.Index("index_chat_messages_session_id", false, Arrays.asList("session_id"), Arrays.asList("ASC")));
        _indicesChatMessages.add(new TableInfo.Index("index_chat_messages_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesChatMessages.add(new TableInfo.Index("index_chat_messages_is_from_user", false, Arrays.asList("is_from_user"), Arrays.asList("ASC")));
        final TableInfo _infoChatMessages = new TableInfo("chat_messages", _columnsChatMessages, _foreignKeysChatMessages, _indicesChatMessages);
        final TableInfo _existingChatMessages = TableInfo.read(db, "chat_messages");
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "chat_messages(com.luna.chat.data.local.entity.ChatMessageEntity).\n"
                  + " Expected:\n" + _infoChatMessages + "\n"
                  + " Found:\n" + _existingChatMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsUserPreferences = new HashMap<String, TableInfo.Column>(10);
        _columnsUserPreferences.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("selected_theme", new TableInfo.Column("selected_theme", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("parental_controls_enabled", new TableInfo.Column("parental_controls_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("api_key_configured", new TableInfo.Column("api_key_configured", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("first_time_user", new TableInfo.Column("first_time_user", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("auto_clear_history_days", new TableInfo.Column("auto_clear_history_days", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("content_filter_enabled", new TableInfo.Column("content_filter_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("voice_input_enabled", new TableInfo.Column("voice_input_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreferences.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserPreferences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserPreferences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserPreferences = new TableInfo("user_preferences", _columnsUserPreferences, _foreignKeysUserPreferences, _indicesUserPreferences);
        final TableInfo _existingUserPreferences = TableInfo.read(db, "user_preferences");
        if (!_infoUserPreferences.equals(_existingUserPreferences)) {
          return new RoomOpenHelper.ValidationResult(false, "user_preferences(com.luna.chat.data.local.entity.UserPreferencesEntity).\n"
                  + " Expected:\n" + _infoUserPreferences + "\n"
                  + " Found:\n" + _existingUserPreferences);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d57bfbf81c639c9958ac1b08ec8ebb92", "2977fdec03ca7505d4b9f7b44e286381");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "chat_messages","user_preferences");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `chat_messages`");
      _db.execSQL("DELETE FROM `user_preferences`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }
}
