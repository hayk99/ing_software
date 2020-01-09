package es.unizar.eina.Test.Tests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
            addingAfter1000();
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

            long id = myNotes.createNote("Some title", "Some body", "cat");
            logger("TEST 1", id);

            Log.d("TEST 2", "Trying to create note without title ...");
            id = myNotes.createNote("", "Some body","cat");
            logger("TEST 2", id);

            Log.d("TEST 3", "Trying to create note without body ...");
            id = myNotes.createNote("Some title", "","cat");
            logger("TEST 3", id);

            Log.d("TEST 4", "Trying to create note title null...");
            id = myNotes.createNote(null, "Some body","cat");
            logger("TEST 4", id);

            Log.d("TEST 5", "Trying to create note body null...");
            id = myNotes.createNote("Some title", null,"cat");
            logger("TEST 5", id);

            Log.d("TEST 6", "Trying to create correct note and delete the same one...");
            id = myNotes.createNote("Some title", "Some body","cat");
            boolean returned = myNotes.deleteNote(id);
            logger("TEST 6", returned);

            Log.d("TEST 7", "Trying to delete nonexistent note...");
            returned = myNotes.deleteNote(58);
            logger("TEST 7", returned);

            Log.d("TEST 8", "Trying to delete note with id < 0");
            returned = myNotes.deleteNote(-20);
            logger("TEST 8", returned);


            Log.d("TEST 9", "Trying to update exisiting note...");
            id = myNotes.createNote("Some title", "Some body","cat");
            returned = myNotes.updateNote(id, "New Title", "New body TEST 9","cat");
            logger("TEST 9", returned);

            Log.d("TEST 10", "Trying to update whit null title...");
            returned = myNotes.updateNote(id, null, "New body 2","cat");
            logger("TEST 10", returned);

            Log.d("TEST 11", "Trying to update whit null body...");
            returned = myNotes.updateNote(id, "New Title 11", null,"cat");
            logger("TEST 11", returned);

            Log.d("TEST 12", "Trying to update whit empty body...");
            returned = myNotes.updateNote(id, "New Title 12", "","cat");
            logger("TEST 12", returned);

            Log.d("TEST 13", "Trying to update whit empty title...");
            returned = myNotes.updateNote(id, "", "New body 13","cat");
            logger("TEST 13", returned);
        } catch (Throwable a){
            Log.d("exception thrown", a.getMessage());
        }

    }
    private void start1002Notes(){

            boolean success = true;
            Log.d("TEST 14", "Trying to create 1002 notes...");
            for (int i = 0; i < 1002 && success; i++) {
                long id = myNotes.createNote("Note Test 1002 notes " + i, "body of note","cat");
                success = id > 0;
            }
            logger("TEST 14", success);
    }

    private void addingAfter1000(){
        Log.d("Test15", "After creating 1002 notes we're going to add another one, edit the 75th and the 76th note");
        try {
            Log.d("Test 15", "creating note");
            long id = myNotes.createNote("Test 15", "creatin new note","cat");
            if (id > 0) {
                Log.d("Test 15", "creted");
                try {
                    Log.d("Test 15", "Editing note with rowid: 75");
                    boolean success = myNotes.updateNote(75, "New title for note 75", "the body","cat");
                    if (success) {
                        Log.d("Test 15", "first edit completed");
                        try {
                            Log.d("Test 15", "Editing note with rowid: 75");
                            success = myNotes.updateNote(76, "New title for note 76", "the body","cat");
                            Log.d("Test 15", "second update succes:" + success);
                        }catch (Throwable t2){
                            Log.d("Test 15"," second update throw exception" + t2.getMessage());
                        }
                    }
                }catch (Throwable t1){
                    Log.d("Test 15"," first update throw exception"+ t1.getMessage());
                }
            }
        }catch(Throwable t){
            Log.d("Test 15", " create throw exception"+ t.getMessage());
        }
    }

    private void  overflow(){
        Log.d("TEST 16", "Trying to overflow database...");
        String body = "Body";
        String title = "Test 16";
        boolean works =true;
        List <Long> idList = new ArrayList<Long>();
        while (works){
            try {
                long id = myNotes.createNote(title, body,"cat");
                if (id < 1){
                    Log.d("Test 16", "overflow with body whose length is: " + body.length());
                    works =false;
                }
                idList.add(id);
                body = body + "BBBBB";
            }
            catch(Throwable t){
                Log.d("Test 16", t.getMessage());
            }
        }
        deleteOverflow(idList);
//        myNotes.deleteAllNotes();
//        Log.d("Test 15", "Deleting all create notes");
//        myNotes.createNote("New note after test 15", "body");
    }
    private void deleteOverflow(List<Long> myList){
        boolean can=true;
        for (long temp : myList) {
            myNotes.deleteNote(temp);
        }
        Log.d("Test 16", "done deleting all notes created by the test");
    }
}
