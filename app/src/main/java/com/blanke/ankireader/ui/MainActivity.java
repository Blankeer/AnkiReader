package com.blanke.ankireader.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.play.PlayerService;
import com.blanke.ankireader.ui.settings.SettingsActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AppCompatButton buStart;
    private AppCompatButton buStop;

    private Toolbar toolbar;
    private RecyclerView mainRv;
    private DeckAdapter madaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buStart = (AppCompatButton) findViewById(R.id.bu_start);
        buStop = (AppCompatButton) findViewById(R.id.bu_stop);

        buStart.setOnClickListener(this);
        buStop.setOnClickListener(this);
        initRecyclerView();

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

    private void initRecyclerView() {
        mainRv = (RecyclerView) findViewById(R.id.main_rv);
        mainRv.setLayoutManager(new LinearLayoutManager(this));
        madaAdapter = new DeckAdapter(this);
        mainRv.setAdapter(madaAdapter);
    }

    private void prepareLoadData() {
        List<Deck> decks;
        try {
            decks = AnkiManager.getAllDecks();
        } catch (Exception e) {
            Toast.makeText(this, "没有识别到 anki 数据库 = =", Toast.LENGTH_LONG).show();
            return;
        }
        if (decks == null || decks.size() == 0) {
            Toast.makeText(this, "牌组为空...", Toast.LENGTH_LONG).show();
        }
        madaAdapter.setDatas(decks);
    }

    private void startPlayService() {
        Intent intent2 = new Intent(this, PlayerService.class);
        startService(intent2);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bu_start) {
//            int position = spinnerPlaydeck.getSelectedItemPosition();
//            if (position == 0) {
//                PlayConfig.saveDeckIds(this, null);
//            } else {
//                PlayConfig.saveDeckIds(this, decks.get(position - 1).getId());
//            }
//            decks.get(position);

            startPlayService();
//            finish();
        } else if (v.getId() == R.id.bu_stop) {
            Intent intent2 = new Intent(this, PlayerService.class);
            stopService(intent2);
        }
    }

    //test
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
