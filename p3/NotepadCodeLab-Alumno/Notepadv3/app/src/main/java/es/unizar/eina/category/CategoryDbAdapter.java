package es.unizar.eina.category;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class CategoryDbAdapter {

    public static final String KEY_TITLE = "cat_name";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "CategoryDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table categories (_id integer primary key autoincrement, "
                    + "cat_name text not null);";

    private static final String DATABASE_NAME = "categories";
    private static final String DATABASE_TABLE = "categories";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public static final String KEY_TITLE = "cat_name";
        public static final String KEY_ROWID = "_id";

        DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public CategoryDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the categories database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public CategoryDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new category using the title and body provided. If the category is
     * successfully created return the new rowId for that category, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the category
     * @return rowId or -1 if failed
     */
    public long createCategory(String title) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_TITLE, title);
        Log.d("catDB", "titulo: "+title);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowIdss
     *
     * @param rowId id of category to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCategory(long rowId) {
        if ( rowId>0) {
            return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
        }else return false;
    }

    public boolean deleteAllCategories(){
        return mDb.delete(DATABASE_TABLE, KEY_ROWID, null) >0;
    }
    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all categories
     */
    public Cursor fetchAllCategories() {

        return mDb.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_TITLE,
                },
                null, null, null, null, KEY_TITLE + " ASC");
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of category to retrieve
     * @return Cursor positioned to matching category, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCategory(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_TITLE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }



    /**
     * Update the category using the details provided. The category to be updated is
     * specified using the rowId, and it is altered to use the title
     *
     * @param rowId id of category to update
     * @param title value to set category title to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCategory(long rowId, String title) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    public Long idFromName(String name){
        Log.d("query sql", "name : "+ name);
        if (!name.equals("")){
            Cursor mCursor = mDb.query(false, DATABASE_TABLE, new String[] {KEY_ROWID}, KEY_TITLE + "= '"+ name+ "'" , null,
                        null, null, null, null);
            Long id = 0L;
            if (mCursor != null) {
                mCursor.moveToFirst();
                String idSting = mCursor.getString(mCursor.getColumnIndex(mDbHelper.KEY_ROWID));
                id = Long.parseLong(idSting);
                Log.d("Query SQL", "Searching category id from name: " + name);
            }
            return id;
        }
        else return -1L;

    }
}