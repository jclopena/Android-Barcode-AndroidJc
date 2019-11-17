package com.iephdevs.androidbarcodegenerator;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String message = "";
    private String type = "";
    private EditText editText1;
    private ImageView imageView;
    private String time;

    private Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        message = "";
        type = "QR Code";
       Button button_generate = findViewById(R.id.generate_button);
        editText1 = findViewById(R.id.edittext2);
        Spinner type_spinner = findViewById(R.id.type_spinner);
        imageView = findViewById(R.id.image_imageview);
        Button scan = findViewById(R.id.scan);
        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position)
                {
                    case 1: type = "Barcode";break;
                    case 2: type = "Data Matrix";break;
                    case 3: type = "PDF 417";break;
                    case 4: type = "Barcode-39";break;
                    case 5: type = "Barcode-93";break;
                    case 6: type = "AZTEC";break;
                    default: type = "QR Code";break;
                }
                Log.d("type", type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scan = new Intent(MainActivity.this,Scanner.class);
                startActivity(scan);
            }
        });
        button_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message = editText1.getText().toString();

                if (message.equals("") || type.equals(""))
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Error");
                    dialog.setMessage("Invalid input!");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //  do nothing
                        }
                    });
                    dialog.create();
                    dialog.show();
                }
                else
                {
                    Bitmap bitmap = null;
                    try
                    {
                        bitmap = CreateImage(message, type);
                        myBitmap = bitmap;
                    }
                    catch (WriterException we)
                    {
                        we.printStackTrace();
                    }
                    if (bitmap != null)
                    {
                        imageView.setImageBitmap(bitmap);
                    }
                    Toast.makeText(MainActivity.this, "Generate success!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public Bitmap CreateImage(String message, String type) throws WriterException
    {
        BitMatrix bitMatrix;
        int size = 660;
        int size_width = 660;
        int size_height = 264;
        switch (type)
        {
            case "Barcode": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_128, size_width, size_height);break;
            case "Data Matrix": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.DATA_MATRIX, size, size);break;
            case "PDF 417": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.PDF_417, size_width, size_height);break;
            case "Barcode-39":bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_39, size_width, size_height);break;
            case "Barcode-93":bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_93, size_width, size_height);break;
            case "AZTEC": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.AZTEC, size, size);break;
            case "QR Code":
            default: bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, size, size);break;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int [] pixels = new int [width * height];
        for (int i = 0 ; i < height ; i++)
        {
            for (int j = 0 ; j < width ; j++)
            {
                if (bitMatrix.get(j, i))
                {
                    pixels[i * width + j] = 0xff000000;
                }
                else
                {
                    pixels[i * width + j] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void saveBitmap (Bitmap bitmap, String message, String bitName)
    {

        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        int permission = ContextCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS,1);
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);

        String fileName = message + "_at_" + year + "_" + month + "_" + day + "_" + hour + "_" + minute + "_" + second + "_"  + millisecond;
        time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + millisecond;
        File file;

        String fileLocation;

        String folderLocation;

        if(Build.BRAND.equals("Xiaomi") ){
            fileLocation = Environment.getExternalStorageDirectory().getPath()+"/IEPH BarcodeGenerator/" + fileName + bitName ;
            folderLocation = Environment.getExternalStorageDirectory().getPath()+"/IEPH BarcodeGenerator/";
        }else{
            fileLocation = Environment.getExternalStorageDirectory().getPath()+"/IEPH BarcodeGenerator/" + fileName + bitName ;
            folderLocation = Environment.getExternalStorageDirectory().getPath()+"/IEPH BarcodeGenerator/";
        }

        Log.d("file_location", fileLocation);

        file = new File(fileLocation);

        File folder = new File(folderLocation);
        if (!folder.exists())
        {
            folder.mkdirs();
        }

        if (file.exists())
        {
            file.delete();
        }


        FileOutputStream out;

        try
        {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out))
            {
                out.flush();
                out.close();
            }
        } catch (IOException fnfe)
        {
            fnfe.printStackTrace();
        }

        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if(editText1 == null){
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Error");
                dialog.setMessage("Generate first!");
                dialog.setCancelable(false);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  do nothing
                    }
                });
                dialog.create();
                dialog.show();
            }
            else {
                try {
                    // Save image
                    saveBitmap(myBitmap, message, ".png");

                    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                    @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.success_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setCancelable(false);
                    builder.setView(view);
                    TextView success_text = view.findViewById(R.id.success_text);
                    success_text.setText("Message : " + message + "\n\n" + "Time and date you create this barcode : " + time);
                    ImageView success_imageview = view.findViewById(R.id.success_imageview);
                    success_imageview.setImageBitmap(myBitmap);
                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create();
                    builder.show();
                    Toast.makeText(MainActivity.this, "Save barcode DONE!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception ignored) {

                }
            }
            return true;
        }
        if (id == R.id.about) {

            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.about, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setCancelable(false);
            builder.setView(view);
            ImageView fb = view.findViewById(R.id.about_facebook);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/lopena.jc")));
                }
            });
            ImageView git = view.findViewById(R.id.about_git);
            git.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jclopena")));
                }
            });
            ImageView ieph = view.findViewById(R.id.iv_about_ieph);
            ieph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://iephdev.com/members/android-jc.56/")));
                }
            });
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });
            builder.create();
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





}
