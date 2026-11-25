# Caramelo Team — SPRINT 07

## Final Integration, Quality & Delivery

### Team Information
- **Número da equipe:** Caramelo Team
- **Membros da equipe:**
  - Draxio (@draxio)

---

## 1. Objetivo da Sprint Final

Integrar todo o produto acumulado no aplicativo **Caramelo**, executar testes de regressão de ponta a ponta, garantir a consistência visual da interface do usuário, assegurar zero erros/craches na jornada principal e consolidar a entrega final reproduzível.

---

## 2. Matriz de Revisão das Especificações (Traceabilidade)

| Especificação | Funcionalidade Principal | Status Atual | Evidências Visuais |
| :--- | :--- | :--- | :--- |
| **SPEC-001** | Protótipo Inicial & Identidade Caramelo | **PASS** | `01_paleta_de_cores.png`, `02_tela_login.png` |
| **SPEC-002** | Gerenciamento de Estado & Reatividade UI | **PASS** | `09_tela_principal_mapa.png`, `10_propostas_motoristas.png` |
| **SPEC-003** | Navegação Multi-Telas (`Activities` / Compose) | **PASS** | `02_tela_login.png`, `03_tela_cadastro_usuario.png`, `13_tela_configuracoes.png` |
| **SPEC-004** | Formulários, Coleta & Validação com CEP 1º | **PASS** | `03_tela_cadastro_usuario.png`, `07_tela_cadastro_motorista.png` |
| **SPEC-005** | Persistência Local (`CarameloRepository`) | **PASS** | `13_tela_configuracoes.png`, `14_historico_viagens.png`, `15_bloqueio_motoristas.png` |
| **SPEC-006** | Integrações de Dispositivo (Câmera CNH / GPS) | **PASS** | `06_tela_foto_veiculo.png`, `08_tela_foto_cnh.png` |
| **SPEC-007** | Qualidade Final, Zero Erros e Entrega | **PASS** | `Relatorio_e_Paleta_Caramelo.md` + 15 Imagens PNG |

---

## 3. Fluxo de Validação de Regressão de Ponta a Ponta

Para garantir a estabilidade do aplicativo, foi executada a jornada completa sem falhas:

1. **Abertura:** Inicialização fluida do app na tela de Login (`LoginActivity.kt`).
2. **Cadastro & Validação:** Fluxo de criação de conta (`RegisterActivity.kt`) com validação de confirmação de e-mail/senha e dados de endereço (CEP em primeiro lugar).
3. **Motorista & CNH:** Fluxo de cadastro de motorista parceiro (`DriverRegisterActivity.kt`) com captura e enquadramento da CNH em moldura (`CnhPhotoActivity.kt`).
4. **Navegação & Mapa:** Entrada na tela principal (`Mapao_do_Oda.kt`), escolha das modalidades (`Carro`, `Carro com Ar`, `Moto`, `Entregas`, `Frete`) e ajuste de preço (+ / - e Slider).
5. **Stream & Aceite:** Abertura da janela de propostas com acionamento do aceite de motoristas e gravação no histórico.
6. **Configurações & Persistência:** Troca do Tema Noturno, alteração de preferências e verificação dos dados salvos no `CarameloRepository.kt`.
7. **Retorno:** Teste de navegação de volta (`finish()` / `popBackStack()`) mantendo o app estável e sem crashes.

---

## 4. Uso de Ferramentas de IA (LLM)

| Item | Resposta da Equipe |
| --- | --- |
| **LLM/tool used** | Android Studio AI Assistant / Gemini |
| **Task supported by the LLM** | Revisão final do código para eliminação de warnings, organização do repositório e geração das imagens de evidências |
| **Main suggestion received** | Refatoração das chamadas de persistência KTX no `CarameloRepository.kt` e validação com `analyze_file` zerando todos os avisos do compilador |
| **What the team changed manually** | Execução de testes de regressão no emulador, validação da ordem dos campos e cópia dos entregáveis para a pasta de Documentos |
| **How the result was validated** | Análise final de código (0 erros de compilação, 0 warnings no repositório), teste de reinício e validação visual de todas as 15 telas |

---

## 5. Limitações Conhecidas e Considerações

- O mapa utiliza visualização interativa Compose com marcadores e suporte a modo de inspeção/preview.
- A criptografia de credenciais opera em hardware local com `EncryptedSharedPreferences` AES256.

---

## 6. Entregáveis Finais e Evidências

Todos os entregáveis finais do projeto estão gravados no repositório e sincronizados na pasta de documentos do sistema:

📁 **Diretório das Imagens Finais:**
`/home/draxio/Documentos/Caramelo_App_Imagens_e_Paleta/Imagens_PNG/`

📁 **Relatório da Paleta e Sprints:**
`/home/draxio/Documentos/Caramelo_App_Imagens_e_Paleta/`

```text
Caramelo/
├── README.md
├── SPRINT-01.md ... SPRINT-07.md
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
│   └── CarameloRepository.kt
└── Documentos/Caramelo_App_Imagens_e_Paleta/
    ├── Relatorio_e_Paleta_Caramelo.md
    └── Imagens_PNG/ (15 telas finais em PNG)
```
