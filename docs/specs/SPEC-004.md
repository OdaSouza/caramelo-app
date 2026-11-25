# SPEC-004 — Formulários, Coleta de Dados e Validações

> **Team:** Caramelo Team  
> **Sprint:** Sprint 04  
> **Status:** Validated  
> **Related Sprint:** `SPRINT-04.md`

---

## 1. Context

Os cadastros de passageiros e motoristas no aplicativo **Caramelo** exigem precisão nos dados de contato, senha e endereço para garantir a entrega do serviço de transporte e a segurança das viagens.

---

## 2. Objective

Implementar formulários controlados em Jetpack Compose com regras de validação estritas (confirmação de e-mail/senha, endereço com CEP em primeiro lugar e dados da CNH), exibindo feedback visual de erro em cenários inválidos e salvamento em cenários válidos.

---

## 3. User Scenario

### Cenário 1: Entrada Inválida (E-mails Divergentes ou Endereço Incompleto)
**Given** que o usuário preencheu e-mails divergentes nos campos "E-mail" e "Confirmar E-mail",  
**When** ele clica no botão "Cadastrar",  
**Then** o aplicativo exibe o aviso visual *"Os e-mails informados não coincidem"* e impede o avanço.

### Cenário 2: Entrada Válida (Cadastro Concluído com Sucesso)
**Given** que o usuário preencheu e-mails e senhas idênticos e o endereço completo com CEP em primeiro lugar,  
**When** ele clica em "Cadastrar",  
**Then** os dados são validados, armazenados com segurança local e o app avança para a tela seguinte.

---

## 4. Functional Requirements

### FR-01 — Email & Password Confirmation
Campos "Confirmar E-mail" e "Confirmar Senha" obrigatórios com checagem de igualdade (`email == confirmEmail` e `password == confirmPassword`).

### FR-02 — Ordered Address Fields (CEP 1º)
Seção de endereço de cadastro estruturada com o campo **CEP em primeiro lugar**, seguido por Logradouro, Número, Bairro, Complemento, Cidade e Estado.

### FR-03 — Driver CNH Form
Campos de CNH (Número de Registro, Categoria A/B/AB e Validade MM/AAAA) no cadastro de motorista parceiro.

---

## 5. Acceptance Criteria

- AC-01 — Validação ativa para confirmação de e-mail e senha.
- AC-02 — Campo CEP posicionado no topo da seção de endereço de cadastro.
- AC-03 — Mensagens de erro amigáveis em cenários inválidos.
- AC-04 — Submissão aprovada apenas quando todos os campos obrigatórios forem válidos.

---

## 6. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01, FR-02 | `RegisterActivity.kt` | AC-01, AC-02, AC-03 | `03_tela_cadastro_usuario.png` |
| FR-03 | `DriverRegisterActivity.kt` | AC-04 | `07_tela_cadastro_motorista.png` |

---

## 7. Deliverables

- `docs/specs/SPEC-004.md`
- Formulários em `RegisterActivity.kt` e `DriverRegisterActivity.kt`
- Evidências em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`
