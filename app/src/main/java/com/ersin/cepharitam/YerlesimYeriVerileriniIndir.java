package com.ersin.cepharitam;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class YerlesimYeriVerileriniIndir {

    //Parametre olarak gönderilen url bilgisine  ait Yerleşim Yer(ler)i verisini indirip dönen sonucu
    //geri döndürmesi için method tanımlaması yapılıyor
    public String urlVerisiAl(String urlParametre) throws IOException
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
