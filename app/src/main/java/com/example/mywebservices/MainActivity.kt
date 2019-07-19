package com.example.mywebservices

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Executable
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    var wsConsultar : String = "http://192.168.7.111/Servicios/MostrarAlumno.php"
    var wsInsertar : String = "http://192.168.7.111/Servicios/insertarAlumno.php"
    var hilo : ObtenerUnServicioWeb? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun consultaXNoControl (v : View) {
        if(etNoControl.text.isEmpty()){
            Toast.makeText(this, "FALTA INGRESAR NUMERO DE CONTROL", Toast.LENGTH_SHORT).show();
            etNoControl.requestFocus()
        }else {
            val no = etNoControl.text.toString()
            hilo = ObtenerUnServicioWeb()
            hilo?.execute("Consulta", no,"","","")
        }
    }
    fun insertAlumno(v : View){

        if (etNoControl.text.isEmpty() || etNombre.text.isEmpty() || etCarrera.text.isEmpty() || etTelefono.text.isEmpty() ){
            Toast.makeText(this, "ERROR:  FALTAN TODOS LOS DATOS POR LLENAR", Toast.LENGTH_SHORT).show();
        }else {
            val no = etNoControl.text.toString()
            val nom = etNombre.text.toString()
            val carr = etCarrera.text.toString()
            val tel = etTelefono.text.toString()
            hilo = ObtenerUnServicioWeb()
            hilo?.execute("Insertar", no,carr,nom,tel)
        }
    }


    inner class ObtenerUnServicioWeb():AsyncTask<String, String, String>(){

        override fun doInBackground(vararg params: String?): String {
            var Url : URL? = null
            var sResultado = ""
            try {
                val urlConn:HttpURLConnection
                val printout:DataOutputStream
                var input : DataOutputStream
                if (params[2].toString().isEmpty() && params[3].toString().isEmpty()){
                    Url = URL(wsConsultar)
                }else {
                    Url= URL(wsInsertar)
                }
                urlConn = Url.openConnection() as HttpURLConnection
                urlConn.doInput = true
                urlConn.doOutput = true
                urlConn.useCaches=false
                urlConn.setRequestProperty("Content-Type","Aplication/json")//TIPO DE DATO QUE RESIBIRA
                urlConn.setRequestProperty("Accept","aplication/json")
                urlConn.connect()
                //PREPARAR LOS DATOS A ENVIAR AL WEB SERVICE
                val jsonParam = JSONObject()
                jsonParam.put("nocontrol",params[1])
                jsonParam.put("carrera",params[2])
                jsonParam.put("nombre",params[3])
                jsonParam.put("telefono",params[4])
                val os = urlConn.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os,"UTF-8"))
                writer.write(jsonParam.toString())
                writer.flush()
                writer.close()
                val respuesta= urlConn.responseCode
                val result = StringBuilder()
                if (respuesta == HttpURLConnection.HTTP_OK){
                    val inStream : InputStream = urlConn.inputStream
                    val isReader = InputStreamReader(inStream)
                    val bReader = BufferedReader(isReader)
                    var tempStr : String?
                    while (true){
                        tempStr = bReader.readLine()
                        if (tempStr == null){
                            break
                        }
                        result.append(tempStr)
                    }
                    urlConn.disconnect()
                    sResultado = result.toString()
                }
            }catch (e: MalformedURLException){
                Log.d("JDTM",e.message)
            }catch (e: IOException){
                Log.d("JDTM",e.message)
            }catch (e: JSONException){
                Log.d("JDTM",e.message)
            }catch (e: Exception){
                Log.d("JDTM",e.message)
            }
           return sResultado
        }//Fin doInBackground    //Metodos que se van a utilizar
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var no :String= ""
            var nom :String= ""
            var carr :String= ""
            var tel :String= ""
            try {
                val respuestaJSON = JSONObject(result)
                val resultJSON = respuestaJSON.getString("success")
                when {
                    resultJSON == "200" ->{
                        val alumnoJSON = respuestaJSON.getJSONArray("alumno")
                        if (alumnoJSON.length() >= 1) {
                            no = alumnoJSON.getJSONObject(0).getString("nocontrol")
                            nom = alumnoJSON.getJSONObject(0).getString("nombre")
                            carr = alumnoJSON.getJSONObject(0).getString("carrera")
                            tel = alumnoJSON.getJSONObject(0).getString("telefono")
                            etNoControl.setText(no)
                            etNombre.setText(nom)
                            etCarrera.setText(carr)
                            etTelefono.setText(tel)
                        }
                    }
                    resultJSON == "201" ->{
                        Toast.makeText(baseContext, "ALUMNO ALMACENADO CONE EXITO", Toast.LENGTH_SHORT).show();
                        etTelefono.setText("")
                        etCarrera.setText("")
                        etNombre.setText("")
                        etNoControl.setText("")
                        etNoControl.requestFocus()
                    }
                    resultJSON == "204" ->{
                        Toast.makeText(baseContext, "ERROR : ALUMNO NO ENCONTRADO", Toast.LENGTH_SHORT).show();
                    }
                    resultJSON == "409" ->{
                        Toast.makeText(baseContext, "ERROR : AL AGREGAR ALUMNO", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (e: JSONException) {
                Log.d("JDTM", e.message)
            }catch (e: Exception) {
                Log.d("JDTM", e.message)
            }

        }//fin  onPostExecute //Metodos que se van a utilizar

    }// Fin  ObtenerUnServicioWeb

}//Fin De La clase MainActivity
