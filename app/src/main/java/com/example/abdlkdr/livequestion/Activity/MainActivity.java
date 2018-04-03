package com.example.abdlkdr.livequestion.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.abdlkdr.livequestion.Model.Answer;
import com.example.abdlkdr.livequestion.Model.QuestionAndAnswer;
import com.example.abdlkdr.livequestion.Model.Video;
import com.example.abdlkdr.livequestion.R;
import com.example.abdlkdr.livequestion.Util.Constant;
import com.example.abdlkdr.livequestion.Util.PahoMqttClient;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

//sorun iki tane counttimer çağrışdığı için iki defa getonly splash ekranına giriyor ve böylece iki defa temp value artırmış oluyoruz
    //yarın burayı düzelt
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'


//    http://www.androidtutorialshub.com/android-count-down-timer-tutorial/

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private long timeCountInMilliSeconds = 1 * 60000;
    private ProgressBar progressBarCircle;
    private VideoView splashVideoView, questionVideoView;
    private FrameLayout questionFrameLayout, progressBarCircleFrameLayout, mainFrameLayout;
    private int tempValue = 1, tempButtonClick = 0, rightAnswer = 0, counter = 0;
    private static final String TAG = "MainActivity";
    private CountDownTimer countDownTimer, answerCountDownTimer;
    private TextView timeTextView, questionTextView;
    private String videoURL = "", splashVideoURL = "";
    //    public static final String clientId = "MobiversiteSamsung";
    public static final String clientId = "MobiversiteEmulatorMarshmallow";
    public static final String serverURI = "tcp://mobiversite.cloudapp.net:1883";
    public static final String publishTopic = "deneme";
    private MqttAndroidClient mqttAndroidClient;
    private Button answerOne, answerSecond, answerThird;
    private Dialog dialog;
    private QuestionAndAnswer questionAndAnswer = new QuestionAndAnswer();
    private Answer answer = new Answer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        createPopUpDialog();
        getSplashVideoView(tempValue);

        PahoMqttClient pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(
                getApplicationContext(), serverURI, clientId);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.e(TAG, "connectComplete: " + s);
                try {
                    subscribe(mqttAndroidClient, publishTopic, 0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.e(TAG, "connectionLost: " + throwable.getLocalizedMessage());
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.e(TAG, "messageArrived: " + new String(mqttMessage.getPayload()));
                //soru geldiğinde soru ekranı gozukecek ve soru timer baslıyacak
                if ((!(new String(mqttMessage.getPayload())).contentEquals("I am going offline"))) {
                    Toast.makeText(MainActivity.this, new String(mqttMessage.getPayload()), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "messageArrived: " + new String(mqttMessage.getPayload()));
                    splashVideoView.setVisibility(View.GONE);
                    parseStringToJson(new String(mqttMessage.getPayload()));
                    setProgressBarCircle();

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.e(TAG, "deliveryComplete: " + new String(iMqttDeliveryToken.toString()));
            }
        });
    }

    private QuestionAndAnswer parseStringToJson(String message) {
        JSONObject jsonObject;
        try {
            if (message != null) {
                jsonObject = new JSONObject(message);
                if (!jsonObject.isNull("question"))
                    questionAndAnswer.setQuestion(jsonObject.getString("question"));
                if (!jsonObject.isNull("answer"))
                    questionAndAnswer.setAnswer(jsonObject.getString("answer"));
                if (!jsonObject.isNull("answer2"))
                    questionAndAnswer.setAnswer2(jsonObject.getString("answer2"));
                if (!jsonObject.isNull("answer3"))
                    questionAndAnswer.setAnswer3(jsonObject.getString("answer3"));
                if (!jsonObject.isNull("questionNumber"))
                    questionAndAnswer.setQuestionNumber(jsonObject.getInt("questionNumber"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        answerOne.setBackgroundColor(Color.WHITE);
        answerSecond.setBackgroundColor(Color.WHITE);
        answerThird.setBackgroundColor(Color.WHITE);
        answerOne.setText(questionAndAnswer.getAnswer());
        answerSecond.setText(questionAndAnswer.getAnswer2());
        answerThird.setText(questionAndAnswer.getAnswer3());
        questionTextView.setText(questionAndAnswer.getQuestion());
        answeringQuestionByUser();
        dialog.show();
        Log.e(TAG, "parseStringToJson: question " + questionAndAnswer.getQuestion() + "  answer " + questionAndAnswer.getAnswer() + "   questionnumber  " + questionAndAnswer.getQuestionNumber());
        return questionAndAnswer;
    }

    private void bindView() {
        splashVideoView = (VideoView) findViewById(R.id.videoView);
        splashVideoView.setZOrderOnTop(true);

        questionVideoView = (VideoView) findViewById(R.id.questionVideoView);
    }

    private void setProgressBarCircle() {
        Log.e(TAG, "setProgressBarCircle: DENEME");
        int time = 15;
        timeCountInMilliSeconds = time * 1000;
        setProgressBarValues();
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeTextView.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timeTextView.setText(hmsTimeFormatter(0));
                // call to initialize the progress bar values
                setProgressBarValues();
                dialog.dismiss();
                Log.e(TAG, "onFinish: tempvalue in progress bar :  " + tempValue);
//                getSplashVideoView(tempValue);
//                splashVideoView.setVisibility(View.VISIBLE);
                splashVideoView.setVisibility(View.VISIBLE);
                getOnlySplash();
                //TEKRARDAN VİDEO DEVREYE GİRECEK procees videosuna girecek
//                    questionVideoView.setVisibility(View.GONE);
//                    dialog.dismiss();
//                    splashVideoView.setVisibility(View.VISIBLE);
//                getNextVideoUrl(tempValue);

            }
        }.start();
        countDownTimer.start();
    }

    private void setProgressBarValues() {
        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }

    private void getVideoUrl(int tempValue) {
        Log.e(TAG, "getVideoUrl: getVideoURL method ");
        final Video video = new Video();
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("sendVideoUrl")
                .addQueryParameter("tempValue", String.valueOf(tempValue))
                .build();
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String myResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body() != null) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(myResponse);
                                if (!jsonObject.isNull("id"))
                                    video.setId(jsonObject.getString("id"));
                                if (!jsonObject.isNull("url"))
                                    video.setUrl(jsonObject.getString("url"));
                                if (!jsonObject.isNull("status"))
                                    video.setStatus(jsonObject.getString("status"));
                                if (!jsonObject.isNull("videoLength"))
                                    video.setVideoLength(jsonObject.getInt("videoLength"));
                                if (!jsonObject.isNull("tempValue"))
                                    video.setTempValue(jsonObject.getInt("tempValue"));

                                Log.e(TAG, "run: videoURL is :  " + video.getUrl());
                                videoURL = video.getUrl();
                                getQuestionVideoView(videoURL);
//                                questionVideoView.requestFocus();
//                                questionVideoView.setZOrderOnTop(false);
//                                DisplayMetrics displayMetrics = new DisplayMetrics();
//                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//                                ViewGroup.LayoutParams videoParams = questionVideoView.getLayoutParams();
//                                videoParams.width = displayMetrics.widthPixels;
//                                videoParams.height = displayMetrics.heightPixels;
//                                questionVideoView.setLayoutParams(videoParams);
//                                questionVideoView.setVideoURI(Uri.parse(videoURL));
//                                new MyAsync().execute();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private class MyAsync extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
//        questionVideoView.start();

            Log.e(TAG, "doInBackground: HHHHHHHHHHHHHHHHHHHHHHHH");
            questionVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e(TAG, "onPrepared: PREPARİNG İŞLEMİ");
                    mp.start();
                    if (mp.isPlaying()) {
                        questionVideoView.setVisibility(View.VISIBLE);
                        splashVideoView.setVisibility(View.GONE);
                    }
                }
            });
            questionVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            questionVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(MainActivity.this,"bitti",Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
    }

    private void getQuestionVideoView(String questionVideoURL) {
        Log.e(TAG, "getQuestionVideoView:  start sonrası questionvideoURL :  " + questionVideoURL);
        if (questionVideoURL != null) {
            Uri uri = Uri.parse(questionVideoURL);
            questionVideoView.setVideoURI(uri);
            questionVideoView.requestFocus();
//            questionVideoView.start();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            ViewGroup.LayoutParams videoParams = questionVideoView.getLayoutParams();
            videoParams.width = displayMetrics.widthPixels;
            videoParams.height = displayMetrics.heightPixels;
            questionVideoView.setLayoutParams(videoParams);

            questionVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    if (mp.isPlaying()) {
                        Log.e(TAG, "onPrepared: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
                        Toast.makeText(MainActivity.this, "mp is playing", Toast.LENGTH_LONG).show();
                        questionVideoView.setZOrderOnTop(false);
                        splashVideoView.setZOrderOnTop(true);
//                    splashVideoView.setZOrderOnTop(false);
//
//                    splashVideoView.setZOrderMediaOverlay(false);
//                    questionVideoView.setZOrderOnTop(true);
//                    questionVideoView.setZOrderMediaOverlay(true);
                        questionVideoView.setVisibility(View.VISIBLE);
                        splashVideoView.setVisibility(View.GONE);
                    }
                }
            });
            questionVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e(TAG, "onCompletion: getquestionvideoview on completion listener");
                    Log.e(TAG, "doInBackground: video bitti");
                    Log.e(TAG, "onCompletion: tempValues is " + tempValue);
                    Toast.makeText(MainActivity.this, "VİDEO BİTTİ", Toast.LENGTH_LONG).show();
                }
            });
            questionVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    counter++;
