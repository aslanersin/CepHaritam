package com.ersin.cepharitam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.ArrayList;
import java.util.HashMap;

public class KonumlariListeleActivity extends AppCompatActivity {

    //Verilerin görüntüleneceği ListView
    ListView listViewKonumlar;

    //Verilerin aktarılacağı ArrayList tanımlaması yapılıyor.
    ArrayList<HashMap<String, String>> konumlarListesi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konumlarilistele);

        //Kayıtların Gösterileceği ListView'a erişim sağlanıyor.
        listViewKonumlar=(ListView) findViewById(R.id.listView_konumlar);

        //Verileri ListView'da göstermeye yarayan method çağrılıyor.
        verileriListele();

        //ListView üzerinde iken istenen konuma tıklanıldığında, konum bilgisi
        //harita üzerinde gösteriliyor.
        listViewKonumlar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                //ListView'a tıklanıldığında seçilen satırdaki veriye ait konum adı, enlem ve boylam bilgisi alınıyor.
                String konumadi=konumlarListesi.get(position).get("konumadi").toString();
                String enlem=konumlarListesi.get(position).get("enlem").toString();
                String boylam=konumlarListesi.get(position).get("boylam").toString();

                //konum adı, enlem ve boylam bilgileri gönderilerek konumun harita üzerinde gösterilmesi sağlanıyor.
                //Bu gösterim işlemi modülerlik adına yeni bir Activity oluşturmadan
                //"KonumAraBulKaydetActivity" üzerinden gerçekleştiriliyor

                Intent intKonumAraBulKaydet=new Intent("android.intent.action.KONUMARABULKAYDET");

                intKonumAraBulKaydet.putExtra("konumadiVeri",konumadi);
                intKonumAraBulKaydet.putExtra("enlemVeri",enlem);
                intKonumAraBulKaydet.putExtra("boylamVeri",boylam);

                startActivity(intKonumAraBulKaydet);

                //Alttaki kodlar yardımıyla kişi konumbulma ekranında tekrar bir yeri eklerse
                //Eklediği bilginin refresh olarak tekrar getirilmesi sağlanıyor.
                finish();
                verileriListele();


            }
        });

        //listView üzerinde silinmek istenilen kayda uzun bir süre basılırsa silinmesi sağlanacak.
        listViewKonumlar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //ListView'a uzun tıklanıldığında seçilen satırdaki veriye ait kayıt Id ve konum adı bilgisi alınıp
                //silme işlemi yapılıyor.
                final String kayitId=konumlarListesi.get(position).get("kayit_id").toString();
                String konumadi=konumlarListesi.get(position).get("konumadi").toString();

                //Kullanıcı seçmiş olduğu konumu silmek isteyip istemediği
                //onay alınarak yapılıyor.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(KonumlariListeleActivity.this, R.style.cepHaritamDialog));

                // alert dialog başlığını tanımlıyoruz.
                alertDialogBuilder.setTitle("Konum Silme");

                // alert dialog özelliklerini oluşturuyoruz.
                alertDialogBuilder
                        .setMessage("Seçmiş olduğunuz;\n\n"+konumadi+"\n\nkaydını silmek istiyor musunuz?")
                        .setCancelable(false)
                        .setIcon(R.drawable.cepharitamlogo)
                        // Evet butonuna tıklanınca yapılacak işlemleri buraya yazıyoruz.
                        .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Kullanıcın seçmiş olduğu kayıt veri tabanından siliniyor.
                                CepHaritamDbClass veriislemleri= CepHaritamDbClass.getInstance(getApplicationContext());
                                veriislemleri.konumBilgisiSil(kayitId);
                                veriislemleri.close();

                                //Kayıt silindikten sonra ListView'daki kayıtlar tekrar güncel bir şekilde listeleniyorlar.
                                verileriListele();

                                Toast.makeText(getApplicationContext(),"Kayıt silindi.",
                                        Toast.LENGTH_LONG).show();

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


                return true;
            }
        });


    }


    private void verileriListele(){

        //Db ye erişmek için gerekli işlemler yapılıyor.
        CepHaritamDbClass veriislemleri= CepHaritamDbClass.getInstance(getApplicationContext());

        //Db'den gelen veriler Bir ArrayList de tutularak ListView'da gösterilmesi sağlanıyor.
        konumlarListesi = veriislemleri.konumBilgileriniListele();

        //Kayıtlar yukarıda tanımlanan ArrayList(konumlarListesi) vasıtasıyla bir Adapter nesnesine aktarılıyor.
        final ListAdapter kayitKumesi = new SimpleAdapter(KonumlariListeleActivity.this, konumlarListesi, R.layout.konumdetaybilgilerilistele,
                new String[]{"konumadi","eklemezamani"}, new int[]{R.id.textView_KonumAdi, R.id.textView_EklenmeZamani});

        //Kayıtların gösterilmesi için gereken bağlama işi yapılıyor.
        listViewKonumlar.setAdapter(kayitKumesi);

        //Konum kaydı yoksa kullanıcıya bilgi veriliyor.
        if (konumlarListesi.size()==0){
            Toast.makeText(getApplicationContext(),"Hiç bir kayda rastlanılamadı",Toast.LENGTH_LONG).show();
        }
    }

}
