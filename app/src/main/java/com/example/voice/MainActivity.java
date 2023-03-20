package com.example.voice;

import static android.os.SystemClock.sleep;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {


    private SpeechRecognizer mRecognizer;
    private TextView textView;
    TextView t;
    private View dalog;
    private View weather;
    private View exit;
    Context cThis;
    TextToSpeech tts;
    Intent intent;
    ImageButton mic;
    ImageView mic2;
    TextView dtext;
    EditText local;
    TextView localtext;
    URL url ;
    String e, address, iconS;
    AlertDialog.Builder Mainbuilder;
    AlertDialog alertDialog;
    LocationManager lm;
    double longitude;
    double latitude;
    String loca;
    Integer hours;
    Integer minute;
    String message;
    Integer seconds;

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dtext = findViewById(R.id.textView4);
        textView = findViewById(R.id.textView);
        localtext = findViewById(R.id.textView5);
        imageView = findViewById(R.id.imageView2);
        mic = findViewById(R.id.imageButton4);
        dalog = (View) View.inflate(MainActivity.this,R.layout.dialog,null);
        exit = (View) View.inflate(MainActivity.this,R.layout.exitdialog,null);
        weather = (View) View.inflate(MainActivity.this,R.layout.weather,null);
        t= dalog.findViewById(R.id.textView2);
        mic2 = dalog.findViewById(R.id.imageView);
        local= weather.findViewById(R.id.editTextTextPersonName);

        // RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); // 언어 설정
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);


        String cancel = "닫기";
        Mainbuilder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
        Mainbuilder.setView(dalog);
        Mainbuilder.setPositiveButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog = Mainbuilder.create();

        //음성출력 생성, 리스너 초기화
        tts =new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // 마이크 버튼설정
        mic.setOnClickListener(new View.OnClickListener() { //imageView localtext dtext
            @Override
            public void onClick(View view) {
                alertDialog.show();
                dtext.setText("");  // 초기화 작업
                local.setText("");
                imageView.setImageResource(0);
                localtext.setText("");
                textView.setText("마이크를 눌러 말해주세요");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(intent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

        // 팝업창 안의 마이크버튼 실행
        mic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dtext.setText("");  // 초기화 작업
                local.setText("");
                imageView.setImageResource(0);
                localtext.setText("");
                textView.setText("마이크를 눌러 말해주세요");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(intent);
                    }catch (SecurityException e){e.printStackTrace();}
                }

            }
        });

        String gpsProvider = LocationManager.GPS_PROVIDER;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0 );
            Toast.makeText(MainActivity.this, "GPS를 허용하지않으면 지역을 입력 해야 합니다.", Toast.LENGTH_SHORT).show();
            //위치기능 허용 X
        //    lm.removeUpdates((LocationListener) listener); 리스너끄기

        }
        else{ //허용한경우 gps리스너 실행
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, gpsLocationListener);
            Toast.makeText(MainActivity.this, "위치기능이 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
        //    lm.removeUpdates((LocationListener) listener);
        }




    }

    //GPS 리스너 설정
    final LocationListener gpsLocationListener = new LocationListener() { //위도,경도를 각각 전역 변수로 값을넣어준다
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude(); //경도
            latitude = location.getLatitude(); //위도

        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
    };



    //입력 리스너 설정
    private RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
            t.setText("음성인식 시작"+"\r\n");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            t.setText("음성인식 종료"+"\r\n");
        }

        @Override
        public void onError(int i) {
            t.setText("마이크 버튼을 눌러 다시말해주세요"+"\r\n");
        }

        @Override //음성을 rs배열에 저장하고 그저장된배열을 textView로 출력
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            textView.setText(rs[0]+"\r\n");
            FuncVoiceOrderCheck(rs[0]);
          //  mRecognizer.startListening(intent);

        }

        @Override
        public void onPartialResults(Bundle bundle) {
            t.setText("onPartialResults..........."+"\r\n"+t.getText());
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            t.setText("onEvent..........."+"\r\n"+t.getText());
        }
    };


    //입력된 음성 메세지 확인 후 동작 처리
    public void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;
        String intbox = VoiceMsg.replaceAll("[^0-9]", "");
        VoiceMsg=VoiceMsg.replace(" ","");//공백제거
        AlertDialog.Builder builder;


        if(VoiceMsg.indexOf("어플종료")>-1 || VoiceMsg.indexOf("종료")>-1) {   // 음성 종료동작
            alertDialog.dismiss();
            builder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid); //완전종료되는것
                    finish();
                }
            });
            builder.setView(exit);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            FuncVoiceOut("종료 하시겠습니까?");
        }

        if(VoiceMsg.indexOf("현재시간")>-1 || VoiceMsg.indexOf("시간")>-1) { //음성 시간출력
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm:ss");
            String getDay = simpleDateFormatDay.format(date);
            String getTime = simpleDateFormatTime.format(date);
            String getDate = getDay + "\n" + getTime;
            dtext.setText(getDate);
            FuncVoiceOut("현재시간은"+ getDate + "입니다");
            alertDialog.dismiss();
        }

        if(VoiceMsg.indexOf("현재날씨")>-1 || VoiceMsg.indexOf("날씨")>-1)
        { // 날씨출력

            alertDialog.dismiss();

            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0 );
                //위치권한이 허용이 안되있을땐 직접 지역입력
                builder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
                alertDialog.dismiss();
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                });

                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String r = local.getText().toString();
                        address = "https://api.openweathermap.org/data/2.5/weather?q="+r+"&appid=7e8998e6fa9380358e9fab0c6cecd789";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                e=aVoid();
                            }
                        }).start();
                        sleep(2500);
                        localtext.setText(r);
                        dtext.setText(e);
                        System.out.println(address);
                        Picasso.get().load(iconS).into(imageView);
                        FuncVoiceOut(r+"의 현재"+e);

                    }
                });
                builder.setTitle("현재 지역을 입력해주세요.");
                builder.setView(weather);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else {

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, gpsLocationListener);
                address = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=7e8998e6fa9380358e9fab0c6cecd789";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        e=aVoid();
                    }
                }).start();

                sleep(2500);
                localtext.setText(loca);
                dtext.setText(e);
                System.out.println(address);
                Picasso.get().load(iconS).into(imageView);
                FuncVoiceOut(loca +"의 현재"+e);


            }




        }


            if(VoiceMsg.indexOf("카카오톡")>-1 || VoiceMsg.indexOf("카톡")>-1){
                movePosition("com.kakao.talk");
                setmRecognizercancle();
                FuncVoiceOut("카카오톡을 엽니다");

        }//카카오톡 어플로 이동


            //갤러리 이동
        if(VoiceMsg.indexOf("겔러리")>-1 || VoiceMsg.indexOf("갤러리")>-1){
            movePosition("com.google.android.apps.photos");
            setmRecognizercancle();
            FuncVoiceOut("갤러리를 엽니다");

        }
        // 네이버 이동
        if(VoiceMsg.indexOf("네이버")>-1 || VoiceMsg.indexOf("내이버")>-1){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com")));
            setmRecognizercancle();
            FuncVoiceOut("네이버를 연결합니다");

        }
        //크롬이동
        if(VoiceMsg.indexOf("크롬")>-1 || VoiceMsg.indexOf("크럼")>-1){
            movePosition("com.android.chrome");
            setmRecognizercancle();
            FuncVoiceOut("크롬을 연결합니다");

        }
            //카메라 이동
        if(VoiceMsg.indexOf("카메라")>-1 || VoiceMsg.indexOf("카매라")>-1){
            movePosition("com.android.camera2");
            setmRecognizercancle();
            FuncVoiceOut("카메라를 켭니다");

        }
        //시스템 설정이동
        if(VoiceMsg.indexOf("설정")>-1 || VoiceMsg.indexOf("시스템설정")>-1){
            movePosition("com.android.settings");
            setmRecognizercancle();
            FuncVoiceOut("설정을 엽니다");

        }
        //유튜브
        if(VoiceMsg.indexOf("유투브")>-1 || VoiceMsg.indexOf("유튜브")>-1){
            movePosition("com.google.android.youtube");
            setmRecognizercancle();
            FuncVoiceOut("유튜브를 엽니다");

        }
        //유튜브뮤직
        if(VoiceMsg.indexOf("유튜브뮤직")>-1 || VoiceMsg.indexOf("유투브뮤직")>-1){
            movePosition("com.google.android.apps.youtube.music");
            setmRecognizercancle();
            FuncVoiceOut("유튜브 뮤직을 엽니다");

        }
        //캘린더이동
        if(VoiceMsg.indexOf("달력")>-1 || VoiceMsg.indexOf("캘린더")>-1|| VoiceMsg.indexOf("켈린더")>-1){
            movePosition("com.google.android.calendar");
            setmRecognizercancle();
            FuncVoiceOut("캘린더을 엽니다");

        }
        //전화
        if(VoiceMsg.indexOf("전화")>-1 || VoiceMsg.indexOf("전화걸어줘")>-1){
            alertDialog.dismiss();
            setmRecognizercancle();
            Uri number = Uri.parse("tel:"+intbox);
            FuncVoiceOut(intbox+"에 "+"전화를겁니다");
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);


        }
        //스크린샷
        if(VoiceMsg.indexOf("스크린샷")>-1 || VoiceMsg.indexOf("스크림샷")>-1|| VoiceMsg.indexOf("스크린샷찍어")>-1){

            mOnCaptureClick();
            FuncVoiceOut("스크린샷을 찍습니다");
            alertDialog.dismiss();
        }
        //알람기능
        if(VoiceMsg.indexOf("알람")>-1 || VoiceMsg.indexOf("알람설정")>-1 ){

             intbox = VoiceMsg.replaceAll("[^0-9]", "");
            Integer in = Integer.parseInt(intbox);
            if (in>100) {
                hours = in / 100;
                minute = in % 100;
            }
            else if (100>in && in>10) {
                hours = in / 10;
                minute = in % 10;
            }
            else if (10>in && in>0) {
                hours = in;
                minute = 0;
            }
            if(VoiceMsg.indexOf("오후")>-1){
                hours=hours+12;
            }

            FuncVoiceOut(hours+"시"+minute+"분 으로 알람을 설정합니다");
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, VoiceMsg)
                    .putExtra(AlarmClock.EXTRA_HOUR, hours)
                    .putExtra(AlarmClock.EXTRA_MINUTES, minute);
            startActivity(intent);

            setmRecognizercancle();


        }

        if(VoiceMsg.indexOf("와이파이")>-1 || VoiceMsg.indexOf("와이파이설정")>-1 ) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                FuncVoiceOut("와이파이가 현재 켜져있습니다");
            }
            if (!wifiManager.isWifiEnabled()) {
                FuncVoiceOut("와이파이가 현재 꺼져있습니다");

            }
            setmRecognizercancle();
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);

        }
        if(VoiceMsg.indexOf("비행기")>-1 || VoiceMsg.indexOf("비행기모드")>-1 ) {
            boolean airplanestates;
            airplanestates =isAirModeOn();
            if(airplanestates){
                FuncVoiceOut("비행기모드가 켜져있습니다");
            }
            else if(!airplanestates){
                FuncVoiceOut("비행기모드가 꺼져있습니다");
            }
            setmRecognizercancle();
            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(intent);
        }

        if(VoiceMsg.indexOf("Timer")>-1 || VoiceMsg.indexOf("타이머")>-1 ) {
            message = VoiceMsg;
            seconds = Integer.parseInt(intbox);
            String task = "초로";

            if (VoiceMsg.indexOf("분")>-1){
                seconds = seconds*60;
                task="분으로";
            }
            else if (VoiceMsg.indexOf("시간")>-1){
                task="시간으로";
                seconds = seconds*3600;
            }

            Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                    .putExtra(AlarmClock.EXTRA_LENGTH, seconds);
            //.putExtra(AlarmClock.EXTRA_SKIP_UI, true); UI를 스킾하는 코드
            FuncVoiceOut("타이머를 "+seconds+task+" 설정합니다");
            startActivity(intent);

        }


    }
    private Boolean isAirModeOn() { //비행기모드 상태값 받아오기
        Boolean isAirplaneMode;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
            isAirplaneMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        }else{
            isAirplaneMode = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        }
        return isAirplaneMode;
    }


    //스크린샷하기
    public void mOnCaptureClick(){
        //전체화면
        View rootView = getWindow().getDecorView();

        File screenShot = ScreenShot(rootView);
        if(screenShot!=null){
            //갤러리에 추가
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
        }
    }

    //갤러리에 저장
    public File ScreenShot(View view){
        view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

        Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환

        String filename = "screenshot.png";
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        view.setDrawingCacheEnabled(false);
        return file;
    }


    //이동기능 메소드
    private void movePosition(String addr){

        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(addr);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (Exception e){ //앱이 없다면 google market으로 이동
            e.printStackTrace();
            String url = "https://play.google.com/store/apps/details?id=" + addr;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
       // Intent intentmove = new Intent(Intent.ACTION_VIEW, Uri.parse(addr));
       // startActivity(intentmove);
    }



    public String aVoid(){
        String s = null;
        try {
            url = new URL(address);
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            // 변경전       InputStreamReader is = new InputStreamReader(url.openStream(),"UTF-8");

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }

            String jsonData = buffer.toString();
            JSONObject obj = new JSONObject(jsonData);
            double tempDo =0;
            //날씨 데이터 받기
            JSONArray weatherJson = obj.getJSONArray("weather");
            JSONObject weatherObj = weatherJson.getJSONObject(0);
            String weather = weatherObj.getString("description");


            String iconW = weatherObj.getString("icon");
            iconS = "https://openweathermap.org/img/wn/"+iconW+".png";


            //날씨 한국어로 변환
            switch (weather){
                case "clear sky":
                    weather ="맑음";
                    break;
                case "broken clouds":
                    weather ="부서진 구름";
                    break;
                case "few clouds":
                    weather ="약간의 구름";
                    break;
                case "scattered clouds":
                    weather ="흐트러진 구름";
                    break;
                case "overcast clouds":
                    weather ="흐린 구름";
                    break;
                case "shower rain":
                    weather ="소나기";
                    break;
                case "light rain":
                    weather ="가벼운 비";
                    break;
                case "moderate rain":
                    weather ="적당한 비";
                    break;
                case "heavy intensity rain":
                    weather ="강한 비";
                    break;
                case "very heavy rain":
                    weather ="매우 폭우";
                    break;
                case "extreme rain" :
                    weather ="폭우";
                    break;
                case "freezing rain":
                    weather ="얼어붙는 비";
                    break;
                case "light intensity shower rain":
                    weather ="광도 소나기";
                    break;
                case "heavy intensity shower rain rain":
                    weather ="강한 소나기";
                    break;
                case "ragged shower rain":
                    weather ="거친 소나기";
                    break;
                case "rain":
                    weather ="비";
                    break;
                case "thunderstorm":
                    weather ="뇌우";
                    break;
                case "thunderstorm with light rain":
                    weather ="약한 비를 동반한 뇌우";
                    break;
                case "thunderstorm with rain":
                    weather ="비를 동반한 뇌우";
                    break;
                case "thunderstorm with heavy rain":
                    weather ="폭우를 동반한 천둥번개";
                    break;
                case "light thunderstorm":
                    weather ="약한 뇌우";
                    break;
                case "heavy thunderstorm":
                    weather ="심한 뇌우";
                    break;
                case "ragged thunderstorm":
                    weather ="들쭉날쭉한 뇌우";
                    break;
                case "thunderstorm with light drizzle":
                    weather ="약한 이슬비가 내리는 뇌우";
                    break;
                case "thunderstorm with drizzle":
                    weather ="이슬비를 동반한 뇌우";
                    break;
                case "thunderstorm with heavy drizzle":
                    weather ="강한 비를 동반한 뇌우";
                    break;
                case "snow":
                    weather ="눈";
                    break;
                case "light snow":
                    weather ="적은 양의 눈";
                    break;
                case "Heavy snow":
                    weather ="폭설";
                    break;
                case "Light shower sleet":
                    weather ="가벼운 샤워 진눈깨비";
                    break;
                case "Sleet":
                    weather ="진눈깨비";
                    break;
                case "Shower sleet":
                    weather ="샤워 진눈깨비";
                    break;
                case "Rain and snow":
                    weather ="비와 눈";
                    break;
                case "Light rain and snow":
                    weather ="가벼운 비와 눈";
                    break;
                case "Light shower snow":
                    weather ="가벼운 소나기 눈";
                    break;
                case "Shower snow":
                    weather ="소나기 눈";
                    break;
                case "Heavy shower snow":
                    weather ="폭우 눈";
                    break;
                case "mist":
                    weather ="안개";
                    break;
                case "Smoke":
                    weather ="연기";
                    break;
                case "Haze":
                    weather ="안개";
                    break;
                case "sand/ dust whirls":
                    weather ="모래/먼지 소용돌이";
                    break;
                case "fog":
                    weather ="안개";
                    break;
                case "sand":
                    weather ="모래";
                    break;
                case "dust":
                    weather ="먼지";
                    break;
                case "volcanic ash":
                    weather ="화산재";
                    break;
                case "squalls":
                    weather ="돌풍";
                    break;
                case "tornado":
                    weather ="폭풍";
                    break;
                case "light intensity drizzle":
                    weather ="광도 이슬비";
                    break;
                case "drizzle":
                    weather ="이슬비";
                    break;
                case "heavy intensity drizzle":
                    weather ="강한 이슬비";
                    break;
                case "light intensity drizzle rain":
                    weather ="광도 이슬비 비";
                    break;
                case "drizzle rain":
                    weather ="이슬비";
                    break;
                case "heavy intensity drizzle rain":
                    weather ="강한 비";
                    break;
                case "shower rain and drizzle":
                    weather ="비와 이슬비";
                    break;
                case "heavy shower rain and drizzle":
                    weather ="강한 소나기";
                    break;
                case "shower drizzle":
                    weather ="소나기 이슬비";
                    break;




                default:
                    break;

            }

            //기온 데이터 받기
            JSONObject tempK = new JSONObject(obj.getString("main"));

            //기온 받고 켈빈 온도를 섭씨 온도로 변경 °C
            tempDo = (Math.round((tempK.getDouble("temp")-273.15)*100)/100.0);

            //지역 데이터받기
            String name = obj.getString("name");
            String lname =locationKoean(name);
            loca=lname;




            s= "날씨  "+weather+"\n"+"온도  "+tempDo+"°C";

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return s;

    }




    //음성 메세지 출력용
    private void FuncVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null);
        //어플이 종료할때는 완전히 제거

    }
    //이동을 했는데 음성인식 어플이 종료되지 않아 계속 실행되는 경우를 막기위해 어플 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }
    //지역 한국어변환
    private String locationKoean(String name){
        String e= "";
        switch (name){
            case "Seoul":
                name = "서울";
                break;
            case "Busan":
                name = "부산";
                break;
            case "Daegu":
                name = "대구";
                break;
            case "Incheon":
                name = "인천";
                break;
            case "Gwangju":
                name = "광주";
                break;
            case "Daejeon":
                name = "대전";
                break;
            case "Ulsan":
                name = "울산";
                break;
            case "Sejong":
                name = "세종";
                break;
            case "Goyang":
                name = "고양";
                break;
            case "Gwacheon":
                name = "과천";
                break;
            case "Gwangmyeong":
                name = "광명";
                break;
            case "Guri":
                name = "구리";
                break;
            case "Gunpo":
                name = "군포";
                break;
            case "Gimpo":
                name = "김포";
                break;
            case "Namyangju":
                name = "남양주";
                break;
            case "Dongducheon":
                name = "동두천";
                break;
            case "Bucheon":
                name = "부천";
                break;
            case "Seongnam":
                name = "성남";
                break;
            case "Suwon":
                name = "수원";
                break;
            case "Ansan":
                name = "안산";
                break;
            case "Siheung":
                name = "시흥";
                break;
            case "Anseong":
                name = "안성";
                break;
            case "Anyang":
                name = "안양";
                break;
            case "Yangju":
                name = "양주";
                break;
            case "Osan":
                name = "오산";
                break;
            case "Yongin":
                name = "용인";
                break;
            case "Uiwang":
                name = "의왕";
                break;
            case "Uijeongbu":
                name = "의정부";
                break;
            case "Icheon":
                name = "이천";
                break;
            case "Paju":
                name = "파주";
                break;
            case "Pyeongtaek":
                name = "평택";
                break;
            case "Pocheon":
                name = "포천";
                break;
            case "Hanam":
                name = "하남";
                break;
            case "Hwaseong":
                name = "화성";
                break;
            case "Gangneung":
                name = "강릉";
                break;
            case "Donghae":
                name = "동해";
                break;
            case "Samcheok":
                name = "삼척";
                break;
            case "Wonju":
                name = "원주";
                break;
            case "Sokcho":
                name = "속초";
                break;
            case "Chuncheon":
                name = "춘천";
                break;
            case "Taebaek":
                name = "태백";
                break;
            case "Jecheon":
                name = "제천";
                break;
            case "Cheongju":
                name = "청주";
                break;
            case "Chungju":
                name = "충주";
                break;
            case "Gyeryong":
                name = "계룡";
                break;
            case "Gongju":
                name = "공주";
                break;
            case "Nonsan":
                name = "논산";
                break;
            case "Dangjin":
                name = "당진";
                break;
            case "Boryeong":
                name = "보령";
                break;
            case "Seosan":
                name = "서산";
                break;
            case "Asan":
                name = "아산";
                break;
            case "Cheonan":
                name = "천안";
                break;
            case "Gyeongsan":
                name = "경산";
                break;
            case "Gyeongju":
                name = "경주";
                break;
            case "Gumi":
                name = "구미";
                break;
            case "Gimcheon":
                name = "김천";
                break;
            case "Mungyeong":
                name = "문경";
                break;
            case "Sangju":
                name = "상주";
                break;
            case "Andong":
                name = "안동";
                break;
            case "Yeongju":
                name = "영주";
                break;
            case "Yeongcheon":
                name = "영천";
                break;
            case "Pohang":
                name = "포항";
                break;
            case "Geoje":
                name = "거제";
                break;
            case "Gimhae":
                name = "김해";
                break;
            case "Miryang":
                name = "밀양";
                break;
            case "Sacheon":
                name = "사천";
                break;
            case "Yangsan":
                name = "양산";
                break;
            case "Jinju":
                name = "진주";
                break;
            case "Changwon":
                name = "창원";
                break;
            case "Tongyeong":
                name = "통영";
                break;
            case "Gunsan":
                name = "군산";
                break;
            case "Gimje":
                name = "김제";
                break;
            case "Namwon":
                name = "남원";
                break;
            case "Jeongeup":
                name = "정읍";
                break;
            case "Iksan":
                name = "익산";
                break;
            case "Jeonju":
                name = "전주";
                break;
            case "Gwangyang":
                name = "광양";
                break;
            case "Naju":
                name = "나주";
                break;
            case "Mokpo":
                name = "목포";
                break;
            case "Suncheon":
                name = "순천";
                break;
            case "Yeosu":
                name = "여수";
                break;
            case "Seogwipo":
                name = "서귀포";
                break;
            case "Jeju":
                name = "제주";
                break;

            default:break;

        }
        e=name;
        return e;
    }
    private void setmRecognizercancle(){ //음성읽기 기능을 끈다
        mRecognizer.destroy();
        mRecognizer.cancel();
        mRecognizer=null;
    }

}



