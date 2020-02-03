package es.unizar.eina.catlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import es.unizar.eina.notepadv3.R;

public class CatEdit extends AppCompatActivity {

    private EditText mNameText;
    private Long mRowId;

    private CatDbAdapter mDbHelper;

    private EditText mIdText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new CatDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.cat_edit);
        setTitle(R.string.edit_cat);

        mNameText = (EditText) findViewById(R.id.cat_name);

        mIdText = (EditText) findViewById(R.id.cat_id);
        mIdText.setEnabled(false);

        Button confirmButton = (Button) findViewById(R.id.cat_confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(CatDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(CatDbAdapter.KEY_ROWID)
                    : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("cat_result", mNameText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor cat = mDbHelper.fetchCategory(mRowId);
            startManagingCursor(cat);
            mNameText.setText(cat.getString(
                    cat.getColumnIndexOrThrow(CatDbAdapter.KEY_TITLE)));

            mIdText.setText(mRowId.toString());
        }
        else {
            mIdText.setText("***");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(CatDbAdapter.KEY_ROWID, mRowId);
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
        String name = mNameText.getText().toString();

        if (name.isEmpty()) {
            name = "Category";
        }

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
