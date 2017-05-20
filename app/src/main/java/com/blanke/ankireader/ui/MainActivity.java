package com.blanke.ankireader.ui;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.play.PlayConfig;
import com.blanke.ankireader.play.PlayerService;
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
    private SwitchCompat switchShownotification;
    private SwitchCompat switchplay;
    private AppCompatEditText editTcpIp;
    private LinearLayout mLLLoopDesc;
    private SwitchCompat mScLoopDesc;
    private PlayConfig mPlayConfig;
    private LinearLayout layoutFloatWrap;
    private AppCompatSpinner spinnerFloatStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        XSocketLog.debug(true);
        editLoopcount = (EditText) findViewById(R.id.edit_loopcount);
        editSleep = (EditText) findViewById(R.id.edit_sleep);
        spinnerPlaydeck = (AppCompatSpinner) findViewById(R.id.spinner_playdeck);
        spinnerPlaymode = (AppCompatSpinner) findViewById(R.id.spinner_playmode);
        switchShowfloatview = (SwitchCompat) findViewById(R.id.switch_showfloatview);
        switchShownotification = (SwitchCompat) findViewById(R.id.switch_shownotification);
        switchplay = (SwitchCompat) findViewById(R.id.switch_play);

        editTcpIp = (AppCompatEditText) findViewById(R.id.edit_tcp_ip);
        buStart = (AppCompatButton) findViewById(R.id.bu_start);
        buStop = (AppCompatButton) findViewById(R.id.bu_stop);
        mLLLoopDesc = (LinearLayout) findViewById(R.id.ll_loop_desc);
        mScLoopDesc = (SwitchCompat) findViewById(R.id.switch_loop_desc);
        layoutFloatWrap = (LinearLayout) findViewById(R.id.view_floatstyle_wrap);
        spinnerFloatStyle = (AppCompatSpinner) findViewById(R.id.spinner_floatview_style);

        buStart.setOnClickListener(this);
        buStop.setOnClickListener(this);
        spinnerDeckAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        spinnerPlaydeck.setAdapter(spinnerDeckAdapter);
        spinnerPlaymode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1
                , new String[]{"循环播放", "随机播放"}));
        spinnerPlaymode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {//循环
                    mLLLoopDesc.setVisibility(View.VISIBLE);
                } else {
                    mLLLoopDesc.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchShowfloatview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutFloatWrap.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        spinnerFloatStyle.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1
                , new String[]{"传统样式", "发射弹幕"}));

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
        loadData();
//        AnkiManager.getNotesByDeckId(decks.get(2));
    }

    private void startPlayService(PlayConfig pc) {
        final Intent intent2 = new Intent(this, PlayerService.class);
        intent2.putExtra("config", pc);
        startService(intent2);
    }

    private void loadData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mPlayConfig = new PlayConfig().load(sp);
        editLoopcount.setText(mPlayConfig.playCount + "");
        editSleep.setText(mPlayConfig.playSleepTime + "");
        int modePosition = 0;
        if (mPlayConfig.playMode == PlayConfig.PlayMode.RANDOM) {
            modePosition = 1;
        }
        spinnerPlaymode.setSelection(modePosition);
        mScLoopDesc.setChecked(mPlayConfig.isLoopDesc);
        if (mPlayConfig.deckId != -1) {
            for (int i = 0; i < decks.size(); i++) {
                if (mPlayConfig.deckId == decks.get(i).getId()) {
                    spinnerPlaydeck.setSelection(i + 1);
                    break;
                }
            }
        }
        switchShowfloatview.setChecked(mPlayConfig.isShowFloatView);
        if (mPlayConfig.tcpPort > 0) {
            editTcpIp.setText(mPlayConfig.tcpIp + ":" + mPlayConfig.tcpPort);
        }
        if (mPlayConfig.floatStyleDanmu) {
            spinnerFloatStyle.setSelection(1);
        }
        switchShownotification.setChecked(mPlayConfig.isShowNotification);
        switchplay.setChecked(mPlayConfig.isPlay);

    }

    private PlayConfig saveData() {
        String loopCountStr = editLoopcount.getText().toString().trim();
        String sleepStr = editSleep.getText().toString().trim();
        int loopCount = 1;
        int sleep = 700;
        try {
            loopCount = Integer.parseInt(loopCountStr);
            sleep = Integer.parseInt(sleepStr);
        } catch (NumberFormatException e) {
            //ignore
        }
        boolean isShowFloat = switchShowfloatview.isChecked();
        mPlayConfig.playCount = loopCount;
        mPlayConfig.playSleepTime = sleep;
        int deckPosition = spinnerPlaydeck.getSelectedItemPosition();
        if (deckPosition == 0) {
            mPlayConfig.deckId = -1;
        } else {
            mPlayConfig.deckId = decks.get(deckPosition - 1).getId();
//            Logger.d("deckId=" + mPlayConfig.deckId);
        }
        mPlayConfig.playMode = PlayConfig.PlayMode.LOOP;
        if (spinnerPlaymode.getSelectedItemPosition() == 1) {
            mPlayConfig.playMode = PlayConfig.PlayMode.RANDOM;
        }
        mPlayConfig.isShowFloatView = isShowFloat;
        mPlayConfig.isLoopDesc = mScLoopDesc.isChecked();
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

        mPlayConfig.floatStyleDanmu = spinnerFloatStyle.getSelectedItemPosition() == 1;
        mPlayConfig.floatStyleCommon = spinnerFloatStyle.getSelectedItemPosition() == 0;
        mPlayConfig.isShowNotification = switchShownotification.isChecked();
        mPlayConfig.isPlay = switchplay.isChecked();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mPlayConfig.save(sp);
//        Logger.d("saveConfig=" + mPlayConfig);
        return mPlayConfig;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bu_start) {
            PlayConfig pc = saveData();
            startPlayService(pc);
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
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
