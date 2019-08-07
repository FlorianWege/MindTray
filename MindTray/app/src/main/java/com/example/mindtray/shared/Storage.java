package com.example.mindtray.shared;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.example.mindtray.memo.AudioContent;
import com.example.mindtray.memo.Content;
import com.example.mindtray.memo.ImageContent;
import com.example.mindtray.memo.Memo;
import com.example.mindtray.memo.TextContent;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
    persistence abilities mostly realized via an sqlite data base, avoid to close data base references manually at all costs
 */

public class Storage {
    //custom exception is fancier
    public static class StorageException extends Exception {
        public StorageException(Exception e) {
            super(e);
        }
    }

    public static class DataBase extends SQLiteOpenHelper {
        //escape names because we need to write raw SQL statements or even the given methods do not do it properly...
        private static String escapeSQLName(String s) {
            return "[" + s + "]";
        }

        //escape whole array of strings
        private static String[] escapeSQLName(String[] sArr) {
            String[] ret = new String[sArr.length];
            int c = 0;

            for (String s : sArr) {
                ret[c++] = escapeSQLName(s);
            }

            return ret;
        }

        //simplified query method
        private static Cursor SQLquery(SQLiteDatabase db, String table, String[] cols) {
            return db.query(false, escapeSQLName(table), escapeSQLName(cols), null, null, null, null, null, null);
        }

        private Context _context = null;

        //handles a memo's contents, so we do not need to pass in memo everytime and it's better encapsulated
        public class MemoHandler {
            private Memo _memo;

            private SQLiteStatement _stmt_contents_createTable = null;
            private SQLiteStatement _stmt_contents_add = null;
            private SQLiteStatement _stmt_contents_remove = null;

            private final String CONTENTS_TABLE;
            private final String CONTENTS_COL_KEY = "key";
            private final String CONTENTS_COL_INDEX = "row";  //sqlite does not like "index" as column name...
            private final String CONTENTS_COL_NAME = "name";
            private final String CONTENTS_COL_TYPE = "type";
            private final String CONTENTS_COL_TEXT = "text";
            private final String CONTENTS_COL_DATA = "data";

            public MemoHandler(Memo memo) {
                _memo = memo;

                CONTENTS_TABLE = String.format("memo_%s_contentsFinal", _memo.getKey());
            }

            public void create() {
                SQLiteDatabase db = getWritableDatabase();

                if (_stmt_contents_createTable == null) {
                    _stmt_contents_createTable = db.compileStatement(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                    "%s NVARCHAR NOT NULL," +
                                    "%s INT NOT NULL, " +
                                    "%s NVARCHAR NOT NULL," +
                                    "%s NVARCHAR NOT NULL," +
                                    "%s NVARCHAR NOT NULL," +
                                    "%s BLOB NOT NULL)", escapeSQLName(CONTENTS_TABLE),
                            escapeSQLName(CONTENTS_COL_KEY), escapeSQLName(CONTENTS_COL_INDEX), escapeSQLName(CONTENTS_COL_NAME), escapeSQLName(CONTENTS_COL_TYPE), escapeSQLName(CONTENTS_COL_TEXT), escapeSQLName(CONTENTS_COL_DATA)));
                }

                _stmt_contents_createTable.execute();
            }

            public synchronized List<Content> getContents() throws StorageException {
                try {
                    SQLiteDatabase base = getReadableDatabase();

                    try {
                        create();

                        Cursor c = SQLquery(base, CONTENTS_TABLE, new String[]{CONTENTS_COL_KEY, CONTENTS_COL_INDEX, CONTENTS_COL_NAME, CONTENTS_COL_TYPE, CONTENTS_COL_TEXT, CONTENTS_COL_DATA});

                        List<Content> ret = new ArrayList<>();

                        if (c.getCount() > 0) {
                            c.moveToFirst();

                            do {
                                String key = c.getString(0);
                                int index = c.getInt(1);
                                String name = c.getString(2);
                                String type = c.getString(3);
                                String text = c.getString(4);
                                byte[] data = c.getBlob(5);

                                Content content = null;

                                switch (type) {
                                    case "text": {
                                        content = new TextContent(name, new String(data, StandardCharsets.UTF_8));

                                        content.setText(text);

                                        break;
                                    }
                                    case "image": {
                                        content = new ImageContent(name, Util.bytesToBitmap(data));

                                        content.setText(text);

                                        break;
                                    }
                                    case "audio": {
                                        content = new AudioContent(name, data);

                                        content.setText(text);

                                        break;
                                    }
                                }

                                ret.add(content);

                                _contentsInDB.add(content);
                            } while (c.moveToNext());
                        }

                        c.close();

                        return ret;
                    } catch (SQLException e) {
                        throw new StorageException(e);
                    } finally {
                        //base.close();
                    }
                } catch (SQLException e) {
                    throw e;
                }
            }

