package com.chatsoone.rechat.ui.pattern;

import static com.chatsoone.rechat.ApplicationClass.ACT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.chatsoone.rechat.R;
import com.chatsoone.rechat.ui.main.MainActivity;

import java.util.List;

public class CreatePatternActivity extends AppCompatActivity {
    PatternLockView mPatternLockView;
    int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pattern);

        // 패턴 모드 확인
        // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
        // 1: 메인 화면의 설정창 -> 변경 모드
        // 2: 폴더 화면의 설정창 -> 변경 모드
        // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
        SharedPreferences modeSPF = getSharedPreferences("mode", 0);
        mode = modeSPF.getInt("mode", 0);

        mPatternLockView = (PatternLockView) findViewById(R.id.create_pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.d(ACT, "CREATEPATTERN/Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                Log.d(ACT, "CREATEPATTERN/Pattern progress: " + PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Log.d(ACT, "CREATEPATTERN/Pattern complete: " + PatternLockUtils.patternToString(mPatternLockView, pattern));
                SharedPreferences lockSPF = getSharedPreferences("lock", 0);
                SharedPreferences.Editor editor = lockSPF.edit();
                editor.putString("pattern", PatternLockUtils.patternToString(mPatternLockView, pattern));
                editor.apply();
                Log.d(ACT, "CREATEPATTERN/" + PatternLockUtils.patternToString(mPatternLockView, pattern));

                if (mode == 0) { // 입력 모드로 가면 된다.
                    Intent intent = new Intent(getApplicationContext(), InputPatternActivity.class);
                    startActivity(intent);
                    finish();
                } else if (mode == 1) {    // 메인 화면으로 가면 된다.
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
//                } else if(mode == 2) {    // 폴더 화면으로 가면 된다.
//                    Intent intent = new Intent(getApplicationContext(), MyFolderActivity.class);
//                    startActivity(intent);
//                    finish();
                } else {    //입력 모드로 가면 된다.
                    Intent intent = new Intent(getApplicationContext(), InputPatternActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCleared() {
                Log.d(ACT, "CREATEPATTERN/Pattern has been cleared");
            }
        });
    }
}
