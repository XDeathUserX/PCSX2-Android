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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private String m_szGamefile = "";

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default resources
        Helpers.copyAssetAll(getApplicationContext(), "bios");
        Helpers.copyAssetAll(getApplicationContext(), "resources");

        Initialize();
        NativeApp.renderGpu(14); //set default to Vk

        makeButtonTouch();

        setSurfaceView(new SDLSurface(this));
    }

    // Buttons
    private void makeButtonTouch() {
        // Game file
        MaterialButton btn_file = findViewById(R.id.btn_file);
        if(btn_file != null) {
            btn_file.setOnClickListener(v -> {
                // Test game file
                File externalFilesDir = getExternalFilesDir(null);
                if(externalFilesDir != null) {
                    m_szGamefile = String.format("%s/GradiusV.iso", externalFilesDir.getAbsolutePath());
                    File _file = new File(m_szGamefile);
                    if(_file.exists()) {
                        // File => /storage/emulated/0/Android/data/kr.co.iefriends.pcsx2/files/GradiusV.iso
                        restartEmuThread();
                    }
                }

                /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("*");
                startActivityResultLocalFileUpload.launch(intent);*/
            });
        }

        // Game save
        /*MaterialButton btn_save = findViewById(R.id.btn_save);
        if(btn_save != null) {
            btn_save.setOnClickListener(v -> {
                if(NativeApp.saveStateToSlot(1)) {
                    // Success
                } else {
                    // Failed
                }
                NativeApp.resume();
            });
        }*/

        // Game load
       /*MaterialButton btn_load = findViewById(R.id.btn_load);
        if(btn_load != null) {
            btn_load.setOnClickListener(v -> {
                if(NativeApp.loadStateFromSlot(1)) {
                    // Success
                } else {
                    // Failed
                }
                NativeApp.resume();
            });
        }*/

        //////
        // RENDERER

        MaterialButton btn_ogl = findViewById(R.id.btn_ogl);
        if(btn_ogl != null) {
            btn_ogl.setOnClickListener(v -> {
                NativeApp.renderGpu(12);
            });
        }
        MaterialButton btn_vulkan = findViewById(R.id.btn_vulkan);
        if(btn_vulkan != null) {
            btn_vulkan.setOnClickListener(v -> {
                NativeApp.renderGpu(14);
            });
        }
        MaterialButton btn_sw = findViewById(R.id.btn_sw);
        if(btn_sw != null) {
            btn_sw.setOnClickListener(v -> {
                NativeApp.renderGpu(13);
            });
        }

        //////
        // PAD

        MaterialButton btn_pad_select = findViewById(R.id.btn_pad_select);
        if(btn_pad_select != null) {
            btn_pad_select.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_SELECT);
                return true;
            });
        }
        MaterialButton btn_pad_start = findViewById(R.id.btn_pad_start);
        if(btn_pad_start != null) {
            btn_pad_start.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_START);
                return true;
            });
        }

         MaterialButton btn_pad_a = findViewById(R.id.btn_pad_a);
        if(btn_pad_a != null) {
            btn_pad_a.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_A);
                return true;
            });
        }
        MaterialButton btn_pad_b = findViewById(R.id.btn_pad_b);
        if(btn_pad_b != null) {
            btn_pad_b.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_B);
                return true;
            });
        }
        MaterialButton btn_pad_x = findViewById(R.id.btn_pad_x);
        if(btn_pad_x != null) {
            btn_pad_x.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_X);
                return true;
            });
        }
        MaterialButton btn_pad_y = findViewById(R.id.btn_pad_y);
        if(btn_pad_y != null) {
            btn_pad_y.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_Y);
                return true;
            });
        }

        ////

        MaterialButton btn_pad_l1 = findViewById(R.id.btn_pad_l1);
        if(btn_pad_l1 != null) {
            btn_pad_l1.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_L1);
                return true;
            });
        }
        MaterialButton btn_pad_r1 = findViewById(R.id.btn_pad_r1);
        if(btn_pad_r1 != null) {
            btn_pad_r1.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_R1);
                return true;
            });
        }

        MaterialButton btn_pad_l2 = findViewById(R.id.btn_pad_l2);
        if(btn_pad_l2 != null) {
            btn_pad_l2.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_L2);
                return true;
            });
        }
        MaterialButton btn_pad_r2 = findViewById(R.id.btn_pad_r2);
        if(btn_pad_r2 != null) {
            btn_pad_r2.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_R2);
                return true;
            });
        }

        MaterialButton btn_pad_l3 = findViewById(R.id.btn_pad_l3);
        if(btn_pad_l3 != null) {
            btn_pad_l3.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_THUMBL);
                return true;
            });
        }
        MaterialButton btn_pad_r3 = findViewById(R.id.btn_pad_r3);
        if(btn_pad_r3 != null) {
            btn_pad_r3.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_BUTTON_THUMBR);
                return true;
            });
        }

        ////

        final int PAD_L_UP = 110;
        final int PAD_L_RIGHT = 111;
        final int PAD_L_DOWN = 112;
        final int PAD_L_LEFT = 113;

        final int PAD_R_UP = 120;
        final int PAD_R_RIGHT = 121;
        final int PAD_R_DOWN = 122;
        final int PAD_R_LEFT = 123;

        MaterialButton btn_pad_joy_lt = findViewById(R.id.btn_pad_joy_lt);
        if(btn_pad_joy_lt != null) {
            btn_pad_joy_lt.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_UP);
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_LEFT);
                return true;
            });
        }
        MaterialButton btn_pad_joy_t = findViewById(R.id.btn_pad_joy_t);
        if(btn_pad_joy_t != null) {
            btn_pad_joy_t.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_UP);
                return true;
            });
        }
        MaterialButton btn_pad_joy_rt = findViewById(R.id.btn_pad_joy_rt);
        if(btn_pad_joy_rt != null) {
            btn_pad_joy_rt.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_UP);
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_RIGHT);
                return true;
            });
        }
        MaterialButton btn_pad_joy_l = findViewById(R.id.btn_pad_joy_l);
        if(btn_pad_joy_l != null) {
            btn_pad_joy_l.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_LEFT);
                return true;
            });
        }
        MaterialButton btn_pad_joy_r = findViewById(R.id.btn_pad_joy_r);
        if(btn_pad_joy_r != null) {
            btn_pad_joy_r.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_RIGHT);
                return true;
            });
        }
        MaterialButton btn_pad_joy_lb = findViewById(R.id.btn_pad_joy_lb);
        if(btn_pad_joy_lb != null) {
            btn_pad_joy_lb.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_LEFT);
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_DOWN);
                return true;
            });
        }
        MaterialButton btn_pad_joy_b = findViewById(R.id.btn_pad_joy_b);
        if(btn_pad_joy_b != null) {
            btn_pad_joy_b.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_DOWN);
                return true;
            });
        }
        MaterialButton btn_pad_joy_rb = findViewById(R.id.btn_pad_joy_rb);
        if(btn_pad_joy_rb != null) {
            btn_pad_joy_rb.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_RIGHT);
                Helpers.sendKeyAction(v, event.getAction(), PAD_L_DOWN);
                return true;
            });
        }

        ////

        MaterialButton btn_pad_dir_top = findViewById(R.id.btn_pad_dir_top);
        if(btn_pad_dir_top != null) {
            btn_pad_dir_top.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_DPAD_UP);
                return true;
            });
        }
        MaterialButton btn_pad_dir_bottom = findViewById(R.id.btn_pad_dir_bottom);
        if(btn_pad_dir_bottom != null) {
            btn_pad_dir_bottom.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_DPAD_DOWN);
                return true;
            });
        }
        MaterialButton btn_pad_dir_left = findViewById(R.id.btn_pad_dir_left);
        if(btn_pad_dir_left != null) {
            btn_pad_dir_left.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_DPAD_LEFT);
                return true;
            });
        }
        MaterialButton btn_pad_dir_right = findViewById(R.id.btn_pad_dir_right);
        if(btn_pad_dir_right != null) {
            btn_pad_dir_right.setOnTouchListener((v, event) -> {
                Helpers.sendKeyAction(v, event.getAction(), KeyEvent.KEYCODE_DPAD_RIGHT);
                return true;
            });
        }
    }

    @Override
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration p_newConfig) {
        super.onConfigurationChanged(p_newConfig);
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
