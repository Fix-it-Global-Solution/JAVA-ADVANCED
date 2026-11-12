package com.fixit.api;

import com.fixit.api.dto.TriagemIaResponseDTO;
import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.Usuario;
import com.fixit.api.model.enums.NivelUrgencia;
import com.fixit.api.model.enums.PerfilUsuario;
import com.fixit.api.model.enums.StatusOcorrencia;
import com.fixit.api.security.TokenService;
import com.fixit.api.service.TriagemIaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class FixItApiApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TriagemIaService triagemIaService;

    @Test
    @DisplayName("01. Deve inicializar o ApplicationContext do Spring Boot com sucesso")
    void contextLoads() {
        assertNotNull(applicationContext, "O ApplicationContext do Spring Boot deve ser carregado com sucesso.");
        assertNotNull(tokenService, "O bean TokenService deve estar presente no contexto.");
        assertNotNull(triagemIaService, "O bean TriagemIaService deve estar presente no contexto.");
    }

    @Test
    @DisplayName("02. Deve gerar token JWT válido para Cidadão e validar o subject (email)")
    void deveGerarEValidarTokenJwtCidadaoComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(100L)
                .nome("Cidadão de Teste Automatizado")
                .email("cidadao.teste@fixit.com.br")
                .senha("SenhaSegura@123")
                .perfil(PerfilUsuario.ROLE_CIDADAO)
                .build();

        String token = tokenService.gerarToken(usuario);

        assertNotNull(token, "O token JWT gerado não deve ser nulo.");
        assertFalse(token.isBlank(), "O token JWT não deve ser vazio.");
        assertTrue(token.split("\\.").length == 3, "O token JWT deve possuir 3 partes separadas por ponto.");

        String subject = tokenService.validarToken(token);
        assertEquals("cidadao.teste@fixit.com.br", subject, "O subject extraído do token deve corresponder ao email do usuário.");
    }

    @Test
    @DisplayName("03. Deve gerar token JWT válido para Gestor Público com perfil correto")
    void deveGerarEValidarTokenJwtGestorComSucesso() {
        Usuario gestor = Usuario.builder()
                .id(200L)
                .nome("Gestora Pública de Teste")
                .email("gestor.teste@fixit.com.br")
                .senha("SenhaSegura@123")
                .perfil(PerfilUsuario.ROLE_GESTOR)
                .build();

        String token = tokenService.gerarToken(gestor);

        assertNotNull(token, "O token JWT gerado para o gestor não deve ser nulo.");
        String subject = tokenService.validarToken(token);
        assertEquals("gestor.teste@fixit.com.br", subject, "O token do gestor deve ser validado com o subject esperado.");
    }

    @Test
    @DisplayName("04. Deve retornar nulo ao tentar validar token JWT corrompido ou falso")
    void deveRetornarNuloAoValidarTokenInvalido() {
        String tokenInvalido = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token.invalido";
        String subject = tokenService.validarToken(tokenInvalido);

        assertNull(subject, "A validação de um token JWT inválido deve retornar nulo sem lançar exceções não tratadas.");
    }

    @Test
    @DisplayName("05. Deve executar fallback técnico da IA para ocorrência de pavimento asfáltico / cratera")
    void deveExecutarFallbackTriagemIaParaPavimentacaoAsfaltica() {
        Ocorrencia ocorrencia = Ocorrencia.builder()
                .id(501L)
                .titulo("Cratera profunda na faixa de rolamento")
                .descricao("Buraco severo no asfalto com risco iminente de acidentes para motociclistas e pedestres.")
                .enderecoReferencia("Av. Paulista, 1500 - Bela Vista, São Paulo - SP")
                .latitude(-23.5629)
                .longitude(-46.6544)
                .status(StatusOcorrencia.ABERTO)
                .build();

        TriagemIaResponseDTO resultado = triagemIaService.analisarOcorrencia(ocorrencia);

        assertNotNull(resultado, "A resposta da triagem técnica não deve ser nula.");
        assertEquals(501L, resultado.ocorrenciaId());
        assertEquals("Cratera profunda na faixa de rolamento", resultado.tituloOcorrencia());
        assertNotNull(resultado.nivelUrgenciaSugerido(), "O nível de urgência deve ser classificado.");
        assertTrue(
                resultado.nivelUrgenciaSugerido() == NivelUrgencia.CRITICA || resultado.nivelUrgenciaSugerido() == NivelUrgencia.ALTA,
                "Crateras e buracos severos devem ser classificados com urgência ALTA ou CRITICA."
        );
        assertNotNull(resultado.equipeResponsavelSugerida(), "Uma equipe municipal deve ser indicada.");
        assertTrue(resultado.prazoEstimadoDias() > 0, "O prazo estimado em dias deve ser positivo.");
        assertNotNull(resultado.dataPrevistaConclusaoSugerida(), "A data prevista de conclusão deve ser calculada.");
        assertNotNull(resultado.parecerTecnicoIa(), "O parecer técnico deve ser preenchido.");
        assertTrue(resultado.parecerTecnicoIa().length() > 20, "O parecer técnico deve conter justificativa detalhada.");
        assertNotNull(resultado.modoExecucao(), "O modo de execução deve ser informado.");
    }

    @Test
    @DisplayName("06. Deve executar fallback técnico da IA para ocorrência de iluminação pública")
    void deveExecutarFallbackTriagemIaParaIluminacaoPublica() {
        Ocorrencia ocorrencia = Ocorrencia.builder()
                .id(502L)
                .titulo("Poste com lâmpada apagada")
                .descricao("Iluminação pública sem luz há vários dias, deixando a calçada totalmente escura.")
                .enderecoReferencia("Rua Augusta, 800 - Consolação, São Paulo - SP")
                .latitude(-23.5532)
                .longitude(-46.6521)
                .status(StatusOcorrencia.ABERTO)
                .build();

        TriagemIaResponseDTO resultado = triagemIaService.analisarOcorrencia(ocorrencia);

        assertNotNull(resultado, "A resposta da triagem de iluminação não deve ser nula.");
        assertEquals(502L, resultado.ocorrenciaId());
        assertNotNull(resultado.nivelUrgenciaSugerido());
        assertNotNull(resultado.equipeResponsavelSugerida());
        assertTrue(
                resultado.equipeResponsavelSugerida().toLowerCase().contains("ilumina")
                        || resultado.equipeResponsavelSugerida().toLowerCase().contains("zeladoria"),
                "A equipe de iluminação ou zeladoria deve ser atribuída."
        );
        assertNotNull(resultado.parecerTecnicoIa());
    }

    @Test
    @DisplayName("07. Deve formatar parecer técnico com metadados para persistência na Ordem de Serviço")
    void deveFormatarParecerTecnicoComMetadados() {
        Ocorrencia ocorrencia = Ocorrencia.builder()
                .id(503L)
                .titulo("Bueiro entupido com acúmulo de lixo")
                .descricao("Galerias pluviais obstruídas causando alagamento na calçada durante chuvas.")
                .enderecoReferencia("Av. Brigadeiro Faria Lima, 3000 - Itaim Bibi, São Paulo - SP")
                .latitude(-23.5866)
                .longitude(-46.6822)
                .status(StatusOcorrencia.ABERTO)
                .build();

        String parecerFormatado = triagemIaService.gerarParecerTecnico(
                ocorrencia,
                NivelUrgencia.ALTA,
                "Secretaria Municipal de Limpeza Urbana"
        );

        assertNotNull(parecerFormatado, "O parecer formatado não deve ser nulo.");
        assertTrue(parecerFormatado.contains("Urgência ALTA"), "Deve conter a urgência aplicada.");
        assertTrue(parecerFormatado.contains("Secretaria Municipal de Limpeza Urbana"), "Deve conter a equipe destacada.");
    }

}