//                    Log.e(TAG, "onInfo: getquestionvideoview onınfo lıstener");
//                    if (mp.isPlaying() && counter==1) {
//                        splashVideoView.setVisibility(View.GONE);
//                        Log.e(TAG, "onInfo: counter içerisişi BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBb" );
//                        Toast.makeText(MainActivity.this,"ON ınfo ",Toast.LENGTH_LONG).show();
//                        questionVideoView.setVisibility(View.VISIBLE);
//                    }
                    return false;
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "URL YOK", Toast.LENGTH_LONG).show();
        }
    }

    private void getSplashVideoView(int tempValue) {
        getVideoUrl(tempValue);
        //https://youtubemp4.to/@download/18-5ac340845a54f-mp4-105301/videos/W1VZ0prt-Dk/Water%2BSplash.mp4
        splashVideoURL = "http://mobiversite.cloudapp.net:8080/videos/splash.mp4";
//        splashVideoURL = "http://mobiversite.cloudapp.net:8080/videos/kisa.MP4";
        final Uri uri = Uri.parse(splashVideoURL);
        splashVideoView.setVideoURI(uri);
        splashVideoView.requestFocus();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ViewGroup.LayoutParams videoParams = splashVideoView.getLayoutParams();
        videoParams.width = displayMetrics.widthPixels;
        videoParams.height = displayMetrics.heightPixels;
        splashVideoView.setLayoutParams(videoParams);
        splashVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();

//                mp.setLooping(true);
            }
        });
        splashVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "onCompletion: splashvideoview on completion questionVideoViewIsActive    ");
