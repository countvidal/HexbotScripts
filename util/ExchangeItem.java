package util;


import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: Bram
 * Date: 28-10-13
 * Time: 23:59
 * To change this template use File | Settings | File Templates.
 */
public final class ExchangeItem {
    private final int id, highAlch;
    private final double recentHigh, recentLow, average;
    private final String name;
    private final Offer[] offers;
    private final BufferedImage image;

    public ExchangeItem(int id, String name, double recentHigh, double recentLow, double average, int highAlch, Offer[] offers, BufferedImage image) {
        this.id = id;
        this.name = name;
        this.recentHigh = recentHigh;
        this.recentLow = recentLow;
        this.average = average;
        this.highAlch = highAlch;
        this.offers = offers;
        this.image = image;
    }

    //getters
    public int getId() { return id; }
    public int getHighAlch() { return highAlch; }
    public double getRecentHigh() { return recentHigh; }
    public double getRecentLow() { return recentLow;  }
    public double getAverage() { return average; }
    public String getName() { return name; }
    public Offer[] getOffers() { return offers.clone(); }
    public BufferedImage getImage() { return new BufferedImage(image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), null); }
}
