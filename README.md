# Caramelo

App Android de corridas urbanas no estilo Uber, 99 e inDrive. O passageiro propõe o preço, escolhe o tipo de motorista (Geral, Mulher, Homem ou LGBTQIA+) e aceita uma das propostas recebidas. Todo motorista passa por uma verificação de segurança.

> **Versão de demonstração.** Não há servidor. Corridas, motoristas, pagamentos e verificações são simulados, e todos os dados ficam só no aparelho. O que falta para produção está em [`docs/specs/SPEC-008.md`](docs/specs/SPEC-008.md).

## Equipe
- **Desenvolvimento:** Draxio (@draxio).
- **UI/UX, identidade visual e logo:** Lucas. O redesign está documentado em [`REDESIGN-UI.md`](REDESIGN-UI.md).

## Tecnologia
Kotlin 2.2, Jetpack Compose (Material 3), Google Maps SDK e Location. Fontes Plus Jakarta Sans e Fredoka (SIL OFL 1.1). minSdk 24 (Android 7) e targetSdk 37.

## Como compilar
1. Instale o Android Studio, ou o JDK 17 ou mais recente com o Android SDK (plataforma 37).
2. Crie o arquivo `local.properties` na raiz. Ele está no `.gitignore` e **nunca deve ser versionado**:
   ```properties
   sdk.dir=/caminho/para/Android/Sdk
   MAPS_API_KEY=sua_chave_do_google_maps
   ```
   Sem uma chave válida, o app abre, mas o mapa fica cinza.
3. Gere o APK de teste: `./gradlew assembleDebug`. O arquivo sai em `app/build/outputs/apk/debug/`.
4. Para conferir a qualidade: `./gradlew lintDebug`.

### Release
`./gradlew assembleRelease` (ou `bundleRelease` para a Play Store) usa o R8 para reduzir o app. A assinatura vem de variáveis de ambiente, e a keystore não fica no projeto:

| Variável | Conteúdo |
|---|---|
| `CARAMELO_KEYSTORE` | Caminho do arquivo `.jks` |
| `CARAMELO_KEYSTORE_PASSWORD` | Senha da keystore |
| `CARAMELO_KEY_ALIAS` | Alias da chave |
| `CARAMELO_KEY_PASSWORD` | Senha da chave |

Sem essas variáveis, o release é gerado sem assinatura.

## Funções (passageiro)
- Entrar, criar conta, recuperar senha e sair. Login e cadastro são locais.
- Mapa com localização atual.
- Destino digitado, com Casa, Trabalho e destinos recentes.
- Categorias: Carro, Carro com Ar, Moto, Entregas e Frete.
- Oferta de preço entre R$ 10 e R$ 80, com o passo definido nas Configurações.
- Tipo de motorista preferido, com a opção de fixar a escolha. As propostas são filtradas pelo tipo.
- Selo **Verificado** e ficha de segurança do motorista.
- Corrida completa:
  - motorista a caminho, com **código de embarque (PIN)**;
  - em viagem;
  - recibo, avaliação em estrelas e gorjeta.
- Cancelamento com motivo.
- Segurança: compartilhar a viagem e ligar 190.
- Mensagens com o motorista: respostas rápidas simuladas.
- Rota no app de mapas.
- Carteira: Pix, Dinheiro ou Cartão de demonstração, e cupom (para testar: `CARAMELO10`).
- Histórico com filtro, nota, gorjeta e situação da viagem.
- Configurações: modo noturno, endereço, preferências, motoristas bloqueados, sair e **apagar meus dados**.
- Ajuda: perguntas frequentes, relatar problema, Termos e Privacidade. Os textos são minuta em revisão jurídica.

## Funções (motorista)
Cadastro em 5 passos:
1. Dados e CNH.
2. Foto da CNH.
3. Dados do veículo.
4. Foto do veículo.
5. **Verificação de segurança:** selfie, certidões de antecedentes criminais federal e estadual, EAR, grupo autodeclarado opcional e autorização.

Depois do envio, o status aparece como "Em análise". Nos builds de teste há o botão "Simular aprovação". O modo motorista (receber corridas e ver ganhos) ainda não existe; veja a SPEC-008.

## Privacidade
- Os dados de CNH e de veículo ficam criptografados no aparelho.
- A cópia de segurança automática do Android está desativada.
- "Apagar meus dados" remove tudo o que o app guardou.
- Minuta de Termos e Política de Privacidade: [`docs/MINUTA-TERMOS-PRIVACIDADE.md`](docs/MINUTA-TERMOS-PRIVACIDADE.md).

## Estrutura
| Arquivo ou pasta | Conteúdo |
|---|---|
| `app/src/main/java/com/draxio/mapa_teste/` | Código |
| `Mapao_do_Oda.kt` | Mapa e painel da corrida |
| `RideFlow.kt`, `DestinationPicker.kt` | Ciclo da corrida e escolha de destino |
| `RideModels.kt`, `RideSafety.kt` | Modelos e segurança |
| `CarameloDrawer.kt`, `TripHistoryScreen.kt`, `WalletScreen.kt`, `HelpScreen.kt`, `PresentationScreen.kt` | Telas do menu |
| `*Activity.kt` | Login, cadastros, configurações e verificação |
| `CarameloRepository.kt` | Dados locais |
| `ui/theme`, `ui/components` | Tema e componentes visuais |
| `docs/specs/` | Especificações SPEC-001 a SPEC-008 |
