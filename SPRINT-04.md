# Caramelo Team — SPRINT 04

## User Input, Forms & Validation

### Team Information
- **Número da equipe:** Caramelo Team
- **Membros da equipe:**
  - Draxio (@draxio)

---

## 1. Objetivo da Sprint

Implementar a coleta controlada de dados do usuário através de formulários com **Jetpack Compose**, estabelecendo regras de validação rigorosas para cenários válidos e inválidos, com feedback visual imediato para o usuário.

---

## 2. Formulários Implementados

No aplicativo **Caramelo**, foram implementados formulários completos com validações de dados e confirmações:

### 1. Cadastro de Usuário e Endereço (`RegisterActivity.kt`):
- **Campos:** Nome Completo, E-mail, Confirmar E-mail, Senha, Confirmar Senha, CEP (1º), Logradouro, Número, Bairro, Complemento, Cidade e Estado.
- **Regras de Validação:**
  - **Campos Obrigatórios:** Nome, E-mail, Senha e Endereço completo não podem ser vazios.
  - **Confirmação de E-mail:** `email == confirmEmail`.
  - **Confirmação de Senha:** `password == confirmPassword`.
  - **Validação de CEP e Endereço:** Padrão CEP (ex: `01310-100`) posicionado em primeiro lugar na seção.

### 2. Cadastro de Motorista e CNH (`DriverRegisterActivity.kt`):
- **Campos:** Nome Completo, E-mail, Confirmar E-mail, Telefone, Registro da CNH, Categoria (A, B, AB, C, D, E) e Validade.
- **Regras de Validação:**
  - Confirmação do e-mail informado (`email == confirmEmail`).
  - Preenchimento do número de registro e validade da CNH antes do avanço para a foto da CNH.

---

## 3. Fluxo de Validação e Feedback

- **Cenário Inválido (`invalid-input.png`):**  
  Se o usuário tentar submeter o cadastro com e-mails/senhas divergentes ou com algum campo de endereço em branco, o aplicativo bloqueia o avanço e exibe um aviso visual explícito (*"Os e-mails informados não coincidem"* ou *"Por favor, preencha todos os campos do endereço"*).

- **Cenário Válido (`valid-input.png`):**  
  Com todos os campos devidamente preenchidos e validados, o aplicativo aceita o formulário, grava os dados localmente no repositório criptografado (`CarameloRepository`) e avança com sucesso para a tela seguinte do aplicativo.

---

## 4. Uso de Ferramentas de IA (LLM)

| Item | Resposta da Equipe |
| --- | --- |
| **LLM/tool used** | Android Studio AI Assistant / Gemini |
| **Task supported by the LLM** | Inclusão de campos de confirmação em Compose, estruturação das regras de validação condicional e salvamento seguro em `CarameloRepository` |
| **Main suggestion received** | Utilização da estrutura `when` no disparo do botão com mensagens de erro contextualizadas para cada campo de validação |
| **What the team changed manually** | Organização da ordem visual dos campos (CEP em primeiro lugar na seção de endereço), criação das telas de CNH e confirmação dupla de e-mail/senha |
| **How the result was validated** | Testes no emulador executando cenários válidos e inválidos, checagem da persistência local e verificação de 0 erros no analisador de código |

---

## 5. Evidências de Execução

As evidências foram geradas e salvas na pasta `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`:

- **Cenário Inválido (Aviso de erro visível):** `03_tela_cadastro_usuario.png` e `07_tela_cadastro_motorista.png`
- **Cenário Válido (Formulário aceito e salvo):** `08_tela_foto_cnh.png` e `09_tela_principal_mapa.png`
