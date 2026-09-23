# Sistema de Gestão de Manutenção de Equipamentos

Aplicação desenvolvida em **Java 17** para gerenciamento do ciclo de vida de manutenção de equipamentos, contemplando abertura e acompanhamento de Ordens de Serviço (OS), diagnóstico, atribuição de técnicos, execução de reparos, controle de usuários e perfis, disponibilidade dos técnicos e registro do histórico das intervenções.

A aplicação possui interface **CLI (Command Line Interface)** e utiliza **MySQL** para persistência dos dados.

---

## 1. Objetivo do Projeto

O objetivo do sistema é permitir o gerenciamento do processo de manutenção de equipamentos desde a abertura de uma Ordem de Serviço até sua conclusão técnica e encerramento administrativo.

O sistema foi desenvolvido considerando dois perfis principais:

* **Gestor**
* **Técnico**

Cada perfil possui permissões e funcionalidades específicas, além de funcionalidades compartilhadas.

O fluxo geral de manutenção pode ser representado da seguinte forma:

```text
Abertura da OS
      ↓
Atribuição do Técnico
      ↓
Diagnóstico
      ↓
Execução do Reparo
      ↓
Apontamento de Horas / Peças / Intervenções
      ↓
Atualização do Status
      ↓
Conclusão Técnica
      ↓
Validação do Gestor
      ↓
Encerramento Administrativo
```

---

# 2. Tecnologias Utilizadas

O projeto utiliza as seguintes tecnologias:

| Tecnologia              | Utilização                              |
| ----------------------- | --------------------------------------- |
| Java 17                 | Linguagem principal                     |
| Maven                   | Gerenciamento do projeto e dependências |
| MySQL 8+                | Banco de dados                          |
| JDBC                    | Comunicação entre aplicação e banco     |
| MySQL Connector/J 8.3.0 | Driver JDBC para MySQL                  |
| CLI                     | Interface de interação com o usuário    |

---

# 3. Requisitos Funcionais Implementados

## 3.1 Gestão de Equipamentos

O sistema permite consultar os equipamentos cadastrados e seus respectivos dados.

Cada equipamento possui:

* ID;
* Código de patrimônio;
* Nome;
* Descrição;
* Localização;
* Situação (ativo/inativo);
* Data de cadastro.

### Funcionalidades

* Cadastro de equipamentos;
* Listagem de equipamentos;
* Listagem de equipamentos ativos;
* Consulta detalhada de equipamento;
* Busca por código de patrimônio;
* Edição de equipamentos;
* Inativação de equipamentos.

As operações de persistência estão concentradas na classe:

```text
EquipamentoDAO
```

---

# 3.2 Controle de Usuários e Perfis

O sistema possui dois perfis de acesso:

```text
GESTOR
TECNICO
```

Cada usuário possui:

* ID;
* Nome;
* E-mail;
* Senha;
* Perfil;
* Status;
* Situação ativo/inativo;
* Data de criação.

O sistema possui controle de sessão por meio da classe:

```text
SessaoAtual
```

A autenticação é realizada pelo:

```text
UsuarioService
```

e a persistência dos usuários pelo:

```text
UsuarioDAO
```

---

# 4. Perfil Gestor

O usuário com perfil **GESTOR** possui funções administrativas sobre o sistema.

## 4.1 Gestão de Equipamentos

O gestor pode:

* Cadastrar equipamentos;
* Consultar equipamentos;
* Visualizar detalhes;
* Editar equipamentos;
* Inativar equipamentos.

---

## 4.2 Gestão de Usuários

O gestor pode:

* Cadastrar usuários;
* Listar usuários;
* Editar usuários;
* Definir o perfil do usuário;
* Inativar usuários;
* Consultar técnicos;
* Alterar a disponibilidade dos técnicos.

Os perfis disponíveis são:

```text
1 - GESTOR
2 - TECNICO
```

---

## 4.3 Atribuição e Reatribuição de OS

O gestor pode atribuir uma Ordem de Serviço a um técnico.

A aplicação verifica se o usuário selecionado possui o perfil:

```text
TECNICO
```

e se está ativo.

Essa funcionalidade permite distribuir os chamados entre os técnicos disponíveis.

---

## 4.4 Aprovação e Encerramento

Após o técnico finalizar o reparo, o gestor pode realizar o encerramento administrativo da OS.

Também é possível cancelar uma Ordem de Serviço quando necessário.

Os estados finais disponíveis são:

```text
CONCLUIDA
CANCELADA
```

---

