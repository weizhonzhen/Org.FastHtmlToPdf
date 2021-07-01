package Org.FastHtmlToPdf.Model;

import Org.FastHtmlToPdf.Interop.FormatEnum;

public class ImageDocument {
    private int height;

    private int width;

    private int quality = 94;

    private String format = String.valueOf(FormatEnum.jpg);

    private boolean smartWidth;

    private boolean transparent;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isSmartWidth() {
        return smartWidth;
    }

    public void setSmartWidth(boolean smartWidth) {
        this.smartWidth = smartWidth;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }
}
