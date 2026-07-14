package util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Codifica/decodifica a senha em Base64 antes de gravar no banco.
 *
 * IMPORTANTE: Base64 NÃO é criptografia nem hash — é apenas uma codificação
 * reversível. Serve como uma camada mínima para não gravar a senha em texto
 * puro no banco, mas não deve ser usada como única proteção em produção
 * (o ideal seria um hash com salt, como BCrypt).
 */
public class SenhaUtil {

    private SenhaUtil() {}

    public static String codificar(String senhaTexto) {
        if (senhaTexto == null) return null;
        return Base64.getEncoder().encodeToString(senhaTexto.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodificar(String senhaCodificada) {
        if (senhaCodificada == null) return null;
        return new String(Base64.getDecoder().decode(senhaCodificada), StandardCharsets.UTF_8);
    }
}
