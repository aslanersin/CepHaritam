package com.ersin.cepharitam;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class VeriAyiklaClass {

    //Google Places Api'nin Json formatında göndermiş olduğu verinin ayıklanıp işlenmesi için
    //aşağıdaki method tanımlanıyor.
    private HashMap<String, String> yerlesimYeriBilgisiAl(JSONObject googleJsonKonumVeri)
    {
        //Bir HashMap yapısı oluşturularak dize değerleri aktarılması için kullanıldı.
        HashMap<String, String> googleHaritaVeri = new HashMap<>();

        //Ayıklanacak veriye ait ilgili değişkenlerin kullanılması için
        //aşağıdaki değişkenler tanımlanıyor.
        String yerlesimBilgisi = "--VeriYok--";
        String cevreBilgisi= "--VeriYok--";
        String enlemBilgisi= "";
        String boylamBilgisi="";

        try {
            //Google tarafından gönderilen ilgili veriler null değilse ilgili atama işlemleri yapılıyor.
            //Bu atama işlemlerinde kullanılacak değişkenler HashMap yapısında kullanılacak.
            if (!googleJsonKonumVeri.isNull("name")) {
                yerlesimBilgisi = googleJsonKonumVeri.getString("name");
            }
            if (!googleJsonKonumVeri.isNull("vicinity")) {
                cevreBilgisi = googleJsonKonumVeri.getString("vicinity");
            }

            //Json formatında dönen veriden enlem ve boylam bilgileri alınıyor.
            enlemBilgisi = googleJsonKonumVeri.getJSONObject("geometry").getJSONObject("location").getString("lat");
            boylamBilgisi = googleJsonKonumVeri.getJSONObject("geometry").getJSONObject("location").getString("lng");

            //Harita üzerinde gösterim işlemlerinde kullanılacak veriler HashMap'deki ilgili değişkenlere aktarılıyor.
            googleHaritaVeri.put("yerlesimYeriAdi", yerlesimBilgisi);
            googleHaritaVeri.put("cevreBilgisi", cevreBilgisi);
            googleHaritaVeri.put("enlemBilgisi", enlemBilgisi);
            googleHaritaVeri.put("boylamBilgisi", boylamBilgisi);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googleHaritaVeri;

    }

    //Yakındaki yerlere ait konumlara ilişkin verilerin alınması için
    //aşağıdaki method kullanıldı.
    private List<HashMap<String, String>> konumlarBilgisiAl(JSONArray jsonDizi)
    {
        //Dönen  veri değerinin uzunluğu alınıyor.
        int sayac = jsonDizi.length();

        //veri aktarımlarında kullanılacak ilgili değişken atamaları yapılıyor.
        List<HashMap<String, String>> yerlesimYeriListesi = new ArrayList<>();
        HashMap<String, String> yerlesimKonumHarita = null;

        //Döngü sayesinde JSON tipinde dönen tüm değerler
        //ilgili değişkenlere aktarılıyor.
        for(int i = 0; i<sayac;i++)
        {
            try {
                yerlesimKonumHarita = yerlesimYeriBilgisiAl((JSONObject) jsonDizi.get(i));
                yerlesimYeriListesi.add(yerlesimKonumHarita);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return yerlesimYeriListesi;
    }


    //Veri Ayıklama işlemi için aşağıdaki method tanımlaması yapılıyor.
    public List<HashMap<String, String>> veriAyikla(String jsonVeri)
    {
        //JSON tipindeki verilerin işlemmesi için aşağıdaki değişkenler tanımlanıyor.
        JSONArray jsonDizi = null;
        JSONObject jsonNesne;

        try {
            jsonNesne = new JSONObject(jsonVeri);
            //JSON tipinde dönen tüm veriler ilgili değişkene (jsonDizi) aktarılıyor.
            jsonDizi = jsonNesne.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return konumlarBilgisiAl(jsonDizi);
    }
}
