package es.unizar.eina.notepadv3;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CategoryEdit extends AppCompatActivity {

    private EditText mIDText;
    private EditText mTitleText;
    private Long mRowId;

    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.category_edit);
        setTitle(R.string.edit_category);

        mIDText = (EditText) findViewById(R.id.iden);
        mIDText.setText("***");
        mTitleText = (EditText) findViewById(R.id.title);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.CAT_KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(NotesDbAdapter.CAT_KEY_ROWID)
                    : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor category= mDbHelper.fetchCategory(mRowId);
            startManagingCursor(category);
            mIDText.setText(category.getString(
                    category.getColumnIndexOrThrow(NotesDbAdapter.CAT_KEY_ROWID)));
            mTitleText.setText(category.getString(
                    category.getColumnIndexOrThrow(NotesDbAdapter.CAT_KEY_NAME)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.CAT_KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String name = mTitleText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createCategory(name);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateCategory(mRowId, name);
        }
    }

}
