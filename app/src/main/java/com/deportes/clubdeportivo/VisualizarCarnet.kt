package com.deportes.clubdeportivo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream



class VisualizarCarnet : AppCompatActivity() {

    private var mostrandoFrente = true
    // Variables para almacenar los IDs de las imágenes
    private var carnetFrontResId: Int = 0
    private var carnetBackResId: Int = 0

    // Vistas que se usarán en el listener y que necesitan ser accesibles
    private lateinit var imageViewCarnet: ImageView
    private lateinit var carnetContentLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_carnet)

        // Lógica de la barra superior
        val btnAtras: ImageView = findViewById(R.id.buttonBack)
        val textViewTitulo: TextView = findViewById(R.id.textViewTitle)

        btnAtras.setOnClickListener {
            finish()
        }
        textViewTitulo.text = "Carnet"

        // Capturamos elementos de la vista
        val textViewNombreCliente = findViewById<TextView>(R.id.TextViewNombrecliente)
        val textViewApellidoCliente = findViewById<TextView>(R.id.TextViewApellidoCliente)
        val textViewIdCliente = findViewById<TextView>(R.id.TextViewIdCliente)
        val textViewDniCliente = findViewById<TextView>(R.id.TextViewDniCliente)
        val textViewAptoFisicoCliente = findViewById<TextView>(R.id.TextViewAptoFisicoCliente)
        val textViewMailCliente = findViewById<TextView>(R.id.TextViewMailCliente)
        val textViewFechaExpiracion = findViewById<TextView>(R.id.TextViewFechaExpiracion)

        // Inicializamos las vistas que se usarán en el listener
        imageViewCarnet = findViewById(R.id.carnetTemplateImageView)
        carnetContentLayout = findViewById(R.id.carnetContentConstraintLayout)

        // Recuperamos los datos del intent anterior
        val idCliente = intent.getStringExtra("idCliente")
        val nombreCliente = intent.getStringExtra("nombreCliente")
        val apellidoCliente = intent.getStringExtra("apellidoCliente")
        val dniCliente = intent.getStringExtra("dniCliente")
        val email = intent.getStringExtra("email")
        val aptoFisico = intent.getIntExtra("aptoFisico", -1) // "true" o "false"
        val fechaDeExpiracion = intent.getStringExtra("fechaDeExpiracion")
        val condSocio = intent.getIntExtra("condSocio", -1) // "true" o "false"

        // --- Asignación de imágenes del carnet según condSocio ---
        if (condSocio == 1) {
            carnetFrontResId = R.drawable.carnet_front_socio
            carnetBackResId = R.drawable.carnet_back_socio
            textViewFechaExpiracion.text = fechaDeExpiracion // Mostramos fecha de expiracion solo para socios
            textViewFechaExpiracion.visibility = View.VISIBLE
        } else {
            carnetFrontResId = R.drawable.carnet_front_no_socio
            carnetBackResId = R.drawable.carnet_back_no_socio
            textViewFechaExpiracion.visibility = View.GONE // Ocultamos fecha de expiracion para no-socios
        }

        // Establecemos la imagen inicial del carnet al frente (determinada por condSocio)
        imageViewCarnet.setImageResource(carnetFrontResId)
        carnetContentLayout.visibility = View.VISIBLE // Asegura que el contenido sea visible al inicio

        // Asignamos los datos a los TextViews
        textViewIdCliente.text = idCliente
        textViewNombreCliente.text = nombreCliente
        textViewApellidoCliente.text = apellidoCliente
        textViewDniCliente.text = dniCliente
        textViewMailCliente.text = email

        // Mostrar campo "Si" o "No" según el valor de aptoFisico
        if (aptoFisico == 1) {
            textViewAptoFisicoCliente.text = "Si"
        } else {
            textViewAptoFisicoCliente.text = "No"
        }


        // --- Lógica para girar el carnet (usando las variables de recursos) ---
        val btnGirarCarnet = findViewById<ImageButton>(R.id.btnGirarCarnet)
        //val carnet = findViewById<ImageView>(R.id.carnetTemplateImageView)
        

        btnGirarCarnet.setOnClickListener {
            imageViewCarnet.animate()
                .rotationXBy(90f) // O rotationYBy(90f) para un giro de volteo de tarjeta
                .setDuration(200)
                .withEndAction {
                    // Cambia la imagen cuando está "girando" (a 90 grados)
                    if (mostrandoFrente) {
                        carnetContentLayout.visibility = View.GONE // Oculta el contenido del frente
                        imageViewCarnet.setImageResource(carnetBackResId) // Muestra el reverso (socio/no-socio)
                    } else {
                        imageViewCarnet.setImageResource(carnetFrontResId) // Muestra el frente (socio/no-socio)
                        // Para el frente, el contenido debe volver a ser visible
                        carnetContentLayout.visibility = View.VISIBLE
                    }
                    mostrandoFrente = !mostrandoFrente

                    // Continua la animación hacia el frente
                    imageViewCarnet.rotationX = -90f // Reinicia la rotación para el segundo tramo
                    imageViewCarnet.animate().rotationX(0f).setDuration(150).start()
                }
                .start()
        }


        val shareButton = findViewById<FloatingActionButton>(R.id.share)
        shareButton.setOnClickListener {
            compartirImagen(imageViewCarnet)
        }


        val printButton = findViewById<FloatingActionButton>(R.id.print)
        printButton.setOnClickListener {
            imprimirCarnet(imageViewCarnet)
        }
    }


    private fun compartirImagen(imageView: ImageView) {
        val bitmap = Bitmap.createBitmap(
            imageView.width,
            imageView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)

        val path = File(cacheDir, "images")
        path.mkdirs()
        val file = File(path, "carnet.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartir carnet"))
    }


    private fun imprimirCarnet(imageView: ImageView) {
        try {
            val bitmap = Bitmap.createBitmap(
                imageView.width,
                imageView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            imageView.draw(canvas)

            val printHelper = PrintHelper(this)
            printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
            printHelper.printBitmap("Carnet del Socio", bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al imprimir el carnet", Toast.LENGTH_SHORT).show()
        }
    }
}
