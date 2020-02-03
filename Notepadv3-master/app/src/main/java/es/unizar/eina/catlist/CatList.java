package es.unizar.eina.catlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import es.unizar.eina.notepadv3.NotesDbAdapter;
import es.unizar.eina.notepadv3.R;

public class CatList extends AppCompatActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int SELECT_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 3;

    private CatDbAdapter mDbHelper;
    private NotesDbAdapter mNotesDbHelper;
    private ListView mList;

    private String oldCat;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catlist);

        mNotesDbHelper = new NotesDbAdapter(this);
        mNotesDbHelper.open();

        mDbHelper = new CatDbAdapter(this);
        mDbHelper.open();
        mList = (ListView)findViewById(R.id.cat_list);
        fillData();

        registerForContextMenu(mList);
    }

    private void fillData() {
        // Get all of the cats from the database and create the item list
        Cursor cat = mDbHelper.fetchAllCategories();
        startManagingCursor(cat);

        // Create an array to specify the fields we want to display in the list
        // (only TITLE)
        String[] from = new String[] { CatDbAdapter.KEY_TITLE };

        // and an array of the fields we want to bind those fields to (in this
        // case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter cats =
                new SimpleCursorAdapter(this, R.layout.notes_row,
                        cat, from, to);
        mList.setAdapter(cats);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createCategory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, SELECT_ID, Menu.NONE, R.string.menu_cat_select);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_cat_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.menu_cat_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case SELECT_ID:
                AdapterView.AdapterContextMenuInfo info =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Long mRowId = info.id;

                String catSelected = "";
                if (mRowId != null) {
                    Cursor cat = mDbHelper.fetchCategory(mRowId);
                    startManagingCursor(cat);
                    catSelected = cat.getString(
                            cat.getColumnIndexOrThrow(CatDbAdapter.KEY_TITLE));
                }

                Intent data = new Intent();
                data.putExtra("cat_result", catSelected);
                setResult(RESULT_OK, data);
                finish();
                return true;
            case DELETE_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mRowId = info.id;

                Cursor cat = mDbHelper.fetchCategory(mRowId);
                startManagingCursor(cat);
                catSelected = cat.getString(
                        cat.getColumnIndexOrThrow(CatDbAdapter.KEY_TITLE));

                mNotesDbHelper.deleteCategory(catSelected);
                mDbHelper.deleteCategory(info.id);

                fillData();
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mRowId = info.id;

                cat = mDbHelper.fetchCategory(mRowId);
                startManagingCursor(cat);
                oldCat = cat.getString(
                        cat.getColumnIndexOrThrow(CatDbAdapter.KEY_TITLE));

                editCategory(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createCategory() {
        Intent i = new Intent(this, CatEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }


    private void editCategory(long id) {
        Intent i = new Intent(this, CatEdit.class);
        i.putExtra(CatDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == ACTIVITY_EDIT) {
            if (resultCode == RESULT_OK) {
                String new_category = intent.getStringExtra("cat_result");
                if (!oldCat.equals(new_category)) {
                    mNotesDbHelper.updateCategory(oldCat, new_category);
                }
            }
        }
        fillData();
    }

}
