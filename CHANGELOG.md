# Changelog — Caramelo

Mudanças de **Lucas** (UI/UX, identidade visual e finalização da versão de demonstração), feitas sobre o código original do **Draxio**. O primeiro commit do repositório é a versão original do Draxio, sem alterações, para que o `git diff` mostre exatamente o que mudou.

Detalhes técnicos: [`REDESIGN-UI.md`](REDESIGN-UI.md). Próximos passos para produção: [`docs/specs/SPEC-008.md`](docs/specs/SPEC-008.md).

---

## [1.0.0-demo] — 25/11/2026 · App completo para demonstração

### Adicionado
- **Escolha de destino:** destino digitado, atalhos Casa e Trabalho e destinos recentes.
- **Ciclo completo da corrida:**
  - motorista a caminho, com **código de embarque (PIN)** e tempo de chegada;
  - em viagem, com progresso;
  - recibo com desconto, **avaliação em estrelas** e **gorjeta**.
- **Cancelamento com motivo.** A corrida cancelada fica registrada no histórico.
- **Na corrida:**
  - mensagens com respostas rápidas;
  - Rota abre o app de mapas;
  - Ligar mostra um aviso de demonstração.
- **Preço das propostas:** calculado a partir do valor oferecido pelo passageiro.
- **Carteira:** Pix, Dinheiro ou Cartão de demonstração, sem coletar dados de cartão. Cupom de teste `CARAMELO10`.
- **Central de Ajuda:** perguntas frequentes, relatar problema, segurança (190) e as telas de **Termos de Uso** e **Política de Privacidade**, com o texto da minuta.
- **Conta:** login lembrado, "Sair da conta" funcionando e **"Apagar meus dados"**.
- **Histórico:** mostra a situação (Concluída ou Cancelada), a nota, a gorjeta e o pagamento. O total gasto é o real.
- **Documentos:**
  - `README.md`;
  - `CHANGELOG.md`;
  - `docs/specs/SPEC-008.md`: caminho para produção;
  - `docs/MINUTA-TERMOS-PRIVACIDADE.md`: minuta para revisão jurídica.
- **Release:** R8 ligado, que reduz o APK de 22 MB para 2,6 MB, e assinatura por variáveis de ambiente.

### Alterado
- **Cadastro de passageiro:** leva direto ao mapa. O cadastro de veículo saiu desse fluxo.
- **Cadastro de motorista:** passou a ter 5 passos: dados → foto da CNH → veículo → foto do veículo → verificação de segurança.
- **Login com Google ou Apple e recuperação de senha:** avisam que estão em modo demonstração, em vez de simular sucesso.
- **Organização do código:** o arquivo do mapa, com 1.860 linhas, foi dividido em `RideModels.kt`, `RideSafety.kt`, `RideFlow.kt`, `DestinationPicker.kt`, `CarameloDrawer.kt`, `TripHistoryScreen.kt`, `PresentationScreen.kt`, `WalletScreen.kt` e `HelpScreen.kt`.
- **Endereço nas Configurações:** campos separados (rua, número, bairro, CEP, cidade, UF).
- **Dependências:** `core-ktx`, `lifecycle`, `play-services-maps` e `play-services-location` atualizadas para versões menores.

### Corrigido
- **Botão Voltar:** passa a funcionar em todas as telas sobrepostas, no menu, na corrida e nos cadastros. Antes, fechava o app.
- **Histórico, Configurações e Apresentação:** não zeram mais o pedido em andamento nem recriam o mapa.
- **Rotação da tela:** não perde mais formulários, corrida, motorista escolhido, fotos e certidões.
- **Modo noturno:** passa a funcionar quando as Configurações são abertas pelo mapa.
- **Preço oferecido:** a barra e os botões agora vão de R$ 10 a R$ 80. Antes, a barra parava em R$ 60 e o botão + ia até R$ 80.
- **Preferências de motorista e passo do preço:** as definidas nas Configurações passam a valer no mapa.
- **Motoristas bloqueados:** não aparecem mais nas propostas.
- **Interruptores das Configurações:** passam a ficar salvos.
- **Endereço salvo:** não se corrompe mais a cada vez que é salvo.
- **Dados de exemplo:** as viagens fictícias e o perfil "Marina Costa" não se misturam mais com os dados reais.
- **Localização:** a permissão "aproximada" é aceita. Se for negada, o mapa abre em São Paulo.
- **Restaurar Propostas:** não duplica mais a lista.
- **Barra de progresso:** acende o primeiro de 3 passos (1/3 × 3 arredondava para 0).
- **Biblioteca de testes:** `ui-test-junit4` estava sem versão, o que impedia o lint de rodar.

