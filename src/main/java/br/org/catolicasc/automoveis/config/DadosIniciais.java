package br.org.catolicasc.automoveis.config;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.model.Modelo;
import br.org.catolicasc.automoveis.model.Opcional;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.OpcionalRepository;

/**
 * Carrega os automoveis de exemplo. Como o banco agora fica em arquivo e
 * sobrevive ao reinicio do servidor, so carrega se ainda nao houver nada.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final MarcaRepository marcaRepository;
    private final AutomovelRepository automovelRepository;
    private final OpcionalRepository opcionalRepository;

    public DadosIniciais(MarcaRepository marcaRepository, AutomovelRepository automovelRepository,
            OpcionalRepository opcionalRepository) {
        this.marcaRepository = marcaRepository;
        this.automovelRepository = automovelRepository;
        this.opcionalRepository = opcionalRepository;
    }

    @Override
    public void run(String... args) {
        carregarOpcionais();
        if (automovelRepository.count() > 0) {
            completarPrecosAusentes();
            automovelRepository.marcarVendidosEmPedidos();
            return;
        }

        Marca gm = new Marca("GM");
        Modelo equinox = new Modelo("EQUINOX");
        Modelo omega = new Modelo("OMEGA");
        gm.adicionarModelo(equinox);
        gm.adicionarModelo(omega);

        Marca honda = new Marca("HONDA");
        Modelo bross = new Modelo("BROSS");
        honda.adicionarModelo(bross);

        Marca toyota = new Marca("TOYOTA");
        Modelo corola = new Modelo("COROLA");
        toyota.adicionarModelo(corola);

        marcaRepository.saveAll(Arrays.asList(gm, honda, toyota));

        Automovel carroEquinox = new Automovel(equinox, 2019, "AAA-0000", new BigDecimal("189900.00"));
        Automovel motoBross = new Automovel(bross, 2022, "RDW-8H97", new BigDecimal("32500.00"));
        Automovel carroOmega = new Automovel(omega, 1997, "MXH-6424", new BigDecimal("28000.00"));
        Automovel carroCorola = new Automovel(corola, 2004, "MGJ-6840", new BigDecimal("41900.00"));

        adicionarOpcionais(carroEquinox, "Ar-condicionado", "Direção hidráulica", "Vidros elétricos", "Airbag",
                "Câmera de ré");
        adicionarOpcionais(carroOmega, "Ar-condicionado", "Direção hidráulica", "Vidros elétricos");
        adicionarOpcionais(carroCorola, "Ar-condicionado", "Direção hidráulica", "Vidros elétricos", "Airbag");

        automovelRepository.saveAll(Arrays.asList(carroEquinox, motoBross, carroOmega, carroCorola));
    }

    /** Os opcionais vieram depois dos automoveis; bancos antigos tambem recebem a lista padrao. */
    private void carregarOpcionais() {
        if (opcionalRepository.count() > 0) {
            return;
        }
        opcionalRepository.saveAll(Arrays.asList(
                new Opcional("Ar-condicionado"),
                new Opcional("Direção hidráulica"),
                new Opcional("Vidros elétricos"),
                new Opcional("Airbag"),
                new Opcional("Câmera de ré")));
    }

    private void adicionarOpcionais(Automovel automovel, String... nomes) {
        for (String nome : nomes) {
            automovel.adicionarOpcional(opcionalRepository.findByNome(nome).orElseThrow());
        }
    }

    /** Bancos criados antes de existir o preco ficam com a coluna nula; preenche com um valor padrao. */
    private void completarPrecosAusentes() {
        automovelRepository.findAll().forEach(a -> {
            if (a.getPreco() == null) {
                a.setPreco(new BigDecimal("50000.00"));
                automovelRepository.save(a);
            }
        });
    }
}
