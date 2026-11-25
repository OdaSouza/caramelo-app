# SPEC-001 — Interface e Identidade Visual do Caramelo App

## 1. Context

Para que os usuários utilizem o aplicativo de mobilidade urbana **Caramelo**, é necessária uma interface inicial de entrada e apresentação da marca que introduza a proposta de valor do aplicativo ("Mobilidade com calor humano"), exibindo a paleta de cores rust/caramelo e botões de acesso à plataforma.

## 2. Task

Implementar a interface e a identidade visual inicial do aplicativo usando Jetpack Compose, contendo a marca e logotipo do Caramelo (badge circular amarelo "C" com tipografia rust), formulários de e-mail e senha, e os botões de login social (Google e Apple) e de criação de conta.

## 3. Functional Requirements

- FR-01 — Display the application logo and brand name ("Caramelo").
- FR-02 — Display a short slogan or subtitle ("Mobilidade com calor humano").
- FR-03 — Display input text fields for e-mail and password credentials.
- FR-04 — Display primary action button "Entrar" and social sign-in buttons ("Entrar com o Google" and "Entrar com a Apple").
- FR-05 — Display navigation links to "Criar conta" and "Recupere sua conta".

## 4. Constraints

- A interface deve ser construída em Kotlin com Jetpack Compose e Material Design 3.
- Deve ser utilizada a paleta de cores oficial Caramelo (`CarameloRust #8C2B0E`, `CarameloYellow #F3A738`, `CarameloBg #FAF7F2`).
- Devem ser utilizados componentes declarativos Compose como `Column`, `Text`, `OutlinedTextField`, `Button`, `OutlinedButton`, `Spacer` e `Surface`.

## 5. Acceptance Criteria

- AC-01 — The Caramelo logo badge and brand name are clearly visible on the screen.
- AC-02 — The app theme uses the official rust and off-white color tokens (`LightColorScheme` / `DarkColorScheme`).
- AC-03 — Input fields for e-mail and password are functional and styled with rounded corners (`12.dp`).
- AC-04 — Social login buttons for Google (`🌐`) and Apple (`🍎`) are present.
- AC-05 — Appropriate spacing, vertical alignment, and responsiveness are applied across device sizes.

## 6. Validation

- Compilação do projeto com sucesso no Android Studio.
- Execução do aplicativo no emulador ou dispositivo físico Android.
- Verificação visual da presença do logotipo, cores da marca, botões de login e links de navegação.

## 7. AI Usage

### Tool
Google Gemini / Android Studio AI Assistant

### Purpose
Auxílio no desenvolvimento do tema Jetpack Compose, definição dos tokens de cores (`Color.kt` e `Theme.kt`) e composição do layout visual da tela de login do Caramelo.

### Generated Content
Estruturação da especificação `SPEC-001.md` em Markdown, tokens de cores Compose e composables de layout da interface inicial.

### Human Changes
A equipe definiu o conceito do aplicativo de transporte inclusivo, personalizou a paleta de cores de tom caramelo e ajustou as margens e acessibilidade dos botões.

## 8. Out of Scope

- Autenticação real com servidores de login do Google/Apple via backend.
- Rastreamento GPS ativo antes da tela principal.
- Integração com gateways de pagamento em cartão de crédito.

## 9. Deliverables

- `docs/specs/SPEC-001.md`.
- Código Android atualizado em `app/src/main/java/com/draxio/mapa_teste/LoginActivity.kt`.
- Tokens de tema em `app/src/main/java/com/draxio/mapa_teste/ui/theme/`.
- Evidência visual em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/02_tela_login.png`.

## 10. Status

- [x] Specification completed
- [x] Implementation completed
- [x] Acceptance criteria validated
- [x] Code reviewed
- [x] Commit created
- [x] Pull Request submitted
