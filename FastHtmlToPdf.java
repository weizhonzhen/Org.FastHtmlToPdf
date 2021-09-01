package Org.FastHtmlToPdf;

import Org.FastHtmlToPdf.Interop.HtmlToPdf;
import Org.FastHtmlToPdf.Model.PdfDocument;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.PointerByReference;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FastHtmlToPdf implements Closeable {
    private HtmlToPdf htmlToPdf = null;
    private Pointer global_settings = Pointer.NULL;
    private Pointer converter = Pointer.NULL;
    private Pointer object_settings = Pointer.NULL;
    private String fileName = String.format("%sFastHtmlToPdf\\wkhtmltox.dll", System.getProperty("java.io.tmpdir"));
    private String zipName = String.format("%sFastHtmlToPdf\\wkhtmltox.zip", System.getProperty("java.io.tmpdir"));

    public FastHtmlToPdf() {
        PdfUtil.create(fileName, zipName);
        htmlToPdf = Native.load(fileName, HtmlToPdf.class);
        htmlToPdf.wkhtmltopdf_init(0);
        global_settings = htmlToPdf.wkhtmltopdf_create_global_settings();
        converter = htmlToPdf.wkhtmltopdf_create_converter(global_settings);

        object_settings = htmlToPdf.wkhtmltopdf_create_object_settings();

        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "web.defaultEncoding", "utf-8");
        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "web.loadImages", "true");
        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "web.enableJavascript", "true");
        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "load.jsdelay", "1000");
        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "load.loadErrorHandling", "skip");
        htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "load.debugJavascript", "true");
    }

    @Override
    public void close() {
        if (converter != Pointer.NULL)
            htmlToPdf.wkhtmltopdf_destroy_converter(converter);
        System.runFinalization();
    }

    public byte[] convert(PdfDocument doc, String html) {
        if (doc == null)
            return null;

        if (doc.isDisplayHeader())
            initHeader(doc, htmlToPdf);

        if (doc.isDisplayFooter())
            initFooter(doc, htmlToPdf);

        if (!isNullOrEmpty(doc.getSize()))
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "size.pageSize", doc.getSize());
        else
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "size.pageSize", "A4");

        if (doc.getWidth() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "size.width", doc.getWidth() * 0.04 + "cm");

        if (doc.getHeight() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "size.height", doc.getHeight() * 0.04 + "cm");

        if (doc.getMarginTop() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "margin.top", doc.getMarginTop() * 0.04 + "cm");

        if (doc.getMarginBottom() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "margin.bottom", doc.getMarginBottom() * 0.04 + "cm");

        if (doc.getMarginLeft() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "margin.left", doc.getMarginLeft() * 0.04 + "cm");

        if (doc.getMarginRight() != 0)
            htmlToPdf.wkhtmltopdf_set_global_setting(global_settings, "margin.right", doc.getMarginRight() * 0.04 + "cm");

        htmlToPdf.wkhtmltopdf_add_object(converter, object_settings, html.getBytes(StandardCharsets.UTF_8));
        htmlToPdf.wkhtmltopdf_set_warning_callback(converter, (c, s) -> System.out.println(s));
        htmlToPdf.wkhtmltopdf_set_error_callback(converter, (c, s) -> System.out.println(s));
        htmlToPdf.wkhtmltopdf_set_progress_changed_callback(converter, (c, phaseProgress) -> {
            int phase = htmlToPdf.wkhtmltopdf_current_phase(c);
            int totalPhases = htmlToPdf.wkhtmltopdf_phase_count(c);
            String phaseDesc = htmlToPdf.wkhtmltopdf_phase_description(c, phase);
        });

        htmlToPdf.wkhtmltopdf_set_finished_callback(converter, (c, i) -> { });

        if (htmlToPdf.wkhtmltopdf_convert(converter) == 1) {
            PointerByReference pointerByReference = new PointerByReference();
            ByteByReference aa=new ByteByReference();
            long len = htmlToPdf.wkhtmltopdf_get_output(converter, pointerByReference);
            byte[] result = pointerByReference.getValue().getByteArray(0, (int) len);
            pointerByReference.setValue(Pointer.NULL);
            return result;
        } else
            return null;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.toString().equals("");
    }

    private void initFooter(PdfDocument doc, HtmlToPdf htmlToPdf) {
        if (doc.getFooter().getFontSize() != 0)
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.fontSize", String.valueOf(doc.getFooter().getFontSize()));

        if (doc.getFooter().getSpacing() != 0)
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.spacing", String.valueOf(doc.getFooter().getSpacing()));

        if (!isNullOrEmpty(doc.getFooter().getUrl()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.htmlUrl", doc.getFooter().getUrl());

        if (!isNullOrEmpty(doc.getFooter().getCenter()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.center", doc.getFooter().getCenter());

        if (!isNullOrEmpty(doc.getFooter().getLeft()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.left", doc.getFooter().getLeft());

        if (!isNullOrEmpty(doc.getFooter().getRight()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.right", doc.getFooter().getRight());

        if (doc.getFooter().isLine())
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.line", "true");

        if (!isNullOrEmpty(doc.getFooter().getFontName()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "footer.fontName", doc.getFooter().getFontName());
    }

    private void initHeader(PdfDocument doc, HtmlToPdf htmlToPdf) {
        if (doc.getHeader().getFontSize() != 0)
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.fontSize", String.valueOf(doc.getHeader().getFontSize()));

        if (doc.getHeader().getSpacing() != 0)
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.spacing", String.valueOf(doc.getHeader().getSpacing()));

        if (!isNullOrEmpty(doc.getHeader().getUrl()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.htmlUrl", doc.getHeader().getUrl());

        if (!isNullOrEmpty(doc.getHeader().getCenter()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.center", doc.getHeader().getCenter());

        if (!isNullOrEmpty(doc.getHeader().getLeft()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.left", doc.getHeader().getLeft());

        if (!isNullOrEmpty(doc.getHeader().getRight()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.right", doc.getHeader().getRight());

        if (doc.getHeader().isLine())
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.line", "true");

        if (!isNullOrEmpty(doc.getHeader().getFontName()))
            htmlToPdf.wkhtmltopdf_set_object_setting(object_settings, "header.fontName", doc.getHeader().getFontName());
    }
}

class PdfUtil {
    public static synchronized void create(String fileName, String zipName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("wkhtmltox.zip");
        File file = new File(String.format("%s\\FastHtmlToPdf", System.getProperty("java.io.tmpdir")));
        if (!file.exists())
            file.mkdirs();

        file = new File(fileName);
        if (file.exists())
            return;

        assert url != null;
        if (url.getPath().contains("BOOT-INF")) {
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
            jarZip(url, fileName, zipName);

        file = new File(zipName);
        file.delete();
    }

    private static void fileZip(URL url, String fileName) {
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

    private static void jarZip(URL url, String fileName, String zipName) {
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
                if (zipEntry.getName().contains(System.getProperty("sun.arch.data.model"))) {
                    InputStream in = zip.getInputStream(zipEntry);
                    File file = new File(fileName);
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    in.close();
                    break;
                }
            }
            zip.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}