### Segurança e privacidade
- Cópia de segurança automática do Android desligada (`allowBackup="false"` e regras de exclusão).
- Grupo autodeclarado do motorista (dado sensível) passou a ser guardado criptografado.
- Senha nunca é guardada, nem no estado da tela.
- `.gitignore` protege `local.properties` (chave do Maps), keystores e arquivos gerados.

### Removido
- Declaração de uma `.MainActivity` que não existia.
- Layouts XML, drawable, cores e textos sem uso.
- `viewBinding` e as dependências sem uso `constraintlayout` e `material`.
- `statusBarColor`, que está obsoleto.

### Qualidade
- `assembleDebug` e `assembleRelease`: 0 erros e 0 avisos do compilador Kotlin.
- `lintDebug`: 0 erros. Antes: 1 erro e 33 avisos. Restam 6 avisos, de versões mais novas e do ícone monocromático.
- Revisão independente de código: 15 achados, todos corrigidos.

---

## [0.3.0] — 24/11/2026 · Escolha de motorista e verificação de segurança

### Adicionado
- **Verificação de segurança do motorista** (novo passo do cadastro):
  - selfie comparada com a CNH;
  - certidões de antecedentes criminais federal e estadual;
  - confirmação da EAR na CNH;
  - grupo autodeclarado opcional;
  - autorização de uso dos dados.
- **Tela de status da verificação** ("Em análise" e "Verificado"). O status também aparece nas Configurações.
- **Selo "Verificado"** nas propostas e **ficha de segurança do motorista**.
- **Botão Segurança na corrida**, com Compartilhar viagem e Ligar 190.

### Alterado
- **Filtro de propostas:** a escolha do tipo de motorista (Geral, Mulher, Homem, LGBTQIA+) passou a filtrar as propostas. A escolha de motorista do app original foi mantida.

## [0.2.0] — 24/11/2026 · Telas refeitas

### Adicionado
- **Biblioteca de componentes** (`ui/components/CarameloComponents.kt`): cartões, botões em pílula, botões circulares, barra de progresso em blocos, chips, barra superior, linhas de lista e de interruptor, campos de texto, menu flutuante e selos de status.
- **Ícones desenhados** (Material Icons Extended) no lugar dos emojis.

### Alterado
- **Telas redesenhadas:**
  - mapa, com menu flutuante;
  - painel "Para onde vamos?";
  - propostas;
  - "Motorista a caminho";
  - menu lateral;
  - histórico;
  - apresentação;
  - Configurações;
  - login, cadastro e recuperação de senha;
  - cadastro de motorista e de veículo;
  - fotos da CNH e do carro.

## [0.1.0] — 24/11/2026 · Identidade visual

### Adicionado
- **Logo oficial** aplicado ao app e ao ícone, em três versões: legado, redondo e adaptativo.
- **Fontes:** Plus Jakarta Sans no texto e Fredoka na marca, ambas com licença SIL OFL 1.1.
- **Canvas de design** com o sistema visual e as telas do fluxo, de 16 telas na versão final.

### Alterado
- **Paleta** alinhada ao logo: caramelo, laranja, chocolate e cacau, nos temas claro e escuro.
- **Escala tipográfica** de 5 tamanhos e cantos arredondados padronizados.
- **Nome do app:** "Caramelo". Antes era "mapa_teste".
