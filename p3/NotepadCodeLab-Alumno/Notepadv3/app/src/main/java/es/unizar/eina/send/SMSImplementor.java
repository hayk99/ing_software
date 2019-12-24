package es.unizar.eina.send;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

/**
 * Created by usuario on 15/11/15.
 */
public class SMSImplementor implements SendImplementor {

    /** actividad desde la cual se abrir� la actividad de gesti�n de correo */
    private Activity sourceActivity;

    /** Constructor
     * @param source actividad desde la cual se abrir� la actividad de gesti�n de correo
     */
    public SMSImplementor(Activity source){
        setSourceActivity(source);
    }

    /**  Actualiza la actividad desde la cual se abrir� la actividad de gesti�n de correo */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /**  Recupera la actividad desde la cual se abrirá la actividad de gestión de correo */
    public Activity getSourceActivity(){
        return sourceActivity;
    }

    /**
     * Implementaci�n del m�todo send utilizando la aplicaci�n de gesti�n de correo de Android
     * Solo se copia el asunto y el cuerpo
     * @param subject asunto
     * @param body cuerpo del mensaje
     */
    public void send (String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("sms_body", subject+": "+body);
        intent.setType("vnd.android-dir/mms-sms");
        getSourceActivity().startActivity(Intent.createChooser(intent, "Send SMS..."));
    }

}
