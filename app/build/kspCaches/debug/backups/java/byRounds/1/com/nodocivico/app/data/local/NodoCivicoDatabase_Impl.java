package com.nodocivico.app.data.local;

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
import com.nodocivico.app.data.local.dao.CategoryDao;
import com.nodocivico.app.data.local.dao.CategoryDao_Impl;
import com.nodocivico.app.data.local.dao.FollowUpDao;
import com.nodocivico.app.data.local.dao.FollowUpDao_Impl;
import com.nodocivico.app.data.local.dao.ReminderDao;
import com.nodocivico.app.data.local.dao.ReminderDao_Impl;
import com.nodocivico.app.data.local.dao.ReportDao;
import com.nodocivico.app.data.local.dao.ReportDao_Impl;
import com.nodocivico.app.data.local.dao.ReportStatusDao;
import com.nodocivico.app.data.local.dao.ReportStatusDao_Impl;
import com.nodocivico.app.data.local.dao.SyncEventDao;
import com.nodocivico.app.data.local.dao.SyncEventDao_Impl;
import com.nodocivico.app.data.local.dao.UserDao;
import com.nodocivico.app.data.local.dao.UserDao_Impl;
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
public final class NodoCivicoDatabase_Impl extends NodoCivicoDatabase {
  private volatile ReportDao _reportDao;

  private volatile CategoryDao _categoryDao;

  private volatile ReportStatusDao _reportStatusDao;

  private volatile UserDao _userDao;

  private volatile ReminderDao _reminderDao;

  private volatile SyncEventDao _syncEventDao;

