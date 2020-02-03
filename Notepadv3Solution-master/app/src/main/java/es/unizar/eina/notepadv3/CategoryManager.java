package es.unizar.eina.notepadv3;

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

/**
 * Shows all categories and allows the user to edit and create categories
 *
 * @autor David Mañas Vidorreta (614590)
 */
public class CategoryManager extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int DELETE_ALL_CATEGORIES_ID = Menu.FIRST + 5;

    private NotesDbAdapter mDbHelper;
    private ListView mList;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView)findViewById(R.id.list);
        fillData();

        registerForContextMenu(mList);

    }

    /**
     * Fills the layout with all the categories stored in the database
     */
    private void fillData() {
        // Get all of the categories from the database and create the item list
        Cursor categoriesCursor = mDbHelper.fetchAllCategories();
        startManagingCursor(categoriesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { NotesDbAdapter.CAT_KEY_NAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter categories =
                new SimpleCursorAdapter(this, R.layout.notes_row, categoriesCursor, from, to);
        mList.setAdapter(categories);
    }

    /**
     * Creates the options menu
     * @param menu
     *          menu in which the options will be added
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_add_cat);
        menu.add(Menu.NONE, DELETE_ALL_CATEGORIES_ID, Menu.NONE, R.string.menu_delete_all_cats);
        return result;
    }

    /**
     * Executes the action that corresponds with the chosen menu option
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createCategory();
                return true;
            case DELETE_ALL_CATEGORIES_ID:
                mDbHelper.deleteAllCategories();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates the context menu for a category
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete_cat);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.menu_edit_cat);
    }

    /**
     * Executes the action that corresponds with the chosen menu option
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteCategory(info.id);
                fillData();
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editCategory(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Starts the CategoryEdit activity to create a new category
     */
    private void createCategory() {
        Intent i = new Intent(this, CategoryEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Starts the CategoryEdit activity to update a category
     * @param id id for the category that will be updated
     */
    private void editCategory(long id) {
        Intent i = new Intent(this, CategoryEdit.class);
        i.putExtra(NotesDbAdapter.CAT_KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /**
     * Rewrites the category layout on creation or updates
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

}
