package Org.FastHtmlToPdf.Interop;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface HtmlToPdf extends Library {

    interface StringCallback extends Callback {
        void callback(Pointer converter, String str);
    }

    interface IntCallback extends Callback {
        void callback(Pointer converter, int i);
    }

    void wkhtmltopdf_init(int use_graphics);

    Pointer wkhtmltopdf_create_global_settings();

    Pointer wkhtmltopdf_create_converter(Pointer converter);

    Pointer wkhtmltopdf_create_object_settings();


    void wkhtmltopdf_set_object_setting(Pointer object_settings, String name, String value);

    void wkhtmltopdf_set_global_setting(Pointer global_settings, String name, String value);

    void wkhtmltopdf_add_object(Pointer converter, Pointer object_settings, byte[] html);

    int wkhtmltopdf_convert(Pointer converter);

    void wkhtmltopdf_set_warning_callback(Pointer converter, StringCallback callback);

    void wkhtmltopdf_set_error_callback(Pointer converter, StringCallback callback);

    void wkhtmltopdf_set_progress_changed_callback(Pointer converter, IntCallback callback);

    void wkhtmltopdf_set_finished_callback(Pointer converter, IntCallback callback);

    long wkhtmltopdf_get_output(Pointer converter, PointerByReference data);


    int wkhtmltopdf_current_phase(Pointer converter);

    int wkhtmltopdf_phase_count(Pointer converter);

    String wkhtmltopdf_phase_description(Pointer converter, int phase);

    String wkhtmltopdf_progress_string(Pointer converter);


    void wkhtmltopdf_destroy_converter(Pointer converter);

    void wkhtmltopdf_destroy_object_settings(Pointer object_settings);

    void wkhtmltopdf_destroy_global_settings(Pointer global_settings);

    void wkhtmltopdf_deinit();
}
