package es.unizar.eina.Test.Tests;

import android.util.Log;

import es.unizar.eina.notepadv3.NotesDbAdapter;

public class Tests {
    private NotesDbAdapter myNotes;

    public Tests(NotesDbAdapter currentNotes){
        myNotes = currentNotes;
    }

    public void throwAllTest(){
        try {
            myNotes.deleteAllNotes();
            startTest();
            start1002Notes();
            overflow();
            //myNotes.deleteAllNotes();
        }
        catch(Throwable t){
            Log.d("Tests error", t.getMessage());
        }
    }

    private void logger(String tag, long value){
        if (value > 0)
            Log.d (tag, "SUCCESS");
        else
            Log.d (tag, "FAILURE");
    }

    private void logger(String tag, boolean value){
        if (value)
            Log.d (tag, "SUCCESS");
        else
            Log.d (tag, "FAILURE");
    }

    private void startTest(){
        try {
            Log.d("TEST 1", "Trying to create correct note...");

            long id = myNotes.createNote("Some title", "Some body");
            logger("TEST 1", id);

            Log.d("TEST 2", "Trying to create note without title ...");
            id = myNotes.createNote("", "Some body");
            logger("TEST 2", id);

            Log.d("TEST 3", "Trying to create note without body ...");
            id = myNotes.createNote("Some title", "");
            logger("TEST 3", id);

            Log.d("TEST 4", "Trying to create note title null...");
            id = myNotes.createNote(null, "Some body");
            logger("TEST 4", id);

            Log.d("TEST 5", "Trying to create note body null...");
            id = myNotes.createNote("Some title", null);
            logger("TEST 5", id);

            Log.d("TEST 6", "Trying to create correct note and delete the same one...");
            id = myNotes.createNote("Some title", "Some body");
            boolean returned = myNotes.deleteNote(id);
            logger("TEST 6", returned);

            Log.d("TEST 7", "Trying to delete nonexistent note...");
            returned = myNotes.deleteNote(58);
            logger("TEST 7", returned);

            Log.d("TEST 8", "Trying to delete note with id < 0");
            returned = myNotes.deleteNote(-20);
            logger("TEST 8", returned);


            Log.d("TEST 9", "Trying to update exisiting note...");
            id = myNotes.createNote("Some title", "Some body");
            returned = myNotes.updateNote(id, "New Title", "New body TEST 9");
            logger("TEST 9", returned);

            Log.d("TEST 10", "Trying to update whit null title...");
            returned = myNotes.updateNote(id, null, "New body 2");
            logger("TEST 10", returned);

            Log.d("TEST 11", "Trying to update whit null body...");
            returned = myNotes.updateNote(id, "New Title 11", null);
            logger("TEST 11", returned);

            Log.d("TEST 12", "Trying to update whit empty body...");
            returned = myNotes.updateNote(id, "New Title 12", "");
            logger("TEST 12", returned);

            Log.d("TEST 13", "Trying to update whit empty title...");
            returned = myNotes.updateNote(id, "", "New body 13");
            logger("TEST 13", returned);
        } catch (Throwable a){
            Log.d("exception thrown", a.getMessage());
        }

    }
    private void start1002Notes(){

            boolean success = true;
            Log.d("TEST 14", "Trying to create 1002 notes...");
            for (int i = 0; i < 1002 && success; i++) {
                long id = myNotes.createNote("Note Test 1002 notes " + i, "body of note");
                success = id > 0;
            }
            logger("TEST 14", success);
    }

    private void  overflow(){
        Log.d("TEST 15", "Trying to overflow database...");
        String body = "Body";
        String title = "Test 15";
        boolean works =true;
        while (works){
            try {
                long id = myNotes.createNote(title, body);
                if (id < 1){
                    Log.d("Test 15", "overflow with body whose length is: " + body.length());
                    works =false;
                }
                body = body + "B";
            }
            catch(Throwable t){
                Log.d("Test 15", t.getMessage());
            }
        }
        myNotes.deleteAllNotes();
        Log.d("Test 15", "Deleting all create notes");
        myNotes.createNote("New note after test 15", "body");
    }
}
