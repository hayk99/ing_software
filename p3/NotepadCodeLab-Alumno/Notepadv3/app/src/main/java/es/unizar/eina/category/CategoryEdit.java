package es.unizar.eina.category;

        import android.content.Intent;
        import android.database.Cursor;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Spinner;

        import java.util.ArrayList;

        import es.unizar.eina.notepadv3.NotesDbAdapter;
        import es.unizar.eina.notepadv3.R;

public class CategoryEdit extends AppCompatActivity {

    private EditText mRowIdText;
    private EditText mTitleText;

    private Long mRowId;
    private  CategoryDbAdapter  mDbHelper;
    private String action;
    private ArrayList<String> categories = new ArrayList<>();
    private Spinner mCategorySpin;
    private int value = 0;
    private EditText new_cat_name;

    private  void  populateFields () {
        Log.d("populates", action);
        if (mRowId  != 0 ) {
            Log.d("populates catedit", "row: " +mRowId);
            Cursor category = mDbHelper.fetchCategory(mRowId);
            startManagingCursor(category);
            mRowIdText.setText(category.getString(category.getColumnIndexOrThrow(CategoryDbAdapter.KEY_ROWID)));
            mTitleText.setText(category.getString(category.getColumnIndexOrThrow(CategoryDbAdapter.KEY_TITLE)));
        }
    }

    public  void  onClick(View  view) {
        setResult(RESULT_OK);
        finish ();
    }
    private void updateCat(String newName, String oldName){
        Long id = mDbHelper.idFromName(oldName);
        mDbHelper.updateCategory(id, newName);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new  CategoryDbAdapter(this);
        mDbHelper.open();

        Bundle param = getIntent().getExtras();
        action = param.getString("action");
        if (action.equals("delete") || action.equals("filter") || action.equals("edit")) {
            if (action.equals("edit")){
                setTitle("Edit Category");
                setContentView(R.layout.category_modify);
              new_cat_name = (EditText) findViewById(R.id.cat_new_name);
            }
            else {
                setTitle("Delete or Filter Category");
                setContentView(R.layout.category_delete);
            }
            Log.d("BUNDLE ACTION", action);

            mDbHelper.open();
            Cursor catCursor = mDbHelper.fetchAllCategories();

            if (catCursor.moveToFirst()) {
                categories.clear();
                if (action.equals("filter")){
                    categories.add("");
                }
                do {
                    String name = catCursor.getString(catCursor.getColumnIndex(mDbHelper.KEY_TITLE));
                    Log.d("Cat", name);
                    categories.add(name);

                }while (catCursor.moveToNext());
            }

            //spiner
            //mCategorySpin.setAdapter(null);
            ArrayAdapter<String> spinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategorySpin = (Spinner) findViewById(R.id.spinner_category);
            mCategorySpin.setAdapter(spinner);

            Button confirmButton = (Button) findViewById(R.id.category_confirm);
            confirmButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    String title = null;
                    try {title = mCategorySpin.getSelectedItem().toString();}
                    catch (Exception e){
                        title = "";
                    }
                    Long idCat = mDbHelper.idFromName(title);
                    if (action.equals("edit")){
                        String newName = new_cat_name.getText().toString();
                        updateCat(newName, title);
                        title = newName;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putLong("id_cat", idCat);
                    bundle.putString("tit_cat", title);
                    bundle.putString("op",action);

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);

                    finish();
                }

            });
        }
        else {
            Log.d("category edit", "entro creacion");
            setContentView(R.layout.category_edit);
            setTitle(R.string.menu_edit_cat);

            //mRowIdText = (EditText) findViewById(R.id.category_id);
            mTitleText = (EditText) findViewById(R.id.category_name);


            mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CategoryDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = (extras != null) ? extras.getLong(CategoryDbAdapter.KEY_ROWID) : null;
            }
            populateFields();

            Button confirmButton = (Button) findViewById(R.id.category_confirm);
            confirmButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString(CategoryDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                    if (mRowId != null) {
                        bundle.putLong(CategoryDbAdapter.KEY_ROWID, mRowId);
                    }
                    bundle.putString("op",action);
                    bundle.putString("tit_cat", mTitleText.getText().toString());


                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    finish();
                }

            });

        }

    }

    private  void  saveState () {
        if (action.equals("create")) {
            Log.d("saveState", "save");

            String title = mTitleText.getText().toString();
            Log.d("SaveState", "name: " + title);
            if (title.isEmpty()) {
                title = "New category" + value;
                value++;
            }
            if (mRowId == 0) {
                Log.d("saveState", "row es null, creo");
                long id = mDbHelper.createCategory(title);
                if (id > 0) {
                    mRowId = id;
                }
            } else {
                Log.d("saveState", "row NO es null, update" + mRowId);
                mDbHelper.updateCategory(mRowId, title);
            }
        }
    }



    @Override
    protected  void  onSaveInstanceState(Bundle  outState){
        super.onSaveInstanceState(outState);
        saveState ();
        outState.putSerializable(CategoryDbAdapter.KEY_ROWID , mRowId);
    }

    @Override
    protected  void  onPause () {
        super.onPause ();
        saveState ();
    }

    @Override
    protected  void  onResume () {
        super.onResume ();
        if (action.equals("create"))
            populateFields ();
    }

}
