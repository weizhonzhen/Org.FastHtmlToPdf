package Org.FastHtmlToPdf.Interop;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface HtmlToImage extends Library {

    int wkhtmltoimage_init(int useGraphics);

    Pointer wkhtmltoimage_create_global_settings();

    int wkhtmltoimage_convert(Pointer converter);


    int wkhtmltoimage_set_global_setting(Pointer settings, String name, String value);

    Pointer wkhtmltoimage_create_converter(Pointer globalSettings, byte[] data);

    long wkhtmltoimage_get_output(Pointer converter, PointerByReference data);


    void wkhtmltoimage_destroy_converter(Pointer converter);

    int wkhtmltoimage_deinit();
}
