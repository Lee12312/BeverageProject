package com.example.beverageProject_test;

//커밋용
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.beverageProject_test.client.ClientService;
import com.example.beverageProject_test.shop.*;
import com.example.beverageProject_test.R;

import android.util.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //컨트롤러
    private static final AppConfig appConfig = new AppConfig();
    //고객 서비스, 음성 서비스, 저장소 변수
    private static final ClientService clientService = appConfig.clientService();
    private static final com.example.beverageProject_test.shop.ShopService shopService = appConfig.shopService();
    private static final com.example.beverageProject_test.shop.ShopRepository shopRepository = appConfig.shopRepository();
    //TTS 변수 선언
    private TextToSpeech tts;
    //STT를 사용할 intent 와 SpeechRecognizer 초기화
    private SpeechRecognizer sRecognizer;
    private Intent intent;
    //음성인식 결과를 담을 변수
    private String result;
    //퍼미션 체크를 위한 변수
    private final int PERMISSION = 1;

    TextView st;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        st = findViewById(R.id.Text_say);

        //음성 버튼
        ImageButton voiceButton = findViewById(R.id.voiceButton);

        //DB 생성 전 테스트용 객체생성
        com.example.beverageProject_test.shop.Beverage testBeverage = new com.example.beverageProject_test.shop.Beverage("1-1", "콜라", com.example.beverageProject_test.shop.BottleType.CAN);
        com.example.beverageProject_test.shop.Beverage testBeverage2 = new com.example.beverageProject_test.shop.Beverage("1-2", "환타", com.example.beverageProject_test.shop.BottleType.CAN);
        com.example.beverageProject_test.shop.Beverage testBeverage3 = new com.example.beverageProject_test.shop.Beverage("1-3", "사이다", com.example.beverageProject_test.shop.BottleType.CAN);
        //테스트용 객체 저장소에 넣기
        shopRepository.save(testBeverage);
        shopRepository.save(testBeverage2);
        shopRepository.save(testBeverage3);

        //TTS 환경설정
        checkTTS();

        //버튼 클릭시 음성 안내 서비스 호출
        voiceButton.setOnClickListener(view -> {
            //음성안내 시작
            shopService.voiceGuidance(tts);
            //음성인식 시작
            startSTT();

        });

    }

    //음성인식 환경설정
    private void startSTT(){
        //퍼미션 체크했는데 오류뜸..
        //STT 퍼미션 체크
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
        //사용자에게 음성을 요구하고 음성 인식기를 통해 전송 시작
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //음성 인식을 위한 음성 인식기의 의도에 사용되는 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //음성을 번역할 언어 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        //새 SpeechRecognizer 을 만드는 팩토리 메서드
        sRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //모든 콜백을 수신하는 리스너 설정
        sRecognizer.setRecognitionListener(listener);
        //음성인식 시작
        sRecognizer.startListening(intent);
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getBaseContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            //사용자가 말한 음료수 저장소에서 찾아오기
            String beverageLocation = clientService.sayBeverageName(result);
            if(beverageLocation != null){
                Toast.makeText(getApplicationContext(),beverageLocation,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {

            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for(int i = 0; i < matches.size() ; i++){
                result = matches.get(i).substring(11);
                // 음료를 말씀해 주세요도 저장되길래 뺐음

                Log.e("test1", "onResults text : " + result);
                st.setText(result);
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private void checkTTS(){
        //TTS를 생성하고 OnInitListener로 초기화
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    //언어 선택
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

    }

    // 앱 종료시
    @Override
    protected void onDestroy() {

        // Don't forget to shutdown!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();

    }

}