            private Set<Content> _contentsInDB = new HashSet<>();

            public synchronized void addContent(Content content) throws StorageException {
                try {
                    if (!_contentsInDB.contains(content)) {
                        SQLiteDatabase base = getWritableDatabase();

                        try {
                            create();

                            if (_stmt_contents_add == null)
                                _stmt_contents_add = base.compileStatement(String.format("INSERT INTO %s(%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)", escapeSQLName(CONTENTS_TABLE),
                                        escapeSQLName(CONTENTS_COL_KEY), escapeSQLName(CONTENTS_COL_INDEX), escapeSQLName(CONTENTS_COL_NAME), escapeSQLName(CONTENTS_COL_TYPE), escapeSQLName(CONTENTS_COL_TEXT), escapeSQLName(CONTENTS_COL_DATA)));

                            _stmt_contents_add.clearBindings();
                            _stmt_contents_add.bindString(1, content.getKey());
                            _stmt_contents_add.bindLong(2, _memo.getContents().indexOf(content));
                            _stmt_contents_add.bindString(3, content.getName());
                            _stmt_contents_add.bindString(5, content.getText());

                            if (content instanceof TextContent) {
                                _stmt_contents_add.bindString(4, "text");
                                _stmt_contents_add.bindBlob(6, ((TextContent) content).getText().getBytes(StandardCharsets.UTF_8));
                            } else if (content instanceof ImageContent) {
                                _stmt_contents_add.bindString(4, "image");
                                _stmt_contents_add.bindBlob(6, Util.bitmapToBytes(((ImageContent) content).getBitmap()));
                            } else if (content instanceof AudioContent) {
                                _stmt_contents_add.bindString(4, "audio");
                                _stmt_contents_add.bindBlob(6, ((AudioContent) content).getBytes());
                            }

                            _stmt_contents_add.executeInsert();
                        } catch (SQLException e) {
                            throw e;
                        } finally {
                            //base.close();
                        }

                        _contentsInDB.add(content);
                    }
                } catch (SQLException e) {
                    throw e;
                }
            }

            public synchronized void removeContent(Content content) throws StorageException {
                try {
                    if (_contentsInDB.contains(content)) {
                        SQLiteDatabase base = getWritableDatabase();

                        try {
                            create();

                            if (_stmt_contents_remove == null)
                                _stmt_contents_remove = base.compileStatement(String.format("DELETE FROM %s WHERE %s = ?", escapeSQLName(CONTENTS_TABLE), escapeSQLName(CONTENTS_COL_KEY)));

                            _stmt_contents_remove.clearBindings();
                            _stmt_contents_remove.bindString(1, content.getKey());

                            if (_stmt_contents_remove.executeUpdateDelete() < 1) {
                                throw new SQLException("nothing to delete");
                            };
                        } catch (SQLException e) {
                            throw e;
                        } finally {
                            //base.close();
                        }

                        _contentsInDB.remove(content);
                    }
                } catch (SQLException e) {
                    throw e;
                }
            }

            public void updateContent(Content content) throws StorageException {
                removeContent(content);

                addContent(content);
            }
        }

        private Map<Memo, MemoHandler> _memoHandlerMap = new HashMap<>();

        public MemoHandler getMemoHandler(Memo memo) {
            if (!_memoHandlerMap.containsKey(memo)) _memoHandlerMap.put(memo, new MemoHandler(memo));

            return _memoHandlerMap.get(memo);
        }

        private SQLiteStatement _stmt_memos_createTable;
        private SQLiteStatement _stmt_memos_add;
        private SQLiteStatement _stmt_memos_remove;
        private SQLiteStatement _stmt_memos_dropTable;

        private final String MEMOS_TABLE = "memos";
        private final String MEMOS_COL_KEY = "key";
        private final String MEMOS_COL_NAME = "name";
        private final String MEMOS_COL_DATE = "date";

        private Map<Memo, String> _keys = new HashMap<>();

        public synchronized void create() {
            SQLiteDatabase db = getWritableDatabase();

            if (_stmt_memos_createTable == null) {
                _stmt_memos_createTable = db.compileStatement(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s NVARCHAR PRIMARY KEY UNIQUE," +
                                "%s NVARCHAR NOT NULL," +
                                "%s NVARCHAR NOT NULL)", escapeSQLName(MEMOS_TABLE),
                        escapeSQLName(MEMOS_COL_KEY), escapeSQLName(MEMOS_COL_NAME), escapeSQLName(MEMOS_COL_DATE)));
            }

