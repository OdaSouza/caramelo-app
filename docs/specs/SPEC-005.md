# SPEC-005 — Persistência Local e Gerenciamento de Dados

> **Team:** Caramelo Team  
> **Sprint:** Sprint 05  
> **Status:** Validated  
> **Related Sprint:** `SPRINT-05.md`

---

## 1. Context

Para que as informações do usuário (perfil, endereço, histórico de viagens, motoristas bloqueados e status de motorista parceiro) não sejam perdidas ao fechar ou reiniciar o aplicativo, é necessária uma camada de armazenamento local permanente no dispositivo.

---

## 2. Objective

Implementar a classe repositório `CarameloRepository.kt` utilizando `EncryptedSharedPreferences` (criptografia AES256) para dados confidenciais e `SharedPreferences` com serialização JSON para dados estruturados, garantindo sobrevivência a reinícios do app.

---

## 3. User Scenario

**Given** que o usuário alterou seu endereço nas configurações, concluiu a foto da CNH ou realizou uma nova viagem,  
**When** ele encerra completamente o processo do aplicativo e o reabre no celular,  
**Then** todos os dados de perfil, status de motorista cadastrado (`🟢 Cadastrado`) e histórico de viagens continuam salvos e são apresentados automaticamente na interface.

---

## 4. Functional Requirements

### FR-01 — Centralized Repository (`CarameloRepository.kt`)
Encapsular todas as rotinas de leitura e gravação local fora dos componentes visuais do Compose.

### FR-02 — Encrypted User & Driver Credentials
Armazenar dados sensíveis de e-mail, senha, endereço e registro de CNH com criptografia de hardware AES256 (`MasterKey`).

### FR-03 — Structured Lists Storage (JSON)
Serializar e desserializar o histórico de viagens (`TripHistoryItem`) e a lista de motoristas bloqueados (`BlockedDriver`) utilizando sintaxe JSON nativa.

### FR-04 — Persistent Settings
Gravar e restaurar as configurações do Modo Noturno (`is_dark_mode`), preferência padrão de motorista e valor do passo de incremento dos botões de preço (`R$ 0,50`, `R$ 1,00`, `R$ 2,00`, `R$ 5,00`).

---

## 5. Acceptance Criteria

- AC-01 — Dados salvos no `CarameloRepository` sobrevivem ao encerramento e reinício do aplicativo.
- AC-02 — Dados confidenciais gravados com criptografia AES256.
- AC-03 — As configurações de tema e histórico são restauradas automaticamente.

---

## 6. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01, FR-02 | `CarameloRepository.kt`, `RegisterActivity.kt` | AC-01, AC-02 | `03_tela_cadastro_usuario.png` |
| FR-03, FR-04 | `CarameloRepository.kt`, `SettingsActivity.kt` | AC-01, AC-03 | `13_tela_configuracoes.png`, `14_historico_viagens.png` |

---

## 7. Deliverables

- `docs/specs/SPEC-005.md`
- `app/src/main/java/com/draxio/mapa_teste/CarameloRepository.kt`
- Evidências em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`
