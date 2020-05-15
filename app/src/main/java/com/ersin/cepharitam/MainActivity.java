package com.ersin.cepharitam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anamenudashboard);

        //"Haritam" tuşuna basılınca çağrılacak ekran ile ilgili tanımlamalar yapılıyor.

        //İlgili CardView nesnesine erişim sağlanıyor.
        CardView nerdeyim=(CardView) findViewById(R.id.Id_KonumBulAraKaydet);
        //Click olayı tanımlanıyor.
        nerdeyim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent oluşturularak "KonumAraBulKaydet" (activity_konumarabulkaydet)" çağrılıyor.
                Intent intKonumAraBulKaydet=new Intent("android.intent.action.KONUMARABULKAYDET");
                startActivity(intKonumAraBulKaydet);

            }
        });

        //"Konumları Listele" tuşuna basılınca çağrılacak ekran ile ilgili tanımlamalar yapılıyor.

        //İlgili CardView nesnesine erişim sağlanıyor.
        CardView konumlariListele=(CardView) findViewById(R.id.Id_KonumlariListele);
        konumlariListele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent oluşturularak Konumları Listeleme çağrılıyor. çağrılıyor.
                Intent intKonumlariListele=new Intent("android.intent.action.EKLEKONUMLISTELE");
                startActivity(intKonumlariListele);

            }
        });


        //"Etrafını Keşfet" tuşuna basılınca çağrılacak ekran ile ilgili tanımlamalar yapılıyor.

        //İlgili CardView nesnesine erişim sağlanıyor.
        CardView etrafiniKestfet=(CardView) findViewById(R.id.Id_KonumYakinYerler);
        etrafiniKestfet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent oluşturularak Konumları Listeleme çağrılıyor. çağrılıyor.
                Intent intKonumYakinYerler=new Intent("android.intent.action.KONUMYAKINYERLERILISTELE");
                startActivity(intKonumYakinYerler);

            }
        });


        //"Yol Tarifi" tuşuna basılınca çağrılacak ekran ile ilgili tanımlamalar yapılıyor.

        //İlgili CardView nesnesine erişim sağlanıyor.
        CardView yolTarifi=(CardView) findViewById(R.id.Id_KonumYolTarifi);
        yolTarifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent oluşturularak Konumları Listeleme çağrılıyor. çağrılıyor.
                Intent intYolTarifi=new Intent("android.intent.action.KONUMLARARASIMESAFE");
                startActivity(intYolTarifi);

            }
        });



    }

}
