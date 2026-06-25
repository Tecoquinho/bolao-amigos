# Bolao dos Amigos da Carlos

Aplicativo mobile-first para acompanhar um bolao da Copa do Mundo 2026 com ranking, jogos, palpites e apuracao de resultados oficiais.

## Visao geral

Este repositorio contem um monorepo simples com:

- `backend`: API REST em Spring Boot
- `frontend`: app web em React + Vite
- `data`: arquivos auxiliares de importacao

O produto foi pensado para uso rapido no celular:

- abrir o ranking
- entrar no participante
- ver os palpites por data
- acompanhar os jogos e os resultados oficiais

Nao e uma plataforma de apostas.
Nao e um dashboard corporativo.
Nao tem login, edicao de palpites nem area administrativa publica.

## Principios do projeto

- backend em `Java 21 + Spring Boot 3`
- frontend em `React + TypeScript`
- banco em `PostgreSQL`
- simplicidade antes de overengineering
- foco em leitura rapida no celular
- regras de negocio importantes devem ser protegidas por testes
- mudancas arquiteturais relevantes devem ser explicadas

## Stack

### Backend

- Java 21
- Spring Boot 3.5
- Spring Data JPA
- Flyway
- PostgreSQL
- H2 para testes
- PDFBox para parser offline dos PDFs

### Frontend

- React 18
- TypeScript
- Vite
- React Router
- Tailwind CSS

## O que o app faz hoje

- mostra ranking geral dos participantes
- mostra detalhe do participante com:
  - posicao
  - pontos
  - acertos exatos
  - acertos de resultado
  - palpites por data
- mostra agenda de jogos da Copa
- permite atualizacao operacional de resultados oficiais no backend
- recalcula automaticamente os pontos de cada palpite por jogo

## Regra de pontuacao

Regra oficial do bolao implementada no backend:

1. escore exato = `5 pontos`
2. empate sem escore exato = `3 pontos`
3. escore do vencedor certo = `2 pontos`
4. escore do perdedor certo = `2 pontos`
5. vencedor certo sem escore exato = `1 ponto`

Importante:

- vale apenas a maior pontuacao aplicavel
- nao ha acumulacao entre regras
- se o palpite for empate e o jogo terminar com vencedor, o palpite recebe `0 pontos`

## Estrutura do projeto

```text
.
|-- backend
|   |-- src/main/java
|   |-- src/main/resources
|   |   |-- db/migration
|   |   `-- import
|   `-- update-known-results.ps1
|-- frontend
|   `-- src
|-- data
`-- render.yaml
```

## Funcionalidades principais

### Ranking

- tela inicial do app
- lista continua de participantes
- top 3 destacado
- ultimos colocados destacados visualmente

### Participante

- resumo do participante no topo
- navegacao por datas disponiveis
- lista de palpites com placar oficial quando o jogo ja terminou

### Jogos

- lista de jogos por data
- horario, fase e placar
- visual compacto no mesmo estilo do ranking

## Ambiente local

### Requisitos

- Java 21
- Maven
- Node.js 18+
- PostgreSQL

### Backend

Arquivo de exemplo:

- [backend/.env.example](backend/.env.example)

Variaveis:

```env
DB_URL=jdbc:postgresql://localhost:5432/bolao_copa
DB_USERNAME=postgres
DB_PASSWORD=postgres
CORS_ALLOWED_ORIGINS=*
PORT=8080
```

Rodando localmente:

```powershell
cd backend

$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;" + $env:Path

mvn spring-boot:run
```

Testes:

```powershell
cd backend
mvn test
```

Observacao:

- existe historico de testes do parser de PDF que podem falhar isoladamente dependendo do corpus local
- os testes principais de API e ranking fazem parte da base do projeto

### Frontend

Arquivo de exemplo:

- [frontend/.env.example](frontend/.env.example)

Variavel:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Rodando localmente:

```powershell
cd frontend
npm install
npm run dev
```

Build:

```powershell
cd frontend
npm run build
```

## Importacao e operacao

### Seed da fase de grupos

O seed canonico da fase de grupos fica em:

- [backend/src/main/resources/import/group-stage-seed.json](backend/src/main/resources/import/group-stage-seed.json)

Script auxiliar:

- [backend/import-group-stage-seed.ps1](backend/import-group-stage-seed.ps1)

### Atualizacao de resultados oficiais

Script operacional:

- [backend/update-known-results.ps1](backend/update-known-results.ps1)
- [backend/recalculate-finished-results.ps1](backend/recalculate-finished-results.ps1)

O fluxo oficial usa:

- `PATCH /api/matches/{matchId}/result`

Esse endpoint:

- grava o placar oficial
- marca o jogo como `FINISHED`
- recalcula os `pointsAwarded` dos palpites daquele jogo
- atualiza o ranking automaticamente

## Endpoints principais

### Publicos

- `GET /api/ranking`
- `GET /api/participants`
- `GET /api/participants/{participantId}`
- `GET /api/participants/{participantId}/predictions`
- `GET /api/matches`
- `GET /api/matches/{matchId}`

### Operacionais

- `POST /api/admin/imports/seed`
- `PATCH /api/matches/{matchId}/result`

## Deploy

### Backend

Preparado para Render:

- [render.yaml](render.yaml)
- [backend/Dockerfile](backend/Dockerfile)

Variaveis esperadas:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `CORS_ALLOWED_ORIGINS`

### Frontend

Preparado para Vercel com:

- `VITE_API_BASE_URL`

## Estado atual da base

Estado de referencia do projeto:

- `48 teams`
- `72 matches`
- `20 participants`
- `1438 predictions`

Os resultados oficiais sao carregados progressivamente ao longo da fase de grupos.

Estado funcional atual:

- ranking publico implementado
- detalhe do participante implementado
- tela de jogos implementada
- seed canonico da fase de grupos implementado
- script operacional para atualizacao de resultados implementado
- backend pronto para Render
- frontend pronto para Vercel

## Design do app

Direcao visual atual:

- mobile-first
- fundo claro
- cards compactos
- bordas grossas
- sombras duras
- leitura rapida
- visual casual de bolao entre amigos

## Roadmap natural

Melhorias mais provaveis sem mudar a essencia do produto:

- testes de frontend
- documentacao operacional mais enxuta
- refinamento de UX mobile
- mais ferramentas de auditoria e operacao

## Repositorio

Se voce quer rodar o projeto completo:

1. suba o PostgreSQL
2. rode o backend
3. rode o frontend
4. importe o seed
5. aplique os resultados oficiais conforme necessario

Pronto. O resto do fluxo gira em torno de ranking, palpites e jogos.
