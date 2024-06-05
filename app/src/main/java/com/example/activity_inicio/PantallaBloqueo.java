package com.example.activity_inicio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.Locale;

public class PantallaBloqueo extends AppCompatActivity {

    private TextView textViewTiempoRestante;
    private CountDownTimer countDownTimer;
    private SharedPreferences sharedPreferences;
    private long tiempoInicial;
    final private long tiempoEspera = 3 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_bloqueo);

        textViewTiempoRestante = findViewById(R.id.textViewTiempoRestante);
        sharedPreferences = getSharedPreferences("contador", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("bloqueado", true);
        editor.apply();

        // Obtener la marca de tiempo inicial del contador
        tiempoInicial = sharedPreferences.getLong("tiempo_inicial", 0);

        if (tiempoInicial == 0) {
            // Si la marca de tiempo inicial es 0, significa que el contador no se ha iniciado antes
            // Así que configuramos el tiempo inicial y lo almacenamos en SharedPreferences
            tiempoInicial = System.currentTimeMillis();
            editor = sharedPreferences.edit();
            editor.putLong("tiempo_inicial", tiempoInicial);
            editor.apply();
        }

        // Calcular el tiempo restante basado en la diferencia entre el tiempo actual y la marca de tiempo inicial
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicial;
        long tiempoRestante = tiempoEspera - tiempoTranscurrido; // 3 horas en milisegundos menos el tiempo transcurrido
        iniciarContador(tiempoRestante);
    }

    private void iniciarContador(long tiempoRestante) {
        countDownTimer = new CountDownTimer(tiempoRestante, 1000) { // Contar cada segundo
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar el TextView con el tiempo restante
                long segundosRestantes = millisUntilFinished / 1000;
                long horas = segundosRestantes / 3600;
                long minutos = (segundosRestantes % 3600) / 60;
                long segundos = segundosRestantes % 60;

                String tiempoRestanteStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
                textViewTiempoRestante.setText(tiempoRestanteStr);
            }

            @Override
            public void onFinish() {
                // Verificar si el contador ha llegado a cero
                if (textViewTiempoRestante.getText().toString().equals("00:00:00") || textViewTiempoRestante.getText().toString().equals("") || textViewTiempoRestante.getText() == null){
                    // Eliminar los intentos fallidos
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("bloqueado", false);
                    editor.putInt("intentos_fallidos", 0);
                    editor.remove("tiempo_inicial");
                    editor.apply();

                    // Redirigir al usuario a la actividad de inicio de sesión
                    Intent intent = new Intent(PantallaBloqueo.this, IniciarSesion_Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Limpiar la pila de actividades
                    startActivity(intent);
                    finish(); // Cerrar la actividad actual
                }
            }
        };

        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el contador cuando la actividad se destruye para evitar fugas de memoria
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}