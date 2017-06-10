package com.blanke.ankireader.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.config.PlayConfig;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.play.PlayerService;
import com.blanke.ankireader.ui.settings.SettingsActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AppCompatSpinner spinnerPlaydeck;
    private ArrayAdapter<String> spinnerDeckAdapter;
    private List<Deck> decks;
    private AppCompatButton buStart;
    private AppCompatButton buStop;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        XSocketLog.debug(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        spinnerPlaydeck = (AppCompatSpinner) findViewById(R.id.spinner_playdeck);

        buStart = (AppCompatButton) findViewById(R.id.bu_start);
        buStop = (AppCompatButton) findViewById(R.id.bu_stop);

        buStart.setOnClickListener(this);
        buStop.setOnClickListener(this);
        spinnerDeckAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        spinnerPlaydeck.setAdapter(spinnerDeckAdapter);

        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        prepareLoadData();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(MainActivity.this, "权限申请失败，程序无法运行",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void prepareLoadData() {
        try {
            decks = AnkiManager.getAllDecks();
        } catch (Exception e) {
            Toast.makeText(this, "没有识别到 anki 数据库 = =", Toast.LENGTH_LONG).show();
            return;
        }
        if (decks == null || decks.size() == 0) {
            Toast.makeText(this, "牌组为空...", Toast.LENGTH_LONG).show();
        }
        List<String> deckLists = new ArrayList<>();
        deckLists.add("全部卡牌");
        for (Deck d : decks) {
            deckLists.add(d.getName());
        }
        spinnerDeckAdapter.addAll(deckLists);
//        AnkiManager.getNotesByDeckId(decks.get(2));
    }

    private void startPlayService() {
        Intent intent2 = new Intent(this, PlayerService.class);
        startService(intent2);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bu_start) {
            int position = spinnerPlaydeck.getSelectedItemPosition();
            if (position == 0) {
                PlayConfig.saveDeckIds(this, null);
            } else {
                PlayConfig.saveDeckIds(this, decks.get(position - 1).getId());
            }
            decks.get(position);

            startPlayService();
//            finish();
        } else if (v.getId() == R.id.bu_stop) {
            Intent intent2 = new Intent(this, PlayerService.class);
            stopService(intent2);
        }
    }

    private void getFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar
                + "AnkiDroid" + File.separatorChar + "collection.media" + File.separatorChar;
        Logger.d("path=" + path);
        File file = new File(path);
        Logger.d("exists=" + file.exists());
        Logger.d("canread=" + file.canRead());
        File[] files = file.listFiles();
        for (File f : files) {
            Logger.d(f.getName());
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_about:
                start(AboutActivity.class);
                break;
            case R.id.menu_main_settings:
                start(SettingsActivity.class);
                break;
        }
        return true;
    }

    private void start(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
