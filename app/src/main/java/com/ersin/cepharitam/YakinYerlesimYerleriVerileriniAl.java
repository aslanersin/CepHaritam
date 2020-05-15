package com.ersin.cepharitam;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class YakinYerlesimYerleriVerileriniAl extends AsyncTask<Object, String, String> {

    private String googleYerlesimYeriVerisi; //url sonucu dönen veri kümesi işlemleri için kullanılacak
    private GoogleMap mMap; //Harita Objesi
    String  url; //Yakın yerlere ait işlemler için kullanıldı.

    //Asenkron olarak arka planda çalışacak işlemler bu methodda tanımlanıyor.
    @Override
    protected String doInBackground(Object... objects){
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        //Yerleşim yerine ait veriler aşağıda tanımlanan Class sayesinde alınarak ilgili değişkene (googleYerlesimYeriVerisi)
        //aktarılıyor.
        YerlesimYeriVerileriniIndir urlVerileriIndir = new YerlesimYeriVerileriniIndir();
        try {
            googleYerlesimYeriVerisi = urlVerileriIndir.urlVerisiAl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleYerlesimYeriVerisi;
    }

    //doInBackground metodunda işlemler bittikten sonra google tarafından Json tipinde gönderilen veriye ait
    //ayıklama işlemi aşağıdaki methodda yapılıyor.
    @Override
    protected void onPostExecute(String s){
        //Yakında bulunan yerlere ait tutulacak veriler için bir List(HashMap) tanımlaması yapılıyor
        List<HashMap<String, String>> yakindaBulunanYerlerListesi;

        //Dönen verileri işleyecek Class'tan bir nesne oluşturuluyor.
        VeriAyiklaClass ayikla = new VeriAyiklaClass();

        //Dönen veriler List'e atılıyor.
        yakindaBulunanYerlerListesi = ayikla.veriAyikla(s);

        //Yakında bulunan yerleri harita üzerinde göstermesi için ilgili method çağrılıyor.
        yakindaBulunanYerleriGoster(yakindaBulunanYerlerListesi);
    }

    //Yakında bulunan yerleri harita üzerinde gösteren method
    private void yakindaBulunanYerleriGoster(List<HashMap<String, String>> yakindaBulunanYerListe)
    {
        //Dönen değerler kümesindeki tüm değerler okunarak harita üzerinde gösterilmeye çalışılıyor.
        for(int i = 0; i < yakindaBulunanYerListe.size(); i++)
        {
            //Harita üzerinde işaretleme için kullanıldı
            MarkerOptions markerOptions = new MarkerOptions();

            //Aşağıdaki HashMap yapısı sayesinde değerler alınarak ilgili harita işlemleri yapılıyor.
            HashMap<String, String> haritaYerlesimYeri = yakindaBulunanYerListe.get(i);

            //Dönen veriler ilgili değişkenlere atılarak harita üzerinde gerekli
            //gösterim aşamaları tamamlanıyor.
            String yerlesimYeriAdi = haritaYerlesimYeri.get("yerlesimYeriAdi");
            String cevreBilgisi = haritaYerlesimYeri.get("cevreBilgisi");
            double enlem = Double.parseDouble( haritaYerlesimYeri.get("enlemBilgisi"));
            double boylam = Double.parseDouble( haritaYerlesimYeri.get("boylamBilgisi"));

            //Konum bilgisi ve adı harita üzerinde işaretleniyor.
            LatLng enlemboylam = new LatLng( enlem, boylam);
            markerOptions.position(enlemboylam);
            markerOptions.title(yerlesimYeriAdi + "--"+ cevreBilgisi);

            //Seçilen konumun yakınındaki yerler harita üzerinde farklı (AZURE) bir renkte gösteriliyor.
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


            //Konum Haritaya eklenip zoomlanıyor.
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(enlemboylam));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }
}
