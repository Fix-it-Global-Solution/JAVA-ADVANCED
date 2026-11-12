package com.fixit.api.config;

import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.OrdemServico;
import com.fixit.api.model.Usuario;
import com.fixit.api.model.enums.NivelUrgencia;
import com.fixit.api.model.enums.PerfilUsuario;
import com.fixit.api.model.enums.StatusOcorrencia;
import com.fixit.api.repository.OcorrenciaRepository;
import com.fixit.api.repository.OrdemServicoRepository;
import com.fixit.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("[DatabaseSeeder] Banco de dados já possui registros iniciais. Seed ignorado.");
            return;
        }

        log.info("[DatabaseSeeder] Inicializando carga de dados de demonstração (Usuários, Ocorrências e Ordens de Serviço com parecer Google Gemini)...");

        // 1. Usuários de Teste (Cidadão e Gestor Público)
        Usuario cidadao = Usuario.builder()
                .nome("Cidadão de Teste Fix-It")
                .email("cidadao@fixit.com")
                .senha(passwordEncoder.encode("123456"))
                .perfil(PerfilUsuario.ROLE_CIDADAO)
                .build();

        Usuario gestor = Usuario.builder()
                .nome("Gestor Público Municipal Fix-It")
                .email("gestor@fixit.com")
                .senha(passwordEncoder.encode("123456"))
                .perfil(PerfilUsuario.ROLE_GESTOR)
                .build();

        usuarioRepository.saveAll(List.of(cidadao, gestor));

        // 2. Ocorrências Urbanas Realistas em São Paulo + Ordens de Serviço com Parecer IA (Google Gemini)
        Ocorrencia ocorrencia1 = Ocorrencia.builder()
                .titulo("Buraco profundo na faixa direita da Av. Paulista")
                .descricao("Cratera aberta após fortes chuvas em frente ao MASP, oferecendo alto risco de queda para motociclistas e ciclistas.")
                .latitude(-23.561414)
                .longitude(-46.655881)
                .enderecoReferencia("Av. Paulista, 1578 - Bela Vista, São Paulo - SP")
                .fotoUrl("https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&w=800&q=80")
                .status(StatusOcorrencia.EM_ANALISE)
                .usuario(cidadao)
                .build();

        OrdemServico os1 = OrdemServico.builder()
                .protocolo(UUID.randomUUID().toString())
                .nivelUrgencia(NivelUrgencia.CRITICA)
                .equipeResponsavel("Secretaria Municipal de Subprefeituras — Equipe de Pavimentação e Tapa-Buraco")
                .dataPrevistaConclusao(LocalDate.now().plusDays(2))
                .parecerTecnicoIa("PARECER TÉCNICO GOOGLE GEMINI (gemini-1.5-flash): Patologia severa no pavimento asfáltico em via arterial de altíssimo fluxo ('Av. Paulista, 1578'). Risco iminente de sinistro viário para veículos de duas rodas. Recomenda-se isolamento imediato da faixa com cones refletivos, corte retangular da área degradada, limpeza com jato de ar comprimido, imprimação ligante e recomposição com CBUQ (Concreto Betuminoso Usinado a Quente) compactado a rolo.")
                .ocorrencia(ocorrencia1)
                .build();
        ocorrencia1.vincularOrdemServico(os1);

        Ocorrencia ocorrencia2 = Ocorrencia.builder()
                .titulo("Poste de iluminação pública apagado na Rua Augusta")
                .descricao("Três luminárias consecutivas estão totalmente apagadas há duas noites, deixando a calçada escura e insegura para pedestres.")
                .latitude(-23.553915)
                .longitude(-46.657022)
                .enderecoReferencia("Rua Augusta, 950 - Consolação, São Paulo - SP")
                .fotoUrl("https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80")
                .status(StatusOcorrencia.CONCLUIDO)
                .usuario(cidadao)
                .build();

        OrdemServico os2 = OrdemServico.builder()
                .protocolo(UUID.randomUUID().toString())
                .nivelUrgencia(NivelUrgencia.ALTA)
                .equipeResponsavel("Departamento de Iluminação Pública (ILUME / Concessionária de Energia)")
                .dataPrevistaConclusao(LocalDate.now().minusDays(1))
                .parecerTecnicoIa("PARECER TÉCNICO GOOGLE GEMINI (gemini-1.5-flash): Falha múltipla no circuito secundário de iluminação pública em corredor comercial de pedestres ('Rua Augusta, 950'). Comprometimento da segurança pública noturna. Determinado deslocamento de equipe com cesto aéreo isolado para substituição de relés fotoelétricos, revisão de conectores perfurantes e instalação de luminárias LED de 150W.")
                .ocorrencia(ocorrencia2)
                .build();
        ocorrencia2.vincularOrdemServico(os2);

        Ocorrencia ocorrencia3 = Ocorrencia.builder()
                .titulo("Bueiro obstruído com acúmulo de detritos na Av. Faria Lima")
                .descricao("Boca de lobo completamente entupida por galhos e resíduos sólidos próxima à ciclovia, gerando empoçamento na pista.")
                .latitude(-23.586803)
                .longitude(-46.682226)
                .enderecoReferencia("Av. Brigadeiro Faria Lima, 3477 - Itaim Bibi, São Paulo - SP")
                .fotoUrl("https://images.unsplash.com/photo-1541888946425-d09bb18086f6?auto=format&fit=crop&w=800&q=80")
                .status(StatusOcorrencia.ABERTO)
                .usuario(cidadao)
                .build();

        OrdemServico os3 = OrdemServico.builder()
                .protocolo(UUID.randomUUID().toString())
                .nivelUrgencia(NivelUrgencia.ALTA)
                .equipeResponsavel("Secretaria de Saneamento e Drenagem Urbana — Equipe Hidrojato")
                .dataPrevistaConclusao(LocalDate.now().plusDays(3))
                .parecerTecnicoIa("PARECER TÉCNICO GOOGLE GEMINI (gemini-1.5-flash): Obstrução crítica de dispositivo de captação pluvial ('Av. Brigadeiro Faria Lima, 3477'). Alto risco de alagamento superficial e invasão da ciclovia em eventos de precipitação. Recomenda-se despacho de caminhão combinado hidrojato e sucção a vácuo para desobstrução do ramal de galeria e limpeza manual da grelha metálica.")
                .ocorrencia(ocorrencia3)
                .build();
        ocorrencia3.vincularOrdemServico(os3);

        ocorrenciaRepository.saveAll(List.of(ocorrencia1, ocorrencia2, ocorrencia3));
        ordemServicoRepository.saveAll(List.of(os1, os2, os3));

        log.info("[DatabaseSeeder] Carga concluída! Usuários criados: cidadao@fixit.com (senha: 123456) e gestor@fixit.com (senha: 123456) + 3 ocorrências com Ordens de Serviço.");
    }

}