# 5. Perfil Técnico

O usuário com perfil **TECNICO** possui funções relacionadas à execução da manutenção.

## 5.1 Consulta de Equipamentos

O técnico pode consultar os equipamentos cadastrados e visualizar suas informações.

---

## 5.2 Consulta de Ordens de Serviço

O técnico pode:

* Listar Ordens de Serviço;
* Visualizar detalhes;
* Filtrar por status;
* Consultar suas próprias OS;
* Consultar o histórico de uma OS.

---

## 5.3 Diagnóstico

O técnico pode registrar:

* Diagnóstico inicial;
* Causa-raiz identificada.

Essas informações são armazenadas diretamente na Ordem de Serviço.

---

## 5.4 Registro de Reparo

Durante a execução da manutenção, o técnico pode registrar:

* Horas trabalhadas;
* Peças utilizadas;
* Materiais utilizados;
* Descrição da intervenção realizada.

As informações são persistidas na Ordem de Serviço e as ações relevantes também são registradas no histórico.

---

## 5.5 Atualização do Status

O técnico pode atualizar o status operacional da OS para:

```text
EM_EXECUCAO
AGUARDANDO_PECA
REPARO_FINALIZADO
```

As alterações são controladas pelo serviço:

```text
OrdemServicoStatusService
```

que também registra a alteração no histórico da Ordem de Serviço.

---

## 5.6 Conclusão Técnica

Depois da realização dos reparos e dos testes funcionais, o técnico pode submeter a OS para encerramento.

A conclusão técnica coloca a Ordem de Serviço no estado:

```text
REPARO_FINALIZADO
```

Posteriormente, o gestor pode realizar a validação e o encerramento administrativo.

---

# 6. Funcionalidades Compartilhadas

Gestores e técnicos possuem acesso a funcionalidades comuns.

## 6.1 Visualização de Equipamentos

Ambos os perfis podem:

* Listar equipamentos;
* Consultar detalhes;
* Consultar informações técnicas do equipamento.

---

## 6.2 Ordens de Serviço

Ambos os perfis podem consultar as Ordens de Serviço existentes.

É possível:

* Listar OS;
* Visualizar uma OS específica;
* Filtrar por status;
* Consultar OS atribuídas;
* Visualizar informações do equipamento;
* Visualizar informações do técnico;
* Consultar histórico.

---

## 6.3 Abertura de Chamados

O sistema permite criar uma nova Ordem de Serviço informando:

* Equipamento afetado;
* Descrição da falha;
* Criticidade.

As opções de criticidade são:

```text
BAIXA
MEDIA
ALTA
CRITICA
```

Toda nova OS é inicialmente criada com o status:

```text
ABERTA
```

---

## 6.4 Consulta de Disponibilidade

O sistema mantém o status dos técnicos.

Os estados disponíveis são:

```text
DISPONIVEL
EM_ATENDIMENTO
AUSENTE
```

Isso permite consultar a disponibilidade dos profissionais para auxiliar na atribuição das Ordens de Serviço.

---

# 7. Fluxo da Ordem de Serviço

Uma Ordem de Serviço possui os seguintes estados:

```text
ABERTA
   ↓
EM_EXECUCAO
   ↓
AGUARDANDO_PECA
   ↓
EM_EXECUCAO
   ↓
REPARO_FINALIZADO
   ↓
CONCLUIDA
```

Também existe a possibilidade de cancelamento:

```text
ABERTA / EM_EXECUCAO / ...
             ↓
         CANCELADA
```

O fluxo exato de alteração de status é controlado pela classe:

```text
OrdemServicoStatusService
```

---

# 8. Histórico de Intervenções

O sistema possui uma tabela específica para o histórico das ações realizadas nas Ordens de Serviço:

```text
historico_intervencoes
```

Cada registro possui:

* ID;
* ID da Ordem de Serviço;
* ID do usuário responsável pela ação;
* Descrição da ação;
* Status anterior;
* Status novo;
* Data e hora do registro.

O histórico permite acompanhar a sequência de ações realizadas durante o atendimento.

Exemplo:

```text
OS #10

[09:10] Gestor abriu a OS
[09:30] Gestor atribuiu a OS ao técnico Carlos
[10:00] Técnico registrou diagnóstico
[10:30] Técnico iniciou o reparo
[11:40] Técnico registrou peças utilizadas
[12:15] Técnico finalizou o reparo
[13:00] Gestor encerrou administrativamente
```

---

# 9. Persistência de Dados

