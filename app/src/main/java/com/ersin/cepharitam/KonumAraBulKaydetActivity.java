package com.ersin.cepharitam;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class KonumAraBulKaydetActivity extends FragmentActivity implements OnMapReadyCallback {


    EditText konumAraText;
    Button konumEkleButton;
    private GoogleMap mMap; //Harita için tanımlandı

    private LocationManager locationManager; //Harita üzerindeki yerleri yönetmek için kullanıldı
    private LocationListener locationListener; //Harita ile ilgili konum bilgisine ait değerler için kullanıldı.
    private LatLng konumum; //Enlem boylam bilgisi için kullanıldı.

    //Adres bilgileri için kullanılacak değişkenler tanımlanıyor.
    private String anaAdres;
    private String detayAdres;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konumarabulkaydet);

        //Haritanın gösterileceği fragment ile ilgili ayarlamalar yapılıyor.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.harita);
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
                        fiedlList).build(KonumAraBulKaydetActivity.this);
                startActivityForResult(intent,1); //Burdaki request code yazılımcı tarafından belirlenebiliyor.

            }
        });

        konumEkleButton=(Button) findViewById(R.id.button_konumekle);
        konumEkleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Herhangi bir ulaşım verisi döndürülmemişse uyarı mesajı verdiriliyor.
                if (anaAdres==null){
                    Toast.makeText(getApplicationContext(),"Lütfen her hangi bir konum araması yapın",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Kullanıcı seçmiş olduğu konumu eklemek isteyip istemediği
                //onay alınarak yapılıyor.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(KonumAraBulKaydetActivity.this, R.style.cepHaritamDialog));

                // alert dialog başlığını tanımlıyoruz.
                alertDialogBuilder.setTitle("Konum Ekleme");

                // alert dialog özelliklerini oluşturuyoruz.
                alertDialogBuilder
                        .setMessage("Seçmiş olduğunuz;\n\n"+detayAdres+"/"+anaAdres+"\n\nadresini kayıtlarınıza eklemek istiyor musunuz?")
                        .setCancelable(false)
                        .setIcon(R.drawable.cepharitamlogo)
                        // Evet butonuna tıklanınca yapılacak işlemleri buraya yazıyoruz.
                        .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Kullanıcın seçmiş olduğu konum bilgisi veri tabanına ekleniyor.
                                CepHaritamDbClass veriislemleri= CepHaritamDbClass.getInstance(getApplicationContext());
                                //Aktif Zamanı milisaniye cinsinden almak için kullanıldı.
                                long aktifZaman=veriislemleri.aktifZamaniAl();
                                veriislemleri.konumEkle(detayAdres+"--"+anaAdres,String.valueOf(konumum.latitude),
                                        String.valueOf(konumum.longitude),aktifZaman);

                                Toast.makeText(getApplicationContext(),"Kayıt başarıyla eklendi. Eklenme Zamanı:"+veriislemleri.tarihSaateDonustur(aktifZaman),
                                        Toast.LENGTH_LONG).show();

                                veriislemleri.close();

                            }
                        })
                        // İptal butonuna tıklanınca yapılacak işlemleri buraya yazıyoruz.
                        .setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // alert dialog nesnesini oluşturuyoruz
                AlertDialog alertDialog = alertDialogBuilder.create();

                // alerti gösteriyoruz
                alertDialog.show();

            }
        });

    }




    //Kullanıcı haritaya erişim izinleri verince burdaki kodlar devreye giriyor.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Talep başarılı olursa veriler alınıyor.
        //Ayrıca if koşulundaki "requestCode==1" bölümü  yukarıda yazılımcı tarafından belirlenen kod kısmına ait.
        if (requestCode==1&&resultCode==RESULT_OK){
            //Google Place Api'nin gönderdiği değerler ilgili değişkene atılacak.
            Place place= Autocomplete.getPlaceFromIntent(data);

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


        //Konumları listeleme ekranından harita üzerinde gösterilmesi istenen bir yer varsa
        //Aşağıdaki Intent tanımlaması sayesinde ilgili veriler çekilip harita üzerinde gösteriliyor.
        Intent intKonumGoster=getIntent();
        String konumadi=intKonumGoster.getStringExtra("konumadiVeri");
        String enlem=intKonumGoster.getStringExtra("enlemVeri");
        String boylam=intKonumGoster.getStringExtra("boylamVeri");

        if ( (enlem!=null)&&(boylam!=null) ){
            konumGosterveZoomla(Double.valueOf(enlem),Double.valueOf(boylam),17,konumadi);
        }


    }

    //Harita üzerinde konumlandırma yapmak için method tanımlaması yapılıyor.
    private void konumGosterveZoomla(Double enlem,Double boylam, float zoom, String title){

        //Enlem ve boylam bilgisini harita üzerine aktarmak için gerekli işlemler yapılıyor.
        LatLng konumEnlemBoylam=new LatLng(enlem,boylam);

        //Harita üzerindeki işaretlemeler siliniyor.
        mMap.clear();

        //Konum bilgisine göre harita zoomlanıyor.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konumEnlemBoylam, zoom));

        //Harita üzerinde seçilen yer işaretleniyor.
        MarkerOptions options = new MarkerOptions()
                .position(konumEnlemBoylam)
                .title(title);
        mMap.addMarker(options);

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

    private void bilinenSonKonumuAl(Location enlemboylam){

        //En son bilinen konum bilgisi telefondan çekilip harita üzerinde gösteriliyor.
        LatLng bilienSonKonum=new LatLng(enlemboylam.getLatitude(),enlemboylam.getLongitude());
        mMap.addMarker(new MarkerOptions().title("Mevcut Konumunuz").position(bilienSonKonum));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bilienSonKonum,17));

    }
}
