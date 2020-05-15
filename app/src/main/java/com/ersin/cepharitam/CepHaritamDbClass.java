package com.ersin.cepharitam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CepHaritamDbClass extends SQLiteOpenHelper {

    //Database işlemlerinde kullanılacak değişken tanımlamaları yapılıyor.
    private final static String DATABASE_ADI="cepharitamdb";
    private final static int DATABASE_VERSIYON=3;
    private static CepHaritamDbClass instance;
    //Eş zamanlı olarak kullanılacak değişken tanımlaması yapılıyor. (Db işlemlerinde kullanılacak)
    private static final AtomicInteger dbSayac = new AtomicInteger();


    private CepHaritamDbClass(final Context context){
        //Database Classı için gerekli olan yapılandırma yapılıyor
        super(context,DATABASE_ADI,null,DATABASE_VERSIYON);
    }

    //Database işlemleri eş zamanlı senkronize edebilmek için gerekli method tanımlaması yapılıyor.
    public static synchronized CepHaritamDbClass getInstance(final Context c) {
        //Instance'ın durumuna göre kendisini çağıran sınıfa ait gerekli instance işlemleri yapılıyor.
        if (instance == null) {
            instance = new CepHaritamDbClass(c.getApplicationContext());
        }
        dbSayac.incrementAndGet();
        return instance;
    }

    @Override
    public void close() {
        //Database'e ait kapatılma işlemleri yapılıyor.
        if (dbSayac.decrementAndGet() == 0) {
            super.close();
        }
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
       //Database oluşturuluyor.
        String sqlCumlesi="CREATE TABLE IF NOT EXISTS "+DATABASE_ADI+" " +
                "(kayit_id INTEGER PRIMARY KEY AUTOINCREMENT ,konumadi VARCHAR, enlem VARCHAR, " +
                "boylam VARCHAR, eklemezamani INTEGER) ";
        //Enlem ve boylam konumlarının double tipinde hassasiyet olduğu için tip dönüşümlerinde veri kaybı
        //yaşanmaması için Enlem ve Boylam Varchar olarak tanımlandı.

        //SQLite'da date tipinde bir değişken yapısı olmadığı için eklemezamani sütunu INTEGER olarak tanımlandı.
        //Burdaki zaman mantığı Unix'in 01.01.1970 tarihinden itibaren sayılmaya başlanan
        //zaman mimarisinden oluşturuldu.

        db.execSQL(sqlCumlesi);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Burda şu an için herhangi bir DB version güncellemesi olmayacağı için
        //bir işlem yapılmadı
    }



    //Database'e istenilen konumu eklemeyi sağlayan method tanımlaması yapılıyor.
    public void konumEkle(String konumadi, String enlem, String boylam,  long eklemezamani) {

        //Database işlemleri başlatılıyor.
        getWritableDatabase().beginTransaction();
        try {
                //Database insert işlemi için gönderilecek veriler tanımlanıyor.
                ContentValues degerlerKumesi = new ContentValues();
                degerlerKumesi.put("konumadi", konumadi);

                        //Kayıt edilecek değerlerin eklenmesi yapılıyor.
                degerlerKumesi.put("enlem", enlem);
                degerlerKumesi.put("boylam", boylam);

                degerlerKumesi.put("eklemezamani", eklemezamani);

                //veriler database gönderiliyor.
                getWritableDatabase().insert(DATABASE_ADI, null, degerlerKumesi);
                getWritableDatabase().setTransactionSuccessful();
            }
        catch(Exception ex) {
            ex.printStackTrace();
        }
         finally {
            getWritableDatabase().endTransaction();
        }
    }


    //SQLite'da date tipinde bir değişken yapısı olmadığı için zaman bilgisi integer olarak tutuldu.
    //Aşağıdaki methodda Unix mimarisinden türetilmiş olan ve 01.01.1970 tarihi başlangıç olarak kabul edilen
    //bir yapı kullanılıyor.Bu method ile aktif zaman bilgisi
    //milisaniye cinsinden bir değere dönüştürülüyor.

    public long aktifZamaniAl() {
        Calendar takvim = Calendar.getInstance();
        takvim.setTimeInMillis(System.currentTimeMillis());
        return takvim.getTimeInMillis();
    }

    //milisaniye olarak gönderilen veriyi normal zaman formatına çevirmek için
    //bu method kullanılıyor.
    public String tarihSaateDonustur (long zaman){
        // milisaniye bilgisi ilgili değişkene Set edilerek tip dönüşüm işlemleri başlatılıyor.
        Date tarihSaat = new java.util.Date(zaman);

        //tarih ve saat bilgisi gün-ay-yıl formatına dönüştürülüyor.
        SimpleDateFormat tarihSaatFormat = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        tarihSaatFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+3"));

        String zamanBilgisi=tarihSaatFormat.format(tarihSaat);
        return zamanBilgisi;

    }


    //Kişinin eklemiş olduğu konum bilgilerini getirmek için aşağıdaki method tanımlandı.
    public  ArrayList<HashMap<String, String>> konumBilgileriniListele()  {

        //Kayıtların Tutulacağı ArrayList Tanımlanıyor.
        ArrayList<HashMap<String, String>> kayitListesi = new ArrayList<>();

        //En son eklenen kayıt başta olmak üzere tüm kayıtlar getiriliyor.
        Cursor kayitSeti = getReadableDatabase()
                .query(DATABASE_ADI, new String[]{"kayit_id","konumadi","enlem","boylam","eklemezamani"}, null,
        null, null, null,"eklemezamani DESC");

        //Döngü ile tüm veriler diziye aktarılıyor.
        while (kayitSeti.moveToNext()){
            HashMap<String,String> kayitAnahtarDeger = new HashMap<>();
            kayitAnahtarDeger.put("kayit_id",kayitSeti.getString(kayitSeti.getColumnIndex("kayit_id")));
            kayitAnahtarDeger.put("konumadi",kayitSeti.getString(kayitSeti.getColumnIndex("konumadi")));
            kayitAnahtarDeger.put("enlem",kayitSeti.getString(kayitSeti.getColumnIndex("enlem")));
            kayitAnahtarDeger.put("boylam",kayitSeti.getString(kayitSeti.getColumnIndex("boylam")));
            kayitAnahtarDeger.put("eklemezamani",tarihSaateDonustur(kayitSeti.getLong(kayitSeti.getColumnIndex("eklemezamani"))));

            kayitListesi.add(kayitAnahtarDeger);
        }
        kayitSeti.close();

        return kayitListesi;

    }

    //Eklenmiş olan konum bilgisi bu method yardımıyla veri tabanından siliniyor.
    public void konumBilgisiSil(String kayitId) {
        getWritableDatabase().delete(DATABASE_ADI, "kayit_id= ?", new String[]{kayitId});
    }


}

