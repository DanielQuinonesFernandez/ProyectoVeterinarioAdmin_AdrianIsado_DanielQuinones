package es.dam.model.objetos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.activity_inicio.IniciarSesion_Activity;
import com.example.activity_inicio.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private Context context;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(com.example.activity_inicio.R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Arrancamos la siguiente actividad
                Intent mainIntent = new Intent().setClass(context, IniciarSesion_Activity.class);
                startActivity(mainIntent);

                // Cerramos esta actividad para que el usuario no pueda volver a ella mediante botón de volver atras
                finish();
            }
        };


        // Simulamos un tiempo en el proceso de carga durante el cual mostramos el splash screen
        Timer timer = new Timer();
        timer.schedule(task, 2000); //Tiempo de espera del temporizador en milisegundos
    }
}