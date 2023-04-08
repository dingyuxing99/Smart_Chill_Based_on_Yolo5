package com.example.finalpoject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    private String[] mTestImages = {"aicook1.jpg", "aicook2.jpg", "aicook3.jpg"};

    private ImageView mImageView;
    private ResultView mResultView;
    private Button mButtonDetect;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        final Button buttonTest = findViewById(R.id.testButton);
        buttonTest.setText(("Test Image 1/3"));
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                mImageIndex = (mImageIndex + 1) % mTestImages.length;
                buttonTest.setText(String.format("Text Image %d/%d", mImageIndex + 1, mTestImages.length));

                try {
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
                    mImageView.setImageBitmap(mBitmap);
                } catch (IOException e) {
                    Log.e("Object Detection", "Error reading assets", e);
                    finish();
                }
            }
        });


        final Button buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);

                final CharSequence[] options = {"Choose from Photos", "Take Picture", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Test Image");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Picture")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        } else if (options[item].equals("Choose from Photos")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 1);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        final Button buttonLive = findViewById(R.id.liveButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        mButtonDetect = findViewById(R.id.detectButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mButtonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonDetect.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonDetect.setText(getString(R.string.run_model));

                mImgScaleX = (float) mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                mImgScaleY = (float) mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float) mImageView.getWidth() / mBitmap.getWidth() : (float) mImageView.getHeight() / mBitmap.getHeight());
                mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth() ? (float) mImageView.getHeight() / mBitmap.getHeight() : (float) mImageView.getWidth() / mBitmap.getWidth());

                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth()) / 2;
                mStartY = (mImageView.getHeight() - mIvScaleY * mBitmap.getHeight()) / 2;

                Thread thread = new Thread(MainActivity.this);
                thread.start();
            }
        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "best.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("aicook.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                mImageView.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);
        String recipe_generated = generate_recipe(results);
        System.out.println(recipe_generated);
        String recipe_suggestion = generate_suggestion(results);
        System.out.println(recipe_suggestion);


        runOnUiThread(() -> {
            mButtonDetect.setEnabled(true);
            mButtonDetect.setText(getString(R.string.detect));
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
        });
    }

    public String generate_recipe(ArrayList<Result> results) {
        OkHttpClient client = new OkHttpClient();
        ArrayList<String> ingredients_list = new ArrayList<>();
        for (Result result : results) {
            System.out.println(PrePostProcessor.mClasses[result.classIndex]);
            ingredients_list.add(PrePostProcessor.mClasses[result.classIndex]);
        }
        if (ingredients_list.size() == 0) {
            return null;
        }
        Collections.shuffle(ingredients_list);
        String url = "https://api.openai.com/v1/completions";
        int maxTokens = 200;
        String model = "text-davinci-003";
        String ingredients = "";
        // if too many ingredients are detected, random choose four to cook;
        if (ingredients_list.size() > 4) {
            List<String> ingredients_list_ = ingredients_list.subList(0, 4);
            ingredients = String.join("\n", ingredients_list_);
        } else {
            ingredients = String.join("\n", ingredients_list);
        }
        String prompt = String.format("Write a recipe based on these ingredients:\n\nIngredients: \n%s\n \nInstructions:", ingredients);
        double temperature = 0.3;
        double top_p = 1.0;
        double frequency_penalty = 0.0;
        double presence_penalty = 0.0;
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject json = new JSONObject();
        try {
            json.put("model", model);
            json.put("prompt", prompt);
            json.put("temperature", temperature);
            json.put("max_tokens", maxTokens);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String requestBody = json.toString();

        String apiKey = getString(R.string.openai_api_key);
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        Response response = null;
        String responseString = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            responseString = Okio.buffer(Okio.source(response.body().byteStream())).readUtf8();
        } catch (IOException e) {
            Toast.makeText(this, "Sorry, Time Out, Please Check Your Network", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        System.out.println(responseString);
        JSONObject jsonResponse = null;
        try {
            assert responseString != null;
            jsonResponse = new JSONObject(responseString);
            String completedText = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
            System.out.println(completedText);
            return (completedText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalResponseString = responseString;
        Toast.makeText(this, finalResponseString, Toast.LENGTH_LONG).show();
        return url;
    }

    public String generate_suggestion(ArrayList<Result> results) {
        OkHttpClient client = new OkHttpClient();
        ArrayList<String> ingredients_list = new ArrayList<>();
        for (Result result : results) {
            ingredients_list.add(PrePostProcessor.mClasses[result.classIndex]);
        }
        if (ingredients_list.size() == 0) {
            Toast.makeText(this, "Try to detect again!", Toast.LENGTH_LONG).show();
            return null;
        }
        String url = "https://api.openai.com/v1/completions";
        int maxTokens = 200;
        String model = "text-davinci-003";
        String ingredients = "";
        ingredients = String.join("\n", ingredients_list);
        String prompt = String.format("Write a brief conclusion of nutrition analysis and make some suggestions on following ingredients based on Healthy Eating Food Pyramid" +
                ":\n\nIngredients: \n%s", ingredients);
        double temperature = 0.3;
        double top_p = 1.0;
        double frequency_penalty = 0.0;
        double presence_penalty = 0.0;
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject json = new JSONObject();
        try {
            json.put("model", model);
            json.put("prompt", prompt);
            json.put("temperature", temperature);
            json.put("max_tokens", maxTokens);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String requestBody = json.toString();

        String apiKey = getString(R.string.openai_api_key);
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        Response response = null;
        String responseString = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            responseString = Okio.buffer(Okio.source(response.body().byteStream())).readUtf8();
        } catch (IOException e) {
            Toast.makeText(this, "Sorry, Time Out, Please Check Your Network", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        System.out.println(responseString);
        JSONObject jsonResponse = null;
        try {
            assert responseString != null;
            jsonResponse = new JSONObject(responseString);
            String completedText = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
            System.out.println(completedText);
            return (completedText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalResponseString = responseString;
        Toast.makeText(this, finalResponseString, Toast.LENGTH_LONG).show();
        return url;
    }
}
