package com.siga.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            serviceAccount.close();
            System.out.println("✅ Firebase inicializado com sucesso! Projeto: " + projectId);
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar credenciais Firebase: " + e.getMessage());
            throw new RuntimeException("Erro ao carregar credenciais Firebase", e);
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar Firebase: " + e.getMessage());
            throw new RuntimeException("Erro ao inicializar Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            System.out.println("✅ Firestore client criado com sucesso");
            return firestore;
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar Firestore client: " + e.getMessage());
            throw new RuntimeException("Erro ao criar Firestore client", e);
        }
    }
}
