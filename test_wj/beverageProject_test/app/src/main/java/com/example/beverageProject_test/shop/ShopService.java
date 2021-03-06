package com.example.beverageProject_test.shop;

import android.speech.tts.TextToSpeech;

public interface ShopService {
    //고객이 버튼 클릭 시 음성안내
    void voiceGuidance(TextToSpeech tts);
    //해당 음료가 있을 시 반환
    String findBeverageByShop(Beverage beverage);
}
