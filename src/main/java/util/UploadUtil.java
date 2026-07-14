package util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Utilitário central para tratar o upload dos dois arquivos do sistema:
 *   - foto de perfil do usuário (subpasta "fotos")
 *   - capa do livro               (subpasta "capas")
 *
 * Os arquivos são gravados dentro de /uploads/{subpasta} do contexto da
 * aplicação, para que possam ser servidos e baixados como recurso estático
 * (ex.: <a href="uploads/fotos/arquivo.jpg" download>).
 */
public class UploadUtil {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024; // 5MB

    /**
     * Salva o arquivo do Part no disco e devolve o caminho relativo
     * (ex.: "uploads/fotos/3f2a-foto.jpg") para gravar no banco.
     * Retorna null se nenhum arquivo foi enviado.
     */
    public static String salvar(Part part, String subpasta, ServletContext context) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        if (part.getSize() > TAMANHO_MAXIMO) {
            throw new IOException("Arquivo maior que o limite permitido (5MB).");
        }

        String nomeOriginal = extrairNomeArquivo(part);
        String extensao = "";
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto >= 0) extensao = nomeOriginal.substring(ponto);

        validarExtensao(extensao);

        String nomeFinal = UUID.randomUUID().toString() + extensao;

        String diretorioReal = context.getRealPath("/uploads/" + subpasta);
        File diretorio = new File(diretorioReal);
        if (!diretorio.exists()) diretorio.mkdirs();

        Path destino = Path.of(diretorio.getAbsolutePath(), nomeFinal);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, destino);
        }

        return "uploads/" + subpasta + "/" + nomeFinal;
    }

    private static void validarExtensao(String extensao) throws IOException {
        String ext = extensao.toLowerCase();
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IOException("Formato de imagem não suportado. Use JPG, PNG ou WEBP.");
        }
    }

    private static String extrairNomeArquivo(Part part) {
        String header = part.getHeader("content-disposition");
        for (String token : header.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "arquivo";
    }
}