            _stmt_memos_createTable.execute();
        }

        private Set<Memo> _memosInDB = new HashSet<>();

        public synchronized void addMemo(final Memo memo) throws StorageException {
            try {
                if (!_memosInDB.contains(memo)) {
                    SQLiteDatabase base = getWritableDatabase();
                    String key = memo.getKey();

                    try {
                        if (_stmt_memos_add == null)
                            _stmt_memos_add = base.compileStatement(String.format("INSERT INTO %s(key, name, date) VALUES (?, ?, ?)", escapeSQLName(MEMOS_TABLE)));

                        _stmt_memos_add.clearBindings();
                        _stmt_memos_add.bindString(1, key);
                        _stmt_memos_add.bindString(2, memo.getName());
                        _stmt_memos_add.bindString(3, memo.getDateS());

                        _stmt_memos_add.executeInsert();

                        for (Content content : memo.getContents()) {
                            getMemoHandler(memo).addContent(content);
                        }
                    } catch (Exception e){
                        throw e;
                    } finally {
                        //base.close();
                    }

                    _keys.put(memo, key);
                    _memosInDB.add(memo);
                }
            } catch (SQLException e) {
                throw e;
            }
        }

        public synchronized void removeMemo(Memo memo) throws StorageException {
            try {
                if (_memosInDB.contains(memo)) {
                    SQLiteDatabase base = getWritableDatabase();

                    try {
                        if (!_keys.containsKey(memo))
                            throw new StorageException(new Exception("memo " + memo + "has no associated key"));

                        String key = _keys.get(memo);

                        if (_stmt_memos_remove == null)
                            _stmt_memos_remove = base.compileStatement(String.format("DELETE FROM %s WHERE key = ?", escapeSQLName(MEMOS_TABLE)));

                        _stmt_memos_remove.clearBindings();
                        _stmt_memos_remove.bindString(1, key);

                        _stmt_memos_remove.executeUpdateDelete();
                    } catch (SQLException e) {
                        throw e;
                    } finally {
                        //base.close();
                    }

                    _keys.remove(memo);
                    _memosInDB.remove(memo);
                }
            } catch (SQLException e) {
                throw e;
            }
        }

        public synchronized List<Memo> getMemos() throws StorageException {
            try {
                SQLiteDatabase base = getReadableDatabase();

                try {
                    create();

                    Cursor c = SQLquery(base, MEMOS_TABLE, new String[]{MEMOS_COL_KEY, MEMOS_COL_NAME, MEMOS_COL_DATE});

                    List<Memo> ret = new ArrayList<>();

                    if (c.getCount() > 0) {
                        c.moveToFirst();

                        do {
                            String key = c.getString(0);
                            String name = c.getString(1);
                            String dateS = c.getString(2);

                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                            Calendar date = Calendar.getInstance();

                            date.setTime(df.parse(dateS));

                            Memo memo = new Memo(name, date);

                            ret.add(memo);

                            _memosInDB.add(memo);
                            _keys.put(memo, key);
                        } while (c.moveToNext());
                    }

                    c.close();

                    return ret;
                } catch (Exception e) {
                    throw new StorageException(e);
                } finally {
                    //base.close();
                }
            } catch (SQLException e) {
                throw e;
            }
        }

        public synchronized void clear() throws StorageException {
            try {
                create();

                if (_stmt_memos_dropTable == null)
                    _stmt_memos_dropTable = getWritableDatabase().compileStatement(String.format("DROP TABLE %s", escapeSQLName(MEMOS_TABLE)));

                _stmt_memos_dropTable.execute();

                _memosInDB.clear();
                _keys.clear();
            } catch (SQLException e) {
                throw e;
            }
        }

        public DataBase(Context context, String name, int version) {
            super(context, name, null, version);

            _context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            create();

            Toast.makeText(_context, "Datenbank wurde erstellt", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                Toast.makeText(_context, String.format("Datenbank-Version von %d auf %d erhöht", oldVersion, newVersion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private DataBase _db = null;

    public DataBase getDB() {
        return _db;
    }

    private List<Memo> _memos = new ArrayList<>();

    public List<Memo> getMemos() {
        return _memos;
    }

    public void addMemo(Memo val) throws StorageException {
        _db.addMemo(val);

        _memos.add(val);
    }

    public void removeMemo(Memo val) throws StorageException {
        _db.removeMemo(val);

        _memos.remove(val);
    }

    private static Storage _instance = null;

    public static Storage getInstance(Context context) {
        if (_instance == null) {
            _instance = new Storage(context);

            //leave constructor empty and save the simpleton object in the variable first in order to avoid possible recursions in constructor
            _instance.load();
        }

        return _instance;
    }

    private void loadMemos() {
        try {
            Collection<Memo> memos = _db.getMemos();

            for (Memo memo : memos) {
                List<Content> contents = _db.getMemoHandler(memo).getContents();

                for (Content content : contents) {
                    memo.addContent(content);
                }

                addMemo(memo);
            }
        } catch (StorageException e) {
            Log.e(getClass().getSimpleName(), "could not load storage");
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void load() {
        loadMemos();
    }

    private Storage(Context context) {
        _db = new DataBase(context, "mind.db", 1);
    }
}