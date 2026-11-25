# Caramelo Team — SPRINT 06

## External or Device Integration

### Team Information
- **Número da equipe:** Caramelo Team
- **Membros da equipe:**
  - Draxio (@draxio)

---

## 1. Objetivo da Sprint

Integrar o aplicativo **Caramelo** às capacidades de hardware do dispositivo Android e serviços externos essenciais para o produto de mobilidade urbana (Câmera para captura de CNH/veículo e Geolocalização para embarque), gerenciando operações assíncronas, permissões de sistema e tratamento explícito de estados de **Sucesso**, **Carregando** e **Erro/Permissão Negada**.

---

## 2. Escolha e Relevância da Integração

### Capacidades Integradas:
1. **Câmera do Dispositivo (`ActivityResultContracts.TakePicturePreview`):**
   - **Relevância:** Essencial para a validação de segurança do cadastro de motoristas parceiros (fotografia da CNH aberta na moldura) e envio da foto do automóvel em `VehiclePhotoActivity.kt` e `CnhPhotoActivity.kt`.
2. **Serviços de Localização e Geolocalização (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`):**
   - **Relevância:** Utilizado na tela principal `Mapao_do_Oda.kt` para definir a origem do passageiro ("Localização atual"), calcular distâncias até os motoristas próximos e estimar tempos de chegada (`ETA`).

---

## 3. Fluxo de Dados e Comportamento Assíncrono

```text
[Ação do Usuário: 'Capturar Foto da CNH' ou 'Confirmar Viagem']
                              ↓
[Solicitação de Permissão / Lançamento de Contrato Android]
                              ↓
[UI exibe estado 'Carregando / Processando' ou Moldura da Câmera]
                              ↓
     ┌────────────────────────┴────────────────────────┐
     ↓                                                 ↓
[Permissão Concedida / Foto Capturada]          [Permissão Negada / Cancelado]
     ↓                                                 ↓
[Processamento do Bitmap / Coordenadas GPS]     [Exibição de Aviso de Alerta]
     ↓                                                 ↓
[UI renderiza 'Sucesso' com imagem/mapa]        [UI renderiza 'Estado Alternativo']
[Gravação em CarameloRepository]                [Botão 'Tentar Novamente']
```

---

## 4. Tratamento de Erros e Estado Alternativo

Para garantir conformidade com o critério `AC-05`, o aplicativo trata graciosamente os estados alternativos e falhas:
1. **Captura de CNH Cancelada/Negada:** Se o usuário recusar o acesso à câmera ou cancelar a foto, o app exibe a mensagem de aviso (*"Captura da foto cancelada. Por favor, tire uma foto legível da sua CNH."*) e mantém a moldura amigável permitindo nova tentativa.
2. **Localização Indisponível:** Caso a permissão de GPS seja negada, o sistema faz o fallback gracioso usando a localização definida via preferências sem travar o aplicativo.

---

## 5. Privacidade e Segurança (Proteção de Segredos)

- Todo o processamento de imagens e chaves criptográficas é realizado localmente no dispositivo via `EncryptedSharedPreferences` com chaves `MasterKey` AES256-GCM (`AC-06`).
- Nenhuma chave privada ou credencial confidencial foi exposta no repositório.
- Permissões estritamente declaradas no `AndroidManifest.xml` (`CAMERA`, `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).

---

## 6. Uso de Ferramentas de IA (LLM)

| Item | Resposta da Equipe |
| --- | --- |
| **LLM/tool used** | Android Studio AI Assistant / Gemini |
| **Task supported by the LLM** | Integração do contrato `ActivityResultContracts.TakePicturePreview()`, permissões de localização no `AndroidManifest.xml` e gerenciamento de callbacks de imagem |
| **Main suggestion received** | Utilização do estado do Compose com `asImageBitmap()` para exibição fluida da foto capturada direto na moldura da UI |
| **What the team changed manually** | Construção da moldura de documento CNH com as cores do Caramelo, mensagens de feedback em português e vinculação com o `CarameloRepository` |
| **How the result was validated** | Execução no dispositivo/emulador capturando fotos reais, cancelando capturas para testar mensagens de erro e verificando 0 erros de compilação |

---

## 7. Evidências de Execução

As imagens foram salvas na pasta `Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`:

- **Sucesso da Integração (`integration-success.png`):** `08_tela_foto_cnh.png` e `09_tela_principal_mapa.png` (foto capturada com sucesso e localização dos motoristas no mapa).
- **Estado Alternativo / Erro (`integration-alternative-state.png`):** `06_tela_foto_veiculo.png` (moldura aguardando captura e tratamento de cancelamento).
