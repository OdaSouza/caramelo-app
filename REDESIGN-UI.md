# Redesign visual do Caramelo — etapa 1 (base)

Referência do design: canvas "Caramelo — UI/UX" (sistema visual + 7 telas).

## O que mudou no código

| Arquivo | Mudança |
|---|---|
| `ui/theme/Color.kt` | Paleta alinhada ao logo oficial. Nomes antigos mantidos; novo token `CarameloCocoa`. |
| `ui/theme/Theme.kt` | Esquemas claro e escuro refeitos; `Shapes` com cantos de 8 a 32 dp. No escuro o `primary` continua caramelo `#B4531A`, porque o código usa texto branco sobre `primary`. |
| `ui/theme/Type.kt` | Escala de 5 tamanhos (40 / 24 / 17 / 15 / 13), com Plus Jakarta Sans no texto e Fredoka na marca (ver "Fontes"). |
| `Mapao_do_Oda.kt` | `CarameloLogo()` agora mostra o pino do logo oficial + "Caramelo". Vale para as 16 chamadas em 10 telas. |
| `LoginActivity.kt` | Logo oficial completo no topo e título "Mobilidade com calor humano". |
| `res/mipmap-*` | Ícone do app trocado pelo pino do logo (legado, redondo e adaptativo). |
| `res/drawable-nodpi` | `caramelo_logo.webp` (logo completo) e `caramelo_marca.png` (pino). |
| `res/values/strings.xml` | Nome do app: "Caramelo" (antes "mapa_teste"). |
| `res/values/colors.xml` | Removidas as cores roxa e verde-água do modelo; entram as da marca. |

## Paleta

| Token | Cor | Uso |
|---|---|---|
| `CarameloRust` | `#B4531A` | Botões e ações (texto branco 5,0:1) |
| `CarameloOrange` | `#F08A1C` | Destaques, mapa, rota, progresso |
| `CarameloRustDark` | `#5A1E0E` | Nome da marca, textos de marca |
| `CarameloCocoa` | `#3A1A0F` | Botões e cartões escuros |
| `CarameloBackground` | `#F3F1ED` | Fundo |
| `CarameloSelectedBg` | `#FFE9D1` | Item selecionado |
| `CarameloTextPrimary` | `#2E140A` | Texto |
| `CarameloTextMuted` | `#6E6762` | Legendas |

# Etapa 2: telas refeitas

## Componentes novos
`ui/components/CarameloComponents.kt` contém os componentes do redesign que todas as telas usam: cartão, botões em pílula (caramelo, cacau e contorno), botões e ações circulares, barra de progresso em blocos, chips, barra superior, linhas de lista e de interruptor, campo de texto, menu inferior flutuante e selo de status.

Nova dependência: `androidx.compose.material:material-icons-extended:1.7.8` (em `libs.versions.toml` e `app/build.gradle.kts`). Ela entra no lugar dos emojis usados como ícone.

## Telas
- **Mapa (`Mapao_do_Oda.kt`):**
  - Menu flutuante embaixo (Início, Viagens, Carteira e Perfil). Ele some durante a corrida.
  - Painel inferior com "Para onde vamos?", as categorias em cartões com ícone, a preferência de motorista em chips e a oferta com os botões − e +.
  - Busca e corrida no estilo "Motorista a caminho": tempo grande, barra em blocos, placa e ações Ligar, Mensagem, Rota e Segurança.
  - Menu lateral, histórico e apresentação no mesmo estilo.
- **Configurações (`SettingsActivity.kt`):** cartão de perfil, seções em cartões com ícones e interruptores, bloqueio de motoristas e histórico.
- **Entrar, cadastro e recuperação de senha:** campos preenchidos, botões em pílula, Google e Apple lado a lado.
- **Cadastro de motorista e de veículo, fotos da CNH e do carro:** barra superior, "Passo 1 de 2" e moldura da foto.

O comportamento foi mantido em todas as telas: estados, validações, Intents, Toasts, repositório e assinaturas das funções.

## Mudanças pequenas de comportamento
- O painel de busca e de corrida aparece por cima do painel inferior. Antes, ele ficava escondido atrás.
- As quatro ações circulares mostram o aviso "… em breve", porque ainda não têm função.
- No cadastro, a mensagem de erro aparece também na tela, além do Toast.

## Problemas que já existiam no original (não foram mexidos)
- O botão + deixa a oferta chegar a R$ 80, mas o controle deslizante vai só até R$ 60.
- Aberto pelo mapa, o interruptor de modo noturno das Configurações não faz nada. Ele só funciona pela `SettingsActivity`.
- O `AndroidManifest.xml` declara uma `.MainActivity` que não existe.

## Fontes
- **Plus Jakarta Sans** é a fonte de todo o texto, em 5 pesos: 400, 500, 600, 700 e 800.
- **Fredoka** é a fonte do nome "Caramelo", em 3 pesos: 500, 600 e 700.
- Os arquivos vieram do Google Fonts, são `.ttf` estáticos (um por peso) e ficam em `res/font`. São estáticos para funcionar também no Android 7, já que fontes variáveis só funcionam a partir do 8.
- As duas fontes usam a licença SIL OFL 1.1, que permite usar e embutir no app, inclusive comercial. O texto das licenças vai junto em `assets/licencas`.
- As fontes somam uns 460 KB no APK.
- Estão ligadas em `Type.kt` (`CarameloFont` e `CarameloBrandFont`). Como a Fredoka vai só até o peso 700, o ExtraBold do nome usa o Bold.

