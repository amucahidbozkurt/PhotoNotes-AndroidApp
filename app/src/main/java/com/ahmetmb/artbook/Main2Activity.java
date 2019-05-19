package com.ahmetmb.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView;
    EditText editText;
    static SQLiteDatabase database;
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equalsIgnoreCase("new")) {

            Bitmap selectimage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.selectimage);
            imageView.setImageBitmap(selectimage);

            button.setVisibility(View.VISIBLE);
            editText.setText("");

        } else {

            String name = intent.getStringExtra("name");
            editText.setText(name);
            int position = intent.getIntExtra("position", 0);

            imageView.setImageBitmap(MainActivity.artImage.get(position));

            button.setVisibility(View.INVISIBLE);

        }

    }

    public void select (View view) {

        //kullanicidan izin aldiktan sonra galeroye erisim sagliyoruz

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        }

    }

        //izin yoktu izin verdi o zaman ne yapacagiz yani ilk izin verdiginde
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 2) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            Uri image = data.getData();

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save (View view) {

        // tum alanlarin eksiksiz bir sekilde doldurulmasi icin kontrol ediyor ve uyariyoruz
        if (editText.getText().toString().isEmpty() || selectedImage.isRecycled()) {

            Toast.makeText(this, "Tum Alanlari Lutfen Eksiksiz Doldurun!", Toast.LENGTH_LONG).show();

        } else {

            String artName = editText.getText().toString();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
            byte[] byteArray = outputStream.toByteArray();

            //aldigimiz verileri tutmak icin database olusturuyoruz ve icine kayit ediyoruz
            try {

                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)");

                String sqlString = "INSERT INTO arts (name, image) VALUES (?, ?)";
                SQLiteStatement statement = database.compileStatement(sqlString);
                statement.bindString(1, artName); //ilk soru isareti
                statement.bindBlob(2, byteArray); //ikinci soru isareti
                statement.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }

    }

}
