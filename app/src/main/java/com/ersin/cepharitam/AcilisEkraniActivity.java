package com.ersin.cepharitam;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Thread.sleep;

public class AcilisEkraniActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Açılış Ekranı 1,5 sn gösterildikten sonra Ana Menüye geçiliyor.
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Ana Menüye geçmek için gereken Intent tanımlanıyor.
        Intent intent = new Intent(AcilisEkraniActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}