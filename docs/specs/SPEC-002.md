# SPEC-002 — Reatividade da Interface e Gerenciamento de Estado

> **Team:** Caramelo Team  
> **Sprint:** Sprint 02  
> **Status:** Validated  
> **Related Sprint:** `SPRINT-02.md`

---

## 1. Context

Para proporcionar uma experiência fluida no aplicativo de transporte **Caramelo**, a interface precisa reagir instantaneamente às escolhas do passageiro (como ajuste da oferta de preço, seleção da modalidade de motorista e alternância para o tema escuro).

**Problem:**  
Interfaces estáticas impedem a negociação de valores e o ajuste de preferências em tempo real na solicitação de corridas.

**User / Actor:**  
Passageiro do Caramelo que ajusta o valor oferecido na corrida e escolhe o perfil de motorista desejado.

**Usage Context:**  
Na tela do mapa, o usuário utiliza os botões `+` e `-` ou desliza a barra (`Slider`) para reajustar o valor da corrida. O aplicativo atualiza o valor da oferta e o texto de confirmação em tempo real.

---

## 2. Objective

Implementar o gerenciamento de estado declarativo em Jetpack Compose com `remember`, `mutableStateOf` e `mutableStateListOf`, permitindo reatividade imediata na precificação, seleção de motoristas, stream de propostas e alternância de temas.

---

## 3. User Scenario

### Cenário 1: Ajuste de Preço e Seleção de Modalidade
**Given** que o usuário está solicitando uma corrida na tela do mapa,  
**When** ele clica no botão `+` ou desliza a barra de preço,  
**Then** o estado `offeredPriceAmount` é atualizado e o texto do botão principal exibe instantaneamente o novo valor formatado em reais (ex: *"Confirmar Carro por R$ 18,40"*).

---

## 4. Functional Requirements

### FR-01 — Price Negotiation Controls
Oferecer controles interativos de incremento e decremento (`+` e `-`) e barra deslizante (`Slider`) para ajuste de preço da corrida entre R$ 10,00 e R$ 60,00.

### FR-02 — Driver Preference Selector
Oferecer chips interativos para escolha do perfil de motorista preferido (`Geral`, `LGBTQIA+`, `Mulher`, `Homem`) e chave de acionamento para fixar a preferência (`isFixedDriverPreference`).

### FR-03 — Dynamic Driver Proposals Stream
Exibir propostas de motoristas em tempo real utilizando `mutableStateListOf<DriverOffer>`, permitindo aceitar a proposta ou descartá-la ao toque.

### FR-04 — Dark Mode Theme Switcher
Permitir a alternância global de tema claro/escuro via `Switch` vinculado ao `ThemePreferences`.

---

## 5. Constraints

- Construído em Kotlin com Jetpack Compose.
- Utilização das primitivas oficiais de estado (`remember`, `mutableStateOf`, `mutableStateListOf`).

---

## 6. Out of Scope

- Pagamento via leitor de cartão NFC físico.
- Processamento bancário em tempo real com operadoras de cartão.

---

## 7. Acceptance Criteria

- AC-01 — O preço reage imediatamente aos cliques nos botões `+` / `-` e ao deslizar da barra.
- AC-02 — Os chips de preferência de motorista atualizam a seleção visualmente.
- AC-03 — Clicar em "Recusar" ou no card do motorista remove a proposta da lista.
- AC-04 — A troca do tema noturno reflete em toda a paleta de cores do app.

---

## 8. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01, FR-02 | `Mapao_do_Oda.kt` | AC-01, AC-02 | `09_tela_principal_mapa.png` |
| FR-03 | `Mapao_do_Oda.kt` | AC-03 | `10_propostas_motoristas.png` |
| FR-04 | `SettingsActivity.kt`, `Theme.kt` | AC-04 | `13_tela_configuracoes.png` |

---

## 9. Deliverables

- `docs/specs/SPEC-002.md`
- `app/src/main/java/com/draxio/mapa_teste/Mapao_do_Oda.kt`
- Evidências em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`