A aplicação utiliza um banco de dados relacional **MySQL**.

O script de criação do banco está localizado em:

```text
src/main/resources/db/schema.sql
```

O script cria o banco:

```text
db_manutencao
```

e as seguintes tabelas:

```text
usuarios
equipamentos
ordens_servico
historico_intervencoes
```

## Relacionamentos principais

```text
USUARIO
   │
   ├──────────────┐
   │              │
   ↓              ↓
ORDENS_SERVICO  HISTORICO_INTERVENCOES
   │
   ↓
EQUIPAMENTO
```

Uma Ordem de Serviço está relacionada a:

* Um equipamento;
* Um usuário responsável pela abertura;
* Um técnico responsável, quando atribuído;
* Diversos registros de histórico.

---

# 10. Modelo de Dados

## Usuários

```text
usuarios
├── id
├── nome
├── email
├── senha
├── perfil
├── status_tecnico
├── ativo
└── criado_em
```

## Equipamentos

```text
equipamentos
├── id
├── codigo_patrimonio
├── nome
├── descricao
├── localizacao
├── ativo
└── criado_em
```

## Ordens de Serviço

```text
ordens_servico
├── id
├── equipamento_id
├── tecnico_id
├── gestor_abertura_id
├── descricao_falha
├── criticidade
├── status
├── diagnostico_inicial
├── causa_raiz
├── horas_trabalhadas
├── pecas_utilizadas
├── aberto_em
└── concluido_em
```

## Histórico

```text
historico_intervencoes
├── id
├── ordem_servico_id
├── usuario_id
├── descricao_acao
├── status_anterior
├── status_novo
└── registrado_em
```

---

# 11. Arquitetura do Projeto

O projeto utiliza uma organização baseada em camadas, separando modelo, persistência, regras de negócio e interface.

```text
src/main/java/com/sistema/manutencao/

├── auth/
│   └── SessaoAtual.java
│
├── config/
│   └── DatabaseConnection.java
│
├── dao/
│   ├── EquipamentoDAO.java
│   ├── HistoricoDAO.java
│   ├── OrdemServicoDAO.java
│   └── UsuarioDAO.java
│
├── menu/
│   ├── EquipamentoMenu.java
│   ├── OrdemServicoMenu.java
│   └── UsuarioMenu.java
│
├── model/
│   ├── Equipamento.java
│   ├── Historico.java
│   ├── OrdemServico.java
│   ├── StatusOS.java
│   ├── Usuario.java
│   │
│   └── enums/
│       ├── CriticidadeOS.java
│       ├── PerfilUsuario.java
│       └── StatusUsuario.java
│
├── service/
│   ├── OrdemServicoStatusService.java
│   └── UsuarioService.java
│
└── Main.java
```

---

# 12. Responsabilidade das Camadas

## `model`

Representa as entidades utilizadas pela aplicação.

Exemplos:

```text
Usuario
Equipamento
OrdemServico
Historico
```

---

## `dao`

Responsável pela comunicação com o banco de dados utilizando JDBC.

Exemplos:

```text
UsuarioDAO
EquipamentoDAO
OrdemServicoDAO
HistoricoDAO
```

---

## `service`

Concentra regras de negócio que não devem ficar diretamente na interface.

Exemplos:

```text
UsuarioService
OrdemServicoStatusService
```

O `UsuarioService`, por exemplo, concentra operações relacionadas à autenticação, cadastro, edição e disponibilidade dos usuários.

O `OrdemServicoStatusService` controla as alterações de status e o registro correspondente no histórico.

---

## `menu`

Responsável pela interação com o usuário por meio da interface CLI.

Exemplos:

```text
EquipamentoMenu
OrdemServicoMenu
UsuarioMenu
```

---

## `auth`

Controla o usuário atualmente autenticado.

A classe:

```text
SessaoAtual
```

permite verificar se o usuário logado é:

```text
GESTOR
```

ou:

```text
TECNICO
```

Isso é utilizado para controlar o acesso às funcionalidades específicas de cada perfil.

---

# 13. Banco de Dados e Configuração

A aplicação utiliza o **MySQL** como banco de dados para realizar a persistência das informações de usuários, equipamentos, Ordens de Serviço e histórico de intervenções.

A conexão com o banco de dados é configurada **durante a inicialização da aplicação**, por meio da própria interface CLI.

Ao executar o programa, a primeira etapa solicitada é a configuração da conexão com o MySQL. O usuário deve informar:

```text
Usuário do MySQL
Senha do MySQL
```

