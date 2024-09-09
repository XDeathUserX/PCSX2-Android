package kr.co.iefriends.pcsx2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    public final ActivityResultLauncher<Intent> startLocalFilePlay = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>());

    /*private String m_szGamefile = "";

    private HIDDeviceManager mHIDDeviceManager;
    private Thread mEmulationThread = null;

    private boolean isThread() {
        if (mEmulationThread != null) {
            Thread.State _thread_state = mEmulationThread.getState();
            return _thread_state == Thread.State.BLOCKED
                    || _thread_state == Thread.State.RUNNABLE
                    || _thread_state == Thread.State.TIMED_WAITING
                    || _thread_state == Thread.State.WAITING;
        }
        return false;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default resources
        copyAssetAll(getApplicationContext(), "bios");
        copyAssetAll(getApplicationContext(), "resources");

        NativeApp.initializeOnce(getApplicationContext());

        FloatingButtonTouch();

        //setSurfaceView(new SDLSurface(this));
    }

    // Buttons
    private void FloatingButtonTouch() {
        // Play Button
        FloatingActionButton btn_play = findViewById(R.id.btn_game_play);
        if(btn_play != null) {
            btn_play.setOnClickListener(v -> {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.addFlags(1);
                intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", false);
                intent.setType("*/*");
                startLocalFilePlay.launch(intent);
            });
        }

    @Override
    public void onConfigurationChanged(@NonNull Configuration p_newConfig) {
        super.onConfigurationChanged(p_newConfig);
    }

    /*@Override
    protected void onPause() {
        NativeApp.pause();
        super.onPause();
        ////
        if (mHIDDeviceManager != null) {
            mHIDDeviceManager.setFrozen(true);
        }
    }

    @Override
    protected void onResume() {
        NativeApp.resume();
        super.onResume();
        ////
        if (mHIDDeviceManager != null) {
            mHIDDeviceManager.setFrozen(false);
        }
    }

    @Override
    protected void onDestroy() {
        NativeApp.shutdown();
        super.onDestroy();
        ////
        if (mHIDDeviceManager != null) {
            HIDDeviceManager.release(mHIDDeviceManager);
            mHIDDeviceManager = null;
        }
        ////
        if (mEmulationThread != null) {
            try {
                mEmulationThread.join();
                mEmulationThread = null;
            }
            catch (InterruptedException ignored) {}
        }

        int appPid = android.os.Process.myPid();
        android.os.Process.killProcess(appPid);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void Initialize() {
        NativeApp.initializeOnce(getApplicationContext());

        // Set up JNI
        SDLControllerManager.nativeSetupJNI();

        // Initialize state
        SDLControllerManager.initialize();

        mHIDDeviceManager = HIDDeviceManager.acquire(this);
    }

    private void setSurfaceView(Object p_value) {
        FrameLayout fl_board = findViewById(R.id.fl_board);
        if(fl_board != null) {
            if(fl_board.getChildCount() > 0) {
                fl_board.removeAllViews();
            }
            ////
            if(p_value instanceof SDLSurface) {
                fl_board.addView((SDLSurface)p_value);
            }
        }
    }

    public void startEmuThread() {
        if(!isThread()) {
            mEmulationThread = new Thread(() -> NativeApp.runVMThread(m_szGamefile));
            mEmulationThread.start();
        }
    }

    private void restartEmuThread() {
        NativeApp.shutdown();
        if (mEmulationThread != null) {
            try {
                mEmulationThread.join();
                mEmulationThread = null;
            }
            catch (InterruptedException ignored) {}
        }
        ////
        startEmuThread();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (SDLControllerManager.isDeviceSDLJoystick(event.getDeviceId())) {
            SDLControllerManager.handleJoystickMotionEvent(event);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int p_keyCode, KeyEvent p_event) {
        if ((p_event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (p_event.getRepeatCount() == 0) {
                SDLControllerManager.onNativePadDown(p_event.getDeviceId(), p_keyCode);
                return true;
            }
        }
        else {
            if (p_keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
        }
        return super.onKeyDown(p_keyCode, p_event);
    }

    @Override
    public boolean onKeyUp(int p_keyCode, KeyEvent p_event) {
        if ((p_event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (p_event.getRepeatCount() == 0) {
                SDLControllerManager.onNativePadUp(p_event.getDeviceId(), p_keyCode);
                return true;
            }
        }
        return super.onKeyUp(p_keyCode, p_event);
    }

    public static void sendKeyAction(View p_view, int p_action, int p_keycode) {
        if(p_action == MotionEvent.ACTION_DOWN) {
            p_view.setPressed(true);
            int pad_force = 0;
            if(p_keycode >= 110) {
                float _abs = 90; // Joystic test value
                _abs = Math.min(_abs, 100);
                pad_force = (int) (_abs * 32766.0f / 100);
            }
            NativeApp.setPadButton(p_keycode, pad_force, true);
        } else if(p_action == MotionEvent.ACTION_UP || p_action == MotionEvent.ACTION_CANCEL) {
            p_view.setPressed(false);
            NativeApp.setPadButton(p_keycode, 0, false);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public static void copyAssetAll(Context p_context, String srcPath) {
        AssetManager assetMgr = p_context.getAssets();
        String[] assets = null;
        try {
            String destPath = p_context.getExternalFilesDir(null) + File.separator + srcPath;
            assets = assetMgr.list(srcPath);
            if(assets != null) {
                if (assets.length == 0) {
                    copyFile(p_context, srcPath, destPath);
                } else {
                    File dir = new File(destPath);
                    if (!dir.exists())
                        dir.mkdir();
                    for (String element : assets) {
                        copyAssetAll(p_context, srcPath + File.separator + element);
                    }
                }
            }
        }
        catch (IOException ignored) {}
    }

    public static void copyFile(Context p_context, String srcFile, String destFile) {
        AssetManager assetMgr = p_context.getAssets();

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = assetMgr.open(srcFile);
            boolean _exists = new File(destFile).exists();
            if(srcFile.contains("shaders")) {
                _exists = false;
            }
            if(!_exists)
            {
                os = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                is.close();
                os.flush();
                os.close();
            }
        }
        catch (IOException ignored) {}
    }
    @Override
    public final void onActivityResult(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            try {
                Intent data = activityResult.getData();
                if (result != null) {
                    String dataString = data.getDataString();
                    if (TextUtils.isEmpty(dataString)) {
                        return;
                    }
                    runGame(dataString);
                }
            } catch (Exception e) {
            }
        }

    public synchronized void runGame(String fileName) {
        try {
            Context applicationContext = getApplicationContext();
            Intent intent = new Intent(applicationContext, NativeActivity.class);
            intent.putExtra("GamePath", fileName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Call jni
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public int openContentUri(String uriString, String mode) {
        Context _context = getApplicationContext();
        if(_context == null) return -1;

        try {
            Uri uri = Uri.parse(uriString);
            ParcelFileDescriptor filePfd = _context.getContentResolver().openFileDescriptor(uri, mode);
            if (filePfd == null) {
                return -1;
            }
            return filePfd.detachFd();  // Take ownership of the fd.
        } catch (Exception e) {
            return -1;
        }
    }

    public final ActivityResultLauncher<Intent> startActivityResultLocalFileUpload = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent _intent = result.getData();
                        if(_intent != null) {
                            m_szGamefile = _intent.getDataString();
                            if(!TextUtils.isEmpty(m_szGamefile)) {
                                restartEmuThread();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
}
