package Org.FastHtmlToPdf;

import Org.FastHtmlToPdf.Interop.FormatEnum;
import Org.FastHtmlToPdf.Interop.HtmlToImage;
import Org.FastHtmlToPdf.Model.ImageDocument;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FastHtmlToImage implements Closeable {
    private HtmlToImage htmlToImage = null;
    private Pointer global_settings = Pointer.NULL;
    private Pointer converter = Pointer.NULL;
    private String fileName = String.format("%sFastHtmlToImage\\wkhtmltox.dll", System.getProperty("java.io.tmpdir"));
    private String zipName = String.format("%sFastHtmlToImage\\wkhtmltox.zip", System.getProperty("java.io.tmpdir"));

    public FastHtmlToImage(){
        ImgUtil.create(fileName,zipName);
        htmlToImage = Native.load(fileName, HtmlToImage.class);
        htmlToImage.wkhtmltoimage_init(0);
        global_settings = htmlToImage.wkhtmltoimage_create_global_settings();

        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.defaultEncoding", "utf-8");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.loadImages", "true");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.enableJavascript", "true");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.jsdelay", "1000");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.loadErrorHandling", "skip");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.debugJavascript", "true");
    }

    @Override
    public void close() {
        if (converter != Pointer.NULL)
            htmlToImage.wkhtmltoimage_destroy_converter(converter);
        System.runFinalization();
    }

    public byte[] convert(ImageDocument doc, String html){
        if (doc == null)
            return null;

        if (doc.getWidth() != 0)
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "screenWidth", String.valueOf(doc.getWidth()));

        if (doc.getHeight() != 0)
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "screenHeight", String.valueOf(doc.getHeight()));

        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "fmt", doc.getFormat());

        if (doc.isSmartWidth())
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "smartWidth", "true");

        if ((doc.getFormat().equals(FormatEnum.png.name()) || doc.getFormat().equals(FormatEnum.svg.name())|| doc.getFormat().equals(FormatEnum.bmp.name())) && doc.isTransparent())
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "transparent", "true");

        converter = htmlToImage.wkhtmltoimage_create_converter(global_settings, html.getBytes(StandardCharsets.UTF_8));

        if (htmlToImage.wkhtmltoimage_convert(converter) ==1){
            PointerByReference pointerByReference = new PointerByReference();
            long len = htmlToImage.wkhtmltoimage_get_output(converter, pointerByReference);
            byte[] result = pointerByReference.getValue().getByteArray(0, (int) len);
            pointerByReference.setValue(Pointer.NULL);
            return result;
        }
        else
            return null;
    }
}

class ImgUtil{
    public static synchronized void create(String fileName,String zipName){
        URL url = Thread.currentThread().getContextClassLoader().getResource("wkhtmltox.zip");
        File file = new File(String.format("%s\\FastHtmlToImage", System.getProperty("java.io.tmpdir")));
        if (!file.exists())
            file.mkdirs();

        file = new File(fileName);
        if (file.exists())
            return;

        assert url != null;
        if(url.getPath().contains("BOOT-INF")) {
            try {
                Enumeration<URL> list = Thread.currentThread().getContextClassLoader().getResources("META-INF");
                while (list.hasMoreElements()) {
                    url = list.nextElement();
                    if (url.getPath().contains("org.FastHtmlToPdf")) {
                        jarZip(url, fileName, zipName);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ("file".equals(url.getProtocol()))
            fileZip(url, fileName);

        if ("jar".equals(url.getProtocol()))
            jarZip(url, fileName,zipName);

        file = new File(zipName);
        file.delete();
    }

    private static void fileZip(URL url, String fileName){
        try {
            String zipName = URLDecoder.decode(url.getFile(), "UTF-8");
            try (ZipFile zip = new ZipFile(zipName)) {
                Enumeration list = zip.entries();
                while (list.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) list.nextElement();
                    if (entry.getName().contains(System.getProperty("sun.arch.data.model"))) {
                        InputStream in = zip.getInputStream(entry);
                        File file = new File(fileName);
                        Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        in.close();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void jarZip(URL url,String fileName,String zipName){
        try {
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> list = jarFile.entries();
            while (list.hasMoreElements()) {
                JarEntry jarEntry = list.nextElement();
                if (jarEntry.getName().equals("wkhtmltox.zip")) {
                    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(jarEntry.getName());
                    File zipfile = new File(zipName);
                    Files.copy(in, zipfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    in.close();
                    break;
                }
            }

            ZipFile zip = new ZipFile(zipName);
            Enumeration zipList = zip.entries();
            while (zipList.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) zipList.nextElement();
                if(zipEntry.getName().contains(System.getProperty("sun.arch.data.model"))) {
                    InputStream in = zip.getInputStream(zipEntry);
                    File file = new File(fileName);
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    in.close();
                    break;
                }
            }
            zip.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
