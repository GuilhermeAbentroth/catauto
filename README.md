# CatAuto — Catálogo de Automóveis e Pedidos de Venda

Sistema para revendas de veículos de pequeno porte: catálogo padronizado (marca → modelo → automóvel, com
opcionais), cadastro de clientes e registro de pedidos de venda com pagamento (à vista, financiamento ou
consórcio), exposto por uma API REST e um painel web.

**Documentação da Etapa 1** (requisitos, diagrama de classes UML, mapeamento JPA e dicionário de dados):
[docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md)

## Equipe

| Papel | Nome |
|-------|------|
| Líder | _(preencher)_ |
| Integrante | _(preencher)_ |
| Integrante | _(preencher)_ |
| Integrante | _(preencher)_ |
| Integrante | _(preencher)_ |

## Tecnologias

Java 17 · Spring Boot 4.1.1 · Spring Data JPA (Hibernate 7.4) · H2 Database · Thymeleaf · Maven Wrapper

## Modelo de domínio

8 entidades JPA com relacionamentos 1:1, 1:N e N:M:

- `Marca` 1:N `Modelo` 1:N `Automovel`
- `Automovel` N:M `Opcional` (`@ManyToMany`, tabela `automovel_opcional`)
- `Cliente` 1:N `Pedido` 1:N `ItemPedido` N:1 `Automovel`
- `Pedido` 1:1 `Pagamento` (`@OneToOne`)

## Como executar

Pré-requisito: JDK 17.

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell ou cmd): `mvnw.cmd spring-boot:run`.

- Painel web: http://localhost:8081/
- API REST: http://localhost:8081/api/automoveis (também `/api/marcas`, `/api/modelos`, `/api/opcionais`, `/api/clientes`, `/api/pedidos`)
- Console do banco: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/catauto`, usuário `sa`, senha `password`)

## Testes

```bash
./mvnw test
```

## Estrutura

```
src/main/java/br/org/catolicasc/automoveis
├── model        entidades JPA (@Entity) e enum FormaPagamento
├── repository   interfaces Spring Data JPA (JpaRepository)
├── controller   API REST, página Thymeleaf e tratamento de erros
└── config       carga inicial de dados
docs/DOCUMENTACAO.md   documentação formal da Etapa 1
```