**Não compilado.** Duas revisões linha por linha não acharam erros, mas é preciso rodar no Android Studio antes de enviar.

# Etapa 3: escolha de motorista e verificação de segurança

A escolha de motorista do app original foi mantida: o tipo preferido (Geral, Mulher, Homem, LGBTQIA+), a opção de fixar a preferência e a lista de propostas em que o passageiro aceita ou recusa cada motorista. A esse fluxo foi somada uma verificação de segurança no modelo dos apps de corrida.

## Motorista: novo passo 3, "Verificação de segurança" (`SecurityCheckActivity.kt`)
O cadastro passa a ter 3 passos: dados, foto da CNH e verificação. O motorista envia:
- uma selfie, para comparar com a foto da CNH;
- a certidão de antecedentes criminais federal, da Polícia Federal (PDF ou foto);
- a certidão de antecedentes criminais estadual, da Secretaria de Segurança Pública ou do Tribunal de Justiça;
- a confirmação de que a CNH tem a observação EAR (Exerce Atividade Remunerada).

Ele também pode informar, se quiser, o grupo em que quer aparecer no filtro de preferência (Mulher, Homem ou LGBTQIA+). É uma autodeclaração.

Por fim, marca a autorização: checagem de antecedentes, uso da selfie (dado biométrico) e uso do grupo informado, só para a segurança das viagens, com nova checagem todo ano.

Depois do envio, o motorista vê a tela de status ("Em análise" e, depois, "Verificado"). As Configurações também mostram esse status.

**Sem servidor ainda.** O envio só grava o status no aparelho (`CarameloRepository`), e os arquivos não são enviados a lugar nenhum. A conferência real é tarefa do backend. Nas versões de teste existe o botão "Simular aprovação (só em teste)", que não aparece na versão final.

## Passageiro
- Toda proposta mostra o selo verde **Verificado**. Ao tocar nele, abre a ficha de segurança do motorista:
  - identidade conferida;
  - CNH com EAR;
  - antecedentes criminais sem registros, com o mês da checagem;
  - veículo conferido;
  - grupo autodeclarado, quando houver.
- **Mudança de comportamento:** a lista de propostas agora mostra só os motoristas do tipo escolhido. "Geral" mostra todos. Antes, o tipo escolhido não filtrava a lista.
- Durante a corrida, o botão **Segurança** (antes "em breve") abre a mesma ficha, com mais dois botões:
  - **Compartilhar viagem:** envia o motorista, o carro, a placa e o destino por qualquer app.
  - **Ligar 190:** abre o discador com o número já digitado.
- Foram incluídos dois motoristas fictícios, Rafael (Homem) e Juliana (Mulher), para todos os filtros terem propostas.
- O botão "Restaurar Propostas" agora limpa a lista antes de recarregar. Antes, ele duplicava as propostas.

## Correção no componente
`CarameloSegmentedProgress`: com 3 segmentos, 1/3 × 3 dava 0,999 e não acendia nenhum. Agora acende corretamente.

## A validar
- Os textos da autorização e da ficha precisam de validação jurídica. Selfie e orientação sexual são dados sensíveis (LGPD, art. 5º, II, e art. 11).
- A empresa precisa confirmar quais certidões vai exigir.

# Etapa 4: app completo para demonstração (25/11/2026)

O projeto foi **compilado nesta máquina** (JDK 25 e Android SDK 37). Resultado:
- `assembleDebug` e `assembleRelease` (com R8) passam sem erro e sem warning do Kotlin;
- o `lintDebug` termina com 0 erros. Antes eram 1 erro e 33 avisos. Os avisos que restam são de versões novas e do ícone monocromático.

## Funções novas
| Função | Onde |
|---|---|
| Destino digitado, com Casa, Trabalho e recentes | `DestinationPicker.kt` |
| Ciclo completo da corrida: a caminho com **PIN de embarque** e tempo de chegada, em viagem, recibo com **avaliação e gorjeta** | `RideFlow.kt` |
| Cancelamento com motivo; viagem cancelada no histórico | `RideFlow.kt` |
| Mensagem com respostas rápidas, Rota no app de mapas, Ligar (aviso de demonstração) | `RideFlow.kt` |
| Propostas com preço a partir do valor oferecido pelo passageiro; bloqueados ficam fora | `RideModels.kt`, `Mapao_do_Oda.kt` |
| Carteira (Pix, Dinheiro, Cartão de demonstração) e cupom `CARAMELO10` | `WalletScreen.kt` |
| Ajuda: perguntas frequentes, relatar problema, segurança, Termos e Privacidade (minuta) | `HelpScreen.kt` |
| Sessão: login lembrado, "Sair da conta" real, **"Apagar meus dados"** | `LoginActivity.kt`, `SettingsActivity.kt` |
| Modo noturno funcionando pelos dois caminhos | `ui/theme/Theme.kt` |
| Histórico com situação, nota, gorjeta e total real | `TripHistoryScreen.kt` |

