# Caramelo Team — SPRINT 03

## Navigation & Multiple Screens

### Team Information
- **Número da equipe:** Caramelo Team
- **Membros da equipe:**
  - Draxio (@draxio)

---

## 1. Objetivo da Sprint

Evoluir o aplicativo **Caramelo** de um protótipo inicial para uma aplicação Android multi-telas utilizando o fluxo de navegação entre Activities e componentes Composables dedicados em **Jetpack Compose**, garantindo transições suaves, passagem de parâmetros, histórico e retorno na pilha de navegação (`back stack`).

---

## 2. Telas Desenvolvidas

O aplicativo conta com telas organizadas em arquivos e composables independentes:

1. **`LoginActivity` (Screen A - Login e Autenticação):**
   - Entradas de e-mail e senha, suporte a login com Google e Apple, navegação para criação de conta e recuperação de senha.

2. **`RegisterActivity` (Screen B - Cadastro de Usuário e Endereço):**
   - Formulário completo de dados pessoais, confirmação de e-mail/senha e seção detalhada de endereço (CEP em primeiro lugar).

3. **`DriverRegisterActivity` e `CnhPhotoActivity` (Screen C - Cadastro de Motorista e CNH):**
   - Tela de registro de motorista parceiro, número/validade da CNH e transição para a tela dedicada de foto da CNH com enquadramento em moldura.

4. **`Mapao_do_Oda` (Screen D - Tela Principal / Mapa):**
   - Tela principal com mapa, escolha de categorias (`Carro`, `Carro com Ar`, `Moto`, `Entregas`, `Frete`), preferências de motorista, ajuste de preço, stream de propostas e drawer lateral.

5. **`SettingsActivity` (Screen E - Configurações e Histórico):**
   - Central de preferências com controle de Modo Noturno, incremento dos botões de preço (`+`/`-`), histórico de viagens e gerenciamento da lista de motoristas bloqueados.

---

## 3. Grafo de Navegação

A navegação entre telas no aplicativo utiliza Intent e navegação em Compose:
- **Ações de Avanço (Forward):**
  - `Login → Register`: Clique em "Criar conta" navega para a tela de registro.
  - `Login → Mapao_do_Oda`: Login bem-sucedido redireciona para a tela principal do mapa.
  - `Settings → DriverRegisterActivity → CnhPhotoActivity`: Fluxo completo de cadastro de motorista parceiro.
- **Ações de Retorno (Back Stack):**
  - O botão `✕ Voltar` e o gesto nativo acionam `finish()` ou o retorno na pilha, devolvendo o usuário à tela anterior sem perda de estado.

```text
 [ LoginActivity ] ── (Criar Conta) ──> [ RegisterActivity ] ──> [ VehicleRegisterActivity ]
         │                                                                   │
    (Login OK)                                                         (Finalizar)
         ↓                                                                   ↓
 [ Mapao_do_Oda ] ◄──────────────────────────────────────────────────────────┘
    │        │
    │        └─ (Menu / Drawer) ─> [ SettingsActivity ]
    │                                      │
    └─ (Seja Motorista) ───────────────────┴─> [ DriverRegisterActivity ] ─> [ CnhPhotoActivity ]
```

---

## 4. Estrutura de Arquivos

Conforme os critérios de aceitação, o projeto está estruturado em módulos limpos:

```text
Caramelo/
├── SPRINT-03.md
├── app/src/main/java/com/draxio/mapa_teste/
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   ├── RecoveryActivity.kt
│   ├── VehicleRegisterActivity.kt
│   ├── VehiclePhotoActivity.kt
│   ├── DriverRegisterActivity.kt
│   ├── CnhPhotoActivity.kt
│   ├── Mapao_do_Oda.kt
│   ├── SettingsActivity.kt
│   ├── CarameloRepository.kt
│   └── ui/theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── Documentos/Caramelo_App_Imagens_e_Paleta/
    ├── Relatorio_e_Paleta_Caramelo.md
    └── Imagens_PNG/ (15 telas geradas)
```

---

## 5. Uso de Ferramentas de IA (LLM)

| Item | Resposta da Equipe |
| --- | --- |
| **LLM/tool used** | Android Studio AI Assistant / Gemini |
| **Task supported by the LLM** | Auxílio no encadeamento de rotas e navegação entre Activities, salvamento seguro em `CarameloRepository` e geração das evidências PNG |
| **Main suggestion received** | Centralização das chamadas de intents e retorno na pilha de telas para preservar os estados de SharedPreferences |
| **What the team changed manually** | Organização da ordem dos campos de endereço (CEP em 1º), estilização com a paleta de cores Caramelo e validações de confirmação |
| **How the result was validated** | Execução e testes de fluxo completo no Android Studio, validação da volta entre telas e verificação de 0 erros de compilação |

---

## 6. Evidências de Execução

As capturas de tela e a paleta de cores foram salvas na pasta `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`:

- **Screen A (Login):** `02_tela_login.png`
- **Screen B (Cadastro Usuário):** `03_tela_cadastro_usuario.png`
- **Screen C (Cadastro Motorista e CNH):** `07_tela_cadastro_motorista.png` e `08_tela_foto_cnh.png`
- **Screen D (Mapa Principal):** `09_tela_principal_mapa.png`
- **Screen E (Configurações):** `13_tela_configuracoes.png`
