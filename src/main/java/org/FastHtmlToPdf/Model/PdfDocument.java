package Org.FastHtmlToPdf.Model;

public class PdfDocument {
    private boolean displayHeader;

    private boolean displayFooter;

    private Header header = new Header();

    private Footer footer = new Footer();

    private int marginTop;

    private int marginLeft;

    private int marginRight;

    private int marginBottom;

    private int height;

    private int width;

    private String size  = "A4";

    public boolean isDisplayHeader() {
        return displayHeader;
    }

    public void setDisplayHeader(boolean displayHeader) {
        this.displayHeader = displayHeader;
    }

    public boolean isDisplayFooter() {
        return displayFooter;
    }

    public void setDisplayFooter(boolean displayFooter) {
        this.displayFooter = displayFooter;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Footer getFooter() {
        return footer;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
