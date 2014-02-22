package util;



import json.JsonArray;
import json.JsonObject;
import json.JsonValue;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bram
 * Date: 28-10-13
 * Time: 23:59
 * To change this template use File | Settings | File Templates.
 */
public final class ExchangeParser {
    private static final String url = "http://forums.zybez.net/runescape-2007-prices/api/item/";
    private static final ArrayList<ExchangeItem> cache = new ArrayList<>();

    //prevent instantiation since the constructor is private
    private ExchangeParser(){}

    public static ExchangeItem lookup(String itemName, boolean reload){
        for(int i=0; i<cache.size(); i++)
            if(cache.get(i).getName().equals(itemName))
                if(reload) cache.remove(cache.get(i)); else return cache.get(i);
        return parseItem(getJSON(url + itemName.replaceAll(" ", "+")));
    }

    public static ExchangeItem lookup(int id, boolean reload){
        for(int i=0; i<cache.size(); i++)
            if(cache.get(i).getId()==id)
                if(reload) cache.remove(cache.get(i)); else return cache.get(i);
        return parseItem(getJSON(url + id));
    }

    public static ExchangeItem lookup(String itemName){ return lookup(itemName, false); }
    public static ExchangeItem lookup(int id){ return lookup(id, false); }

    private static ExchangeItem parseItem(String json){
        //parse json object
        JsonObject object = JsonObject.readFrom(json);

        //parse all offers
        ArrayList<Offer> offers = new ArrayList<>();
        JsonArray offerObjects = object.get("offers").asArray();
        for(JsonValue offerArray : offerObjects){
            JsonObject offerObject = offerArray.asObject();
            offers.add(new Offer(
                    (getInt(offerObject, "selling")==0)? Offer.Type.SELLING: Offer.Type.BUYING,
                    getInt(offerObject, "quantity"),
                    getInt(offerObject, "price"),
                    getString(offerObject, "rs_name"),
                    getString(offerObject, "contact"),
                    getString(offerObject, "notes"), new Date(getInt(offerObject, "date")*1000l)));
        }

        //parse all other attributes and create a new ExchangeItem object
        cache.add(new ExchangeItem(getInt(object, "id"), getString(object, "name"),
                getDouble(object, "recent_high"), getDouble(object, "recent_low"), getDouble(object, "average"),
                getInt(object, "high_alch"), offers.toArray(new Offer[offers.size()]), loadImage(getString(object, "image"))));

        return cache.get(cache.size()-1);
    }

    private static BufferedImage loadImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) { return null; }
    }

    private static String getJSON(String url) {

        try {
            URLConnection cn = new URL(url).openConnection();
            cn.setRequestProperty(
                    "User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            InputStreamReader in = new InputStreamReader(cn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while (null != (line = br.readLine()))
                stringBuilder.append(line);

            return stringBuilder.toString();

        } catch (MalformedURLException e) { e.printStackTrace();
        } catch (IOException e) {e.printStackTrace();}

        return "";
    }

    private static final int getInt(JsonObject object, String name){ return Integer.parseInt(object.get(name).asString().replaceAll("\"\"", "")); }
    private static final double getDouble(JsonObject object, String name){ return Double.parseDouble(object.get(name).asString().replaceAll("\"\"", "")); }
    private static final String getString(JsonObject object, String name){ return object.get(name).asString().replaceAll("\"\"", ""); }
}
