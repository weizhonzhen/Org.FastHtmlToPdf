package Org.FastHtmlToPdf;

import Org.FastHtmlToPdf.Interop.FormatEnum;
import Org.FastHtmlToPdf.Interop.HtmlToImage;
import Org.FastHtmlToPdf.Model.ImageDocument;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FastHtmlToImage {
    private static HtmlToImage htmlToImage = null;
    private static Pointer global_settings = Pointer.NULL;
    private static Pointer converter = Pointer.NULL;
    private final static ExecutorService ex = Executors.newFixedThreadPool(1);

    public static byte[] convert(ImageDocument doc, String html) {
        try {
            Future<byte[]> result = ex.submit(new FastHtmlToImage.TaskResult(doc, html));
            return (byte[]) result.get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void create() throws Exception {
        if (htmlToImage == null) {
            htmlToImage = (new ImgUtil()).create();
            htmlToImage.wkhtmltoimage_init(0);
        }

        global_settings = htmlToImage.wkhtmltoimage_create_global_settings();
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.defaultEncoding", "utf-8");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.loadImages", "true");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "web.enableJavascript", "true");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.jsdelay", "1000");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.loadErrorHandling", "skip");
        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "load.debugJavascript", "true");
    }

    private static byte[] convertThread(ImageDocument doc, String html) throws Exception {
        if (doc == null)
            return null;

        if (doc.getWidth() != 0)
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "screenWidth", String.valueOf(doc.getWidth()));

        if (doc.getHeight() != 0)
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "screenHeight", String.valueOf(doc.getHeight()));

        htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "fmt", doc.getFormat());

        if (doc.isSmartWidth())
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "smartWidth", "true");

        if ((doc.getFormat().equals(FormatEnum.png.name()) || doc.getFormat().equals(FormatEnum.svg.name()) || doc.getFormat().equals(FormatEnum.bmp.name())) && doc.isTransparent())
            htmlToImage.wkhtmltoimage_set_global_setting(global_settings, "transparent", "true");

        converter = htmlToImage.wkhtmltoimage_create_converter(global_settings, html.getBytes(StandardCharsets.UTF_8));

        if (htmlToImage.wkhtmltoimage_convert(converter) == 1) {
            PointerByReference pointerByReference = new PointerByReference();
            long len = htmlToImage.wkhtmltoimage_get_output(converter, pointerByReference);
            byte[] result = pointerByReference.getValue().getByteArray(0, (int) len);
            htmlToImage.wkhtmltoimage_destroy_converter(converter);
            taskWait();
            return result;
        } else {
            htmlToImage.wkhtmltoimage_destroy_converter(converter);
            taskWait();
            return null;
        }
    }

    private static class TaskResult implements Callable<byte[]> {
        private ImageDocument doc;
        private String html;

        public TaskResult(ImageDocument doc, String html) {
            this.doc = doc;
            this.html = html;
        }

        @Override
        public byte[] call() throws Exception {
            FastHtmlToImage.create();
            return FastHtmlToImage.convertThread(doc, html);
        }
    }

    private static void taskWait() throws Exception {
        int num =(new Random()).nextInt(2) + 4;
        Thread.sleep(1000 * num);
    }

    static class ImgUtil{
        private String fileName = String.format("%sFastHtmlToImage\\wkhtmltox.dll", System.getProperty("java.io.tmpdir"));
        private String zipName = String.format("%sFastHtmlToImage\\wkhtmltox.zip", System.getProperty("java.io.tmpdir"));
        public HtmlToImage create() throws Exception {
            if(!System.getProperty("os.name").toLowerCase().startsWith("win"))
                throw new Exception("FastHtmlToPdf is on Windows");

            URL url = Thread.currentThread().getContextClassLoader().getResource("wkhtmltox.zip");
            File file = new File(String.format("%s\\FastHtmlToImage", System.getProperty("java.io.tmpdir")));
            if (!file.exists())
                file.mkdirs();

            file = new File(fileName);
            if (file.exists())
                return Native.load(fileName, HtmlToImage.class);

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

            return Native.load(fileName, HtmlToImage.class);
        }

        private void fileZip(URL url, String fileName){
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

        private void jarZip(URL url,String fileName,String zipName){
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
}
