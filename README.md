****Control Financiero App – Frontend (Android)****
Aplicación móvil desarrollada en Android (Java) que permite registrar ingresos y gastos personales, visualizar un resumen financiero y mostrar un gráfico de distribución de gastos por categoría.
Se comunica con un backend desarrollado en FastAPI mediante servicios REST.

******************************************************************************************
**Características Principales**

- Pantalla de autenticación por PIN.
- Registro de movimientos (ingresos y gastos).
- Selección intuitiva de categoría mediante íconos.
- Selección de fecha con DatePickerDialog.
- Visualización de resumen:
  - Total de ingresos
  - Total de gastos
  - Saldo disponible
  - Gráfico circular por categorías (MPAndroidChart)
- Integración total con backend REST.
- Validaciones de datos antes de enviar la información.

******************************************************************************************
****Tecnologías Utilizadas****
**Android / Java**
- Actividades (AppCompatActivity)
- Intents para navegación entre pantallas
- HttpURLConnection para consumo de API REST
- Hilos (Thread) para evitar bloqueo del UI Thread
- runOnUiThread para actualizar la interfaz desde procesos en background
- MPAndroidChart para los gráficos
- DatePickerDialog para selección de fecha
- Diseño basado en ConstraintLayout y LinearLayout

******************************************************************************************
****Dependencias externas****
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

******************************************************************************************
****Estructura del Proyecto****
app/
 └── java/com.example.prj_control_financiero/
      ├── MainActivity.java
      ├── DashboardActivity.java
      ├── RegistrarMovimientoActivity.java
      ├── PresupuestoActivity.java
      ├── ResumenMensualActivity.java
      └── data/models/MovimientoRequest.java

******************************************************************************************
****Flujo de Uso****
1. El usuario inicia sesión con el PIN.
2. La aplicación solicita al backend la validación del PIN.
3. Se abre el Dashboard con datos reales del backend.
4. El usuario puede:
   - Registrar un movimiento.
   - Ver su presupuesto
   - Revisar un resumen mensual
5. Cada movimiento registrado se envía al backend por POST.

******************************************************************************************
****Consumo de API****
**Login**
POST http://10.0.2.2:8000/login

**Body:**
{ "pin": "1234" }

**Registrar movimiento**
POST http://10.0.2.2:8000/movimiento

{
  "tipo": "Gasto",
  "categoria": "Alimentos",
  "monto": 25.5,
  "fecha": "11/12/2025"
}

**Obtener resumen**
GET http://10.0.2.2:8000/resumen

******************************************************************************************
****Backend del Proyecto****
El backend se encuentra en un repositorio independiente:
https://github.com/iHardyCM/backend-control-financiero
