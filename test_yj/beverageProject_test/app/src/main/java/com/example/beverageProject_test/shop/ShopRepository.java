package com.example.beverageProject_test.shop;

public interface ShopRepository {
    //음료 위치 저장
    void save(Beverage beverage);
    //편의점 음료위치 반환
    String findBeverageLocation(String beverageName);
}
