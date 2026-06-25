package com.proxy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private boolean isProxyRunning = false;
    private Process proxyProcess = null;
    private TextView statusText;
    private Button btnToggleProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liaison des éléments graphiques du XML
        statusText = findViewById(R.id.statusText);
        btnToggleProxy = findViewById(R.id.btnToggleProxy);

        // Extraction initiale du binaire Go depuis les ressources de l'application
        extractEngineIfNeeded();

        // Gestion du clic sur le bouton d'activation
        btnToggleProxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProxyRunning) {
                    startProxyEngine();
                } else {
                    stopProxyEngine();
                }
            }
        });
    }

    private void extractEngineIfNeeded() {
        try {
            File outFile = new File(getFilesDir(), "proxy_motor");
            if (!outFile.exists()) {
                InputStream is = getAssets().open("proxy_motor");
                FileOutputStream os = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteRead;
                while ((byteRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, byteRead);
                }
                is.close();
                os.close();
                outFile.setExecutable(true);
            }
        } catch (Exception e) {
            statusText.setText("ERREUR INTEGRITE ENGINE");
        }
    }

    private void startProxyEngine() {
        try {
            File engineFile = new File(getFilesDir(), "proxy_motor");
            // Exécute le binaire Go compilé de manière isolée en tâche de fond
            proxyProcess = Runtime.getRuntime().exec(engineFile.getAbsolutePath());
            
            // Mise à jour visuelle du thème en mode actif
            isProxyRunning = true;
            statusText.setText("STATUS: ONLINE [PORT 8080]");
            statusText.setTextColor(Color.parseColor("#00FF66"));
            statusText.setBackgroundColor(Color.parseColor("#13231B"));
            btnToggleProxy.setText("DESACTIVER");
            btnToggleProxy.setBackgroundColor(Color.parseColor("#FF3333"));
            btnToggleProxy.setTextColor(Color.parseColor("#FFFFFF"));

        } catch (Exception e) {
            statusText.setText("FAIL TO LAUNCH EXEC");
        }
    }

    private void stopProxyEngine() {
        if (proxyProcess != null) {
            proxyProcess.destroy();
            proxyProcess = null;
        }
        
        // Réinitialisation de l'interface en mode veille
        isProxyRunning = false;
        statusText.setText("STATUS: STANDBY");
        statusText.setTextColor(Color.parseColor("#FF3333"));
        statusText.setBackgroundColor(Color.parseColor("#1F1515"));
        btnToggleProxy.setText("INITIALISER");
        btnToggleProxy.setBackgroundColor(Color.parseColor("#00FF66"));
        btnToggleProxy.setTextColor(Color.parseColor("#0D1117"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mesure de sécurité : Coupe le proxy si l'utilisateur ferme l'application
        stopProxyEngine();
    }
                                }
                  
