package com.ersin.cepharitam;

import com.google.android.gms.maps.model.LatLng;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class KonumlarArasiMesafeVeriIslemleri {

    /*İki konum arasındaki uzaklık verilerini tutmak için Hashmap tanımlanıyor*/
    public HashMap<Integer, String> mesafeHashMapVeri = new HashMap<Integer, String>();


    public String sure="";
    public int mesafe_metre;
    public int mesafe_sure_oneri;


    //Google API tarafından JSON tipinde döndürülen verinin ayıklanıp anlamlı hale getirilmesi
    //bu method sayesinde oluyor
    public List<List<HashMap<String, String>>> jsonVeriAyikla(JSONObject jObject){

        //Veri işlemlerinde kullanılacak değişken tanımlamaları yapılıyor
        List<List<HashMap<String, String>>> rotalar = new ArrayList<List<HashMap<String, String>>>() ;
        JSONArray jRotalar = null;
        JSONArray jMesafeVeri = null;
        JSONArray jAdimVeri = null;


        try {

            //Yürüme veya Araç parametresi sonucu döndürülen verideki rota bilgisi alınıyor.
            jRotalar = jObject.getJSONArray("routes");

            //Rotaya ait mesafe ve süre verileri işleniyor.
            for(int i=0;i<jRotalar.length();i++){
                //Mesafe verisi alınıyor.
                jMesafeVeri = ( (JSONObject)jRotalar.get(i)).getJSONArray("legs");

                //Yol verilerini tutmak için Liste tanımlanıyor
                List yolrotaVeri = new ArrayList<HashMap<String, String>>();

                //Rota verisi içindeki adım verisi işleniyor.
                for(int j=0;j<jMesafeVeri.length();j++){
                    jAdimVeri = ( (JSONObject)jMesafeVeri.get(j)).getJSONArray("steps");

                    //Aşağıdaki değişkenlere JSON tipinde dönen verilerin ayıklanması sonucu gerekli olan
                    //değer atama işlemleri yapılıyor.
                    mesafe_metre=(int)((JSONObject)((JSONObject)jMesafeVeri.get(j)).get("distance")).get("value");
                    sure=(String)((JSONObject)((JSONObject)jMesafeVeri.get(j)).get("duration")).get("text");
                    mesafe_sure_oneri=(int)((JSONObject)((JSONObject)jMesafeVeri.get(j)).get("duration")).get("value");

                    //Debug işlemlerinde doğrulama için kullanıldı.
                    mesafeHashMapVeri.put(j,"adım :"+(String)((JSONObject)((JSONObject)jAdimVeri.get(j)).get("duration")).get("text")
                            +", yürüme :"+(String)((JSONObject)((JSONObject)jMesafeVeri.get(j)).get("duration")).get("text")
                            +" uzaklık:"+ String.valueOf(mesafe_metre));


                    //Yol verileri oluşturuluyor
                    for(int k=0;k<jAdimVeri.length();k++){
                        String cizgiVeri = ""; //Konumlar üzerindeki yol grafiğine ait verileri oluşturmak için kullanıldı.
                        cizgiVeri = (String)((JSONObject)((JSONObject)jAdimVeri.get(k)).get("polyline")).get("points");

                        //Google Directions API'nin gönderdiği yol verileri decode edilip liste değişkenine atılıyor.
                        //Decode etmek için "https://stackoverflow.com/questions/15380712/how-to-decode-polylines-from-google-maps-direction-api-in-php"
                        //ve "http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java" adresindeki methodlardan yararlanıldı.
                        List listeVeri = decodePoly(cizgiVeri);

                        //Decode Edilen konumlar arasındaki veriler ayıklanıp HashMap'e aktarılıyor.
                        for(int l=0;l <listeVeri.size();l++){
                            HashMap<String, String> konumVeri = new HashMap<String, String>();
                            konumVeri.put("enlem", Double.toString(((LatLng)listeVeri.get(l)).latitude) );
                            konumVeri.put("boylam", Double.toString(((LatLng)listeVeri.get(l)).longitude) );
                            yolrotaVeri.add(konumVeri);
                        }
                    }
                    rotalar.add(yolrotaVeri);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return rotalar;
    }



    /* Aşağıdaki decodePoly methodu;
     * "https://stackoverflow.com/questions/15380712/how-to-decode-polylines-from-google-maps-direction-api-in-php" adresi
     * ve "http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java" adresindeki methodlardan
     * yararlanılarak oluşturuldu.
    */
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

}
