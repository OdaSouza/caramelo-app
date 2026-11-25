# Caramelo Team — SPRINT 05

## Local Persistence & Data

### Team Information
- **Número da equipe:** Caramelo Team
- **Membros da equipe:**
  - Draxio (@draxio)

---

## 1. Objetivo da Sprint

Implementar uma camada de **persistência local de dados** no aplicativo **Caramelo**, fazendo com que os dados estruturados do usuário, motorista, histórico de viagens e preferências sobrevivam ao encerramento e reinício do aplicativo, dissociando o estado transitório de tela da persistência permanente.

---

## 2. Decisão de Persistência e Modelo de Dados

### Mecanismo de Armazenamento:
Foi implementado um repositório dedicado (`CarameloRepository`) que gerencia o armazenamento local persistente no dispositivo, utilizando `EncryptedSharedPreferences` (criptografia AES256-GCM) para dados sensíveis e `SharedPreferences` com JSON para registros estruturados.

### Estruturas de Dados Persistidas:
1. **`UserProfile` (Dados do Usuário e Endereço):**
   - `name`: Nome Completo
   - `email`: E-mail de cadastro
   - `cep`: CEP do endereço (posicionado em 1º lugar na seção)
   - `logradouro`: Nome da rua/avenida
   - `number`: Número do imóvel
   - `bairro`: Bairro residencial
   - `city`: Cidade e estado (UF)

2. **`DriverProfile` (Dados do Motorista Parceiro e CNH):**
   - `cnhNumber`: Número de registro da CNH
   - `cnhCategory`: Categoria (B, AB, C, D, E)
   - `cnhValidity`: Data de validade
   - `isDriverRegistered`: Flag que indica se o motorista concluiu o cadastro e foto da CNH

3. **`TripHistoryItem` (Histórico de Viagens):**
   - `date`: Data e hora da corrida
   - `origin` / `destination`: Pontos de partida e chegada
   - `driverName` / `driverRating`: Nome do motorista e avaliação
   - `category`: Categoria (`Carro`, `Carro com Ar`, `Moto`, `Entregas`, `Frete`)
   - `price`: Valor pago na corrida

4. **`BlockedDriver` (Lista de Motoristas Bloqueados):**
   - `id`: Identificador do motorista
   - `name`: Nome do motorista bloqueado
   - `reason`: Motivo do bloqueio registrado

---

## 3. Fluxo de Salvamento e Leitura

```text
[Alteração na UI: Cadastro / Solicitação de Viagem / Alteração de Endereço]
                                   ↓
[CarameloRepository salva os registros atualizados em disco]
                                   ↓
─────── [Usuário encerra o app / Reinicia o processo] ───────
                                   ↓
[Ao reabrir: CarameloRepository lê os dados salvos do disco]
                                   ↓
[Perfil, endereço, status de motorista e histórico exibidos intactos]
```

---

## 4. Uso de Ferramentas de IA (LLM)

| Item | Team response |
| --- | --- |
| **LLM/tool used** | Google Antigravity (Gemini 3.8 Flash) |
| **Task supported by the LLM** | Modelagem da camada de dados (`CarameloRepository`), elaboração da `SPEC-005.md` e refatoração do armazenamento criptografado em Compose |
| **Main suggestion received** | Criação de um repositório isolado `CarameloRepository` com rotinas KTX `edit {}` e métodos estáticos de leitura/gravação |
| **What the team changed manually** | Adaptação das regras de negócio do Caramelo, organização dos campos de endereço com CEP em primeiro lugar e adição das validações de CNH |
| **How the result was validated** | Execução no emulador Android Studio, cadastro do usuário, encerramento forçado do app e verificação da manutenção dos dados salvos ao reabrir |

---

## 5. Evidências de Execução

As imagens foram salvas na pasta `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`:

- **Criação de Dados (`data-created.png`):** `03_tela_cadastro_usuario.png` e `07_tela_cadastro_motorista.png`
- **Restauração de Dados (`data-restored.png`):** `13_tela_configuracoes.png`, `14_historico_viagens.png` e `15_bloqueio_motoristas.png`
