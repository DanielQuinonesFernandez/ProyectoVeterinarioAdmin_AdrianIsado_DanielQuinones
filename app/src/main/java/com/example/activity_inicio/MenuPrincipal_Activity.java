package com.example.activity_inicio;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

public class MenuPrincipal_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        // Obtener referencias a los TextViews
        TextView btnVerCitas = findViewById(R.id.btnVerCitas);
        TextView btnVerUsuarios = findViewById(R.id.btnVerUsuarios);

        // Configurar OnClickListener para cada TextView
        btnVerCitas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la actividad correspondiente a GESTIONAR CITAS
                Intent intent = new Intent(MenuPrincipal_Activity.this, GestionarCitas_Activity.class);
                startActivity(intent);
            }
        });

        btnVerUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la actividad correspondiente a GESTIONAR USUARIOS
                Intent intent = new Intent(MenuPrincipal_Activity.this, GestionarUsuarios_Activity.class);
                startActivity(intent);
            }
        });

    }
}
