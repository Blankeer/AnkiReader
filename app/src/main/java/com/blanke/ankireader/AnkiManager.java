package com.blanke.ankireader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.bean.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by on 2016/10/14.
 */

public class AnkiManager {
    public static final String ANKI_HOME_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separatorChar + "AnkiDroid" + File.separatorChar;
    public static final String ANKI_DB_PATH = ANKI_HOME_PATH + "collection.anki2";
    public static final String ANKI_MEDIA_PATH = ANKI_HOME_PATH + "collection.media" + File.separatorChar;
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_COL = "col";

    public static SQLiteDatabase openAnkiDb() {
        return SQLiteDatabase.openOrCreateDatabase(ANKI_DB_PATH, null);
    }

    public static List<Note> getAllNotes() {
        return getNotesByDeck(null);
    }

    public static List<File> getMediaFile(List<Note> notes) {
        List<File> re = new ArrayList<>();
        for (Note n : notes) {
            re.add(getMediaFile(n));
        }
        return re;
    }

    public static File getMediaFile(Note note) {
        String path = ANKI_MEDIA_PATH + note.getMediaPath();
        File file = new File(path);
        note.setMedia(file);
        return file;
    }

    public static List<String> getMediaPath(List<Note> notes) {
        List<String> re = new ArrayList<>();
        for (Note n : notes) {
            re.add(ANKI_MEDIA_PATH + n.getMediaPath());
        }
        return re;
    }

    /**
     * 得到所有的卡牌信息
     *
     * @return
     */
    public static List<Deck> getAllDecks() {
        List<Deck> decks = new ArrayList<>();
        SQLiteDatabase db = openAnkiDb();
        String sql = "select * from " + TABLE_COL;
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            String decksJson = c.getString(c.getColumnIndex("decks"));
            try {
                JSONObject jsonObject = new JSONObject(decksJson);
                Iterator<String> iter = jsonObject.keys();
                while (iter.hasNext()) {
                    String idStr = iter.next();
                    JSONObject tempJson = jsonObject.getJSONObject(idStr);
                    String name = tempJson.getString("name");
                    long id = Long.parseLong(idStr);
                    Deck d = new Deck(id, name);
                    decks.add(d);
//                    Logger.d(d.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        c.close();
        db.close();
        return decks;
    }

    public static List<Note> getNotesByDeck(Deck deck) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = openAnkiDb();
        if (db == null) {
            return null;
        }
        String sql = "select * from " + TABLE_NOTES;
        if (deck != null) {
            sql = "select n.id,n.tags, flds, sfld" +
                    " from notes as n, cards as c where c.nid=n.id  and c.did=" + deck.getId()
                    + " order by n.id desc";
        }
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            Note note = new Note();
            note.setId(c.getLong(c.getColumnIndex("id")));
            note.setTags(c.getString(c.getColumnIndex("tags")));
            String flds = c.getString(c.getColumnIndex("flds"));
            String sfld = c.getString(c.getColumnIndex("sfld"));
            String soundStart = "[sound:";
            String soundend = ".mp3]";
            int souStart = sfld.indexOf(soundStart);
            int souEnd = sfld.lastIndexOf(soundend);
            if (souEnd < 0 || souStart < 0 || souStart >= souEnd) {
                continue;
            }
            String mediaPath = sfld.substring(souStart + soundStart.length()
                    , souEnd + soundend.length() - 1).trim();
            note.setMediaPath(mediaPath);
            flds = flds.substring(sfld.length(), flds.length());
            sfld = sfld.replaceAll("\\[sound:.*\\.mp3\\]", "");
            flds = flds.replaceAll("\\[sound:.*\\.mp3\\]", "");
//            flds=flds.replaceAll("<br.*?>","\n");//把<br>变成回车
//            flds=flds.replaceAll("<.*?>","");//去掉html标签
            //update 不需要去掉html标签，textview支持显示html，效果更好
            note.setFront(sfld.trim());
            note.setBack(flds.trim());
            notes.add(note);
//            Logger.d(note);
        }
        c.close();
        db.close();
        return notes;
    }
}