//                splashVideoView.setVideoURI(uri);
//                splashVideoView.start();
                mp.start();
            }
        });
        splashVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
//        splashVideoView.start();
    }

    public void subscribe(@NonNull MqttAndroidClient client,
                          @NonNull final String topic, int qos) throws MqttException {
        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Subscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic);
            }
        });
    }

    private void createPopUpDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_deneme);
        dialog.setCanceledOnTouchOutside(true);

//        mainFrameLayout = dialog.findViewById(R.id.mainFrameLayout);
//        progressBarCircleFrameLayout = dialog.findViewById(R.id.progressBarCircleFrameLayout);
//        questionFrameLayout = dialog.findViewById(R.id.questionFrameLayout);

        answerOne = dialog.findViewById(R.id.answerOne);
        answerSecond = dialog.findViewById(R.id.answerSecond);
        answerThird = dialog.findViewById(R.id.answerThird);

        questionTextView = dialog.findViewById(R.id.questionTextView);
        timeTextView = dialog.findViewById(R.id.timeTextView);

        progressBarCircle = dialog.findViewById(R.id.progressBarCircle);

    }

    private void answeringQuestionByUser() {
        tempButtonClick = 0;
        answerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempButtonClick = 1;
                answerSecond.setClickable(false);
                answerThird.setClickable(false);
                answerOne.setBackgroundColor(Color.BLUE);
            }
        });
        answerSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempButtonClick = 2;
                answerOne.setClickable(false);
                answerThird.setClickable(false);
                answerSecond.setBackgroundColor(Color.BLUE);
            }
        });
        answerThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempButtonClick = 3;
                answerOne.setClickable(false);
                answerSecond.setClickable(false);
                answerThird.setBackgroundColor(Color.BLUE);
            }
        });
    }

    private void getOnlySplash() {
        getRightAnswer();
        splashVideoURL = "http://mobiversite.cloudapp.net:8080/videos/splash.mp4";
        final Uri uri = Uri.parse(splashVideoURL);
        splashVideoView.setVideoURI(uri);
        splashVideoView.requestFocus();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ViewGroup.LayoutParams videoParams = splashVideoView.getLayoutParams();
        videoParams.width = displayMetrics.widthPixels;
        videoParams.height = displayMetrics.heightPixels;
        splashVideoView.setLayoutParams(videoParams);
        splashVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
            }
        });
        splashVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setDialogButtons();
                questionAnswerDialog();
                dialog.show();
            }
        });
        splashVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        splashVideoView.start();
    }

    private void questionAnswerDialog() {
        int time = 5;
        timeCountInMilliSeconds = time * 1000;
        setProgressBarValues();
        tempValue = tempValue + 2;
        answerCountDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeTextView.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timeTextView.setText(hmsTimeFormatter(0));
                // call to initialize the progress bar values
                setProgressBarValues();
                dialog.dismiss();
//                tempValue=tempValue+2;
                splashVideoView.setVisibility(View.VISIBLE);
                getSplashVideoView(tempValue);
            }
        }.start();
        answerCountDownTimer.start();
    }

    private void setDialogButtons() {
        Log.e(TAG, "setDialogButtons: right answer is " + rightAnswer);
        rightAnswer = answer.getRightAnswer();
        answerOne.setText(questionAndAnswer.getAnswer());
        answerSecond.setText(questionAndAnswer.getAnswer2());
        answerThird.setText(questionAndAnswer.getAnswer3());
        questionTextView.setText(questionAndAnswer.getQuestion());
        if (rightAnswer == 1 && tempButtonClick == rightAnswer) {
            answerOne.setBackgroundColor(Color.GREEN);
        }
        if (rightAnswer == 2 && tempButtonClick == rightAnswer) {
            answerSecond.setBackgroundColor(Color.GREEN);
        }
        if (rightAnswer == 3 && tempButtonClick == rightAnswer) {
            answerThird.setBackgroundColor(Color.GREEN);
        }
        if (tempButtonClick == 1 && tempButtonClick != rightAnswer) {
            answerOne.setBackgroundColor(Color.RED);
            if (rightAnswer == 2)
                answerSecond.setBackgroundColor(Color.GREEN);
            if (rightAnswer == 3)
                answerThird.setBackgroundColor(Color.GREEN);
        }
        if (tempButtonClick == 2 && tempButtonClick != rightAnswer) {
            answerSecond.setBackgroundColor(Color.RED);
            if (rightAnswer == 1)
                answerOne.setBackgroundColor(Color.GREEN);
            if (rightAnswer == 3)
                answerThird.setBackgroundColor(Color.GREEN);
        }
        if (tempButtonClick == 3 && tempButtonClick != rightAnswer) {
            answerThird.setBackgroundColor(Color.RED);
            if (rightAnswer == 1)
                answerOne.setBackgroundColor(Color.GREEN);
            if (rightAnswer == 2)
                answerSecond.setBackgroundColor(Color.GREEN);
        }
    }

    private void getRightAnswer() {
        //http://localhost:8080/answer/getAnswer?questionId=3
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("answer")
                .addPathSegment("getAnswer")
                .addQueryParameter("questionId", String.valueOf(questionAndAnswer.getQuestionNumber()))
                .build();
        Log.e(TAG, "getRightAnswer: URL :" + url.toString());
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                int myResponse = Integer.parseInt(response.body().string());
                if (response.body() != null) {
                    answer.setRightAnswer(myResponse);
                    Log.e(TAG, "onResponse: answer is " + answer.getRightAnswer());
                    rightAnswer = answer.getRightAnswer();

                }
            }
        });
    }
}

