package com.ersin.cepharitam;

import android.Manifest;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KonumlarArasiMesafe extends FragmentActivity implements OnMapReadyCallback {


    //Konumları Tutmak için kullanılıyor.
    ArrayList konumNoktalari= new ArrayList();

    //Konumlar mesafe bilgilerini tutmak için aşağıdaki değişkenler kullanıldı.
    private TextView konumMesafeBilgi;
    private String mesajBilgi="";


    private GoogleMap mMap; //Harita için tanımlandı

    private LocationManager locationManager; //Harita üzerindeki yerleri yönetmek için kullanıldı
    private LocationListener locationListener; //Harita ile ilgili konum bilgisine ait değerler için kullanıldı.

    private LatLng mevcutKonum;//Kişinin mevcut konum bilgisini tutmak için kullanıldı.
    private LatLng hedefKonum;//Kişinin gitmek istediği hedef konum bilgisini tutmak için kullanıldı.

    //RadioGroup (Yürüyerek veya Araçla) nesnesine erişip ilgili kontrolleri yapmak için kullanıldı.
    private RadioGroup ulasimSekli;

    //Google Directions API ve mesafe renklendirme işlemlerinde gerekli kontrolleri yapmak için kullanıldı.
    private String ulasimSecenek="walking"; //varsayılan olarak yürüyüş seçeneğinin söz konusu API'deki karşılığı olan "walking" değeri kullanıldı.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konumlararasimesafe);

        //Mesafe bilgisinin gösterileceği nesneye erişiliyor.
        konumMesafeBilgi=(TextView) findViewById(R.id.textViewBilgi);

        //Haritanın gösterileceği fragment ile ilgili ayarlamalar yapılıyor.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.harita_konumlararasi_mesafe);
        mapFragment.getMapAsync(this);


        //Ulaşım şeklinin seçildiği RadioGroup nesnesine erişilip ona göre gerekli kontroller yapılacak
        ulasimSekli =(RadioGroup) findViewById(R.id.radiogroupulasimsekli);

        //RadioGroup nesnesine tıklama event'i tanımlanarak kişinin seçmiş olduğu ulaşım şekline göre
        //gerekli işlemeler yapılıyor.
        ulasimSekli.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.yuruyus:
                        //Google Directions API'de yürüyüş seçeneğini belirtmek için oluşturulacak url'de
                        //parametre olarak "walking" gönderilmesi gerekiyor.
                        ulasimSecenek="walking";


                        //Harita üzerinde mevcut konum dahil olmak üzere 2 ve daha fazla nokta işaretlenmişse
                        //gerekli değerler oluşturulup ulaşım seçeneğine göre hesaplama yapılması sağlanıyor.
                        if (konumNoktalari.size() >= 2) {

                            //Mevcut konum ve gidilmesi gereken konum seçildiğinde aşağıdaki method kullanılarak
                            //Google Directions API'de kullanılacak veri seti için gerekli hazırlıklar yapılıyor.
                            mesafeVeriOlustur((LatLng)konumNoktalari.get(0),(LatLng)konumNoktalari.get(1));
                        }else{
                            Toast.makeText(getApplicationContext(),"Lütfen gitmek istediğiniz yeri harita üzerinde işaretleyin",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.arac:
                        //Google Directions API'de Araçla gitme seçeneğini belirtmek için oluşturulacak url'de
                        //parametre olarak "driving" gönderilmesi gerekiyor.
                        ulasimSecenek="driving";

                        //Harita üzerinde mevcut konum dahil olmak üzere 2 ve daha fazla nokta işaretlenmişse
                        //gerekli değerler oluşturulup ulaşım seçeneğine göre hesaplama yapılması sağlanıyor.
                        if (konumNoktalari.size() >= 2) {

                            //Mevcut konum ve gidilmesi gereken konum seçildiğinde aşağıdaki method kullanılarak
                            //Google Directions API'de kullanılacak veri seti için gerekli hazırlıklar yapılıyor.
                            mesafeVeriOlustur((LatLng)konumNoktalari.get(0),(LatLng)konumNoktalari.get(1));
                        }else{
                            Toast.makeText(getApplicationContext(),"Lütfen gitmek istediğiniz yeri harita üzerinde işaretleyin",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });

    }

    private void mesafeVeriOlustur(LatLng mevcutKonumDeger, LatLng hedefKonumDeger){

        //Hesaplama işlemlerinin yenilenmesinde harita üzerindeki veriler silinip tekrar oluşturuluyor.
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mevcutKonum).title("Mevcut Konumunuz"));
        mMap.addMarker(new MarkerOptions().position(hedefKonum).title("Seçtiğiniz Konumunuz"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mevcutKonum,16));

        //Harita üzerindeki hedef konumu farklı bir renkte işaretlemek için aşağıdaki tanımlamalar kullanılıyor.
        MarkerOptions isaretlemeOzellikleri = new MarkerOptions();
        isaretlemeOzellikleri.position(hedefKonum);
        isaretlemeOzellikleri.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(isaretlemeOzellikleri);

        //Google Directions API'a gerekli url bilgilerini göndermek için parametre oluşturma
        //işlemleri yapılıyor.
        String veriUrl = mesafeVerileriniGonder(mevcutKonumDeger, hedefKonumDeger);

        //Google Directions API'ye veri gönderilme işlemi gerçekleştirilirken
        //Herhangi bir aksamaya neden vermemek için Asenkron olarak tanımlanan Class kullanılıyor.
        verileriIsleveAl haritaVeriIsle = new verileriIsleveAl();

        //Google Directions API'ye gerekli veri kümesi gönderilerek dönen sonuç JSON formatında geri alınıp ayıklanacak
        haritaVeriIsle.execute(veriUrl);

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

                // Son bilinen konuma göre işlem yapılıyor
                Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (bilinenSonKonum!=null) {
                    //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                    bilinenSonKonumuAl(bilinenSonKonum);
                }
            }
        }else{
            //Son bilinen konuma göre işlem yapılıyor
            Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (bilinenSonKonum!=null) {
                //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                bilinenSonKonumuAl(bilinenSonKonum);
            }
        }


        //Harita üzerinde tıklanılan yeni noktaya ait konum alınıp mesafe hesaplaması yapılıyor
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();//harita üzerindeki her şey temizleniyor.
                konumNoktalari.clear(); //markerlar temizleniyor.

                //Mevcut konum bilgisi harita üzerine eklenip zoomlama işlemi yapılıyor.
                mMap.addMarker(new MarkerOptions().position(mevcutKonum).title("Mevcut Konumunuz"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mevcutKonum,16));

                //Mevcut Konum bilgisi var olan Array'e ekleniyor.
                konumNoktalari.add(mevcutKonum);

                //Seçilen konum bilgisi ekleniyor.
                konumNoktalari.add(latLng);

                hedefKonum=latLng;//Kişinin hedef konumunun her hesaplama işleminde tekrar kullanılması için kullanıldı.

                //Seçilen konum bilgisini farklı renkte gösterebilmek için aşağıdaki tanımlamalar yapılıyor.
                MarkerOptions secilenKonumOzellik = new MarkerOptions();
                secilenKonumOzellik.position(latLng);
                secilenKonumOzellik.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mMap.addMarker(secilenKonumOzellik);


                //Harita üzerinde gidilmek istenilen konum bilgisi de seçildiğinde aşağıdaki kontrol yapılarak
                //Google Directions API'ye veri Asenkron olarak gönderiliyor.
                if (konumNoktalari.size() >= 2) {
                    LatLng mevcutKonum = (LatLng) konumNoktalari.get(0);
                    LatLng hedefKonum = (LatLng) konumNoktalari.get(1);

                    //Google Directions API'a gerekli url bilgilerini göndermek için parametre oluşturma
                    //işlemleri yapılıyor.
                    String veriUrl = mesafeVerileriniGonder(mevcutKonum, hedefKonum);

                    //Google Directions API'ye veri gönderilme işlemi gerçekleştirilirken
                    //Herhangi bir aksamaya neden vermemek için Asenkron olarak tanımlanan Class kullanılıyor.
                    verileriIsleveAl haritaVeriIsle = new verileriIsleveAl();

                    //Google Directions API'ye gerekli veri kümesi gönderilerek dönen sonuç JSON formatında geri alınıp ayıklanacak
                    haritaVeriIsle.execute(veriUrl);

                }
            }
        });



    }


    private void bilinenSonKonumuAl(Location enlemboylam){
        //En son bilinen konum bilgisi telefondan çekilip harita üzerinde gösteriliyor.
        LatLng bilienSonKonum=new LatLng(enlemboylam.getLatitude(),enlemboylam.getLongitude());
        mevcutKonum=bilienSonKonum;
        mMap.addMarker(new MarkerOptions().title("Mevcut Konumunuz").position(bilienSonKonum));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bilienSonKonum,16));

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

                // Son bilinen konuma göre işlem yapılıyor
                Location bilinenSonKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (bilinenSonKonum!=null) {
                    //son bilinen konum method yardımıyla harita üzerinde gösteriliyor.
                    bilinenSonKonumuAl(bilinenSonKonum);
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

                //Ulaşım şekline göre mesaj bilgisinin nasıl görüntüleneceğine karar veriliyor.

                if ((ulasimSecenek=="walking")||(ulasimSecenek==null)) {
                    mesajBilgi = "Tahmini Yürüme Mesafesi:" + yolBilgiText + ", Tahmini Yürüyerek Varış Süresi:" + veriAyikla.sure;
                }else{
                    mesajBilgi = "Tahmini Araçla Gidiş Mesafesi:" + yolBilgiText + ", Tahmini Araçla Giderek Varış Süresi:" + veriAyikla.sure;
                }

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

                //ulaşım şekline göre harita üzerindeki yol çiziminde renklendirme işlemi yapılıyor.
                //yürüyerek-Yeşil Renk
                //araçla-Siyah Renk
                if ((ulasimSecenek=="walking")||(ulasimSecenek==null)) {
                    cizgiVeri.color(Color.GREEN);
                }else{
                    cizgiVeri.color(Color.BLACK);
                }
                //rota şekline ait özellik seçeneği belirtiliyor
                cizgiVeri.geodesic(true);

            }


            //Yukarıdaki işlemler sonucunda harita üzerindeki rota çizgisi ekleniyor.
            mMap.addPolyline(cizgiVeri);

            //İki konum arasındaki tahmini yol rotası ve süre bilgilerinin gösterilmesi için gerekli işlemler yapılıyor
            konumMesafeBilgi.setText(mesajBilgi);
        }
    }

    //Google Directions API kullanılarak iki konum arasındaki verileri elde etmek için
    //gereken url yapısı hazırlanıyor.
    private String mesafeVerileriniGonder(LatLng mevcutKonumEnlemBoylam, LatLng hedefKonumEnlemBoylam) {

        //Mevcut konuma ait enlem ve boylam bilgileri giriliyor.
        String mevcutKonum = "origin=" + mevcutKonumEnlemBoylam.latitude + "," + mevcutKonumEnlemBoylam.longitude;

        //Hedef konuma ait enlem ve boylam bilgileri giriliyor.
        String hedefKonum = "destination=" + hedefKonumEnlemBoylam.latitude + "," + hedefKonumEnlemBoylam.longitude;

        //Ulaşımın seçeneğini (yürüme/araç) belirtiliyor.
        String ulasimModu = "mode="+ulasimSecenek;

        //Dönen verilere ait konum bilgilerinin dil özelliği belirleniyor.
        String dilSecenek="language=tr-TR";

        //Yukarıdaki parametreler birleştiriliyor
        String parameters = mevcutKonum + "&" + hedefKonum + "&sensor=false&" +dilSecenek+"&"+ ulasimModu;

        //İşlenecek olan verinin formatı(JSON) belirleniyor
        String veriIslemeTuru = "json";

        //Artık yukarıdaki değişkenlerden meydana gelen url bilgisi aşağıdaki değişkende toplanıyor.
        String urlVeri = "https://maps.googleapis.com/maps/api/directions/" + veriIslemeTuru + "?" + parameters +
                "&key=" + getString(R.string.google_maps_key);

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
