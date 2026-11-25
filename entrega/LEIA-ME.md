# Entrega — Caramelo 1.0.0-demo (25/11/2026)

| Arquivo | Para quê |
|---|---|
| `Caramelo-demo-debug.apk` | Instalar no celular para testar (versão de teste). O mapa fica cinza porque foi compilado com uma chave fictícia do Maps; compile com o seu `local.properties` para ver o mapa. |
| `Caramelo-release-sem-assinatura.apk` | Versão de release reduzida pelo R8 (2,6 MB), sem assinatura. Para publicar, assine com a sua keystore (ver README). |

- Telas do app: `docs/telas` (e no README).
- Design editável (canvas): `docs/design-ui`.
- O que mudou: `CHANGELOG.md` e `REDESIGN-UI.md`.
- Próximos passos para produção: `docs/specs/SPEC-008.md`.
- Comparar com a versão original: `git diff 95a3154 main`.