//private class AsyncSplashVideoView extends AsyncTask<Void, Integer, Void> {
//    int counter = 1;
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        splashVideoView.requestFocus();
//        splashVideoView.start();
//        splashVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(true);
//                Log.e(TAG, "onCompletion: counter   " + counter);
//                counter++;
//            }
//        });
//        splashVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.e(TAG, "onCompletion: splash video stop question is active boolen " + questionVideoViewIsActive);
//                if (questionVideoViewIsActive ) {
//                    splashVideoView.setVisibility(View.GONE);
//                    splashVideoView.setZOrderOnTop(false);
//                    questionVideoView.setVisibility(View.VISIBLE);
//                    questionVideoView.setZOrderOnTop(true);
//                    questionVideoView.setZOrderMediaOverlay(true);
//                }
//
//            }
//        });
//        splashVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
////                    Log.e(TAG, "doInBackground: splashvideo video asenkron task temp values " + tempValue + "   questıon is active boolen : " + questionVideoViewIsActive);
////                    if (questionVideoView.isPlaying()) {
////                        splashVideoView.setVisibility(View.GONE);
////                        splashVideoView.setZOrderOnTop(false);
////                        questionVideoView.setVisibility(View.VISIBLE);
////                        questionVideoView.setZOrderOnTop(true);
////                        questionVideoView.setZOrderMediaOverlay(true);
////                    }
//                return false;
//            }
//        });
//
//        return null;
//    }
//}

