package com.example.textrecognition;

import static android.Manifest.permission.CAMERA;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {
    private ImageView capture;
    private TextView resultTV;
    private Bitmap image;
    static final int REQUEST_IMAGE_CAPTURE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        capture=findViewById(R.id.capture);
        resultTV=findViewById(R.id.text2);
        Button snap = findViewById(R.id.btn1);
        Button detection = findViewById(R.id.btn2);

        detection.setOnClickListener(view -> detectText());
        snap.setOnClickListener(view -> {
            if(checkPermission()){
                captureImage();
            }
            else{
                requestPermission();
            }
        });

    }
    private boolean checkPermission(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        int PERMISSION_CODE=200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERMISSION_CODE);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void captureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission =grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(cameraPermission) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                captureImage();
            }
                else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                }

            }
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK) {
            assert data != null;
            Bundle extras = data.getExtras();
            image = (Bitmap) extras.get("data");
            capture.setImageBitmap(image);

        }
    }

    private void detectText() {
        InputImage inputImage= InputImage.fromBitmap(image,0);
        TextRecognizer recognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(inputImage).addOnSuccessListener(text -> {
            StringBuilder results = new StringBuilder();
            for(Text.TextBlock block: text.getTextBlocks()){
                String blockText =block.getText();
                Point[] blockCornerPoint = block.getCornerPoints();
                Rect blockFrame = block.getBoundingBox();
                for(Text.Line line:block.getLines()){
                    String lineText = line.getText();
                    Point[] lineCornerPoint = line.getCornerPoints();
                    Rect lineRect = line.getBoundingBox();
                    for(Text.Element element : line.getElements()){
                        String elementText = element.getText();
                        results.append(elementText);
                    }
                  resultTV.setText(blockText);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(ScannerActivity.this,"Failed to detect",Toast.LENGTH_SHORT).show());
    }
}