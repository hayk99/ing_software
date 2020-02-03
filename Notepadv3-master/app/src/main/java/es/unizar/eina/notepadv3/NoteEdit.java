package es.unizar.eina.notepadv3;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import es.unizar.eina.catlist.CatList;

public class NoteEdit extends AppCompatActivity {

    private static final int ACTIVITY_CATEGORY = 1;

    private EditText mTitleText;
    private EditText mBodyText;
    private EditText mCatText;
    private Long mRowId;

    private NotesDbAdapter mDbHelper;

    private EditText mIdText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        mCatText = (EditText) findViewById(R.id.category);
        mCatText.setEnabled(false);

        mIdText = (EditText) findViewById(R.id.id);
        mIdText.setEnabled(false);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                    : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });

        Button modifyButton = (Button) findViewById(R.id.cat_modify);
        modifyButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), CatList.class);
                startActivityForResult(i, ACTIVITY_CATEGORY);
            }

        });

        Button resetButton = (Button) findViewById(R.id.cat_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mCatText.setText("");
                saveState();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            mCatText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY)));

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
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
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
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String category = mCatText.getText().toString();

        if (title.isEmpty()) {
            title = "Note";
        }

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, category);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, category);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == ACTIVITY_CATEGORY) {
            if (resultCode == RESULT_OK) {
                mCatText.setText(intent.getStringExtra("cat_result"));
                saveState();
            }
        }
    }

}
