# Project Status

## Visao geral

Projeto do bolao da Copa com:

- Backend em Java 21 + Spring Boot 3
- Banco PostgreSQL com Flyway
- Frontend em React + TypeScript + Vite

Estado atual em 2026-06-19:

- Backend principal do MVP implementado
- Testes do backend passando
- Frontend MVP publico implementado
- Build do frontend validado com sucesso
- Backend preparado para Render Free com `PORT`, CORS e blueprint `render.yaml`
- Deploy do backend no Render ajustado para Docker por compatibilidade com a documentacao atual do Render
- Frontend preparado para consumir `VITE_API_BASE_URL` no Vercel

## Arquitetura atual

### Backend

Estrutura atual do backend:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`
- `resources/db/migration`

Padrao aplicado:

- Controllers expoem endpoints REST
- Services concentram regras de negocio
- Repositories usam Spring Data JPA
- DTOs definem contratos de entrada e saida
- Flyway versiona schema do banco

Principais modulos:

- Importacao de dados do bolao fechado
- Consulta de jogos
- Atualizacao de resultados oficiais
- Consulta de participantes
- Consulta de palpites por participante
- Calculo de pontuacao
- Calculo de ranking

### Frontend

Estrutura atual do frontend:

- `app`
- `features/ranking`
- `features/participants`
- `features/matches`
- `shared/components`
- `shared/services`
- `shared/types`
- `shared/utils`
- `styles`

Padrao aplicado:

- React + TypeScript + Vite
- React Router para navegacao
- Tailwind CSS para layout e estilo
- Organizacao por `features`
- Servicos HTTP simples baseados em `fetch`
- Layout mobile first com visual de aplicativo
- Menu inferior fixo com apenas `Ranking` e `Jogos`
- Aplicacao somente leitura, sem login e sem acoes administrativas

Telas implementadas:

- Ranking como tela inicial
- Detalhe do participante
- Jogos

## Entidades

Entidades de dominio presentes no backend e consumidas pelo frontend:

### Participant

- Representa um participante do bolao
- Campos principais:
  - `id`
  - `name`

### Team

- Representa uma selecao
- Campos principais:
  - `id`
  - `name`
  - `fifaCode`
  - `flagUrl`

### Match

- Representa um jogo da Copa
- Campos principais:
  - `id`
  - `phase`
  - `matchNumber`
  - `homeTeam`
  - `awayTeam`
  - `startsAt`
  - `status`
  - `venue`
  - `homeScore`
  - `awayScore`
  - `officialResultAt`

### Prediction

- Representa o palpite de um participante para um jogo
- Campos principais:
  - `id`
  - `participant`
  - `match`
  - `predictedHomeScore`
  - `predictedAwayScore`
  - `pointsAwarded`

### Enums

- `MatchStatus`
- `TournamentPhase`

## Endpoints

### Admin / Importacao

- `POST /api/admin/imports/seed`
  - Importa participantes, selecoes, jogos e palpites do bolao fechado
  - Faz deduplicacao por chaves naturais
  - Nao e consumido pela UI publica do MVP

### Ranking

- `GET /api/ranking`
  - Retorna ranking consolidado dos participantes
  - Consumido por Ranking e detalhe do participante

### Participantes

- `GET /api/participants`
  - Lista participantes
  - Disponivel no backend, nao e usado pela UI atual

- `GET /api/participants/{participantId}`
  - Retorna dados de um participante
  - Consumido pelo detalhe do participante

- `GET /api/participants/{participantId}/predictions`
  - Retorna os palpites do participante
  - Consumido pelo detalhe do participante

### Jogos

- `GET /api/matches`
  - Lista jogos
  - Filtros opcionais:
    - `status`
    - `phase`
    - `date`
  - Consumido por Jogos

- `GET /api/matches/{matchId}`
  - Retorna detalhe de um jogo
  - Disponivel no backend, nao usado pela UI atual

- `PATCH /api/matches/{matchId}/result`
  - Atualiza resultado oficial do jogo
  - Recalcula a pontuacao dos palpites do jogo
  - Fora da UI publica atual

## Migrations

### V1__create_initial_schema.sql

Cria:

- tabela `participants`
- tabela `teams`
- tabela `matches`
- tabela `predictions`
- constraints de integridade
- indices principais de consulta

Regras relevantes:

- `prediction` unico por participante + jogo
- placares nao negativos
- jogo nao pode ter o mesmo time dos dois lados

### V2__add_seed_natural_keys.sql

Adiciona:

- unique em `participants.name`
- unique em `matches.match_number`

Objetivo:

- suportar importacao idempotente do seed do bolao fechado

## Funcionalidades concluidas

### Backend

- Estrutura base Spring Boot configurada
- Integracao com PostgreSQL configurada
- Migrations Flyway implementadas
- Modelo de dominio principal implementado
- Importacao de seed do bolao fechado implementada
- Seed canonico da fase de grupos gerado em `backend/src/main/resources/import/group-stage-seed.json`
- Validacoes de payload de importacao implementadas
- Deduplicacao de importacao implementada
- Atualizacao de resultado oficial implementada
- Recalculo de pontos por jogo implementado
- Regra de pontuacao implementada
- Calculo de ranking implementado
- Consulta de participantes implementada
- Consulta de palpites por participante implementada
- Consulta de jogos com filtros implementada
- Tratamento basico de erros implementado
- Script operacional `backend/update-known-results.ps1` implementado para aplicar 28 resultados oficiais conhecidos da fase de grupos via `PATCH /api/matches/{matchId}/result`

### Frontend

- App React + TypeScript + Vite criado
- Tailwind CSS configurado
- React Router configurado
- Layout mobile first implementado
- Ranking como tela inicial implementado
- Pagina de Detalhe do participante implementada
- Pagina de Jogos implementada
- Navegacao por datas disponiveis implementada em participante e jogos
- Menu inferior fixo com `Ranking` e `Jogos` implementado
- Consumo dos endpoints reais do backend implementado
- Estados de loading, erro e vazio implementados
- Navegacao entre ranking e detalhe do participante implementada
- Mojibake corrigido nas telas publicas principais
- Resumo de jogos finalizados exibido no topo do ranking
- Resumo de pontos do dia adicionado no detalhe do participante
- Build de producao executado com sucesso via `npm run build`

### Testes e validacao

- Testes unitarios da regra de pontuacao
- Testes unitarios de ranking
- Testes unitarios de atualizacao de resultado
- Testes de integracao dos controllers principais
- Importacao do seed canonico da fase de grupos validada via endpoint
- Atualizacao operacional de 28 resultados conhecidos validada via API e banco
- Ordenacao estavel de jogos aplicada por `startsAt` + `matchNumber` no backend e no frontend
- `mvn test` executado com sucesso em 2026-06-19
- `npm run build` executado com sucesso em 2026-06-19

## Funcionalidades pendentes

### Produto / Operacao

- Documentar payload de importacao com exemplo de uso
- Construir parser offline dos PDFs do bolao para gerar `seed-import.json`
- Documentar fluxo operacional do MVP
- Documentar como executar frontend e backend juntos
- Validar o frontend contra uma base real importada no backend

### Frontend / Qualidade

- Adicionar testes do frontend
- Refinar UX de navegacao conforme uso real em celular
- Adicionar configuracao explicita de ambiente para URL da API em deploy
- Publicar o backend no Render Free e o frontend no Vercel com Supabase como banco

### Melhorias tecnicas

- Mover filtros de jogos do processamento em memoria para consultas no repository
- Revisar setup futuro do Mockito agent no JDK 21
- Avaliar correcao das vulnerabilidades reportadas pelo `npm install`

## Fora do escopo do MVP

Itens explicitamente fora do produto atual:

- Criacao de palpites por participante
- Edicao de palpites
- Bloqueio de palpite por horario
- Autenticacao de participante
- Login no frontend
- Importacao no frontend
- Atualizacao de resultado oficial na UI publica

## Proximos passos

### Prioridade imediata

1. Validar o frontend com backend rodando e dados reais importados
2. Adicionar testes do frontend para as paginas principais
3. Documentar setup e execucao local
4. Refinar detalhes visuais e navegacao a partir do uso

### Proxima fase recomendada

- Criar experiencia administrativa separada para importacao e atualizacao de resultados
- Melhorar filtros e usabilidade das listagens
- Adicionar exportacoes ou relatorios, se necessario
- Reforcar observabilidade e preparo para deploy

## Resumo do estado atual

O projeto ja possui backend funcional e frontend MVP publico em modo somente leitura. O produto agora cobre ranking, detalhe do participante e jogos em um fluxo mobile first, faltando principalmente validacao integrada, testes do frontend e documentacao operacional complementar.
