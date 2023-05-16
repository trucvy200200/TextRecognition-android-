package com.vuhoangtrucvy20110415.ocr;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.vuhoangtrucvy20110415.ocr.databinding.ActivityMainBinding;

import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_REQUEST_CODE=400;
   // private static final int IMAGE_PICK_GALLERY_CODE=1000;
    ActivityMainBinding binding;
    EditText mResult;
    ImageView mPreview;
    Bitmap bitmap;
    String[] storagePermission;
    String extractedText = "";
    String translatedText = "";
    boolean newText = true;
    Uri image_uri;

    ModelLanguage modelLanguage;
    Callback translationCallback = new Callback() {
            @Override
            public void onTranslationComplete(String text) {
                translatedText = text;
                mResult.setText(translatedText);
                newText = false;
                Log.e("translatedText", "saved translated text");
            }
        };


    final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
        new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                image_uri=uri;
                try {
                    bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                    mPreview.setImageBitmap(bitmap);
                    if (bitmap!=null){
                        Log.e("bitmap",bitmap.toString());
                        newText = true;
                        runTextRecognition();
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        modelLanguage = new ModelLanguage();

        ActionBar actionBar=getSupportActionBar();
        assert actionBar != null;
        actionBar.setSubtitle("Click + button to insert Image");
        mPreview = binding.imageView;
        mResult = binding.result;
        mResult.setText("");
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        binding.switchcompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (newText) {
                    mResult.setText("");
                    modelLanguage.translate(extractedText, translationCallback);
//                    translatedText = mResult.getText().toString();
                } else {
                    mResult.setText(translatedText);
                }
            } else {
                mResult.setText(extractedText);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.addImage){
            showImageImportDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog(){
        if (!checkStoragePermission()){
            requestStoragePermission();
        }
        else{
            pickGallery();
        }
    }

    private void pickGallery() {
        mGetContent.launch("image/*");
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission,STORAGE_REQUEST_CODE );
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorageAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                if (writeStorageAccepted) {
                    pickGallery();
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    extractedText = visionText.getText();

                    if (binding.switchcompat.isChecked()){
                        mResult.setText("");
                        modelLanguage.translate(extractedText, translationCallback);
//                        mResult.setText(translatedText);
//                        translatedText = mResult.getText().toString();
                        Log.e("translatedText", "saved translated text");
                    } else {
                        mResult.setText(extractedText);
                    }
                    showToast("Texts are successfully extracted", true);
                })
                .addOnFailureListener(
                    e -> {
                        Log.e(TAG, "Text recognition failed: " + e.getMessage());
                        showToast("Text recognition failed", false);
                });
    }

    private void showToast(String message, boolean success) {

        Snackbar snackbar = Snackbar.make(binding.getRoot(),message,Snackbar.LENGTH_SHORT);

        if (success) {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setAction("COPY", v -> {
                copyToClipboard(mResult.getText().toString());
//                change action button
                snackbar.setAction("COPIED", v1 -> {
                    snackbar.setDuration(Snackbar.LENGTH_LONG);
                    new CountDownTimer(2000, 1000) {
                       public void onTick(long millisUntilFinished) {
                       }
                       public void onFinish() {
                           snackbar.dismiss();
                       }
                    }.start();
                });
            });
        }
        snackbar.show();
    }

    void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("extracted", text);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            Toast.makeText(this , "Copied", Toast.LENGTH_SHORT).show();
    }
}