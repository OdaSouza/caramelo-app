# SPEC-003 — Navegação Multi-Telas e Grafo de Rotas

> **Team:** Caramelo Team  
> **Sprint:** Sprint 03  
> **Status:** Validated  
> **Related Sprint:** `SPRINT-03.md`

---

## 1. Context

Para que o usuário utilize os serviços do aplicativo **Caramelo**, é necessária uma estrutura de navegação entre diferentes telas e fluxos (Login, Cadastro de Usuário, Cadastro de Motorista, Foto da CNH, Mapa Principal e Configurações).

---

## 2. Objective

Evoluir a aplicação para uma arquitetura multi-telas utilizando transições de Intents e Composables desacoplados, garantindo preservação do estado e suporte ao retorno na pilha de telas (`back stack`).

---

## 3. User Scenario

**Given** que o usuário está na tela de Login (`LoginActivity`),  
**When** ele clica em "Criar conta",  
**Then** o aplicativo navega para a tela `RegisterActivity`. Ao concluir o cadastro ou acionar o botão voltar, o usuário retorna à tela inicial preservando o estado.

---

## 4. Functional Requirements

### FR-01 — Login to Register Navigation
Navegação da `LoginActivity` para `RegisterActivity` ao clicar em "Criar conta".

### FR-02 — Login to Map Navigation
Login bem-sucedido redireciona para a tela principal `Mapao_do_Oda`.

### FR-03 — Driver Registration Flow
Navegação da `SettingsActivity` para `DriverRegisterActivity` e em seguida para `CnhPhotoActivity`.

### FR-04 — Back Stack Management
Garantir o retorno correto ao acionar o botão "Voltar" ou o gesto do sistema operacional Android sem fechamento inesperado da aplicação.

---

## 5. Constraints

- Módulos organizados em arquivos Kotlin independentes (`LoginActivity.kt`, `RegisterActivity.kt`, `DriverRegisterActivity.kt`, `CnhPhotoActivity.kt`, `Mapao_do_Oda.kt`, `SettingsActivity.kt`).
- Registro das Activities no `AndroidManifest.xml`.

---

## 6. Acceptance Criteria

- AC-01 — Navegação fluida de entrada (Forward) e saída (Back) entre todas as telas do aplicativo.
- AC-02 — Módulos desacoplados em arquivos independentes.
- AC-03 — Retorno na pilha de telas preserva o histórico do usuário.

---

## 7. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01, FR-02 | `LoginActivity.kt`, `RegisterActivity.kt` | AC-01, AC-02 | `02_tela_login.png`, `03_tela_cadastro_usuario.png` |
| FR-03 | `DriverRegisterActivity.kt`, `CnhPhotoActivity.kt` | AC-01 | `07_tela_cadastro_motorista.png`, `08_tela_foto_cnh.png` |
| FR-04 | Todas as Activities | AC-03 | `13_tela_configuracoes.png` |

---

## 8. Deliverables

- `docs/specs/SPEC-003.md`
- Telas em `app/src/main/java/com/draxio/mapa_teste/`
- Evidências em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`
