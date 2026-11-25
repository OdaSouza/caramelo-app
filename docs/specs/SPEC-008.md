# SPEC-008 — Do protótipo à produção

> Proposta da equipe de UI (Lucas) para o Draxio. Não altera as SPEC-001 a 007.
> Situação em 25/11/2026: o app compila (`assembleDebug` e `lintDebug` sem erros) e a jornada do passageiro funciona com dados simulados no aparelho. Este documento lista o que falta para publicar como serviço real.

## 1. Três níveis de "pronto"

| Nível | O que significa | Situação |
|---|---|---|
| **A. Demonstração** | APK instalável; jornada completa sem travar; README explica o que é simulado | Pronto nesta entrega |
| **B. Candidato a release** | Backup desligado, manifest limpo, R8, assinatura por variável, versão | Pronto nesta entrega, exceto a keystore (seção 4) |
| **C. Produção** | Backend, login real, matching em tempo real, pagamento, verificação real, políticas | Este documento |

**Não publique o nível B na Play Store como serviço real.** Hoje:
- os motoristas e o selo "Verificado" são fictícios;
- o login aceita qualquer senha;
- a selfie e o grupo autodeclarado são coletados sem operador nem política em vigor.

## 2. O que hoje é simulado e o que substitui

| Função no app | Hoje | Produção |
|---|---|---|
| Login, cadastro, Google, Apple | Flag local `is_logged_in` | Firebase Auth, Auth0 ou backend próprio (OAuth2/OIDC); sessão com token; recuperação de senha por e-mail |
| Propostas de motoristas | 5 motoristas fixos em `RideHailingOverlay` | Backend de matching em tempo real (WebSocket ou FCM); geolocalização de motoristas; expiração de propostas |
| Destino | Texto livre, Casa/Trabalho e recentes | Google Places Autocomplete e Directions/Routes (rota, distância, ETA, preço sugerido) |
| Ciclo da corrida (a caminho, PIN, em viagem) | Temporizador local | Estado da corrida no servidor; PIN gerado no servidor e validado pelo app do motorista |
| Pagamento (Pix, Dinheiro, Cartão) | Escolha gravada no aparelho, sem cobrança | PSP (Mercado Pago, Pagar.me, Stripe etc.) com tokenização. **Nunca** guardar número de cartão no app |
| Cupom CARAMELO10 | Código fixo | Motor de promoções no servidor |
| Avaliação e gorjeta | Gravadas no histórico local | Gravadas no servidor; gorjeta repassada ao motorista pelo PSP |
| Verificação do motorista | Status local + "Simular aprovação" (só no debug) | Upload seguro (selfie, certidões); conferência facial por provedor de KYC; consulta de antecedentes; revalidação anual; painel de operador |
| Mensagem com o motorista | Respostas rápidas simuladas | Chat via backend, com número mascarado para ligação |
| Histórico | SharedPreferences (JSON) | API, com cache local em Room |
| Apagar meus dados | Limpa o aparelho | Exclusão no servidor (exigida pela Play e pela LGPD, art. 18 — conferir) |
| Modo motorista (receber corridas, ganhos) | Não existe | App ou módulo separado |
| Agendar viagem, várias paradas | Não existe | Depois do backend de corridas |

## 3. Segurança e privacidade (bloqueia a publicação)
1. Política de Privacidade e Termos finais. A minuta está em `docs/MINUTA-TERMOS-PRIVACIDADE.md` e aguarda revisão jurídica do Lucas.
2. URL pública da Política de Privacidade (exigida pela Play).
3. Formulário "Segurança dos dados" da Play Console, coerente com a política.
4. Dados sensíveis: biometria (selfie) e grupo autodeclarado. Pedem consentimento específico e destacado (LGPD, art. 11 — conferir); a tela de verificação já pede.
5. Trocar `androidx.security:security-crypto` (descontinuado) por DataStore + Tink, ou mandar os dados ao servidor.
6. Chave do Maps restrita por pacote e SHA-1 no Google Cloud Console.
7. Exclusão de conta pelo app e por link web.

## 4. Release (decisões do dono da conta)
- **applicationId:** hoje `com.draxio.mapa_teste`. É definitivo depois da primeira publicação. Sugestão: `br.com.caramelo.app` ou outro domínio da marca. Decidir antes de publicar; o `namespace` pode continuar.
- **Keystore:** quem guarda a chave de assinatura é o dono da conta Play. O `app/build.gradle.kts` já lê a assinatura de variáveis de ambiente:
  - `CARAMELO_KEYSTORE`: caminho do .jks;
  - `CARAMELO_KEYSTORE_PASSWORD`;
  - `CARAMELO_KEY_ALIAS`;
  - `CARAMELO_KEY_PASSWORD`.

  Sem essas variáveis, o release sai sem assinatura. Recomendado: Play App Signing.
- **versionCode/versionName:** subir a cada envio.
- **Ícone monocromático** (Android 13+): falta um vetor de uma cor só do pino.

## 5. Ordem sugerida
1. Backend mínimo: autenticação e corridas, com motoristas de teste.
2. Places, Routes e preço sugerido.
3. App ou modo motorista.
4. PSP e pagamentos.
5. KYC e painel de operador.
6. Políticas, Play Console e teste fechado.
