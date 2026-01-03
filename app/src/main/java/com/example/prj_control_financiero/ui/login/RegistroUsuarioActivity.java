package com.example.prj_control_financiero.ui.login;

import android.os.Bundle;
import retrofit2.Call;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prj_control_financiero.R;
import com.example.prj_control_financiero.data.api.ApiClient;
import com.example.prj_control_financiero.data.api.ApiService;
import com.example.prj_control_financiero.data.models.UsuarioRequest;

import java.util.Map;

import retrofit2.Response;
import retrofit2.Callback;

public class RegistroUsuarioActivity extends AppCompatActivity {

    private EditText edt_registro_usuario_Username, edt_registro_usuario_Email, edt_registro_usuario_Pin, edt_registro_usuario_ConfirmPin;
    private Button btn_registro_usuario_Registrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edt_registro_usuario_Username = findViewById(R.id.edt_registro_usuario_Username);
        edt_registro_usuario_Email = findViewById(R.id.edt_registro_usuario_Email);
        edt_registro_usuario_Pin = findViewById(R.id.edt_registro_usuario_Pin);
        edt_registro_usuario_ConfirmPin = findViewById(R.id.edt_registro_usuario_ConfirmPin);
        btn_registro_usuario_Registrar = findViewById(R.id.btn_registro_usuario_Registrar);

        btn_registro_usuario_Registrar.setOnClickListener(v -> validarFormulario());

    }

    private void validarFormulario() {
        String username = edt_registro_usuario_Username.getText().toString().trim();
        String email = edt_registro_usuario_Email.getText().toString().trim();
        String pin = edt_registro_usuario_Pin.getText().toString().trim();
        String confirmPin = edt_registro_usuario_ConfirmPin.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || pin.isEmpty() || confirmPin.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pin.equals(confirmPin)) {
            Toast.makeText(this, "Los PIN no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        UsuarioRequest request = new UsuarioRequest(username, email, pin);

        apiService.registrarUsuario(request).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && Boolean.TRUE.equals(response.body().get("success"))) {

                    Toast.makeText(RegistroUsuarioActivity.this,
                            "Usuario creado correctamente",
                            Toast.LENGTH_SHORT).show();

                    finish(); // vuelve al login

                } else {
                    Toast.makeText(RegistroUsuarioActivity.this,
                            "Usuario ya existe o error en registro",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegistroUsuarioActivity.this,
                        "Error de conexión con el servidor",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}