  private volatile FollowUpDao _followUpDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `categoryId` INTEGER NOT NULL, `statusId` INTEGER NOT NULL, `priority` TEXT NOT NULL, `location` TEXT NOT NULL, `date` INTEGER NOT NULL, `imageUri` TEXT, `synced` INTEGER NOT NULL, `createdBy` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `report_status` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `zone` TEXT NOT NULL, `role` TEXT NOT NULL, `token` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `reportId` INTEGER NOT NULL, `reminderDate` INTEGER NOT NULL, `message` TEXT NOT NULL, FOREIGN KEY(`reportId`) REFERENCES `reports`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_reportId` ON `reminders` (`reportId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `follow_ups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `reportId` INTEGER NOT NULL, `comment` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`reportId`) REFERENCES `reports`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_follow_ups_reportId` ON `follow_ups` (`reportId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityType` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `actionType` TEXT NOT NULL, `syncDate` INTEGER NOT NULL, `status` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'cc9991dad758d6682269a7d612a12b56')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `reports`");
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `report_status`");
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `reminders`");
        db.execSQL("DROP TABLE IF EXISTS `follow_ups`");
        db.execSQL("DROP TABLE IF EXISTS `sync_events`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsReports = new HashMap<String, TableInfo.Column>(11);
        _columnsReports.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("statusId", new TableInfo.Column("statusId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("priority", new TableInfo.Column("priority", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("location", new TableInfo.Column("location", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("imageUri", new TableInfo.Column("imageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReports.put("createdBy", new TableInfo.Column("createdBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReports = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReports = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReports = new TableInfo("reports", _columnsReports, _foreignKeysReports, _indicesReports);
        final TableInfo _existingReports = TableInfo.read(db, "reports");
        if (!_infoReports.equals(_existingReports)) {
          return new RoomOpenHelper.ValidationResult(false, "reports(com.nodocivico.app.data.local.entity.ReportEntity).\n"
                  + " Expected:\n" + _infoReports + "\n"
                  + " Found:\n" + _existingReports);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(3);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.nodocivico.app.data.local.entity.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsReportStatus = new HashMap<String, TableInfo.Column>(2);
        _columnsReportStatus.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReportStatus.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReportStatus = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReportStatus = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReportStatus = new TableInfo("report_status", _columnsReportStatus, _foreignKeysReportStatus, _indicesReportStatus);
        final TableInfo _existingReportStatus = TableInfo.read(db, "report_status");
        if (!_infoReportStatus.equals(_existingReportStatus)) {
          return new RoomOpenHelper.ValidationResult(false, "report_status(com.nodocivico.app.data.local.entity.ReportStatusEntity).\n"
                  + " Expected:\n" + _infoReportStatus + "\n"
                  + " Found:\n" + _existingReportStatus);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(6);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("zone", new TableInfo.Column("zone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("token", new TableInfo.Column("token", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.nodocivico.app.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsReminders = new HashMap<String, TableInfo.Column>(4);
        _columnsReminders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("reportId", new TableInfo.Column("reportId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("reminderDate", new TableInfo.Column("reminderDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminders = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReminders.add(new TableInfo.ForeignKey("reports", "CASCADE", "NO ACTION", Arrays.asList("reportId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReminders = new HashSet<TableInfo.Index>(1);
        _indicesReminders.add(new TableInfo.Index("index_reminders_reportId", false, Arrays.asList("reportId"), Arrays.asList("ASC")));
        final TableInfo _infoReminders = new TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders);
        final TableInfo _existingReminders = TableInfo.read(db, "reminders");
        if (!_infoReminders.equals(_existingReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "reminders(com.nodocivico.app.data.local.entity.ReminderEntity).\n"
                  + " Expected:\n" + _infoReminders + "\n"
                  + " Found:\n" + _existingReminders);
        }
        final HashMap<String, TableInfo.Column> _columnsFollowUps = new HashMap<String, TableInfo.Column>(4);
        _columnsFollowUps.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowUps.put("reportId", new TableInfo.Column("reportId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowUps.put("comment", new TableInfo.Column("comment", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowUps.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFollowUps = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysFollowUps.add(new TableInfo.ForeignKey("reports", "CASCADE", "NO ACTION", Arrays.asList("reportId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesFollowUps = new HashSet<TableInfo.Index>(1);
        _indicesFollowUps.add(new TableInfo.Index("index_follow_ups_reportId", false, Arrays.asList("reportId"), Arrays.asList("ASC")));
        final TableInfo _infoFollowUps = new TableInfo("follow_ups", _columnsFollowUps, _foreignKeysFollowUps, _indicesFollowUps);
        final TableInfo _existingFollowUps = TableInfo.read(db, "follow_ups");
        if (!_infoFollowUps.equals(_existingFollowUps)) {
          return new RoomOpenHelper.ValidationResult(false, "follow_ups(com.nodocivico.app.data.local.entity.FollowUpEntity).\n"
                  + " Expected:\n" + _infoFollowUps + "\n"
                  + " Found:\n" + _existingFollowUps);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncEvents = new HashMap<String, TableInfo.Column>(6);
        _columnsSyncEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncEvents.put("entityType", new TableInfo.Column("entityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncEvents.put("entityId", new TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncEvents.put("actionType", new TableInfo.Column("actionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncEvents.put("syncDate", new TableInfo.Column("syncDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncEvents.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncEvents = new TableInfo("sync_events", _columnsSyncEvents, _foreignKeysSyncEvents, _indicesSyncEvents);
        final TableInfo _existingSyncEvents = TableInfo.read(db, "sync_events");
        if (!_infoSyncEvents.equals(_existingSyncEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_events(com.nodocivico.app.data.local.entity.SyncEventEntity).\n"
                  + " Expected:\n" + _infoSyncEvents + "\n"
                  + " Found:\n" + _existingSyncEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "cc9991dad758d6682269a7d612a12b56", "d7235057e42b74fb060c4eec55d5189b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "reports","categories","report_status","users","reminders","follow_ups","sync_events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `reports`");
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `report_status`");
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `reminders`");
      _db.execSQL("DELETE FROM `follow_ups`");
      _db.execSQL("DELETE FROM `sync_events`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(ReportDao.class, ReportDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReportStatusDao.class, ReportStatusDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderDao.class, ReminderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncEventDao.class, SyncEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FollowUpDao.class, FollowUpDao_Impl.getRequiredConverters());
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
  public ReportDao reportDao() {
    if (_reportDao != null) {
      return _reportDao;
    } else {
      synchronized(this) {
        if(_reportDao == null) {
          _reportDao = new ReportDao_Impl(this);
        }
        return _reportDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public ReportStatusDao reportStatusDao() {
    if (_reportStatusDao != null) {
      return _reportStatusDao;
    } else {
      synchronized(this) {
        if(_reportStatusDao == null) {
          _reportStatusDao = new ReportStatusDao_Impl(this);
        }
        return _reportStatusDao;
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public ReminderDao reminderDao() {
    if (_reminderDao != null) {
      return _reminderDao;
    } else {
      synchronized(this) {
        if(_reminderDao == null) {
          _reminderDao = new ReminderDao_Impl(this);
        }
        return _reminderDao;
      }
    }
  }

  @Override
  public SyncEventDao syncEventDao() {
    if (_syncEventDao != null) {
      return _syncEventDao;
    } else {
      synchronized(this) {
        if(_syncEventDao == null) {
          _syncEventDao = new SyncEventDao_Impl(this);
        }
        return _syncEventDao;
      }
    }
  }

  @Override
  public FollowUpDao followUpDao() {
    if (_followUpDao != null) {
      return _followUpDao;
    } else {
      synchronized(this) {
        if(_followUpDao == null) {
          _followUpDao = new FollowUpDao_Impl(this);
        }
        return _followUpDao;
      }
    }
  }
}
