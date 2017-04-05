package com.blanke.ankireader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.utils.ObjectUtils;
import com.blanke.xsocket.utils.XSocketLog;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editLoopcount;
    private EditText editSleep;
    private AppCompatSpinner spinnerPlaydeck;
    private AppCompatSpinner spinnerPlaymode;
    private ArrayAdapter<String> spinnerDeckAdapter;
    private List<Deck> decks;
    private AppCompatButton buStart;
    private AppCompatButton buStop;
    private SwitchCompat switchShowfloatview;
    private AppCompatEditText editTcpIp;
    private PlayConfig mPlayConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XSocketLog.debug(true);
        editLoopcount = (EditText) findViewById(R.id.edit_loopcount);
        editSleep = (EditText) findViewById(R.id.edit_sleep);
        spinnerPlaydeck = (AppCompatSpinner) findViewById(R.id.spinner_playdeck);
        spinnerPlaymode = (AppCompatSpinner) findViewById(R.id.spinner_playmode);
        switchShowfloatview = (SwitchCompat) findViewById(R.id.switch_showfloatview);
        editTcpIp = (AppCompatEditText) findViewById(R.id.edit_tcp_ip);
        buStart = (AppCompatButton) findViewById(R.id.bu_start);
        buStop = (AppCompatButton) findViewById(R.id.bu_stop);

        buStart.setOnClickListener(this);
        buStop.setOnClickListener(this);
        spinnerDeckAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        spinnerPlaydeck.setAdapter(spinnerDeckAdapter);
        spinnerPlaymode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1
                , new String[]{"循环播放", "随机播放"}));


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
        }catch (Exception e){
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
        loadData();
//        AnkiManager.getNotesByDeck(decks.get(2));
    }

    private void startPlayService(PlayConfig pc) {
        final Intent intent2 = new Intent(this, PlayerService.class);
        intent2.putExtra("config", pc);
        startService(intent2);
    }

    private void loadData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String str = sp.getString(Config.KEY_PLAY_CONFIG, "");
        if (!TextUtils.isEmpty(str)) {
            mPlayConfig = new ObjectUtils<PlayConfig>().getSerialData(str);
        } else {
            mPlayConfig = new PlayConfig();
        }
        editLoopcount.setText(mPlayConfig.playCount + "");
        editSleep.setText(mPlayConfig.playSleepTime + "");
        spinnerPlaymode.setSelection(mPlayConfig.playMode);
        if (mPlayConfig.playDeck != null) {
            for (int i = 0; i < decks.size(); i++) {
                if (mPlayConfig.playDeck.equals(decks.get(i))) {
                    spinnerPlaydeck.setSelection(i + 1);
                    break;
                }
            }
        }
        switchShowfloatview.setChecked(mPlayConfig.isShowFloatView);
        if (mPlayConfig.tcpPort > 0) {
            editTcpIp.setText(mPlayConfig.tcpIp + ":" + mPlayConfig.tcpPort);
        }
    }

    private PlayConfig saveData() {
        String loopCountStr = editLoopcount.getText().toString().trim();
        int loopCount = Integer.parseInt(loopCountStr);
        String sleepStr = editSleep.getText().toString().trim();
        int sleep = Integer.parseInt(sleepStr);
        boolean isShowFloat = switchShowfloatview.isChecked();
        mPlayConfig.playCount = loopCount;
        mPlayConfig.playSleepTime = sleep;
        int deckPosition = spinnerPlaydeck.getSelectedItemPosition();
        if (deckPosition == 0) {
            mPlayConfig.playDeck = null;
        } else {
            mPlayConfig.playDeck = decks.get(deckPosition - 1);
        }
        mPlayConfig.playMode = spinnerPlaymode.getSelectedItemPosition();
        mPlayConfig.isShowFloatView = isShowFloat;
        String ipPort = editTcpIp.getText().toString().trim();
        if (!TextUtils.isEmpty(ipPort)) {
            String temp[] = ipPort.split(":");
            if (temp.length == 2) {
                if (temp[0].matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}$")) {//ip
                    if (temp[1].matches(
                            "^([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$")) {//port
                        mPlayConfig.tcpIp = temp[0];
                        mPlayConfig.tcpPort = Integer.parseInt(temp[1]);
                    }
                }
            }
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.KEY_PLAY_CONFIG,
                new ObjectUtils<PlayConfig>().getSerialString(mPlayConfig));
        editor.commit();
        Logger.d("saveConfig=" + mPlayConfig);
        return mPlayConfig;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bu_start) {
            PlayConfig pc = saveData();
            startPlayService(pc);
            finish();
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
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
