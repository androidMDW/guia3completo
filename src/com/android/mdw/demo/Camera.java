package com.android.mdw.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

public class Camera extends Activity {
	/**
	 * Constantes para identificar la acción realizada (tomar una fotografía 
	 * o bien seleccionarla de la galería)
	 */
	private static int TAKE_PICTURE = 1;
	private static int SELECT_PICTURE = 2;
	
	/**
	 * Variable que define el nombre para el archivo donde escribiremos
	 * la fotografía de tamaño completo al tomarla.
	 */
	private String name = "";
	
    /** Este método es llamado cuando la actividad es creada */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        name = Environment.getExternalStorageDirectory() + "/test.jpg";

        Button btnAction = (Button)findViewById(R.id.btnPic);
        btnAction.setOnClickListener(new OnClickListener() {       		
       		@Override
       		public void onClick(View v) {
       			/**
       			 * Obtenemos los botones de imagen completa y de galería para revisar su estatus
       			 * más adelante
       			 */
       			RadioButton rbtnFull = (RadioButton)findViewById(R.id.radbtnFull);
       			RadioButton rbtnGallery = (RadioButton)findViewById(R.id.radbtnGall);
       			
       			/**
       			 * Para todos los casos es necesario un intent, si accesamos la cámara con la acción
       			 * ACTION_IMAGE_CAPTURE, si accesamos la galería con la acción ACTION_PICK. 
       			 * En el caso de la vista previa (thumbnail) no se necesita más que el intent, 
       			 * el código e iniciar la actividad. Por eso inicializamos las variables intent y
       			 * code con los valores necesarios para el caso del thumbnail, así si ese es el
       			 * botón seleccionado no validamos nada en un if. 
       			 */
       			Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
       			int code = TAKE_PICTURE;
       			
       			/**
       			 * Si la opción seleccionada es fotografía completa, necesitamos un archivo donde
       			 * guardarla
       			 */
       			if (rbtnFull.isChecked()) {					
       				Uri output = Uri.fromFile(new File(name));
       		    	intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
       			/**
       			 * Si la opción seleccionada es ir a la galería, el intent es diferente y el código
       			 * también, en la consecuencia de que esté chequeado el botón de la galería se hacen
       			 * esas asignaciones
       			 */       		    	
       			} else if (rbtnGallery.isChecked()){       				
       				intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
       				code = SELECT_PICTURE;
       			}
       			
       			/**
       			 * Luego, con todo preparado iniciamos la actividad correspondiente.
       			 */
       			startActivityForResult(intent, code);				
       		}
       	});        
    }
    
    /**
     * Función que se ejecuta cuando concluye el intent en el que se solicita una imagen
     * ya sea de la cámara o de la galería
     */
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	/**
    	 * Se revisa si la imagen viene de la cámara (TAKE_PICTURE) o de la galería (SELECT_PICTURE)
    	 */
    	if (requestCode == TAKE_PICTURE) {
    		/**
    		 * Si se reciben datos en el intent tenemos una vista previa (thumbnail)
    		 */
    		if (data != null) {
    			/**
    			 * En el caso de una vista previa, obtenemos el extra “data” del intent y 
    			 * lo mostramos en el ImageView
    			 */
    			if (data.hasExtra("data")) { 
    				ImageView iv = (ImageView)findViewById(R.id.imgView);
    				iv.setImageBitmap((Bitmap) data.getParcelableExtra("data"));
    			}
    		/**
    		 * De lo contrario es una imagen completa
    		 */    			
    		} else {
    			/**
    			 * A partir del nombre del archivo ya definido lo buscamos y creamos el bitmap
    			 * para el ImageView
    			 */
    			ImageView iv = (ImageView)findViewById(R.id.imgView);
    			iv.setImageBitmap(BitmapFactory.decodeFile(name));
    			/**
    			 * Para guardar la imagen en la galería, utilizamos una conexión a un MediaScanner
    			 */
    			new MediaScannerConnectionClient() {
    				private MediaScannerConnection msc = null; {
    					msc = new MediaScannerConnection(getApplicationContext(), this); msc.connect();
    				}
    				public void onMediaScannerConnected() { 
    					msc.scanFile(name, null);
    				}
    				public void onScanCompleted(String path, Uri uri) { 
    					msc.disconnect();
    				} 
    			};				
    		}
    	/**
    	 * Recibimos el URI de la imagen y construimos un Bitmap a partir de un stream de Bytes
    	 */
    	} else if (requestCode == SELECT_PICTURE){
    		Uri selectedImage = data.getData();
    		InputStream is;
    		try {
    			is = getContentResolver().openInputStream(selectedImage);
    	    	BufferedInputStream bis = new BufferedInputStream(is);
    	    	Bitmap bitmap = BitmapFactory.decodeStream(bis);            
    	    	ImageView iv = (ImageView)findViewById(R.id.imgView);
    	    	iv.setImageBitmap(bitmap);						
    		} catch (FileNotFoundException e) {}
    	}
    }


    
}