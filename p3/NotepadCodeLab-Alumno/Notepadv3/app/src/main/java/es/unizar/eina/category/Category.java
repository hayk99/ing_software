package es.unizar.eina.category;


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

import es.unizar.eina.notepadv3.NoteEdit;
import es.unizar.eina.notepadv3.NotesDbAdapter;
import es.unizar.eina.notepadv3.R;
import es.unizar.eina.send.SendAbstractionImpl;

public class Category extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_CAT = Menu.FIRST;
    private static final int DELETE_ALL_CAT = Menu.FIRST + 1;
    private static final int EDIT_CAT = Menu.FIRST + 2;
    private static final int DELETE_CAT =Menu.FIRST +3;



    private static final int TEST_ID = Menu.FIRST +3;
    private static final int CREATE_ID= Menu.FIRST +6;

    private CategoryDbAdapter catDbHelper;
    private NotesDbAdapter mDbHelper;
    private Cursor notesCursor;
    private Cursor categoriesCursor;
    private String beforeEdit;
    private ListView mList;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        catDbHelper = new CategoryDbAdapter(this);
        catDbHelper.open();

        mList = (ListView)findViewById(R.id.list);
        fillData();

        registerForContextMenu(mList);

    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor  categoryCrusor = catDbHelper.fetchAllCategories ();
        startManagingCursor(categoryCrusor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { CategoryDbAdapter.KEY_TITLE };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter categories =
                new SimpleCursorAdapter(this, R.layout.notes_row, categoryCrusor, from, to);
        mList.setAdapter(categories);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_CAT, Menu.NONE, R.string.menu_create_cat);
        menu.add(Menu.NONE, DELETE_ALL_CAT, Menu.NONE, R.string.menu_delete_all_cat);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_CAT:
                createCategory();
                return true;
            case DELETE_ALL_CAT:
                catDbHelper.deleteAllCategories();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, EDIT_CAT, Menu.NONE, R.string.menu_edit_cat);
        menu.add(Menu.NONE, DELETE_CAT, Menu.NONE, R.string.menu_delete_cat );

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ALL_CAT:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                catDbHelper.deleteAllCategories();
                fillData();
                return true;

            case EDIT_CAT:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Long rowId = info.id;
                editCategory(rowId);
                return true;

        }
        return super.onContextItemSelected(item);
    }

    private void createCategory() {
        Intent i = new Intent(this, Category.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }


    protected void editCategory(long id) {

        Intent i = new Intent(this, Category.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

}
