package com.example.p1pmdm.UI;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p1pmdm.DDBB.EntrenamientoLab;
import com.example.p1pmdm.R;
import com.example.p1pmdm.core.Entrenamiento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> itemList;
    private ArrayAdapter<String> listAdapter;
    private List<Entrenamiento> entrenamientos;
    private int posicion;
    private EditText fechaEd;
    private String fecha;
    private EntrenamientoLab mTrainLab;
    boolean doubleBackToExitPressedOnce = false;
    TextView nombre, edad, altura, peso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Crea el archivo para guardar las estadisticas
        nombre = findViewById(R.id.nombreTextView);
        edad = findViewById(R.id.edadTextView);
        altura = findViewById(R.id.alturaTextView);
        peso = findViewById(R.id.pesoTextView);


        File estadisticas = new File(MainActivity.this.getFilesDir(), "EstadisticasGenerales.txt");
        mTrainLab = EntrenamientoLab.get(this);
        this.entrenamientos = mTrainLab.getEntrenamientos();

        this.itemList = new ArrayList<>();

        ListView listView = this.findViewById(R.id.viewList1);
        FloatingActionButton floatButton = this.findViewById(R.id.floatingActionButton2);
        listView.setLongClickable(true);
        listAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1, this.itemList);
        listView.setAdapter(this.listAdapter);

        //Hay que recorrer todos los entrenamientos y meter su toString en itemList que es la que añade a la vista
        if (!entrenamientos.isEmpty()) {
            for (Entrenamiento entreno : entrenamientos) {
                String horasFormateadas, minutosFormateados, segundosFormateados;
                horasFormateadas = String.format("%02d", entreno.getHoras());
                minutosFormateados = String.format("%02d", entreno.getMinutos());
                segundosFormateados = String.format("%02d", entreno.getSegundos());
                String textoVisible = entreno.getFecha() +
                        "      Distancia: " + entreno.getDistancia() + "m" +
                        "\n                             Tiempo: " + horasFormateadas + ":" + minutosFormateados + ":" + segundosFormateados;
                MainActivity.this.listAdapter.add(textoVisible);
                MainActivity.this.listAdapter.notifyDataSetChanged();
            }
        }

        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    posicion = position;
                    MainActivity.this.mostrar(posicion);
                }
            }
        });
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                LLamar a una nueva actividad, que se encargue de hacer tod el proceso de añadit a la BD
                que devuelva el id del objeto entrenamiento para poder añadirlo a la lista y usar el adapter para
                mostrarlo por pantalla.
                 */
                launchAddActivity(v);
            }
        });

        ImageButton buttonEditar = findViewById(R.id.botonEditar);
        buttonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarDatosPersonales();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.misPreferencias), Context.MODE_PRIVATE);
        nombre.setText(sharedPreferences.getString(getString(R.string.nombre), ""));
        edad.setText(sharedPreferences.getString(getString(R.string.edad), ""));
        altura.setText(sharedPreferences.getString(getString(R.string.altura), ""));
        peso.setText(sharedPreferences.getString(getString(R.string.peso), ""));

    }

    @Override
    protected void onStop() {
        super.onStop();
        int metrosTotales = 0;
        double minsKmTotales = 0;
        double kmTotales;
        double mediaMinsKm;
        double minutosKmEach;

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("EstadisticasGenerales.txt", Context.MODE_PRIVATE);
            for (Entrenamiento ent : entrenamientos) {
                metrosTotales = metrosTotales + ent.getDistancia();
                //Calcular los mins por km de cada entrenamiento
                minutosKmEach = (1000 * ((ent.getHoras() * 60) + ent.getMinutos())) / ent.getDistancia();
                minsKmTotales = minsKmTotales + minutosKmEach;
            }
            kmTotales = (double) metrosTotales / 1000;
            mediaMinsKm = minsKmTotales / entrenamientos.size();

            String estadisticas = "Estadisticas Generales\n\n" + "Kilometros totales recorridos: " + String.format("%.2f", kmTotales) + "\n\nMedia de minutos por kilometro recorrido: " + String.format("%.2f", mediaMinsKm);
            outputStream.write(estadisticas.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TextView nombre, edad, altura, peso;
        nombre = findViewById(R.id.nombreTextView);
        edad = findViewById(R.id.edadTextView);
        altura = findViewById(R.id.alturaTextView);
        peso = findViewById(R.id.pesoTextView);

        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.misPreferencias), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.nombre), nombre.getText().toString());
        editor.putString(getString(R.string.edad), edad.getText().toString());
        editor.putString(getString(R.string.altura), altura.getText().toString());
        editor.putString(getString(R.string.peso), peso.getText().toString());
        editor.apply();
    }

    public void launchAddActivity(View view) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            String id = data.getStringExtra("uuid");
            Entrenamiento trainSecondActivity = mTrainLab.getEntrenamiento(id);
            String horasFormateadas = getString(R.string._00);
            String minsFormateados = getString(R.string._00);
            String segsFormateados = getString(R.string._00);
            horasFormateadas = String.format("%02d", trainSecondActivity.getHoras());
            minsFormateados = String.format("%02d", trainSecondActivity.getMinutos());
            segsFormateados = String.format("%02d", trainSecondActivity.getSegundos());
            //Aqui hacer tod para meter en las listas lo necesario para que aparezca en vista
            String textoVisible = trainSecondActivity.getFecha() +
                    "      Distancia: " + trainSecondActivity.getDistancia() + "m" +
                    "\n                             Tiempo: " + horasFormateadas + ":" + minsFormateados + ":" + segsFormateados;
            MainActivity.this.listAdapter.add(textoVisible);
            MainActivity.this.listAdapter.notifyDataSetChanged();
            MainActivity.this.entrenamientos.add(trainSecondActivity);
        }
        if (resultCode == 3) {
            String id = data.getStringExtra("uuid");
            Entrenamiento trainModificarActivity = mTrainLab.getEntrenamiento(id);
            String horasFormateadas = getString(R.string._00);
            String minsFormateados = getString(R.string._00);
            String segsFormateados = getString(R.string._00);
            horasFormateadas = String.format("%02d", trainModificarActivity.getHoras());
            minsFormateados = String.format("%02d", trainModificarActivity.getMinutos());
            segsFormateados = String.format("%02d", trainModificarActivity.getSegundos());
            //Aqui hacer tod para meter en las listas lo necesario para que aparezca en vista
            String textoVisible = trainModificarActivity.getFecha() +
                    " Distancia: " + trainModificarActivity.getDistancia() + "m" +
                    " Tiempo: " + horasFormateadas + ":" + minsFormateados + ":" + segsFormateados;
            itemList.remove(posicion);
            MainActivity.this.listAdapter.add(textoVisible);
            MainActivity.this.listAdapter.notifyDataSetChanged();
        }
        if (resultCode == 4) {
            nombre = findViewById(R.id.nombreTextView);
            edad = findViewById(R.id.edadTextView);
            altura = findViewById(R.id.alturaTextView);
            peso = findViewById(R.id.pesoTextView);
            String nombreDato, edadDato, alturaDato, pesoDato;
            nombreDato = data.getStringExtra("nombre");
            edadDato = data.getStringExtra("edad");
            alturaDato = data.getStringExtra("altura");
            pesoDato = data.getStringExtra("peso");


            nombre.setText(nombreDato);
            edad.setText(edadDato);
            altura.setText(alturaDato);
            peso.setText(pesoDato);



        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Presiona atrás de nuevo para salir.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

//    public void addTraining() {
//
//        final Entrenamiento[] train = new Entrenamiento[1];
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final View customLayout = getLayoutInflater().inflate(R.layout.alert_dialog_train, null);
//        builder.setView(customLayout);
//        TextView title = new TextView(this);
//        title.setText(R.string.alDiag_train);
//        title.setTextSize(25);
//        title.setPadding(25, 25, 25, 25);
//        title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        title.setTextColor(getResources().getColor(R.color.blanco));
//        title.setGravity(Gravity.CENTER);
//        builder.setCustomTitle(title);
//
//        fechaEd = customLayout.findViewById(R.id.fechaEditText);
//        fechaEd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDatePickerDialog();
//            }
//        });
//
//        final EditText distEd = customLayout.findViewById(R.id.distanciaEditText);
//        final EditText horasEd = customLayout.findViewById(R.id.horasEditText);
//        final EditText minsEd = customLayout.findViewById(R.id.minutosEditText);
//        minsEd.setFilters(new InputFilter[]{new InputFilterMinMax(getString(R.string.minSegs), getString(R.string.maxSegs))});
//        final EditText segsEd = customLayout.findViewById(R.id.segundosEditText);
//        segsEd.setFilters(new InputFilter[]{new InputFilterMinMax(getString(R.string.minSegs), getString(R.string.maxSegs))});
//        builder.setPositiveButton(R.string.alDiag_posButton, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                int dist = 0;
//                int horas = 0;
//                int mins = 0;
//                int segs = 0;
//                String horasFormateadas = getString(R.string._00);
//                String minsFormateados = getString(R.string._00);
//                String segsFormateados = getString(R.string._00);
//
//                double minsKm;
//
//                if (horasEd.getText().toString().isEmpty() && minsEd.getText().toString().isEmpty() && segsEd.getText().toString().isEmpty() && distEd.getText().toString().isEmpty()) {
//                    Toast toast = Toast.makeText(MainActivity.this, "No se admiten todos los campos vacíos.", Toast.LENGTH_LONG);
//                    toast.show();
//                    addTraining();
//                } else if (horasEd.getText().toString().isEmpty() && minsEd.getText().toString().isEmpty() && segsEd.getText().toString().isEmpty() || distEd.getText().toString().isEmpty()) {
//                    Toast toast = Toast.makeText(MainActivity.this, "Es necesario introducir una distancia o alguna unidad de tiempo.", Toast.LENGTH_LONG);
//                    toast.show();
//                    addTraining();
//                } else {
//                    if (!horasEd.getText().toString().isEmpty()) {
//                        horas = Integer.parseInt(horasEd.getText().toString());
//                        horasFormateadas = String.format("%02d", horas); //Deberia poner los numeros con dos cifras rellenando con 0s
//                    }
//                    if (!minsEd.getText().toString().isEmpty()) {
//                        mins = Integer.parseInt(minsEd.getText().toString());
//                        minsFormateados = String.format("%02d", mins);
//                    }
//                    if (!segsEd.getText().toString().isEmpty()) {
//                        segs = Integer.parseInt(segsEd.getText().toString());
//                        segsFormateados = String.format("%02d", segs);
//                    }
//                    if (!distEd.getText().toString().isEmpty()) {
//                        dist = Integer.parseInt(distEd.getText().toString());
//                    }
//                    if (fechaEd.getText().toString().isEmpty()) {
//                        int day, month, year;
//                        Calendar calendario = Calendar.getInstance();
//                        day = calendario.get(Calendar.DAY_OF_MONTH);
//                        month = calendario.get(Calendar.MONTH);
//                        year = calendario.get(Calendar.YEAR);
//                        String selectedDate = day + "/" + month + "/" + year;
//                        fecha = selectedDate;
//                    }
//
//                    minsKm = (double) (((horas * 60 + mins) * 1000) / dist);
//
//
//                    train[0] = new Entrenamiento(fecha, dist, horas, mins, segs, minsKm);
//
//                    if (horas == 0 && mins == 0 && segs == 0) {
//                        Toast toast = Toast.makeText(MainActivity.this, "El tiempo no puede ser 00:00:00", Toast.LENGTH_LONG);
//                        toast.show();
//                        addTraining();
//                    } else {
//                        String textoVisible = fecha +
//                                " Distancia: " + train[0].getDistancia() + "m" +
//                                " Tiempo: " + horasFormateadas + ":" + minsFormateados + ":" + segsFormateados;
//
//
//                        //Añadir a la base de datos
//                        mTrainLab.addTrain(train[0]);
//
//                        //Añade el texto formateado
//                        MainActivity.this.listAdapter.add(textoVisible);
//                        MainActivity.this.listAdapter.notifyDataSetChanged();
//                        MainActivity.this.entrenamientos.add(train[0]);
//                    }
//                }
//            }
//        });
//
//        builder.setNegativeButton(R.string.alDiag_canButton, null);
//        final AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    public void mostrar(int position) {
        String formattedVelMedia = "";
        String formattedMinsKm = "";
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        View customLayout = getLayoutInflater().inflate(R.layout.mostrar_lay, null);
        dialogBuilder.setView(customLayout);

        TextView title = new TextView(this);
        title.setText(R.string.training);
        title.setTextSize(25);
        title.setPadding(25, 25, 25, 25);
        title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        title.setTextColor(getResources().getColor(R.color.blanco));
        title.setGravity(Gravity.CENTER);
        dialogBuilder.setCustomTitle(title);

        TextView fechaTextView = customLayout.findViewById(R.id.textViewFecha);
        fechaTextView.setText(getString(R.string.fechaTextView) + MainActivity.this.entrenamientos.get(position).getFecha());

        TextView distanciaTextView = customLayout.findViewById(R.id.textViewDistancia);
        distanciaTextView.setText(getString(R.string.distanciaTextView) + MainActivity.this.entrenamientos.get(position).getDistancia() + getString(R.string.metros));

        TextView tiempoTextView = customLayout.findViewById(R.id.textViewTiempo);
        tiempoTextView.setText(getString(R.string.tiempoTextView) + String.format("%02d", MainActivity.this.entrenamientos.get(position).getHoras()) + ":" + String.format("%02d", MainActivity.this.entrenamientos
                .get(position).getMinutos()) + ":" + String.format("%02d", MainActivity.this.entrenamientos.get(position).getSegundos()));

        int minutosHora = (MainActivity.this.entrenamientos.get(position).getHoras() * 60) + MainActivity.this.entrenamientos
                .get(position).getMinutos();

        double minutosKm;
        double kilometros = (double) MainActivity.this.entrenamientos.get(position).getDistancia() / (double) 1000;
        if (MainActivity.this.entrenamientos.get(position).getDistancia() != 0) {
            minutosKm = (double) minutosHora / kilometros;
            formattedMinsKm = String.format("%.2f", minutosKm);
        }

        TextView minsxKm = customLayout.findViewById(R.id.textViewMinsKm);
        minsxKm.setText(getString(R.string.minsKm) + formattedMinsKm);
        double velMedia;
        double horas;
        horas = ((double) MainActivity.this.entrenamientos.get(position).getHoras() + ((double) MainActivity.this.entrenamientos.get(position).getMinutos() / (double) 60));
        if (horas != 0) {
            velMedia = ((double) MainActivity.this.entrenamientos.get(position).getDistancia() / (double) 1000) / horas;
            formattedVelMedia = String.format("%.2f", velMedia);
        }

        TextView velMed = customLayout.findViewById(R.id.textViewVelMedia);
        velMed.setText(getString(R.string.velMed) + formattedVelMedia + getString(R.string.kmH));
        dialogBuilder.create().show();
    }

