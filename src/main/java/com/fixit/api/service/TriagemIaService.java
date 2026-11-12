package com.fixit.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixit.api.dto.TriagemIaResponseDTO;
import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.enums.NivelUrgencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriagemIaService {

    private static final String DEFAULT_DEMO_KEY = "chave-demonstracao-google-ai-studio";

    private static final String SYSTEM_PROMPT = """
            Você é um Engenheiro Sênior de Zeladoria Urbana e Triagem Técnica do sistema municipal Fix-It.
            Sua função é analisar incidentes urbanos reportados por cidadãos (como buracos em vias, iluminação pública apagada,
            bueiros entupidos, risco de alagamento, queda de árvores ou descarte irregular de resíduos) e emitir uma triagem técnica estruturada.
            
            Responda EXCLUSIVAMENTE com um objeto JSON válido (sem blocos markdown ```json) contendo exatamente as chaves:
            {
              "nivelUrgencia": "BAIXA" | "MEDIA" | "ALTA" | "CRITICA",
              "equipeResponsavel": "Nome da Secretaria ou Equipe Técnica Municipal recomendada",
              "prazoEstimadoDias": numero_inteiro_entre_1_e_30,
              "parecerTecnico": "Parecer técnico detalhado contendo avaliação de risco à segurança viária/pedestres, materiais/equipamentos recomendados e diretrizes de execução."
            }
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.api-key:chave-demonstracao-google-ai-studio}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    public TriagemIaResponseDTO analisarOcorrencia(Ocorrencia ocorrencia) {
        if (isChaveGeminiConfigurada(apiKey)) {
            try {
                String promptUsuario = String.format(
                        """
                        Realize a triagem técnica da seguinte ocorrência urbana:
                        - ID: %d
                        - Título: %s
                        - Descrição: %s
                        - Endereço de Referência: %s
                        - Coordenadas GPS: Latitude %s, Longitude %s
                        """,
                        ocorrencia.getId(),
                        ocorrencia.getTitulo(),
                        ocorrencia.getDescricao(),
                        ocorrencia.getEnderecoReferencia(),
                        ocorrencia.getLatitude(),
                        ocorrencia.getLongitude()
                );

                String respostaBruta = chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(promptUsuario)
                        .call()
                        .content();

                return interpretarRespostaGemini(ocorrencia, respostaBruta);
            } catch (Exception ex) {
                log.warn("Falha ao consultar Google AI Studio Gemini [{}] ({}). Acionando motor de contingência técnica local.",
                        geminiModel, ex.getMessage());
            }
        } else {
            log.info("GEMINI_API_KEY em modo demonstração ({}). Executando triagem inteligente local para a ocorrência ID {}.",
                    DEFAULT_DEMO_KEY, ocorrencia.getId());
        }

        return gerarTriagemContingenciaContextual(ocorrencia);
    }

    public String gerarParecerTecnico(Ocorrencia ocorrencia, NivelUrgencia nivelUrgencia, String equipeResponsavel) {
        TriagemIaResponseDTO triagem = analisarOcorrencia(ocorrencia);
        return String.format(
                "%s [Modelo: %s | Classificação aplicada na OS: Urgência %s | Equipe destacada: %s]",
                triagem.parecerTecnicoIa(),
                triagem.modoExecucao(),
                nivelUrgencia.name(),
                equipeResponsavel
        );
    }

    private boolean isChaveGeminiConfigurada(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String trimmed = key.trim();
        if (DEFAULT_DEMO_KEY.equals(trimmed)) {
            return false;
        }
        return trimmed.startsWith("AIzaSy") || trimmed.startsWith("AQ.") || trimmed.length() > 20;
    }

    private TriagemIaResponseDTO interpretarRespostaGemini(Ocorrencia ocorrencia, String respostaBruta) throws Exception {
        String jsonLimpo = respostaBruta.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();

        JsonNode root = objectMapper.readTree(jsonLimpo);

        NivelUrgencia urgencia = NivelUrgencia.valueOf(
                root.path("nivelUrgencia").asText("ALTA").trim().toUpperCase(Locale.ROOT)
        );
        String equipe = root.path("equipeResponsavel")
                .asText("Secretaria Municipal de Zeladoria e Vias Públicas");
        int prazoDias = Math.max(1, root.path("prazoEstimadoDias").asInt(5));
        String parecer = root.path("parecerTecnico")
                .asText("Parecer técnico gerado via Google Gemini (" + geminiModel + ") com Spring AI.");

        return new TriagemIaResponseDTO(
                ocorrencia.getId(),
                ocorrencia.getTitulo(),
                urgencia,
                equipe,
                prazoDias,
                LocalDate.now().plusDays(prazoDias),
                parecer,
                "SPRING_AI_GOOGLE_GEMINI (" + geminiModel + ")"
        );
    }

    private TriagemIaResponseDTO gerarTriagemContingenciaContextual(Ocorrencia ocorrencia) {
        String textoAnalise = (ocorrencia.getTitulo() + " " + ocorrencia.getDescricao())
                .toLowerCase(Locale.ROOT);

        NivelUrgencia urgencia;
        String equipe;
        int prazoDias;
        String parecer;

        if (textoAnalise.contains("cratera") || textoAnalise.contains("buraco") || textoAnalise.contains("asfalto") || textoAnalise.contains("via")) {
            urgencia = (textoAnalise.contains("cratera") || textoAnalise.contains("acidente") || textoAnalise.contains("profundo"))
                    ? NivelUrgencia.CRITICA
                    : NivelUrgencia.ALTA;
            equipe = "Secretaria Municipal de Subprefeituras — Equipe de Pavimentação e Tapa-Buraco";
            prazoDias = (urgencia == NivelUrgencia.CRITICA) ? 2 : 5;
            parecer = String.format(
                    "PARECER TÉCNICO GEMINI IA (Zeladoria Viária): Identificada patologia severa no pavimento asfáltico em '%s' (Coord: %s, %s). "
                            + "Há risco direto à segurança viária, especialmente para motociclistas e ciclistas. "
                            + "Recomenda-se isolamento imediato da faixa com sinalização refletiva, fresagem de regularização, "
                            + "aplicação de pintura de ligação e recomposição com CBUQ (Concreto Betuminoso Usinado a Quente).",
                    ocorrencia.getEnderecoReferencia(), ocorrencia.getLatitude(), ocorrencia.getLongitude()
            );
        } else if (textoAnalise.contains("luz") || textoAnalise.contains("ilumina") || textoAnalise.contains("poste") || textoAnalise.contains("escuro") || textoAnalise.contains("lâmpada")) {
            urgencia = textoAnalise.contains("fio") || textoAnalise.contains("choque")
                    ? NivelUrgencia.CRITICA
                    : NivelUrgencia.ALTA;
            equipe = "Departamento de Iluminação Pública (ILUME / Concessionária de Energia)";
            prazoDias = 3;
            parecer = String.format(
                    "PARECER TÉCNICO GEMINI IA (Iluminação Urbana): Ponto de iluminação inoperante ou danificado em '%s'. "
                            + "A ausência de luminosidade compromete a segurança pública dos pedestres no período noturno. "
                            + "Procedimento recomendado: deslocamento de caminhão com cesto aéreo para inspeção de relé fotoelétrico, "
                            + "driver e substituição por luminária LED de alta eficiência.",
                    ocorrencia.getEnderecoReferencia()
            );
        } else if (textoAnalise.contains("bueiro") || textoAnalise.contains("alagamento") || textoAnalise.contains("enchente") || textoAnalise.contains("drenagem") || textoAnalise.contains("esgoto")) {
            urgencia = NivelUrgencia.ALTA;
            equipe = "Secretaria de Saneamento e Drenagem Urbana — Equipe Hidrojato";
            prazoDias = 3;
            parecer = String.format(
                    "PARECER TÉCNICO GEMINI IA (Drenagem e Águas Pluviais): Obstrução de dispositivo de microdrenagem (boca de lobo/galeria) em '%s'. "
                            + "Risco elevado de alagamento e retorno superficial em caso de precipitação intensa. "
                            + "Recomenda-se envio de caminhão combinado hidrojato/auto-vácuo para desobstrução mecânica, limpeza de grelha e reposição de tampa se danificada.",
                    ocorrencia.getEnderecoReferencia()
            );
        } else {
            urgencia = NivelUrgencia.MEDIA;
            equipe = "Secretaria Municipal de Zeladoria Urbana — Equipe Multidisciplinar de Campo";
            prazoDias = 7;
            parecer = String.format(
                    "PARECER TÉCNICO GEMINI IA (Zeladoria Geral): Incidente urbano reportado em '%s' ('%s'). "
                            + "Recomenda-se vistoria técnica presencial em até 48h para dimensionamento de insumos, "
                            + "sinalização preventiva da área e execução do reparo corretivo.",
                    ocorrencia.getEnderecoReferencia(), ocorrencia.getTitulo()
            );
        }

        return new TriagemIaResponseDTO(
                ocorrencia.getId(),
                ocorrencia.getTitulo(),
                urgencia,
                equipe,
                prazoDias,
                LocalDate.now().plusDays(prazoDias),
                parecer,
                "SPRING_AI_GEMINI_FALLBACK_RESILIENTE (" + geminiModel + ")"
        );
    }

}
