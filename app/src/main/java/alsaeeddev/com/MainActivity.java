package alsaeeddev.com;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btnCreatePDf;
    Bitmap bitmap, scaledBitmap;
    EditText etCustomerName;
    private final String[] informationArray = new String[]{"Name", "Company Name", "Address", "Phone", "Email"};

    private int srNumber;
    String ItemName, price, Quantity, priceTotal;


    private RecyclerView recyclerView;

    private MyAdapter adapter;
    private final List<ItemModel> listItem = new ArrayList<>();
    private final List<String> fullCodeList = new ArrayList<>();


    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    private DecoratedBarcodeView barcodeView;

    private final Map<String, String> map = new HashMap<>();

    int startingIndex = 5;
    String copyDecodeText;
    int yAxisForValue = 280;
    private List<Double> totalPricePerItemList = new ArrayList<>();
    private String customerName;

    String pattern = "^[-+]?[\\d&]*\\.?\\d+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etCustomerName = findViewById(R.id.etCustomerName);
        btnCreatePDf = findViewById(R.id.btnNext);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_2);

        recyclerView = findViewById(R.id.myRecyclerView);
        barcodeView = findViewById(R.id.zxingBarcodeScanner);
        barcodeView.setStatusText("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this, listItem, (position, quantity) -> {
            listItem.get(position).setItemQuantity(quantity);
        });
        recyclerView.setAdapter(adapter);

        map.put("1244", "Cold Drink");
        map.put("0054", "Burger");
        map.put("0187", "Sandwich");
        map.put("7176", "Pizza");

        startScanning();
        //scaledBitmap = Bitmap.createScaledBitmap(bitmap,100,100,false);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        {

            //  createPdf();
            startScanning();
            createPdf();
        }

    }


    private void startScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Handle the decoded result
                String decodedText = result.getText();
                // addItemToRecyclerView(decodedText);

                if (decodedText.length() == 14) {
                    // Check if barcode has been scanned before
                    if (!fullCodeList.contains(decodedText)) {

                        fullCodeList.add(decodedText);

                        copyDecodeText = decodedText;

                        // Extract the last 4 digits using substring
                        double lastDigitPrice = Double.parseDouble(decodedText.substring(decodedText.length() - 4));

                        String productId = copyDecodeText.substring(startingIndex, startingIndex + 4);

                        if (String.valueOf(lastDigitPrice).matches(pattern) && productId.matches(pattern)) {
                            ItemModel itemModel = new ItemModel(lastDigitPrice, map.get(productId), 1);
                            addItemToRecyclerView(itemModel);
                        }
                    }
                }

            }


            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // You can use this callback method to show visual cues on the viewfinder.
            }


        });
    }


    private void addItemToRecyclerView(ItemModel item) {
        //  listItem.add(item);
        //   adapter.addItem(item);
        listItem.add(item);
        adapter.notifyItemInserted(listItem.size() - 1);
    }


    private void createPdf() {
        btnCreatePDf.setOnClickListener(v -> {

            if(etCustomerName.getText().length() != 0 && adapter.getItemCount() != 0){

                customerName = etCustomerName.getText().toString();

                PdfDocument pdfDocument = new PdfDocument();
                Paint paint = new Paint();
                //    paint.setLetterSpacing(0.01f);

                PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page myPage1 = pdfDocument.startPage(pageInfo1);
                Canvas canvas = myPage1.getCanvas();


                // insert the picture
                int endPosition = pageInfo1.getPageWidth() - 100;
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

                canvas.drawBitmap(scaledBitmap, endPosition, 0, paint);

                // draw a text as like invoice
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(32.0f);
                paint.setColor(ContextCompat.getColor(this, R.color.green));
                paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                canvas.drawText("Invoice", (float) pageInfo1.getPageWidth() / 2, 100, paint);


                // customer name headiing
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(10.0f);
                paint.setColor(Color.BLACK);
                paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                canvas.drawText("Customer Name: "+customerName, 80, 170, paint);


          /*  int endX = pageInfo1.getPageWidth() - 5;

            float textWidth = paint.measureText(getCurrentDate());
            float x = pageInfo1.getPageWidth() - textWidth;*/

                // date and time heading
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("Date: " + getCurrentDate(), 590, 170, paint);
                canvas.drawText("Time: " + getCurrentTime(), 590, 180, paint);

                //draw rectangle stroke etc
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1);
                //draw rectangle
                canvas.drawRect(10, 220, pageInfo1.getPageWidth() - 10, 250, paint);
                // draw four line vertical, to make portions in rectangle
                canvas.drawLine(100, 220, 100, 250, paint);
                canvas.drawLine(300, 220, 300, 250, paint);
                canvas.drawLine(430, 220, 430, 250, paint);
                canvas.drawLine(500, 220, 500, 250, paint);


                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(12.0f);
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                // draw texts invoice data headings
                canvas.drawText("Sr No.", 15, 240, paint);
                canvas.drawText("Item Name", 105, 240, paint);
                canvas.drawText("Price", 305, 240, paint);
                canvas.drawText("Qty", 435, 240, paint);
                canvas.drawText("Total", 505, 240, paint);

                srNumber = 0;
                for (int sNo = 0; sNo < listItem.size(); sNo++) {
                    srNumber++;
                    // draw values as like qty price  item name etc
                    canvas.drawText(srNumber + "", 17, yAxisForValue, paint);
                    canvas.drawText(listItem.get(sNo).getItemName(), 106, yAxisForValue, paint);
                    canvas.drawText(String.valueOf(listItem.get(sNo).getItemPrice()), 306, yAxisForValue, paint);
                    canvas.drawText(String.valueOf(listItem.get(sNo).getItemQuantity()), 435, yAxisForValue, paint);
                    canvas.drawText(calculateTotalPerItem(listItem.get(sNo).getItemPrice(), listItem.get(sNo).getItemQuantity()), 508, yAxisForValue, paint);
                    yAxisForValue += 20;
                }



                yAxisForValue += 20;
                // draw line below of value and above of total
                canvas.drawLine(300, yAxisForValue, pageInfo1.getPageWidth() - 10, yAxisForValue, paint);

                yAxisForValue += 20;
                // draw texts as like total
                canvas.drawText("Sub Total", 306, yAxisForValue, paint);
                canvas.drawText(String.valueOf(calculateSubTotal()), 508, yAxisForValue, paint);
                canvas.drawText(":", 435, yAxisForValue, paint);

                yAxisForValue += 20;
                canvas.drawText("Tax (5%)", 306, yAxisForValue, paint);
                canvas.drawText(String.valueOf(calculateTax()), 508, yAxisForValue, paint);
                canvas.drawText(":", 435, yAxisForValue, paint);


                Paint paint2 = new Paint();
                paint2.setStrokeWidth(1); // Set the stroke width to 5 (adjust as needed)
                paint2.setColor(ContextCompat.getColor(this, R.color.black)); // Set the stroke color to black
                paint2.setStyle(Paint.Style.STROKE); // Set the style to fill and stroke


                yAxisForValue += 35; // this value uses for top
                int bottom = yAxisForValue + 45;
                paint.setColor(ContextCompat.getColor(this, R.color.green));
                canvas.drawRect(300, yAxisForValue, pageInfo1.getPageWidth() - 10, bottom, paint);
                canvas.drawRect(300, yAxisForValue, pageInfo1.getPageWidth() - 10, bottom, paint2);

                int yValueForGrandTotal = bottom - yAxisForValue;
                int calc = yValueForGrandTotal / 3;
                int finalCalc = calc * 2;
                yAxisForValue += finalCalc;

                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                paint.setTextSize(16.0f);
                canvas.drawText("Total", 320, yAxisForValue, paint);
                canvas.drawText(String.valueOf(calculateSubTotal() + calculateTax()), 515, yAxisForValue, paint);


                paint.setColor(ContextCompat.getColor(this, R.color.black));
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(8.0f);
                canvas.drawText("Invoice Number: " + System.currentTimeMillis(), 17, pageInfo1.getPageHeight() - 20, paint);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("Generate by Al Saeed", pageInfo1.getPageWidth() - 17, pageInfo1.getPageHeight() - 20, paint);





                pdfDocument.finishPage(myPage1);


                String folderName = "AlsaeedFolder"; // Define your custom folder name
                File customFolder = new File(Environment.getExternalStorageDirectory(), folderName);
                if (!customFolder.exists()) {
                    customFolder.mkdirs(); // Create the folder if it doesn't exist
                }
                File file = new File(customFolder, "myPDF.pdf");

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        pdfDocument.writeTo(Files.newOutputStream(file.toPath()));
                    } else {
                        pdfDocument.writeTo(new FileOutputStream(file));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Toast.makeText(this, "File save in " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                // pdfDocument.close();


                // Open the PDF file using a PDF viewer app
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri pdfUri = FileProvider.getUriForFile(this, "alsaeeddev.com.fileProvider", file);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permissions

                try {
                    startActivity(intent); // Launch the PDF viewer activity
                } catch (ActivityNotFoundException e) {
                    // Handle the case where no PDF viewer app is available
                    Toast.makeText(getApplicationContext(), "No PDF viewer app found", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(this, "File saved in " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                pdfDocument.close();

            }else {
                etCustomerName.setError("Enter Name");
            }




        });
    }


    private String getCurrentDate() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is zero-based, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);

// Construct the date string
        return year + "-" + month + "-" + day;

    }


    private String getCurrentTime() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

// Construct the time string
        return hour + ":" + minute + ":" + second;
    }


    private String calculateTotalPerItem(double price, int quantity) {
        double total = price * quantity;
        totalPricePerItemList.add(total);
        return String.valueOf(total);
    }


    private double calculateSubTotal() {
        double subTotal = 0;
        for (int i = 0; i < totalPricePerItemList.size(); i++) {
            subTotal += totalPricePerItemList.get(i);
        }
        return subTotal;
    }


    private double calculateTax() {

        return calculateSubTotal() * 0.5;
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        yAxisForValue = 280;
        totalPricePerItemList.clear();
        barcodeView.pause();

    }

    @Override
    protected void onStop() {
        yAxisForValue = 280;
        totalPricePerItemList.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        listItem.clear();
        fullCodeList.clear();
        super.onDestroy();
    }
}
