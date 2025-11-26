package com.siga.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de upload de fotos usando ImgBB (100% gratuito)
 * 
 * ImgBB: 32MB por imagem, armazenamento ilimitado, sem expiração
 * Documentação: https://api.imgbb.com/
 */
@Service
public class FotoService {

    @Value("${imgbb.api.key:}")
    private String imgbbApiKey;

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Faz upload de uma foto para o ImgBB (gratuito e ilimitado)
     * 
     * @param chamadoId ID do chamado (usado para organização/log)
     * @param file Arquivo da foto
     * @return URL pública da foto
     */
    public String uploadFoto(String chamadoId, MultipartFile file) {
        try {
            System.out.println("📤 Service: Iniciando upload de foto para chamado: " + chamadoId);

            // Validar arquivo
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Arquivo não pode ser vazio");
            }

            // Validar tipo de arquivo (apenas imagens)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Arquivo deve ser uma imagem (image/*)");
            }

            // Validar tamanho (máximo 32MB para ImgBB)
            long maxSize = 32 * 1024 * 1024; // 32MB
            if (file.getSize() > maxSize) {
                throw new RuntimeException("Arquivo muito grande. Máximo: 32MB. Tamanho atual: " + (file.getSize() / 1024 / 1024) + "MB");
            }

            // Converter para Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            System.out.println("📦 Service: Imagem convertida para Base64 (" + (base64Image.length() / 1024) + " KB)");

            // Se não tem API key, usar modo simulado (retorna URL fictícia para testes)
            if (imgbbApiKey == null || imgbbApiKey.isEmpty()) {
                System.out.println("⚠️ Service: ImgBB API Key não configurada. Usando modo simulado.");
                String fakeUrl = "https://i.ibb.co/placeholder/" + chamadoId + "/" + UUID.randomUUID().toString() + ".jpg";
                System.out.println("✅ Service: URL simulada gerada: " + fakeUrl);
                return fakeUrl;
            }

            // Fazer upload para ImgBB
            Map<String, String> result = uploadToImgBB(base64Image, chamadoId);

            String imageUrl = result.get("url");
            System.out.println("✅ Service: Upload concluído!");
            System.out.println("   📷 URL: " + imageUrl);
            System.out.println("   🔗 Viewer: " + result.get("url_viewer"));
            System.out.println("   🗑️ Delete URL: " + result.get("delete_url"));

            return imageUrl;

        } catch (IOException e) {
            System.err.println("❌ Service: Erro ao fazer upload da foto: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage(), e);
        }
    }

    /**
     * Faz upload para o ImgBB via API
     * Retorna Map com: url, url_viewer, delete_url, thumb_url
     */
    private Map<String, String> uploadToImgBB(String base64Image, String chamadoId) throws IOException {
        URL url = new URL(IMGBB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000); // 30 segundos
            conn.setReadTimeout(60000); // 60 segundos para upload
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");

            // Nome do arquivo baseado no chamado e timestamp
            String imageName = "chamado_" + chamadoId + "_" + System.currentTimeMillis();

            // Preparar dados do formulário
            StringBuilder postData = new StringBuilder();
            postData.append("key=").append(URLEncoder.encode(imgbbApiKey, StandardCharsets.UTF_8));
            postData.append("&image=").append(URLEncoder.encode(base64Image, StandardCharsets.UTF_8));
            postData.append("&name=").append(URLEncoder.encode(imageName, StandardCharsets.UTF_8));
            // Sem expiração - imagem permanente

            System.out.println("🌐 Service: Enviando para ImgBB...");

            // Enviar dados
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            // Verificar resposta
            int responseCode = conn.getResponseCode();
            System.out.println("📡 Service: Resposta HTTP: " + responseCode);

            // Ler resposta
            String response;
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    response = br.lines().collect(Collectors.joining());
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    response = br.lines().collect(Collectors.joining());
                }
                System.err.println("❌ Service: Erro do ImgBB: " + response);
                throw new RuntimeException("Erro no upload: HTTP " + responseCode + " - " + response);
            }

            // Parse da resposta JSON
            JsonNode jsonResponse = objectMapper.readTree(response);

            // Verificar sucesso
            boolean success = jsonResponse.path("success").asBoolean(false);
            if (!success) {
                String error = jsonResponse.path("error").path("message").asText("Erro desconhecido");
                throw new RuntimeException("ImgBB retornou erro: " + error);
            }

            // Extrair dados da imagem
            JsonNode data = jsonResponse.path("data");
            
            Map<String, String> result = new HashMap<>();
            result.put("id", data.path("id").asText());
            result.put("url", data.path("url").asText()); // URL direta da imagem
            result.put("display_url", data.path("display_url").asText()); // URL otimizada
            result.put("url_viewer", data.path("url_viewer").asText()); // URL da página de visualização
            result.put("delete_url", data.path("delete_url").asText()); // URL para deletar
            result.put("thumb_url", data.path("thumb").path("url").asText()); // Thumbnail

            return result;

        } finally {
            conn.disconnect();
        }
    }

    /**
     * Faz upload e retorna informações completas da imagem
     */
    public Map<String, String> uploadFotoCompleto(String chamadoId, MultipartFile file) {
        try {
            System.out.println("📤 Service: Upload completo para chamado: " + chamadoId);

            // Validações
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Arquivo não pode ser vazio");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Arquivo deve ser uma imagem");
            }

            if (file.getSize() > 32 * 1024 * 1024) {
                throw new RuntimeException("Arquivo muito grande. Máximo: 32MB");
            }

            // Converter e fazer upload
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            
            if (imgbbApiKey == null || imgbbApiKey.isEmpty()) {
                Map<String, String> fakeResult = new HashMap<>();
                fakeResult.put("url", "https://i.ibb.co/placeholder/" + UUID.randomUUID() + ".jpg");
                fakeResult.put("thumb_url", fakeResult.get("url"));
                return fakeResult;
            }

            return uploadToImgBB(base64Image, chamadoId);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta uma foto (ImgBB não suporta deleção via API gratuita)
     * O delete_url pode ser usado manualmente
     */
    public void deletarFoto(String fotoUrl) {
        System.out.println("⚠️ Service: Para deletar imagens do ImgBB, use o delete_url manualmente");
        System.out.println("ℹ️ Service: URL da imagem: " + fotoUrl);
    }
}

