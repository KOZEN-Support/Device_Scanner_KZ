package com.xcheng.demo.scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tools.XCImage;
import com.xcheng.scanner.AimerMode;
import com.xcheng.scanner.BarcodeType;
import com.xcheng.scanner.FlashMode;
import com.xcheng.scanner.LicenseState;
import com.xcheng.scanner.OutputMethod;
import com.xcheng.scanner.RegionSizeType;
import com.xcheng.scanner.ScannerResult;
import com.xcheng.scanner.TextCaseType;
import com.xcheng.scanner.XcBarcodeScanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = "ScanDemo";
    private Button mButtonScan;

    private Button mButtonSuspend;
    private Button mButtonResume;

    private PictureDialog myPictureDialog;

    private Button mButtonActicveLicense;
    private TextView mTextResult;
    private ToggleButton mToggleIndicator;

    private ToggleButton mToggleLoopScan;

    private ToggleButton mToggleQrCode;

    private ToggleButton mToggleMultiBarcodes;

    private ToggleButton mTogglePrecisionScan;

    private ToggleButton mToggleAimerMode;
    private ToggleButton mToggleFlashMode;
    private Spinner mSpinTimeout;
    private Spinner mSpinTextCase;
    private Spinner mSpinOutputMethod;
    private ToggleButton mTogglePrefix;
    private ToggleButton mToggleSuffix;

    private String allResult = "";

    private boolean loopScanEnabled;

    private void showAlertDialog(String title,
                                 String msg,
                                 boolean cancelAble,
                                 String positiveButton,
                                 android.content.DialogInterface.OnClickListener positiveButtonCb) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(cancelAble);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton(positiveButton, positiveButtonCb);
        Dialog dialog = alertDialogBuilder.create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextResult = (TextView)findViewById(R.id.textview_result);
        mTextResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        mButtonScan = (Button)findViewById(R.id.button_scan);

        mButtonSuspend = (Button)findViewById(R.id.button_suspend);
        mButtonResume = (Button)findViewById(R.id.button_resume);
        mButtonActicveLicense = (Button)findViewById(R.id.button_active_license);

        mToggleQrCode = (ToggleButton)findViewById(R.id.toggle_enable_qr);
        mToggleQrCode.setOnCheckedChangeListener((CompoundButton buttonView,
                                                     boolean isChecked)->{
            if (isChecked) {
                Log.d(TAG, "Enable QR");
                XcBarcodeScanner.enableBarcodeType(BarcodeType.QRCODE, true);
            } else {
                Log.d(TAG, "Disable QR");
                XcBarcodeScanner.enableBarcodeType(BarcodeType.QRCODE, false);
            }
        });
        mToggleQrCode.setChecked(false);

        // Toggle indicator
        mToggleIndicator = (ToggleButton)findViewById(R.id.toggle_indicator);
        mToggleIndicator.setOnCheckedChangeListener((CompoundButton buttonView,
                                                     boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.enableSuccessIndicator(true);
            } else {
                XcBarcodeScanner.enableSuccessIndicator(false);
            }
        });
        mToggleIndicator.setChecked(false);

        mToggleLoopScan = (ToggleButton)findViewById(R.id.toggle_loopscan);
        mToggleLoopScan.setOnCheckedChangeListener((CompoundButton buttonView,
                                                     boolean isChecked)-> {
            loopScanEnabled = isChecked;

            if (mButtonScan != null) {
                if (loopScanEnabled) {
                    mButtonScan.setText(R.string.scanloop);
                } else {
                    mButtonScan.setText(R.string.scan);
                }
            }
        });
        mToggleLoopScan.setChecked(false);

        // Spinner timeout
        mSpinTimeout = (Spinner)findViewById(R.id.spin_timeout);
        List<String> timeOutList = Arrays.asList("1 Second",
                "2 Seconds",
                "3 Seconds",
                "4 Seconds",
                "5 Seconds",
                "6 Seconds",
                "7 Seconds",
                "8 Seconds",
                "9 Seconds");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeOutList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinTimeout.setAdapter(adapter);

        mSpinTimeout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String timeOutStr = adapter.getItem(position);
                XcBarcodeScanner.setTimeout(Integer.parseInt(timeOutStr.substring(0, 1)));
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinTimeout.setSelection(4); // Default set 5 seconds.


        // Spinner textCase
        mSpinTextCase = (Spinner)findViewById(R.id.spin_text_case);
        List<String> caseList = Arrays.asList(
                TextCaseType.NONE,
                TextCaseType.LOWER,
                TextCaseType.UPPER);
        ArrayAdapter<String> adapterCase=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, caseList);
        adapterCase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinTextCase.setAdapter(adapterCase);

        mSpinTextCase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String caseStr = adapter.getItem(position);
                XcBarcodeScanner.setTextCase(caseStr);
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinTextCase.setSelection(0); // Default set none case.

        // Spinner outputMethod
        mSpinOutputMethod = (Spinner)findViewById(R.id.spin_output_method);
        List<String> outputMethodList = Arrays.asList(
                OutputMethod.NONE,
                OutputMethod.BROADCAST,
                OutputMethod.KEYBOARD,
                OutputMethod.CLIPBOARD,
                OutputMethod.BROADCAST_KEYBOARD,
                OutputMethod.BROADCAST_CLIPBOARD);
        ArrayAdapter<String> adapterOutMethod=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, outputMethodList);
        adapterOutMethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinOutputMethod.setAdapter(adapterOutMethod);

        mSpinOutputMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String method = adapter.getItem(position);
                XcBarcodeScanner.setOutputMethod(method);
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinOutputMethod.setSelection(0); // Default set none case.

        // Toggle prefix
        mTogglePrefix = (ToggleButton)findViewById(R.id.toggle_prefix);
        mTogglePrefix.setOnCheckedChangeListener((CompoundButton buttonView,
                                                     boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.setTextPrefix("<");
            } else {
                XcBarcodeScanner.setTextPrefix("Empty");
            }
        });
        mTogglePrefix.setChecked(false);

        // Toggle suffix
        mToggleSuffix = (ToggleButton)findViewById(R.id.toggle_suffix);
        mToggleSuffix.setOnCheckedChangeListener((CompoundButton buttonView,
                                                  boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.setTextSuffix(">");
            } else {
                XcBarcodeScanner.setTextSuffix("Empty");
            }
        });
        mToggleSuffix.setChecked(false);

        // MultiBarcodes
        mToggleMultiBarcodes = (ToggleButton)findViewById(R.id.toggle_multibarcodes);
        mToggleMultiBarcodes.setOnCheckedChangeListener((CompoundButton buttonView,
                                                  boolean isChecked)->{
            if (isChecked) {
                // Config mutiBarcodes to 3, and fixed.
                XcBarcodeScanner.setMultiBarcodes(3, true);
            } else {
                XcBarcodeScanner.setMultiBarcodes(1, false);
            }
        });
        mToggleMultiBarcodes.setChecked(false);

        // PrecisionScan
        mTogglePrecisionScan = (ToggleButton)findViewById(R.id.toggle_precision_1d_scan);
        mTogglePrecisionScan.setOnCheckedChangeListener((CompoundButton buttonView,
                                                         boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.setScanRegionSize(RegionSizeType.VIEWSIZE_1D);
            } else {
                XcBarcodeScanner.setScanRegionSize(RegionSizeType.VIEWSIZE_100);
            }
        });
        mTogglePrecisionScan.setChecked(false);

        // Aimer mode
        mToggleAimerMode = (ToggleButton)findViewById(R.id.toggle_aim_mode);
        mToggleAimerMode.setOnCheckedChangeListener((CompoundButton buttonView,
                                                         boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.setAimerLightsMode(AimerMode.TRIGGER_ON);
            } else {
                XcBarcodeScanner.setAimerLightsMode(AimerMode.ALWAYS_OFF);
            }
        });
        mToggleAimerMode.setChecked(false);

        // Flash mode
        mToggleFlashMode = (ToggleButton)findViewById(R.id.toggle_flash_mode);
        mToggleFlashMode.setOnCheckedChangeListener((CompoundButton buttonView,
                                                         boolean isChecked)->{
            if (isChecked) {
                XcBarcodeScanner.setFlashLightsMode(FlashMode.ILLUME_STROBE);
            } else {
                XcBarcodeScanner.setFlashLightsMode(FlashMode.ILLUME_ONLY);
            }
        });
        mToggleFlashMode.setChecked(false);

        mButtonScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "startScan");
                    if (loopScanEnabled) {
                        // If loopscan enabled, we use only ACTION_DOWN to control start or stop.
                        if (XcBarcodeScanner.isLoopScanRunning()) {
                            XcBarcodeScanner.stopLoopScan();
                        } else {
                            XcBarcodeScanner.setLoopScanInterval(100);
                            XcBarcodeScanner.startLoopScan();
                        }
                    } else {
                        XcBarcodeScanner.startScan();
                    }
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "stopScan");
                    if (loopScanEnabled) {
                        // If loopscan enabled, not need process ACTION_UP event.
                    } else {
                        XcBarcodeScanner.stopScan();
                    }
                    return true;
                }

                //view.performClick();
                //return mButtonScan.onTouchEvent(motionEvent);
                return false;
            }
        });

    }

    private void connectScanService() {
        XcBarcodeScanner.init(this, new ScannerResult() {
            @Override
            public void onResult(String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allResult =  allResult + "\n" + result;
                        mTextResult.setText(allResult);

                        // Scroll to bottom
                        int offset = mTextResult.getLayout().getLineTop(mTextResult.getLineCount()) + mTextResult.getCompoundPaddingTop() + mTextResult.getCompoundPaddingBottom();
                        if(offset > mTextResult.getHeight()){
                            mTextResult.scrollTo(0,offset - mTextResult.getHeight());
                        }

                    }

                });
            }
        });
    }

    private void onScanServiceStateChanged() {
        boolean isServiceSuspending = XcBarcodeScanner.isScanServiceSuspending();

        if (isServiceSuspending) {
            showAlertDialog("Service state:", "Scan service is suspending, resume it firstly.", false, "OK", null);
            mButtonScan.setEnabled(false);
            mButtonSuspend.setEnabled(false);
            mButtonResume.setEnabled(true);
        } else {
            mButtonScan.setEnabled(true);
            mButtonSuspend.setEnabled(true);
            mButtonResume.setEnabled(false);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Connect with scan service
        Log.d(TAG, "Connect barcode service");

        connectScanService();

        // Get SDK version and ScannerService version.
        // This need connection ready, simply delay 0.5 second after connect.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Get sdk version
                        String sdkVer = XcBarcodeScanner.getSdkVersion(getApplicationContext());

                        // Get service version
                        String serviceVer = XcBarcodeScanner.getServiceVersion();

                        onScanServiceStateChanged();

                        // Get license state
                        int licState = XcBarcodeScanner.getLicenseState();
                        String licMsg = "";
                        switch(licState) {
                            case LicenseState.INACTIVE:
                                licMsg = "Need to active license firstly!";
                                mButtonActicveLicense.setEnabled(true);
                                break;

                            case LicenseState.ACTIVATING:
                                licMsg = "License activating...";
                                mButtonActicveLicense.setEnabled(false);
                                break;

                            case LicenseState.ACTIVED:
                                licMsg = "License actived, be happy!";
                                mButtonActicveLicense.setEnabled(false);
                                break;

                            case LicenseState.INVALID:
                                licMsg = "License invalid, check with vendor please!";
                                mButtonActicveLicense.setEnabled(true);
                                break;

                            case LicenseState.NETWORK_ISSUE:
                                licMsg = "Need network to active license!";
                                mButtonActicveLicense.setEnabled(true);
                                break;

                            case LicenseState.EXPIRED:
                                licMsg = "License expired, check with vendor please!";
                                mButtonActicveLicense.setEnabled(true);
                                break;
                        }


                        allResult =  allResult + "\n" +
                                "SDK ver: " + sdkVer + "\n" +
                                "Service ver: " + serviceVer + "\n" +
                                licMsg;
                        mTextResult.setText(allResult);

                        // Scroll to bottom
                        int offset = mTextResult.getLayout().getLineTop(mTextResult.getLineCount()) + mTextResult.getCompoundPaddingTop() + mTextResult.getCompoundPaddingBottom();
                        if(offset > mTextResult.getHeight()){
                            mTextResult.scrollTo(0,offset - mTextResult.getHeight());
                        }
                    }
                });
            }
        },500);
    }

    @Override
    protected void onPause() {
        super.onPause();

        XcBarcodeScanner.deInit(this);
    }

    private static int cnt = 0;

    /**
     * 8位灰度转Bitmap
     *
     * 图像宽度必须能被4整除
     *
     * @param data
     *            裸数据
     * @param width
     *            图像宽度
     * @param height
     *            图像高度
     * @return
     */
    public Bitmap raw8ToBitmap(byte[] data, int width, int height)
    {
        byte[] Bits = new byte[data.length * 4]; //RGBA 数组

        int i;
        for (i = 0; i < data.length; i++)
        {
            // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
            Bits[i * 4 + 0] = data[i]; // R
            Bits[i * 4 + 1] = data[i]; // G
            Bits[i * 4 + 2] = data[i]; // B
            Bits[i * 4 + 3] = -1;  // 0xFF, A
        }

        // Bitmap.Config.ARGB_8888 表示：图像模式为8位
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return bmp;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_suspend:
                Log.d(TAG, "Suspend");
                XcBarcodeScanner.suspendScanService();
                onScanServiceStateChanged();
                break;

            case R.id.button_resume:
                Log.d(TAG, "Resume");
                XcBarcodeScanner.resumeScanService();
                onScanServiceStateChanged();
                break;

            case R.id.button_active_license:
                Log.d(TAG, "Active license");
                XcBarcodeScanner.activateLicense();
                break;

            case R.id.button_get_lastimage:
                String imageStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "demoImgSave";
                File storageDir = new File(imageStorageDir + File.separator);
                if (!storageDir.exists()) {
                    Log.d(TAG, "Dir not exist, create" + storageDir);
                    storageDir.mkdirs();
                }

                XCImage lastImg = XcBarcodeScanner.getLastDecodeImage();
                if (lastImg != null) {
                    int width = lastImg.getWidth();
                    int height = lastImg.getHeight();
                    int stride = lastImg.getStride();
                    byte[] data = lastImg.getData();
                    Bitmap bmp = raw8ToBitmap(data, width, height);
                    String imgFilePath = imageStorageDir + File.separator + "lastImage_w" + width + "_h"+height + "_s" + stride + "_" + cnt + ".png";
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    byte[] bmpData = bos.toByteArray();

                    try {
                        FileOutputStream imgFo = new FileOutputStream(imgFilePath);
                        //imgFo.write(data); // write raw data to file
                        imgFo.write(bmpData);
                        imgFo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String infoStr = "W=" + lastImg.getWidth() + ", " +
                                     "H=" + lastImg.getHeight() + ", " +
                                     "Size=" + lastImg.getData().length + " Bytes";
                    infoStr += "\n" + "Image path: " + imgFilePath;
                    //myPictureDialog = new PictureDialog(this, "Image Info:", infoStr, imgFilePath);
                    myPictureDialog = new PictureDialog(this, "Image Info:", infoStr, bmp);
                    myPictureDialog.show();

                    cnt++;
                } else {
                    showAlertDialog("", "No image!", false, "OK", null);
                }
                break;
        }
    }
}
