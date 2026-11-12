package com.fixit.api.model;

import com.fixit.api.model.enums.NivelUrgencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "TB_ORDEM_SERVICO",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_ORDEM_SERVICO_PROTOCOLO", columnNames = "protocolo"),
                @UniqueConstraint(name = "UK_TB_ORDEM_SERVICO_OCORRENCIA", columnNames = "ocorrencia_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"ocorrencia"})
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_ordem_servico")
    @SequenceGenerator(name = "sq_ordem_servico", sequenceName = "SQ_TB_ORDEM_SERVICO", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Builder.Default
    @Column(name = "protocolo", nullable = false, unique = true, updatable = false, length = 36)
    private String protocolo = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_urgencia", nullable = false, length = 20)
    private NivelUrgencia nivelUrgencia;

    @Lob
    @Column(name = "parecer_tecnico_ia")
    private String parecerTecnicoIa;

    @Column(name = "equipe_responsavel", nullable = false, length = 150)
    private String equipeResponsavel;

    @Column(name = "data_prevista_conclusao")
    private LocalDate dataPrevistaConclusao;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ocorrencia_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "FK_TB_ORDEM_SERVICO_OCORRENCIA")
    )
    private Ocorrencia ocorrencia;

    @PrePersist
    public void gerarProtocoloSeAusente() {
        if (this.protocolo == null || this.protocolo.isBlank()) {
            this.protocolo = UUID.randomUUID().toString();
        }
    }

}

