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

    private ArrayList<String> categories = new ArrayList<>();
    private Spinner mCategorySpin;

    private  void  populateFields () {
        if (mRowId  != null) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new  CategoryDbAdapter(this);
        mDbHelper.open();

        Bundle param = getIntent().getExtras();
        String action = param.getString("action");
        if (action.equals("delete")){
            setContentView(R.layout.category_delete);
            setTitle("Delete Category");
            Log.d("BUNDLE ACTION", action);

            mDbHelper.open();
            Cursor catCursor = mDbHelper.fetchAllCategories();

            if (catCursor.moveToFirst()) {
                categories.clear();
                categories.add("");
                do {
                    String name = catCursor.getString(catCursor.getColumnIndex(mDbHelper.KEY_TITLE));
                    categories.add(name);

                }while (catCursor.moveToNext());
            }

            //spiner
            //mCategorySpin.setAdapter(null);
            ArrayAdapter<String> spinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategorySpin = (Spinner) findViewById(R.id.spinner_category);
            mCategorySpin.setAdapter(spinner);

            Button confirmButton = (Button) findViewById(R.id.confirm);
            confirmButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                    bundle.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());
                    if (mRowId != null) {
                        bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                    }
                    if (mCatText != null){
                        bundle.putString(NotesDbAdapter.KEY_CATEGORY, mCatText.getText().toString());
                    }
                    else{
                        bundle.putString(NotesDbAdapter.KEY_CATEGORY, "Category: none");
                    }
                    Log.d("noteedit", "antes intent");

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);

                    Log.d("noteedit", "fin");
                    finish();
                }

            });
        }
        else {
            setContentView(R.layout.category_edit);
            setTitle(R.string.menu_edit_cat);

            mTitleText = (EditText) findViewById(R.id.category_name);

            Button confirmButton = (Button) findViewById(R.id.category_confirm);

            mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CategoryDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = (extras != null) ? extras.getLong(CategoryDbAdapter.KEY_ROWID) : null;
            }
            populateFields();
            confirmButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString(CategoryDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                    if (mRowId != null) {
                        bundle.putLong(CategoryDbAdapter.KEY_ROWID, mRowId);
                    }

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    finish();
                }

            });
        }

    }

    private  void  saveState () {
        int value = 0;
        String  title = mTitleText.getText ().toString ();

        if (title.isEmpty()){
            title = "New category"+value;
            value++;
        }
        if (mRowId  == null) {
            long id = mDbHelper.createCategory(title);
            if (id > 0) {mRowId = id;
            }
        } else {
            mDbHelper.updateCategory(mRowId , title);
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
        populateFields ();
    }

}
