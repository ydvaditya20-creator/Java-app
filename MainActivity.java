package com.example.firebasehttpupload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    private ImageView imageView;
    
    // अपना Firebase Storage URL या Firebase Functions का HTTP URL यहाँ डालें
    // उदाहरण: "https://googleapis.com"
    private final String FIREBASE_UPLOAD_URL = "YOUR_FIREBASE_HTTP_URL_HERE";

    @Override
    protected void Bundle) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button btnCapture = findViewById(R.id.btnCapture);

        btnCapture.setOnClickListener(v -> {
            // कैमरा ओपन करने के लिए Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // Bitmap को Byte Array में बदलें
            byte[] imageData = convertBitmapToByteArray(imageBitmap);

            // HTTP Request से Firebase पर अपलोड करें
            uploadImageToFirebase(imageData);
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }

    private void uploadImageToFirebase(byte[] bytes) {
        OkHttpClient client = new OkHttpClient();

        // Image MIME Type सेट करें
        RequestBody requestBody = RequestBody.create(bytes, MediaType.parse("image/jpeg"));

        // HTTP PUT Request बनाएँ (Firebase Storage REST API के लिए PUT का इस्तेमाल होता है)
        Request request = new Request.Builder()
                .url(FIREBASE_UPLOAD_URL)
                .put(requestBody)
                .addHeader("Content-Type", "image/jpeg")
                .build();

        // Asynchronous Network Call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
