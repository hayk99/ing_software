package es.unizar.eina.category;

        import android.content.Intent;
        import android.database.Cursor;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;

        import es.unizar.eina.notepadv3.R;

public class CategoryEdit extends AppCompatActivity {

    private EditText mRowIdText;
    private EditText mTitleText;

    private Long mRowId;
    private  CategoryDbAdapter  mDbHelper;

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
        setContentView(R.layout.category_edit);
        setTitle(R.string.menu_edit_cat);

        mTitleText = (EditText) findViewById(R.id.category_name);
        mRowIdText = (EditText) findViewById(R.id.category_id);

        Button confirmButton = (Button) findViewById(R.id.category_confirm);

        mRowId = (savedInstanceState  == null) ? null :(Long) savedInstanceState.getSerializable(CategoryDbAdapter.KEY_ROWID);
        if (mRowId  == null) {
            Bundle  extras = getIntent ().getExtras ();
            mRowId = (extras  != null) ? extras.getLong(CategoryDbAdapter.KEY_ROWID): null;
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

    private  void  saveState () {
        String  title = mTitleText.getText ().toString ();

        if (title.isEmpty()){
            title = "New category";
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