//private class MyAsync extends AsyncTask<Void, Integer, Void> {
//    @Override
//    protected Void doInBackground(Void... voids) {
//        questionVideoView.start();
//        questionVideoView.requestFocus();
//        questionVideoView.setZOrderOnTop(true);
//        Log.e(TAG, "doInBackground: HHHHHHHHHHHHHHHHHHHHHHHH");
//        questionVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                Log.e(TAG, "onPrepared: PREPARİNG İŞLEMİ");
//            }
//        });
//        questionVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                if (mp.isPlaying()) {
//                    Log.e(TAG, "onInfo: is playing right now !");
//                    questionVideoViewIsActive = true;
//                }
//                return false;
//            }
//        });
//        questionVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                tempValue++;
//                Log.e(TAG, "doInBackground: video bitti");
//                Log.e(TAG, "onCompletion: tempValues is " + tempValue);
////                    if ((tempValue%2)!=0){
////                          getNextVideoUrl(tempValue);
////                    }else {
////                        videoView.setVisibility(View.INVISIBLE);
////                        questionFrameLayout.setVisibility(View.VISIBLE);
////                        progressBarCircleFrameLayout.setVisibility(View.VISIBLE);
////                        progressBarCircle.setVisibility(View.VISIBLE);
////                        setProgressBarCircle();
////                    }
//            }
//        });
//        return null;
//    }
//}


//     videoURL= myResponse;
//             Uri uri = Uri.parse(videoURL);
//             videoView.setVideoURI(uri);
//             DisplayMetrics displayMetrics = new DisplayMetrics();
//             getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//             ViewGroup.LayoutParams videoParams = videoView.getLayoutParams();
//             videoParams.width =displayMetrics.widthPixels;
//             videoParams.height =displayMetrics.heightPixels;
//             videoView.setLayoutParams(videoParams);
//             getNextVideoUrl(videoURL);
//             new MyAsync().execute();