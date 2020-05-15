package com.ersin.cepharitam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KonumYakinYerler extends FragmentActivity implements OnMapReadyCallback {


    EditText konumAraText;//Konum aramak için kullanıldı
    String url="";//Yakın yerlere ait yapılacak işlem ile ilgili tanımlamalar için kullanıldı.

    //Yakın yerlere ait yerleşim tiplerinde verilerde kullanılmak için tanımlandı.
    Object veriTransferi[] = new Object[2];


    int yerlesimYeriKapsami=5000; //5 Km'lik bir kapsama alanında seçilen konuma ait yerleşim yeri bilgileri gelecek.

    private GoogleMap mMap; //Harita için tanımlandı

    private LocationManager locationManager; //Harita üzerindeki yerleri yönetmek için kullanıldı
    private LocationListener locationListener; //Harita ile ilgili konum bilgisine ait değerler için kullanıldı.
    private LatLng konumum; //Aktif Enlem boylam bilgisi için kullanıldı.

    private LatLng yakindabulunankonum; //Mevcut konum üzerindeki Enlem boylam bilgisi için kullanıldı.
    private LatLng konumumYakinYerler; // Yakın yerlere ait uzaklık hesaplamada kullanıldı.


    //Adres bilgileri için kullanılacak değişkenler tanımlanıyor.
    private String anaAdres;
    private String detayAdres;

    private String mesajVeri="";//Kullanıcıya yürüme mesafesi ile ilgi bilgi vermek için kullanıldı.

    //Yakın Yerlere ait konumları Tutmak için kullanılıyor.
    ArrayList yakinkonumNoktalari= new ArrayList();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konumyakinyerlerigoster);

        //Haritanın gösterileceği fragment ile ilgili ayarlamalar yapılıyor.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.harita_yakinyerler);
        mapFragment.getMapAsync(this);


        //Layout üzerindeki arama alanına erişim sağlanıyor.
        konumAraText=(EditText)findViewById(R.id.editText_AramaOner);

        //Google'a ait Places Api'yi kullanmak için gerekli ayarlamalar yapılıyor.
        Places.initialize(getApplicationContext(),"AIzaSyA1SBtf5Dzq13llj_4i7VJpi5EjIMcOXKg");

        //Arama alanı focus olmayacak hale getiriliyor.
        konumAraText.setFocusable(false);

        //Arama alanına dokunulduğunda devreye girecek event tanımlaması yapılıyor
        konumAraText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Adres bilgileri(enlem ve boylam) alınması için gerekli işlemler yapılıyor.
                List<Place.Field> fiedlList= Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG, Place.Field.NAME);

                //Otomatik tamamlamanın başlaması için gerekli intent oluşturulup başlatılıyor.
                Intent intent=new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fiedlList).build(KonumYakinYerler.this);
                startActivityForResult(intent,2); //Burdaki request code yazılımcı tarafından belirlenebiliyor.

            }
        });

        //Oteller tuşuna erişim sağlanıp, click event'ı tanımlanıyor.
        Button btnOtel=(Button)findViewById(R.id.button_Otel);
        btnOtel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Seçilen yerleşim yerine ait yakınındaki otellerin gösterilmesini sağlayan
                //method çağrılıyor.
                yakinYerleriGetirveGoster("hotel");

            }
        });

        //Restorantlar tuşuna erişim sağlanıp, click event'ı tanımlanıyor.
        Button btnRestorant=(Button)findViewById(R.id.button_Restorant);
        btnRestorant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Seçilen yerleşim yerine ait yakınındaki Restorantların gösterilmesini sağlayan
                //method çağrılıyor.
                yakinYerleriGetirveGoster("restaurant");
            }
        });

        //Hastaneler tuşuna erişim sağlanıp, click event'ı tanımlanıyor.
        Button btnHastane=(Button)findViewById(R.id.button_Hastane);
        btnHastane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Seçilen yerleşim yerine ait yakınındaki Hastanelerin gösterilmesini sağlayan
                //method çağrılıyor.
                yakinYerleriGetirveGoster("hospital");

            }
        });

        //ATM'ler tuşuna erişim sağlanıp, click event'ı tanımlanıyor.
        Button btnATM=(Button)findViewById(R.id.button_ATM);
        btnATM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Seçilen yerleşim yerine ait yakınındaki Banka ATM'lerinin gösterilmesini sağlayan
                //method çağrılıyor.
                yakinYerleriGetirveGoster("atm");

            }
        });


    }

    //Seçilen Konumun Yakınındaki yerleri gösterebilmek için Google'ın PLACE API'sini kullanmak gerekiyor.
    //Place Api'yi kullanırken talep edilen url bilgisi, aşağıdaki method kullanılarak tanımlanıyor.
    private String haritaVeriLinkOlustur(double enlem , double boylam , String yakindakiYerler)
    {

        //Bir StringBuilder nesnesi oluşturularak söz konusu veri isteme url'sine ait
        //değer atamaları gerçekleştiriliyor.
        StringBuilder haritaGoogleLink = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

        //Url için gerekli değer atamaları gerçekleştiriliyor.
        haritaGoogleLink.append("location="+enlem+","+boylam);
        haritaGoogleLink.append("&radius="+yerlesimYeriKapsami);
        haritaGoogleLink.append("&type="+yakindakiYerler);
        haritaGoogleLink.append("&sensor=false");
        haritaGoogleLink.append("&language=tr-TR");
        haritaGoogleLink.append("&key=AIzaSyA1SBtf5Dzq13llj_4i7VJpi5EjIMcOXKg");


        return haritaGoogleLink.toString();
    }



    //Kullanıcı haritaya erişim izinleri verince burdaki kodlar devreye giriyor.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Talep başarılı olursa veriler alınıyor.
        //Ayrıca if koşulundaki "requestCode==2" bölümü  yukarıda yazılımcı tarafından belirlenen kod kısmına ait.
        if (requestCode==2&&resultCode==RESULT_OK){
            Place place= Autocomplete.getPlaceFromIntent(data);

            //Harita üzerindeki işaretlenen konumlar temizleniyor.
            mMap.clear();

            //Adres bilgisi alınıyor.
            anaAdres=place.getAddress();

            //Adres bilgisinin İsmi alınıyor.
            detayAdres=place.getName();

            //Adrese ait Enlem ve boylam bilgisi alınıyor.
            LatLng searchResultLatLng= place.getLatLng();

            //Seçilen yer bilgisi db işlemlerinde de kullanılması için tanımlandı.
            konumum=place.getLatLng();

            //Seçilen yer bilgisi harita üzerinde method kullanılarak konumlandırılıyor.
            konumuBulveZoomla(place.getLatLng(),16,anaAdres);
            Toast.makeText(getApplicationContext(),place.getName()+"/"+place.getAddress(),Toast.LENGTH_LONG).show();
        }else if(resultCode== AutocompleteActivity.RESULT_ERROR){
            //Oluşan hatanın bilgileri alınıyor
            Status hataAciklama= Autocomplete.getStatusFromIntent(data);

            //Hata mesajı ekrana yazdırılıyor.
            Toast.makeText(getApplicationContext(),hataAciklama.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }

    }


    //Google'a ait harita key'i(Androidmanifest ve google_maps_api'de tanımlanan) girildikten sonra,
    //harita hazır olunca yapılacak işlemler bu methodda yazılıyor.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Mevcut konum bilgisini almak için gerekli tanımlamalar yapılıyor.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {


            @Override
            public void onLocationChanged(Location location) {
                //Arka planda bu hizmet çok fazla pil tüketimine neden olduğu için
                //Kullanıcının belli aralıklarla konumunun alınması aşağıdaki kodlarla sağlanmıştır.

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

        //Cep telefonunda Android 6.0 ve üstü bir sürüm mevcutsa
        //konum bilgisine erişmek için izin istenmesi işlemleri yapılıyor.
        if (Build.VERSION.SDK_INT>=23){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},2); //Burdaki request code yazılımcı tarafından belirlenebiliyor.
            }else{

                //Konumun alınması için 10 sn bekleniliyor.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,locationListener);

                //* Son bilinen konuma göre işlem yapılıyor
                Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (bilinenSonKonum!=null) {
                    //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                    bilinenSonKonumuAl(bilinenSonKonum);
                }

            }
        }else{
            //* Son bilinen konuma göre işlem yapılıyor
            Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (bilinenSonKonum!=null) {
                //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                bilinenSonKonumuAl(bilinenSonKonum);
            }


        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Mevcut Konum bilgisi var olan Array'e ekleniyor.
                yakinkonumNoktalari.add(yakindabulunankonum);

                //Seçilen konum bilgisine ait enlem ve boylam bilgisi alınıyor.
                double enlem = marker.getPosition().latitude;
                double boylam = marker.getPosition().longitude;

                //Seçilen konum bilgisi ekleniyor.
                LatLng hedefSecilenKonum=new LatLng(enlem,boylam);
                yakinkonumNoktalari.add(hedefSecilenKonum);

                //Google Directions API'a gerekli url bilgilerini göndermek için parametre oluşturma
                //işlemleri yapılıyor.
                String veriUrl = mesafeVerileriniGonder(konumumYakinYerler, hedefSecilenKonum);

                //Google Directions API'ye veri gönderilme işlemi gerçekleştirilirken
                //Herhangi bir aksamaya neden vermemek için Asenkron olarak tanımlanan Class kullanılıyor.
                verileriIsleveAl haritaVeriIsle = new verileriIsleveAl();

                //Google Directions API'ye gerekli veri kümesi gönderilerek dönen sonuç JSON formatında geri alınıp ayıklanacak
                haritaVeriIsle.execute(veriUrl);
                return false;
            }
        });



    }

    //Kullanıcı izinleri verince burdaki kodlar devreye giriyor.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Verilen izin sonucu her hangi bir değer döndüyse
        if (grantResults.length>0){
            // requestCode'u yazılımcı tarafından set edilebiliyor
            if (requestCode==2){
                //İznin verilip verilmediği kontrol ediliyor.
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)                //Konumun alınması için 10 sn bekleniliyor.
                    //Konumun alınması için 10 sn bekleniliyor.
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,locationListener);

                //* Son bilinen konuma göre işlem yapılıyor
                Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (bilinenSonKonum!=null) {
                    //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                    bilinenSonKonumuAl(bilinenSonKonum);
                }

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    //Harita üzerinde konumlandırma yapmak için method tanımlaması yapılıyor.
    private void konumuBulveZoomla(LatLng konumEnlemBoylam, float zoom, String title){

        //Konum bilgisine göre harita zoomlanıyor.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konumEnlemBoylam, zoom));

        //Harita üzerinde seçilen yer işaretleniyor.
        MarkerOptions options = new MarkerOptions()
                .position(konumEnlemBoylam)
                .title(title);
        mMap.addMarker(options);

    }

    //Bilinen Son Konum bilgisini parametre alarak harita üzerinde gösterilmesi için
    //aşağıdaki method tanımlandı.
    private void bilinenSonKonumuAl(Location enlemboylam){

        //En son bilinen konum bilgisi telefondan çekilip harita üzerinde gösteriliyor.
        LatLng bilinenSonKonum=new LatLng(enlemboylam.getLatitude(),enlemboylam.getLongitude());

        //mevcut konumun yakınındaki yerleşim yeri bilgilerini getirmek için kullanılıyor.
        yakindabulunankonum=bilinenSonKonum;

        //Mevcut konum bilgisi işaretlenip zoomlanıyor.
        mMap.addMarker(new MarkerOptions().title("Mevcut Konumunuz").position(bilinenSonKonum));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bilinenSonKonum,17));

    }

    //Seçilecek olan yakın yer türüne göre (Otel,Restorant, Hastane vb.) harita üzerinde
    //yakın yerleri getiren method tanımlaması yapılıyor.
    private void yakinYerleriGetirveGoster(String yakinYerTuru){

        //harita temizleniyor.
        mMap.clear();

        //Kişi bir yer seçtiyse oranın yakınındaki yerleşim yeri bilgisi geliyor.
        if (konumum!=null) {
            url = haritaVeriLinkOlustur(konumum.latitude, konumum.longitude, yakinYerTuru);

            //Mevcut konuma olan yürüme mesafısını hesaplamada kullanılıyor
            konumumYakinYerler=new LatLng(konumum.latitude, konumum.longitude);
        }else{
            //Kişi bir yer seçmemişse şu an mevcut konumunun yakınındaki yerler getiriliyor.
            url = haritaVeriLinkOlustur(yakindabulunankonum.latitude, yakindabulunankonum.longitude, yakinYerTuru);

            //Mevcut konuma olan yürüme mesafısını hesaplamada kullanılıyor
            konumumYakinYerler=new LatLng(yakindabulunankonum.latitude, yakindabulunankonum.longitude);
        }

        //haritanın yakınındaki yerlerin getirilmesi için kullanılan dizi değerlerine atama yapılıyor.
        veriTransferi[0] = mMap;
        veriTransferi[1] = url;

        //yerlesim yeri verileri üzerinde işlem yapabilmek için tanımlandı
        YakinYerlesimYerleriVerileriniAl yerlesimYeriVeriAl = new YakinYerlesimYerleriVerileriniAl();
        yerlesimYeriVeriAl.execute(veriTransferi);

        Toast.makeText(KonumYakinYerler.this,
                "Seçmiş olduğunuz yere ait yakın yerleşim yerlerinin listelenmesi tamamlandı.", Toast.LENGTH_SHORT).show();

        //Kişi bir yer seçtiyse oranın yakınındaki yerleşim yeri bilgisi geliyor ve tekrar seçmiş olduğu
        //yer zoomlanıyor
        if (konumum!=null){
            //yakındaki yerler gösterildikten sonra ana konum tekrar işaretleniyor.
            konumuBulveZoomla(konumum,15,"Seçtiğiniz Konum");
        }else{
            //Kişi bir yer seçmemişse mevcut konumunun yakınındaki yerler getirilip mevcut
            //konum tekrar zoomlanıyor.
            konumuBulveZoomla(yakindabulunankonum,15,"Seçtiğiniz Konum");
        }
    }


    //Asenkron olarak çalışarak arka planda verilerin hazırlanarak gönderilmesini sağlayan
    //işlem sonunda dönen veriyi ayıklayarak işleyen Class tanımı yapılıyor.
    private class verileriIsleveAl extends AsyncTask<String, Void, String> {

        //Asenkron olarak arka planda çalışacak işlemler bu methodda tanımlanıyor.
        @Override
        protected String doInBackground(String... url) {
            String veri = "";
            try {
                //Gönderilen parametre sonucu dönen değerleri alan methodu kullanarak işlem yapılıyor.
                veri = urlVerisiAl(url[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return veri;
        }

        //Üstteki method (doInBackground) sonucu dönen veri kümesine ait dönen değerler ayıklanarak
        //gerekli yerde kullanmak için işleme alınıyor.
        @Override
        protected void onPostExecute(String veriSonuc) {
            super.onPostExecute(veriSonuc);

            //VerileriAyikla adi altında Asenkron olarak çalışan Class sayesinde JSON tipinde dönen veri kümesi işleniyor.
            VerileriAyikla verileriAyikla = new VerileriAyikla();
            verileriAyikla.execute(veriSonuc);
        }
    }


    //Google Directions API kullanılarak JSON tipinde dönen verileri ayıklayıp
    //daha anlamlı hale getirmek için aşağıdaki Class tanımlandı.
    private class VerileriAyikla extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        //Asenkron olarak arka planda çalışacak işlemler bu methodda tanımlanıyor.
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonVeri) {

            JSONObject jVeriNesnesi;
            List<List<HashMap<String, String>>> rota = null;

            //Veriler ayıklanarak daha anlamlı hale getirildikten sonra
            //Bilgi olarak kullanılması için ilgili değişkenlere aktarılıyor.
            try {
                jVeriNesnesi = new JSONObject(jsonVeri[0]);

                //JSON tipinde dönen verilerin daha anlamlı bir hale getirilmesi
                //Aşağıdaki Class'a bağlı method sayesinde yapılıyor.
                KonumlarArasiMesafeVeriIslemleri veriAyikla = new KonumlarArasiMesafeVeriIslemleri();
                rota = veriAyikla.jsonVeriAyikla(jVeriNesnesi);

                //Dönen mesafe bilgisini tutmak için kullanıldı.
                Double mesafeDeger;

                //Kullanıcıya bilgi vermek için kullanıldı
                String yolBilgiText="";


                //Aşağıdaki if-else bloğu sayesinde dönen mesafe uzunluğu 1 km ve üstünde bir değer ise km olarak
                //değilse metre (m) olarak yazdırılıyor.
                if (veriAyikla.mesafe_metre>=1000)
                {
                    mesafeDeger= Double.valueOf(veriAyikla.mesafe_metre)/1000;
                    yolBilgiText=mesafeDeger.toString().replace(".",",")+" km";
                }else{
                    mesafeDeger=Double.valueOf(veriAyikla.mesafe_metre);
                    yolBilgiText=mesafeDeger.toString()+" m";
                }

                //Kişiye mevcut konum ile seçmiş olduğu konum arasındaki mesafe ve süre bilgisini vermek için kullanıldı.
                mesajVeri = "Tahmini Yürüme Mesafesi:" + yolBilgiText + ", Tahmini Yürüyerek Varış Süresi:" + veriAyikla.sure;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rota;
        }

        //"doInBackground" methoduna ait işlemler bittikten sonra bu method çalışacak.
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> sonucveriKumesi) {

            //Konumlara ait mevcut yerler ile mesafeler arası çizgi işlemlerinde kullanılacak
            //değişkenler tanımlanıyor.
            ArrayList noktaVeri = null;
            PolylineOptions cizgiVeri = null;

            //Dönen değerler sonucu Harita üzerindeki belirlenen konumlara ait yol işlemleri yapılıyor.
            for (int i = 0; i < sonucveriKumesi.size(); i++) {
                noktaVeri = new ArrayList();
                cizgiVeri = new PolylineOptions();

                //HashMap yapısı kullanılarak ikili dize değerleri ilgili değişkenlere aktarılıyor.
                List<HashMap<String, String>> yol = sonucveriKumesi.get(i);
                for (int j = 0; j < yol.size(); j++) {
                    HashMap<String, String> noktaKonumVeri = yol.get(j);

                    //Enlem ve boylam bilgileri alınarak konum'a ekleniyor.
                    double enlem = Double.parseDouble(noktaKonumVeri.get("enlem"));
                    double boylam = Double.parseDouble(noktaKonumVeri.get("boylam"));
                    LatLng konum = new LatLng(enlem, boylam);

                    noktaVeri.add(konum);
                }

                //Yol(rota) verisinin özellikleri belirleniyor.
                cizgiVeri.addAll(noktaVeri);
                cizgiVeri.width(12);

                //mevcut konum ile seçilen konum arasındaki yol verisinin rengi  yeşil yapılıyor
                cizgiVeri.color(Color.GREEN);
                //rota şekline ait özellik seçeneği belirtiliyor
                cizgiVeri.geodesic(true);

            }

            //Yukarıdaki işlemler sonucunda harita üzerindeki rota çizgisi ekleniyor.
            mMap.addPolyline(cizgiVeri);

            //İki konum arasındaki tahmini yol rotası ve süre bilgilerinin gösterilmesi için gerekli işlemler yapılıyor.
            //Mesaj bilgisi kullanıcının dikkatini çekmek farklı renkte gösteriliyor.
            Toast toast = Toast.makeText(getApplicationContext(),mesajVeri, Toast.LENGTH_LONG);
            toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
            toast.show();

        }
    }



    //Google Directions API kullanılarak iki konum arasındaki verileri elde etmek için
    //gereken url yapısı hazırlanıyor.
    private String mesafeVerileriniGonder(LatLng mevcutKonumEnlemBoylam, LatLng hedefKonumEnlemBoylam) {

        //Mevcut konuma ait enlem ve boylam bilgileri giriliyor.
        String mevcutKonum = "origin=" + mevcutKonumEnlemBoylam.latitude + "," + mevcutKonumEnlemBoylam.longitude;

        //Hedef konuma ait enlem ve boylam bilgileri giriliyor.
        String hedefKonum = "destination=" + hedefKonumEnlemBoylam.latitude + "," + hedefKonumEnlemBoylam.longitude;

        //Ulaşımın seçeneğini yürüme olrak belirleniyor.
        String ulasimModu = "mode=walking";

        //Dönen verilere ait konum bilgilerinin dil özelliği belirleniyor.
        String dilSecenek="language=tr-TR";

        //Yukarıdaki parametreler birleştiriliyor
        String parameters = mevcutKonum + "&" + hedefKonum + "&sensor=false&" +dilSecenek+"&"+ ulasimModu;

        //İşlenecek olan verinin formatı(JSON) belirleniyor
        String veriIslemeTuru = "json";

        //Artık yukarıdaki değişkenlerden meydana gelen url bilgisi aşağıdaki değişkende toplanıyor.
        String urlVeri = "https://maps.googleapis.com/maps/api/directions/" + veriIslemeTuru + "?" + parameters + "&key=" + getString(R.string.google_maps_key);

        return urlVeri;
    }

    //Parametre olarak gönderilen url bilgisine  ait Yerleşim Yer(ler)i verisini indirip dönen sonucu
    //geri döndürmesi için method tanımlaması yapılıyor
    private String urlVerisiAl(String urlParametre) throws IOException
    {
        String veri = ""; //veriler bu değişkende tutulacak.
        InputStream veriAkisi = null; //Stream işlemlerinde kullanılmak için tanımlandı.
        HttpURLConnection urlBaglantisi = null;//Url bağlantısı işlemleri için tanımlandı.

        try {
            //Yeni bir url nesnesi tanımlanıp, url bağlantı işlemlerinin gerçekleştirilmesi sağlanıyor.
            URL url = new URL(urlParametre);
            urlBaglantisi=(HttpURLConnection) url.openConnection();
            urlBaglantisi.connect();

            //Url sonucu dönen değerlere ait işlemler burda yapılıyor.
            veriAkisi = urlBaglantisi.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(veriAkisi));
            StringBuffer sb = new StringBuffer();

            String satir = "";//satır satır veriler okunarak ilgili değişkene(satir) atılıyor.
            while((satir = br.readLine()) != null)
            {
                sb.append(satir);
            }

            veri = sb.toString();
            br.close();//Buffer nesnesi ile işimiz kalmadığı için kapatılıyor

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            //İşlem bitişi, söz konusu nesnelerin kapatılması işlemi yapılıyor.
            veriAkisi.close();
            urlBaglantisi.disconnect();
        }

        return veri;
    }

}
