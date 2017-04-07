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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return getNotesByDeck(-1);
    }

    public static List<File> getMediaFile(List<Note> notes) {
        List<File> re = new ArrayList<>();
        for (Note n : notes) {
            re.addAll(getMediaFile(n));
        }
        return re;
    }

    public static List<File> getMediaFile(Note note) {
        List<File> res = new ArrayList<>();
        for (String str : note.getMediaPaths()) {
            String path = ANKI_MEDIA_PATH + str;
            File file = new File(path);
            note.addMedia(file);
        }
        return res;
    }

    public static List<String> getMediaPath(List<Note> notes) {
        List<String> re = new ArrayList<>();
        for (Note n : notes) {
//            for (String path : n.getMediaPaths()) {
//                re.add(ANKI_MEDIA_PATH + path);
//            }
            if (n.getMediaPaths().size() > 0) {//默认添加第一个
                re.add(ANKI_MEDIA_PATH + n.getMediaPaths().get(0));
            }
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

    /**
     * 获得全部含有音频的 note
     *
     * @param deckId
     * @return
     */
    public static List<Note> getAllHasMediaNotesByDeckId(long deckId) {
        List<Note> sources = getNotesByDeck(deckId);
        List<Note> res = new ArrayList<>();
        for (Note source : sources) {
            if (source.getMediaPaths().size() > 0) {
                res.add(source);
            }
        }
        return res;
    }

    public static List<Note> getNotesByDeck(long deckId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = openAnkiDb();
        if (db == null) {
            return null;
        }
        String sql = "select * from " + TABLE_NOTES;
        if (deckId != -1) {
            sql = "select n.id,n.tags, flds, sfld" +
                    " from notes as n, cards as c where c.nid=n.id  and c.did=" + deckId
                    + " order by n.id desc";
        }
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            Note note = new Note();
            note.setId(c.getLong(c.getColumnIndex("id")));
            note.setTags(c.getString(c.getColumnIndex("tags")));
            String flds = c.getString(c.getColumnIndex("flds"));//front
            String sfld = c.getString(c.getColumnIndex("sfld"));//front+back
            Pattern r = Pattern.compile(Config.REG_SOUND);
            Matcher m = r.matcher(flds);
            while (m.find()) {
                String path = m.group(1);
                note.addMediaPath(path);
            }
            String back = flds.substring(sfld.length(), flds.length());
            String front = sfld;
            back = back.replaceAll(Config.REG_SOUND, "");
            front = front.replaceAll(Config.REG_SOUND, "");
//            flds=flds.replaceAll("<br.*?>","\n");//把<br>变成回车
//            flds=flds.replaceAll("<.*?>","");//去掉html标签
            //update 不需要去掉html标签，textview支持显示html，效果更好
            note.setFront(front);
            note.setBack(back);
            notes.add(note);
//            Logger.d(note);
        }
        c.close();
        db.close();
        return notes;
    }
}
