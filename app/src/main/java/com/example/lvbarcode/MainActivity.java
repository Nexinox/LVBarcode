package com.example.lvbarcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.barcode.Barcode;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isOnline()) {

            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[] { new TrustAllX509TrustManager() }, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }


            setContentView(R.layout.activity_main);
            barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        } else {
            setContentView(R.layout.noinet_view);
        }

    }

    String ArticleName;
    int ArticleDuration;
    int ProductQuantity;

    @Override
    public void onScanned(final Barcode barcode) {
        barcodeReader.pauseScanning();
        barcodeReader.playBeep();

        final Long ean = Long.parseLong(barcode.displayValue);
        AsyncTask.execute(() -> {

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            String Cehckurl ="http://192.168.43.110:8080/Article/rest/check/" + ean;

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Cehckurl,
                    response -> {

                        MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show());

                        if (response.equals("y")) {

                            AlertDialog.Builder Namebuilder = new AlertDialog.Builder(MainActivity.this);
                            Namebuilder.setTitle("Menge Eingeben: ");

                            final EditText QuantInput = new EditText(MainActivity.this);

                            QuantInput.setInputType(InputType.TYPE_CLASS_TEXT);

                            Namebuilder.setView(QuantInput);


                            Namebuilder.setPositiveButton("OK", (dialog, which) -> {
                                ProductQuantity = Integer.parseInt(QuantInput.getText().toString());

                                String CreateLVURL = "http://192.168.43.110:8080/LV/rest/" + ean +
                                        "/" + ProductQuantity;


                                StringRequest CreatestringRequest = new StringRequest(Request.Method.POST, CreateLVURL,
                                        Createresponse -> {

                                            if (Createresponse.equals("Workde")){
                                                MainActivity.this.runOnUiThread(() -> {
                                                    Toast.makeText(MainActivity.this, "Added Sucsessfully", Toast.LENGTH_SHORT).show();
                                                    barcodeReader.resumeScanning();
                                                });
                                            }

                                        }, error -> {
                                            error.printStackTrace();
                                            barcodeReader.resumeScanning();
                                        });
                                queue.add(CreatestringRequest);
                            });
                            Namebuilder.setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.cancel();
                                barcodeReader.resumeScanning();
                            });


                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Namebuilder.show();
                                }
                            });






                        } else if (response.equals("n")) {





                            AlertDialog.Builder Durationbuilder = new AlertDialog.Builder(MainActivity.this);
                            Durationbuilder.setTitle("Haltbarkeit Eingeben: ");

                            final EditText DurationInput = new EditText(MainActivity.this);

                            DurationInput.setInputType(InputType.TYPE_CLASS_TEXT);

                            Durationbuilder.setView(DurationInput);


                            Durationbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ArticleDuration = Integer.parseInt(DurationInput.getText().toString());

                                    String CreateArticleURL = "http://192.168.43.110:8080/Article/rest/"+ean+"/"+ArticleName+"/"+ArticleDuration;

                                    StringRequest CreatestringRequest = new StringRequest(Request.Method.POST, CreateArticleURL,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String Createresponse) {

                                                    if (Createresponse.equals("Workde")){
                                                        MainActivity.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(MainActivity.this, "Added Sucsessfully", Toast.LENGTH_SHORT).show();
                                                                barcodeReader.resumeScanning();
                                                            }
                                                        });
                                                    }

                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            error.printStackTrace();
                                            barcodeReader.resumeScanning();
                                        }
                                    });
                                    queue.add(CreatestringRequest);

                                }
                            });
                            Durationbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    barcodeReader.resumeScanning();
                                }
                            });


                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Durationbuilder.show();
                                }
                            });

                            AlertDialog.Builder Namebuilder = new AlertDialog.Builder(MainActivity.this);
                            Namebuilder.setTitle("Namen Eingeben: ");

                            final EditText Nameinput = new EditText(MainActivity.this);

                            Nameinput.setInputType(InputType.TYPE_CLASS_TEXT);

                            Namebuilder.setView(Nameinput);


                            Namebuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ArticleName = Nameinput.getText().toString();
                                }
                            });
                            Namebuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Namebuilder.show();
                                }
                            });



                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(stringRequest);

        });
    }

    @Override
    public void onScannedMultiple(List<Barcode> list) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String s) {

    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(), "Camera permission denied!", Toast.LENGTH_LONG).show();
    }

    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            return true;

        } else {

            return false;

        }

    }


}