## Mudanças de fluxo (decisões do Lucas)
- **Cadastro de passageiro** leva direto ao mapa. O veículo saiu desse fluxo.
- **Cadastro de motorista** passa a ter 5 passos: dados → foto da CNH → veículo → foto do veículo → verificação de segurança.
- Login com Google ou Apple e recuperação de senha avisam que estão em **modo demonstração**, em vez de fingir sucesso.

## Correções
- **Navegação:** o botão Voltar do sistema funciona em todas as telas sobrepostas, no menu e na corrida. Histórico, Configurações e Apresentação abrem por cima do mapa, sem zerar o painel nem recriar o mapa.
- **Rotação de tela:** os formulários, a corrida, o motorista aceito e as fotos e certidões do cadastro sobrevivem.
- **Oferta:** a barra e os botões vão de R$ 10 a R$ 80. O passo dos botões vem das Configurações.
- **Preferências ligadas ao mapa:** a preferência de motorista e o "fixar" que estão nas Configurações agora valem no mapa.
- **Configurações:** o endereço tem campos separados e não se corrompe mais. Os interruptores ficam salvos. O histórico das Configurações é o real.
- **Dados de exemplo:** as viagens de exemplo e o perfil "Marina Costa" saíram dos dados reais.
- **Localização:** a permissão aproximada é aceita. Se a permissão for negada, o mapa abre em São Paulo.
- **Privacidade:**
  - o grupo autodeclarado do motorista agora fica criptografado;
  - a cópia de segurança automática está desligada (`allowBackup=false` e regras de exclusão);
  - a senha nunca é guardada.
- **Limpeza:**
  - saíram a `.MainActivity` inexistente, os layouts XML e o drawable sem uso, o `viewBinding`, o `constraintlayout` e o `material` (eram dependências sem uso);
  - `statusBarColor` deprecated removido;
  - `String.format` agora usa Locale pt-BR;
  - o BOM do Compose passou a valer também para os testes;
  - `core-ktx`, `lifecycle`, `maps` e `location` foram atualizados para versões menores novas. O BOM do Compose ficou em 2026.02.01, porque o novo exige Android 37.1 e migrar os campos de senha.
- **Organização:** o arquivo do mapa (1.860 linhas) foi dividido em `RideModels.kt`, `RideSafety.kt`, `CarameloDrawer.kt`, `TripHistoryScreen.kt` e `PresentationScreen.kt`.

## Release
- R8 ligado.
- A assinatura vem das variáveis `CARAMELO_KEYSTORE*` (ver `README.md`).
- O APK de release fica com cerca de 2,6 MB (o debug tem 22 MB).

## Documentos novos
- `README.md`.
- `docs/specs/SPEC-008.md`: o caminho para produção.
- `docs/MINUTA-TERMOS-PRIVACIDADE.md`: aguarda revisão jurídica.

## Ainda falta (ver SPEC-008)
- Backend: login real, motoristas reais, pagamento e verificação.
- Modo motorista.
- Decidir o `applicationId` e a keystore.
- Política de Privacidade publicada.
- Ícone monocromático.

# Como testar (Draxio)
1. **Chave do mapa.** O pacote não traz o `local.properties`, porque ele tem a `MAPS_API_KEY`. Copie o seu para a raiz do projeto. Sem ele, o mapa fica cinza.
2. **Abrir e sincronizar.** Abra a pasta no Android Studio e rode o Gradle Sync. Ele baixa a dependência nova de ícones.
3. **Ver o que mudou.** O `.git` veio junto. O `git status` e o `git diff` mostram só as mudanças do redesign em relação à versão original.
4. **Roteiro de teste no aparelho ou emulador, nos temas claro e escuro:**
   1. Entrar, Criar conta e Esqueci minha senha. Teste também a mensagem de erro com os campos vazios.
   2. No mapa:
      - Escolha a categoria e o motorista e mude a oferta com − e +.
      - Peça a corrida e acompanhe a busca e o "Motorista a caminho".
      - Cancele a corrida.
   3. O menu lateral, o histórico e a apresentação.
   4. As Configurações: os interruptores, o bloqueio de motoristas e o "Seja motorista".
   5. O cadastro de motorista em 3 passos:
      - a foto da CNH;
      - a verificação de segurança: selfie, envio das 2 certidões, EAR e autorização;
      - o status, com "Simular aprovação" e o selo nas Configurações.
   6. Escolha Mulher, Homem e LGBTQIA+ e veja a lista de propostas filtrar. Toque em "Verificado" na proposta e em "Segurança" durante a corrida. Teste também Compartilhar e Ligar 190.
   7. O cadastro de veículo, com a foto do carro.
   8. O ícone do app na tela inicial, em formato redondo e quadrado.
5. **Em caso de erro de compilação,** devolva a mensagem completa do Build. A correção é feita do lado da UI.
