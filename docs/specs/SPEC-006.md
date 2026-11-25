# SPEC-006 — Integração de Hardware do Dispositivo e Recursos Externos

> **Team:** Caramelo Team  
> **Sprint:** Sprint 06  
> **Status:** Validated  
> **Related Sprint:** `SPRINT-06.md`

---

## 1. Context

O funcionamento seguro e eficiente do aplicativo **Caramelo** depende de capacidades físicas do celular Android: a câmera para fotografia e enquadramento de documentos (CNH e veículo) e a geolocalização para تحديد ponto de partida e cálculo de motoristas por perto.

---

## 2. Objective

Integrar a captura de fotos da câmera via contratos do Android (`ActivityResultContracts.TakePicturePreview`) e serviços de geolocalização GPS com tratamento explícito dos estados de **Sucesso**, **Carregando** e **Erro / Permissão Negada**.

---

## 3. User Scenario

### Cenário 1: Captura de Foto da CNH com Sucesso
**Given** que o usuário concedeu a permissão de câmera,  
**When** ele clica no botão "Capturar Foto da CNH",  
**Then** a câmera do sistema é acionada, a foto é capturada e o Compose exibe em tempo real a imagem renderizada dentro da moldura do documento.

### Cenário 2: Permissão Negada ou Captura Cancelada (Estado Alternativo)
**Given** que o usuário cancelou a captura ou negou a permissão de câmera,  
**When** ele retorna para a tela do aplicativo,  
**Then** o app exibe uma mensagem de alerta explicativa (*"Captura da foto cancelada"*) mantendo a moldura ativa para uma nova tentativa.

---

## 4. Functional Requirements

### FR-01 — Camera Hardware Integration
Integração com a câmera do celular através do contrato oficial `TakePicturePreview` em `VehiclePhotoActivity.kt` e `CnhPhotoActivity.kt`.

### FR-02 — Document Photo Framing
Exibição da foto do documento capturada com `Image` e `asImageBitmap()` dentro de uma moldura arredondada.

### FR-03 — Geolocation & GPS Integration
Permissões de geolocalização (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) declaradas no `AndroidManifest.xml` para identificação de ponto de embarque e busca de motoristas.

---

## 5. Acceptance Criteria

- AC-01 — Abertura e captura de fotos via câmera do dispositivo funcionando.
- AC-02 — Renderização da foto dentro da moldura da CNH.
- AC-03 — Estado alternativo amigável em caso de cancelamento da foto ou recusa de permissão.

---

## 6. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01, FR-02 | `CnhPhotoActivity.kt`, `VehiclePhotoActivity.kt` | AC-01, AC-02 | `06_tela_foto_veiculo.png`, `08_tela_foto_cnh.png` |
| FR-03 | `Mapao_do_Oda.kt`, `AndroidManifest.xml` | AC-03 | `09_tela_principal_mapa.png` |

---

## 7. Deliverables

- `docs/specs/SPEC-006.md`
- Integradores em `CnhPhotoActivity.kt` e `Mapao_do_Oda.kt`
- Evidências em `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`