//    public void modificar() {
//        final Entrenamiento[] train = new Entrenamiento[1];
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final View customLayout = getLayoutInflater().inflate(R.layout.alert_dialog_train, null);
//        builder.setView(customLayout);
//        train[0] = entrenamientos.get(posicion);
//        TextView title = new TextView(this);
//        title.setText(R.string.modificar);
//        title.setTextSize(25);
//        title.setPadding(25, 25, 25, 25);
//        title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        title.setTextColor(getResources().getColor(R.color.blanco));
//        title.setGravity(Gravity.CENTER);
//        builder.setCustomTitle(title);
//
//        fechaEd = customLayout.findViewById(R.id.fechaEditText);
//        fechaEd.setHint(entrenamientos.get(posicion).getFecha());
//        fechaEd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDatePickerDialog();
//            }
//        });
//
//        final EditText distEd = customLayout.findViewById(R.id.distanciaEditText);
//        distEd.setHint(String.valueOf(entrenamientos.get(posicion).getDistancia()));
//        final EditText horasEd = customLayout.findViewById(R.id.horasEditText);
//        horasEd.setHint(String.valueOf(entrenamientos.get(posicion).getHoras()));
//        final EditText minsEd = customLayout.findViewById(R.id.minutosEditText);
//        minsEd.setHint(String.valueOf(entrenamientos.get(posicion).getMinutos()));
//        final EditText segsEd = customLayout.findViewById(R.id.segundosEditText);
//        segsEd.setHint(String.valueOf(entrenamientos.get(posicion).getSegundos()));
//
//        final String[] horasFormateadas = new String[1];
//        final String[] minsFormateados = new String[1];
//        final String[] segsFormateados = new String[1];
//
//
//        builder.setPositiveButton(R.string.alDiag_posButton, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                int horas = entrenamientos.get(posicion).getHoras();
//                horasFormateadas[0] = String.format("%02d", horas);
//                if (!horasEd.getText().toString().isEmpty()) {
//                    horas = Integer.parseInt(horasEd.getText().toString());
//                    horasFormateadas[0] = String.format("%02d", horas);
//                }
//
//                int mins = entrenamientos.get(posicion).getMinutos();
//                minsFormateados[0] = String.format("%02d", mins);
//                if (!minsEd.getText().toString().isEmpty()) {
//                    mins = Integer.parseInt(minsEd.getText().toString());
//                    minsFormateados[0] = String.format("%02d", mins);
//                }
//
//                int segs = entrenamientos.get(posicion).getSegundos();
//                segsFormateados[0] = String.format("%02d", segs);
//                if (!segsEd.getText().toString().isEmpty()) {
//                    segs = Integer.parseInt(segsEd.getText().toString());
//                    segsFormateados[0] = String.format("%02d", segs);
//                }
//
//                int dist = entrenamientos.get(posicion).getDistancia();
//                if (!distEd.getText().toString().isEmpty()) {
//                    dist = Integer.parseInt(distEd.getText().toString());
//                }
//                String fechaAntigua = entrenamientos.get(posicion).getFecha();
//                if (!fechaEd.getText().toString().isEmpty()) {
//                    fechaAntigua = fechaEd.getText().toString();
//                }
//                double minsKm = entrenamientos.get(posicion).getMinsKm();
//
//
//                train[0].setFecha(fechaAntigua);
//                train[0].setDistancia(dist);
//                train[0].setHoras(horas);
//                train[0].setMinutos(mins);
//                train[0].setSegundos(segs);
//                train[0].setMinsKm(minsKm);
//
//                if (horas == 0 && mins == 0 && segs == 0) {
//                    Toast toast = Toast.makeText(MainActivity.this, "El tiempo no puede ser 00:00:00", Toast.LENGTH_LONG);
//                    toast.show();
//                    modificar();
//                } else {
//                    String textoVisible = fechaAntigua +
//                            " Distancia: " + train[0].getDistancia() + "m" +
//                            " Tiempo: " + horasFormateadas[0] + ":" + minsFormateados[0] + ":" + segsFormateados[0];
//
//
//                    //Modificar en la base de datos
//                    mTrainLab.updateTrain(train[0]);
//
//                    MainActivity.this.listAdapter.remove(itemList.get(posicion));
//                    MainActivity.this.listAdapter.add(textoVisible);
//                    MainActivity.this.listAdapter.notifyDataSetChanged();
//                    MainActivity.this.entrenamientos.remove(posicion);
//                    MainActivity.this.entrenamientos.add(posicion, train[0]);
//                }
//
//
//            }
//        });
//
//        builder.setNegativeButton(R.string.alDiag_canButton, null);
//        builder.create().show();
//
//    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contextual, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mostrar) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            posicion = (int) info.id;
            mostrar(posicion);
        } else if (item.getItemId() == R.id.modificar) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            posicion = (int) info.id;
            Entrenamiento train = entrenamientos.get(posicion);
            String id = train.getIdTrain();
            //Enviar id de la posición a la segunda activity
            Intent intent = new Intent(MainActivity.this, ModificarActivity.class);
            intent.putExtra("uuid", id);
            startActivityForResult(intent, 3);

        } else if (item.getItemId() == R.id.borrar) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            posicion = (int) info.id;
            itemList.remove(posicion);

            //Borrar elemento de la base de datos
            mTrainLab.delTrain(entrenamientos.get(posicion));

            entrenamientos.remove(posicion);
            listAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.datosPersonales) {
            //Lamzar nueva activity y guardar los datos en las shared
            nombre = findViewById(R.id.nombreTextView);
            edad = findViewById(R.id.edadTextView);
            altura = findViewById(R.id.alturaTextView);
            peso = findViewById(R.id.pesoTextView);
            String textoNombre = nombre.getText().toString();
            String textoEdad = edad.getText().toString();
            String textoAltura = altura.getText().toString();
            String textoPeso = peso.getText().toString();

            Intent intent = new Intent(MainActivity.this, DatosPersonalesActivity.class);
            intent.putExtra("nombre", textoNombre)
                    .putExtra("edad", textoEdad)
                    .putExtra("altura", textoAltura)
                    .putExtra("peso", textoPeso);
            startActivityForResult(intent, 4);
            iniciarDatosPersonales();


        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.stats) {
            int distanciaTotal = kmTotales();
            double minsKmtotales = minutosKmTotales();

            double velocidadeMediaKMH = calcularMediaVelocidadesKMH();
            double tiempoTotalMinutos = tiempoTotalMinutos();
            double tiempoTotalHoras = tiempoTotalHoras();

            Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
            intent.putExtra("distanciaTotal", distanciaTotal)
                    .putExtra("minsKmtotales", minsKmtotales)
                    .putExtra("tiempoTotalMinutos", tiempoTotalMinutos)
                    .putExtra("tiempoTotalHoras", tiempoTotalHoras)
                    .putExtra("mediaVelocidadesKMH", velocidadeMediaKMH);

            startActivity(intent);
            return true;

        } else if (id == R.id.add) {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(intent, 2);
            return true;
        } else if (id == R.id.mod) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ArrayAdapter<String> listAdapterModificar = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_single_choice, this.itemList);

            TextView title = new TextView(this);
            title.setText(R.string.modificar);
            title.setTextSize(25);
            title.setPadding(25, 25, 25, 25);
            title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            title.setTextColor(getResources().getColor(R.color.blanco));
            title.setGravity(Gravity.CENTER);
            builder.setCustomTitle(title);

            int checkedItem = -1;
            builder.setSingleChoiceItems(listAdapterModificar, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    posicion = which;

                }
            });
            builder.setPositiveButton(R.string.alDiag_posButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    modificar();

                    Entrenamiento train = entrenamientos.get(posicion);
                    String id = train.getIdTrain();
                    //Enviar id de la posición a la segunda activity
                    Intent intent = new Intent(MainActivity.this, ModificarActivity.class);
                    intent.putExtra("uuid", id);
                    startActivityForResult(intent, 3);
                }

            });
            builder.setNegativeButton(R.string.alDiag_canButton, null);
            builder.create().show();
            return true;
        } else if (id == R.id.clear) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            TextView title = new TextView(this);
            title.setText(R.string.eliminar);
            title.setTextSize(25);
            title.setPadding(25, 25, 25, 25);
            title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            title.setTextColor(getResources().getColor(R.color.blanco));
            title.setGravity(Gravity.CENTER);
            builder.setCustomTitle(title);

            final ArrayList<Integer> itemsSelected = new ArrayList<>();
            CharSequence[] cs = itemList.toArray(new CharSequence[itemList.size()]);
            builder.setMultiChoiceItems(cs, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        itemsSelected.add(posicion);
                    } else if (itemsSelected.contains(posicion)) {
                        itemsSelected.remove(Integer.valueOf(posicion));
                    }
                }
            });

            builder.setPositiveButton(R.string.alDiag_posButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder aceptarBuilder = new AlertDialog.Builder(MainActivity.this);
                    aceptarBuilder.setMessage("¿Estas seguro?");

                    aceptarBuilder.setPositiveButton(R.string.alDiag_posButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i : itemsSelected) {
                                itemList.remove(i);
                                mTrainLab.delTrain(entrenamientos.get(i));
                                entrenamientos.remove(i);
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    aceptarBuilder.setNegativeButton(R.string.alDiag_canButton, null);
                    aceptarBuilder.create().show();
                }
            });

            builder.setNegativeButton(R.string.alDiag_canButton, null);
            builder.create().show();

            return true;
        } else if (item.getItemId() == R.id.datosPersonales) {
            //Lamzar nueva activity y guardar los datos en las shared

            String textoNombre = nombre.getText().toString();
            String textoEdad = edad.getText().toString();
            String textoAltura = altura.getText().toString();
            String textoPeso = peso.getText().toString();

            Intent intent = new Intent(MainActivity.this, DatosPersonalesActivity.class);
            intent.putExtra("nombre", textoNombre)
                    .putExtra("edad", textoEdad)
                    .putExtra("altura", textoAltura)
                    .putExtra("peso", textoPeso);
            startActivityForResult(intent, 4);

            return true;
        } else {
            return false;
        }
    }

    private void showDatePickerDialog() {
        int day, month, year;
        final Calendar calendario = Calendar.getInstance();
        day = calendario.get(Calendar.DAY_OF_MONTH);
        month = calendario.get(Calendar.MONTH);
        year = calendario.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth) {
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendario.set(Calendar.MONTH, month1);
                calendario.set(Calendar.YEAR, year1);
                String selectedDate = dayOfMonth + "/" + month1 + "/" + year1;
                fecha = selectedDate;
                fechaEd.setText(selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }


    private double calcularMediaVelocidadesKMH(){
        double velocidadMediaTotal = 0.0;
        for (Entrenamiento entrenamiento : entrenamientos){
            double velodidadMedia =  ((double)(entrenamiento.getDistancia()/1000)/(double)(entrenamiento.getHoras()+(entrenamiento.getMinutos()/60)+(entrenamiento.getSegundos()/3600)));
            velocidadMediaTotal += velodidadMedia;
        }
        return velocidadMediaTotal/entrenamientos.size();
    }

    private int kmTotales(){
        int kmTotales = 0;
        for (Entrenamiento entrenamiento : entrenamientos){
            int km = entrenamiento.getDistancia();
            kmTotales += km;
        }
        return kmTotales;
    }

    private double minutosKmTotales(){
        double minsKmTotales = 0;
        for (Entrenamiento entrenamiento : entrenamientos){
            double minsKm = entrenamiento.getMinsKm();
            minsKmTotales += minsKm;
        }
        return minsKmTotales;
    }
    private double tiempoTotalMinutos (){
        double minutosTotales = 0.0;
        for (Entrenamiento entrenamiento : entrenamientos){
            double minutos = (double) ( (entrenamiento.getHoras()*60) + (entrenamiento.getMinutos()) + (entrenamiento.getSegundos()/60));
            minutosTotales += minutos;
        }
        return minutosTotales;
    }
    private double tiempoTotalHoras (){
        double horasTotales = 0.0;
        for (Entrenamiento entrenamiento : entrenamientos){
            double horas = (double) ( (entrenamiento.getHoras()) + (entrenamiento.getMinutos()/60) + (entrenamiento.getSegundos()/3600));
            horasTotales += horas;
        }
        return horasTotales;
    }

    private void iniciarDatosPersonales(){
        String textoNombre = nombre.getText().toString();
        String textoEdad = edad.getText().toString();
        String textoAltura = altura.getText().toString();
        String textoPeso = peso.getText().toString();

        Intent intent = new Intent(MainActivity.this, DatosPersonalesActivity.class);
        intent.putExtra("nombre", textoNombre)
                .putExtra("edad", textoEdad)
                .putExtra("altura", textoAltura)
                .putExtra("peso", textoPeso);
        startActivityForResult(intent, 4);
    }
}
