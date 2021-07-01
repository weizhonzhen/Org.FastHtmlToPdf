package Org.FastHtmlToPdf.Interop;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface HtmlToPdf extends Library {
    void wkhtmltopdf_init(int use_graphics);

    Pointer wkhtmltopdf_create_global_settings();

    Pointer wkhtmltopdf_create_converter(Pointer converter);

    Pointer wkhtmltopdf_create_object_settings();


    void wkhtmltopdf_set_object_setting(Pointer object_settings, String name, String value);

    void wkhtmltopdf_set_global_setting(Pointer global_settings, String name, String value);

    void wkhtmltopdf_add_object(Pointer converter, Pointer object_settings, byte[] html);

    int wkhtmltopdf_convert(Pointer converter);

    long wkhtmltopdf_get_output(Pointer converter, PointerByReference data);


    void wkhtmltopdf_destroy_converter(Pointer converter);

    void wkhtmltopdf_destroy_object_settings(Pointer object_settings);

    void wkhtmltopdf_destroy_global_settings(Pointer global_settings);

    void wkhtmltopdf_deinit();
}