Essas informações são utilizadas pela aplicação para estabelecer a conexão com o banco de dados antes de disponibilizar as funcionalidades do sistema.

### Exemplo de inicialização

Ao executar a aplicação, será solicitada a configuração da conexão:

```text
=== CONFIGURAÇÃO DO BANCO DE DADOS ===

Usuário: root
Senha: 1234
```

Após o preenchimento das informações e o estabelecimento da conexão, o sistema prossegue para a tela de autenticação da aplicação.

> **Importante:** as credenciais solicitadas nessa etapa são as credenciais do **MySQL**, utilizadas exclusivamente para estabelecer a conexão com o banco de dados. Elas são diferentes das credenciais dos usuários do sistema (Gestor e Técnico), que são utilizadas posteriormente no login da aplicação.

O banco de dados utilizado pelo projeto é:

```text
db_manutencao
```

O script de criação e configuração das tabelas está localizado em:

```text
src/main/resources/db/schema.sql
```

Esse script contém a estrutura necessária para criação das tabelas utilizadas pela aplicação.

As principais tabelas são:

```text
usuarios
equipamentos
ordens_servico
historico_intervencoes
```

## Preparação do Banco de Dados

É necessário possuir o **MySQL 8 ou superior** instalado e em execução.

Antes da primeira execução da aplicação, o banco `db_manutencao` deve estar criado e o script:

```text
src/main/resources/db/schema.sql
```

deve ser executado no MySQL.

Por exemplo:

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

Após a criação do banco, execute a aplicação e informe, quando solicitado, as credenciais de acesso ao MySQL.

### Fluxo de inicialização

```text
Executar aplicação
        ↓
Informar dados de conexão com MySQL
        ↓
Estabelecer conexão com o banco
        ↓
Tela de login do sistema
        ↓
Informar usuário e senha da aplicação
        ↓
Identificar perfil
        ↓
Exibir menu correspondente
```

Dessa forma, não é necessário deixar usuário e senha do MySQL fixos no código ou no README. Cada execução pode utilizar as credenciais de um ambiente MySQL diferente.

---

# 14. Preparação do Banco de Dados

É necessário possuir o **MySQL 8 ou superior** instalado e em execução.

Execute o arquivo:

```text
src/main/resources/db/schema.sql
```

no MySQL.

Por exemplo, pelo terminal:

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

Após a execução, o banco `db_manutencao` estará disponível para a aplicação.

> Caso o banco e as tabelas já tenham sido criados, não é necessário executar o script novamente.

---

# 15. Usuários de Teste

O banco possui usuários iniciais para facilitar a demonstração da aplicação.

## Gestor

```text
E-mail: gestor@sistema.com
Senha: 123456
Perfil: GESTOR
```

## Técnico

```text
E-mail: tecnico@sistema.com
Senha: 123456
Perfil: TECNICO
```

Esses usuários podem ser utilizados para demonstrar as diferenças de acesso entre os dois perfis.

---

# 16. Execução do Projeto

## Pré-requisitos

Antes de executar a aplicação, é necessário possuir:

* Java 17 ou superior;
* Maven;
* MySQL 8 ou superior;
* Banco de dados configurado;
* Dependências Maven disponíveis.

Para verificar o Java:

```bash
java -version
```

Para verificar o Maven:

```bash
mvn -version
```

---

## Compilação

Na raiz do projeto:

```bash
mvn clean package
```

Após a compilação, será gerado o arquivo:

```text
target/gestao-manutencao-cli-1.0.0-jar-with-dependencies.jar
```

---

## Execução

Execute:

```bash
java -jar target/gestao-manutencao-cli-1.0.0-jar-with-dependencies.jar
```

Também é possível executar diretamente pelo Maven:

```bash
mvn exec:java
```

ou executar a classe principal pela IDE:

```text
com.sistema.manutencao.Main
```

---

# 17. Exemplo de Fluxo de Demonstração

Para demonstrar o funcionamento completo do sistema, recomenda-se utilizar o seguinte fluxo.

### 1. Login como Gestor

Utilizar:

```text
gestor@sistema.com
123456
```

### 2. Cadastrar um equipamento

Informar:

```text
Código de patrimônio
Nome
Descrição
Localização
```

### 3. Abrir uma Ordem de Serviço

Informar:

```text
Equipamento
Descrição da falha
Criticidade
```

A OS será criada como:

```text
ABERTA
```

### 4. Atribuir a OS a um técnico

O gestor seleciona um técnico ativo.

### 5. Entrar como Técnico

Utilizar:

```text
tecnico@sistema.com
123456
```

### 6. Consultar a OS atribuída

O técnico poderá visualizar os detalhes do chamado.

### 7. Registrar o diagnóstico

Informar:

```text
Diagnóstico inicial
Causa-raiz
```

### 8. Registrar o reparo

Informar:

```text
Horas trabalhadas
Peças/materiais utilizados
Intervenção realizada
```

### 9. Alterar o status

O técnico pode utilizar:

```text
EM_EXECUCAO
AGUARDANDO_PECA
REPARO_FINALIZADO
```

### 10. Concluir tecnicamente

Após os testes funcionais, o técnico submete a OS para encerramento.

### 11. Retornar ao Gestor

O gestor valida o serviço e encerra a OS como:

```text
CONCLUIDA
```

ou cancela a OS quando necessário:

```text
CANCELADA
```

### 12. Consultar o histórico

As ações realizadas podem ser consultadas no histórico da OS.

---

# 18. Correspondência com o Enunciado

| Requisito                   | Implementação                             |
| --------------------------- | ----------------------------------------- |
| Cadastro de equipamentos    | `EquipamentoDAO` / `EquipamentoMenu`      |
| Listagem de equipamentos    | `EquipamentoDAO` / `EquipamentoMenu`      |
| Visualização detalhada      | `EquipamentoDAO` / `EquipamentoMenu`      |
| Usuário Gestor              | `PerfilUsuario.GESTOR`                    |
| Usuário Técnico             | `PerfilUsuario.TECNICO`                   |
| Autenticação                | `UsuarioService`                          |
| Controle de sessão          | `SessaoAtual`                             |
| Abertura de OS              | `OrdemServicoDAO` / `OrdemServicoMenu`    |
| Criticidade da OS           | `CriticidadeOS`                           |
| Consulta de OS              | `OrdemServicoMenu`                        |
| Filtro de OS                | `OrdemServicoDAO.listarPorStatus()`       |
| Atribuição de técnico       | `OrdemServicoDAO.atribuirTecnico()`       |
| Disponibilidade de técnicos | `StatusUsuario` / `UsuarioService`        |
| Diagnóstico                 | `OrdemServicoDAO.atualizarDiagnostico()`  |
| Causa-raiz                  | `OrdemServicoDAO.atualizarDiagnostico()`  |
| Horas trabalhadas           | Campo `horas_trabalhadas`                 |
| Peças utilizadas            | Campo `pecas_utilizadas`                  |
| Intervenções                | `Historico` / `HistoricoDAO`              |
| Alteração de status         | `OrdemServicoStatusService`               |
| Conclusão técnica           | `OrdemServicoMenu`                        |
| Encerramento administrativo | `OrdemServicoMenu`                        |
| Cancelamento                | `OrdemServicoMenu`                        |
| Histórico                   | `HistoricoDAO` / `historico_intervencoes` |
| Persistência                | MySQL + JDBC                              |
| Interface                   | CLI                                       |
| Build                       | Maven                                     |

---

# 19. Estrutura de Status

## Status da Ordem de Serviço

```text
ABERTA
EM_EXECUCAO
AGUARDANDO_PECA
REPARO_FINALIZADO
CONCLUIDA
CANCELADA
```

## Status dos Técnicos

```text
DISPONIVEL
EM_ATENDIMENTO
AUSENTE
```

---

# 20. Decisões Técnicas

## 20.1 Utilização de JDBC

Foi utilizado JDBC para manter uma implementação direta e simples da persistência de dados em MySQL, sem introduzir um framework ORM.

Essa escolha também reduz a quantidade de dependências externas e permite demonstrar explicitamente as operações de conexão, consulta, inserção e atualização no banco.

---

## 20.2 Separação entre DAO e Service

A aplicação separa:

```text
DAO → acesso aos dados
Service → regras de negócio
Menu → interação com o usuário
Model → representação dos dados
```

Essa organização facilita a manutenção e evolução do projeto.

---

## 20.3 Controle de Acesso por Perfil

O sistema utiliza o enum:

```text
PerfilUsuario
```

para representar os perfis disponíveis.

O controle é realizado durante a execução das opções dos menus, permitindo diferenciar operações administrativas das operações relacionadas à execução da manutenção.

---

## 20.4 Registro de Histórico

As alterações relevantes das Ordens de Serviço são registradas na tabela:

```text
historico_intervencoes
```

Isso permite manter uma sequência das operações realizadas e identificar o usuário responsável por cada ação.
