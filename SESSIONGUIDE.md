# Dansk Lov AI Platform · Sessionguide

> Jesper · Aalborg · April 2026 · Java 21 / Quarkus / LangChain4j / Ollama

## Sådan bruger du denne guide

Hver session følger samme struktur: **mål → trin → checklist → læringspunkt → Continue.dev prompt**.  
Du er færdig med en session når alle checklistepunkter er opfyldt — ikke før.

- 3-5 timer pr. session
- Lidt teori, mest praksis — du bygger noget der virker
- Commit til Git ved afslutning af hver session
- Brug Continue.dev prompten til at dykke dybere hvis du har tid

---

## Faseoversigt

| Nr | Fase | Sessions | Milepæl | Teknologier | Status |
|----|------|----------|---------|-------------|--------|
| S1-S3 | Fundament | 1-3 | Lokal LLM kørende fra Java | Ollama, Quarkus, LangChain4j | ✅ Klar |
| S4-S6 | MCP + Data | 4-6 | Første MCP tool med live lovdata | Retsinformation, MCP | ⏳ Næste |
| S7-S9 | Persistens | 7-9 | Cache og automatisk sync | PostgreSQL, Liquibase | 🔜 Kommende |
| S10-S12 | Fuld stak | 10-12 | Produktionsklar i Docker Compose | Docker, Nginx, Open WebUI | 🔜 Kommende |
| S13+ | Udvidelse | 13+ | Bred juridisk platform | Folketinget, CVR | 🔜 Kommende |

---

## Detaljerede Sessioner

---

### S1 · Lokal LLM kørende

| | |
|--|--|
| **Fase** | Fase 1 |
| **Tid** | 3-5 timer |
| **Status** | ✅ Gennemført |
| **Teknologier** | Ollama, Gemma 3 12B, Terminal / curl |

#### 🎯 Mål
Ollama installeret, Gemma 3 12B downloaded og kørende. Du har mærket hvad temperature og kontekstvindue betyder i praksis.

#### 📋 Trin

**1. Installér Ollama**  
Download fra ollama.com, træk til Applications, start fra menulinjen.
```bash
ollama --version
```

**2. Download Gemma 3 12B**  
Ca. 8 GB download. Kører fuldt lokalt, ingen data forlader Mac.
```bash
ollama pull gemma3:12b
```

**3. Første samtale**  
Test at modellen svarer på dansk juridisk spørgsmål.
```bash
ollama run gemma3:12b
```

**4. Eksperimentér med temperature**  
Kald Ollama REST API direkte med temperature 0.1 og 1.5. Se forskel i svarene.
```bash
curl http://localhost:11434/api/generate \
  -d '{"model":"gemma3:12b","prompt":"Hvad er straffeloven?","options":{"temperature":0.1},"stream":false}'
```

**5. Eksperimentér med kontekstvindue**  
Prøv `num_ctx` på 128, 512 og 2048. Forstå hvad det betyder for modellens hukommelse.

#### ✅ Checklist
- [x] `ollama --version` virker
- [x] Gemma 3 12B svarer på dansk
- [x] Du har set forskel på temperature 0.1 og 1.5
- [x] Du forstår hvad `num_ctx` kontrollerer

#### 💡 Læringspunkt
Temperature styrer kreativitet vs. determinisme. Kontekstvinduet er modellens arbejdshukommelse — jo større, jo mere lovtekst kan vi sende ind.

---

### S2 · Quarkus projekt + første Java-kald til Ollama

| | |
|--|--|
| **Fase** | Fase 1 |
| **Tid** | 3-5 timer |
| **Status** | ✅ Gennemført |
| **Teknologier** | Quarkus 3.x, Maven, REST Client Reactive, Jackson, IntelliJ IDEA, Continue.dev |

#### 🎯 Mål
Et kørende Quarkus-projekt der kalder din lokale Ollama fra Java og returnerer et dansk juridisk svar.

#### 📋 Trin

**1. Opret projekt på code.quarkus.io**  
Group: `dk.hansen`, Artifact: `dansk-lov-mcp`, Java 21, Maven.  
Tilføj: `REST Client` og `REST Client Jackson`.

**2. Konfigurér application.properties**
```properties
quarkus.rest-client."dk.hansen.ollama.OllamaClient".url=http://localhost:11434
quarkus.rest-client."dk.hansen.ollama.OllamaClient".scope=jakarta.inject.Singleton
quarkus.rest-client."dk.hansen.ollama.OllamaClient".read-timeout=120000
quarkus.rest-client."dk.hansen.ollama.OllamaClient".connect-timeout=5000
```

**3. Opret ollama-pakke med tre klasser**  
`OllamaRequest` (record), `OllamaResponse` (record), `OllamaClient` (interface med `@RegisterRestClient`).

**4. Opret LovResource**  
REST endpoint `/lov/sporg` der modtager et spørgsmål og returnerer Ollamas svar.
```bash
./mvnw quarkus:dev
```

**5. Test end-to-end**
```bash
curl "http://localhost:8080/lov/sporg?q=Hvad+er+en+bekendtgørelse"
```

**6. Installér Continue.dev**  
IntelliJ → Settings → Plugins → søg `Continue` → Install.  
Kobles til Ollama — ingen tokens.

#### ✅ Checklist
- [x] `./mvnw quarkus:dev` starter uden fejl
- [x] curl returnerer svar fra Ollama
- [x] Continue.dev installeret og koblet til Ollama
- [x] Kode committed til Git

#### 💡 Læringspunkt
REST Client i Quarkus bruger interface-mønsteret med `@RegisterRestClient` — samme mønster vi bruger til alle fremtidige API-klienter inkl. Retsinformation.

#### 🤖 Continue.dev prompt
```
Forklar hvad @RegisterRestClient gør i Quarkus og hvordan den adskiller sig fra en almindelig HTTP-klient.
```

---

### S3 · LangChain4j integration

| | |
|--|--|
| **Fase** | Fase 1 |
| **Tid** | 3-5 timer |
| **Status** | ⏳ Næste session |
| **Teknologier** | LangChain4j, OllamaChatModel, AI Services |

#### 🎯 Mål
LangChain4j tilføjet som abstraktionslag. Din kode er nu model-agnostisk — den kan skifte mellem Ollama, OpenAI og Anthropic uden refaktorering.

#### 📋 Trin

**1. Tilføj LangChain4j dependency**  
Tilføj `langchain4j-ollama` til `pom.xml`.

**2. Opret LovAssistent interface**  
Et LangChain4j AI Service interface med `@SystemMessage` og `@UserMessage` annoteringer.

**3. Konfigurér OllamaChatModel**  
Byg en `@ApplicationScoped` bean der opretter `OllamaChatModel` med URL og modelNavn.

**4. Refaktér LovResource**  
Erstat `OllamaClient` med `LovAssistent`. Samme funktionalitet — renere kode.

**5. Test model-agnostisk design**  
Skift midlertidigt til `AnthropicChatModel` og se at resten af koden er uændret.

#### ✅ Checklist
- [ ] LangChain4j dependency tilføjet
- [ ] `LovAssistent` interface oprettet med `@SystemMessage`
- [ ] `OllamaChatModel` konfigureret som bean
- [ ] `LovResource` bruger `LovAssistent` i stedet for `OllamaClient`
- [ ] Model kan skiftes uden kodeændringer

#### 💡 Læringspunkt
LangChain4j er dit model-agnostiske abstraktionslag. Det er den vigtigste arkitekturbeslutning i hele projektet — det er det der gør din kode portabel uanset hvilken LLM der vinder om 2 år.

#### 🤖 Continue.dev prompt
```
Vis mig hvordan jeg opretter et LangChain4j AI Service interface med @SystemMessage 
der siger 'Du er en dansk juridisk assistent' og en metode spørg(String spørgsmål).
```

---

### S4 · Retsinformation API-klient

| | |
|--|--|
| **Fase** | Fase 2 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Retsinformation API, REST Client Reactive, Java Records |

#### 🎯 Mål
Java-klient mod Retsinformation API. Du kan hente en dansk lov som struktureret Java-objekt.

#### 📋 Trin

**1. Registrér på Retsinformation**  
Gå til `api.retsinformation.dk` og opret en gratis API-nøgle.

**2. Tilføj API-nøgle til application.properties**  
Gem nøglen sikkert — aldrig i Git.

**3. Opret RetsinformationDocument record**  
Felterne: `id`, `titel`, `type`, `dato`, `tekst`.

**4. Opret RetsinformationClient interface**  
`@RegisterRestClient` med `GET /api/document/{id}` endpoint.

**5. Opret RetsinformationService**  
`@ApplicationScoped` service med metoden `hentLov(String id)`.

**6. Test med et rigtigt lovdokument**  
Hent straffeloven eller grundloven og print titlen.

#### ✅ Checklist
- [ ] API-nøgle på plads i `application.properties`
- [ ] `RetsinformationClient` returnerer et dokument
- [ ] `RetsinformationDocument` har alle felter udfyldt
- [ ] Service-lag isolerer klienten fra resten af koden

#### 💡 Læringspunkt
Retsinformation API returnerer lovtekster som JSON. Vi parser dem til Java records — samme mønster som Ollama-svaret. Lagdelt arkitektur: Resource → Service → Client.

#### 🤖 Continue.dev prompt
```
Jeg har en RetsinformationClient der returnerer et RetsinformationDocument. 
Vis mig hvordan jeg håndterer HTTP 404 og 429 (rate limit) fejl elegant i Quarkus.
```

---

### S5 · Første MCP Tool

| | |
|--|--|
| **Fase** | Fase 2 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | MCP protokol, quarkus-mcp-server-http, @Tool annotation |

#### 🎯 Mål
Din Quarkus-applikation er nu en MCP-server. Den eksponerer `søg_love` som et tool som AI-modeller kan kalde.

#### 📋 Trin

**1. Tilføj quarkus-mcp-server-http dependency**  
Quarkiverse extension der gør din app til en MCP-server.

**2. Opret LovgivningTools klasse**  
`@ApplicationScoped` klasse med `@Tool` annoterede metoder.

**3. Implementér søg_love tool**  
Metode der modtager et søgeord og returnerer en liste af love fra Retsinformation.

**4. Test MCP endpoint**
```bash
curl http://localhost:8080/mcp
```

**5. Forbind Open WebUI til MCP**  
Admin Settings → External Tools → Add Server → MCP.

#### ✅ Checklist
- [ ] `quarkus-mcp-server-http` tilføjet til `pom.xml`
- [ ] `LovgivningTools` har mindst ét `@Tool`
- [ ] `/mcp` endpoint svarer på curl
- [ ] Open WebUI kan se toolet

#### 💡 Læringspunkt
MCP er standarden for hvordan AI-modeller kalder eksterne værktøjer. `@Tool` er alt hvad du skal tilføje — Quarkus håndterer resten. Det er det der adskiller en simpel chatbot fra en reel AI-assistent.

#### 🤖 Continue.dev prompt
```
Forklar forskellen på RAG og MCP tool use. Hvornår bruger man hvad?
```

---

### S6 · LLM + lovdata kombineret

| | |
|--|--|
| **Fase** | Fase 2 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | LangChain4j AI Services, MCP tool use, System prompts |

#### 🎯 Mål
End-to-end: Et spørgsmål på naturligt dansk → LLM slår op i Retsinformation via MCP tool → struktureret svar tilbage.

#### 📋 Trin

**1. Kobl LovAssistent med MCP tools**  
Fortæl LangChain4j at `LovAssistent` har adgang til `LovgivningTools`.

**2. Skriv system prompt til juridisk kontekst**
```
Du er en dansk juridisk assistent. Du bruger altid de tilgængelige 
tools til at slå love op.
```

**3. Test med naturligt dansk spørgsmål**  
Stil et spørgsmål — LLM'en ræsonnerer og kalder tool automatisk.

**4. Observér tool-kaldene i loggen**  
Quarkus logger hvilke tools der kaldes og med hvilke parametre.

#### ✅ Checklist
- [ ] LLM kalder `søg_love` tool automatisk ved relevante spørgsmål
- [ ] Svar indeholder referencer til specifikke love
- [ ] System prompt sætter dansk juridisk kontekst
- [ ] Tool-kald er synlige i Quarkus-loggen

#### 💡 Læringspunkt
Dette er agentic reasoning i praksis: LLM'en beslutter selv hvornår og hvordan den kalder tools. Du har nu en AI-assistent der ikke gætter — den slår op.

#### 🤖 Continue.dev prompt
```
Hvad er en god system prompt til en dansk juridisk AI-assistent der bruger 
Retsinformation tools? Giv mig tre varianter med forskellig tone.
```

---

### S7 · PostgreSQL + Liquibase

| | |
|--|--|
| **Fase** | Fase 3 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | PostgreSQL 16, Docker, Liquibase, Quarkus Hibernate/Panache |

#### 🎯 Mål
PostgreSQL kørende i Docker. Liquibase opretter database-skemaet automatisk ved opstart.

#### 📋 Trin

**1. Tilføj PostgreSQL til Docker Compose**  
`postgres:16` image med `lovdb` database.

**2. Tilføj Quarkus datasource dependencies**  
`quarkus-hibernate-orm-panache` og `quarkus-jdbc-postgresql`.

**3. Konfigurér datasource i application.properties**  
JDBC URL, bruger og password.

**4-6. Opret Liquibase changesets**
- `dokument_cache` — id, kilde, ekstern_id, indhold, udloeber
- `sync_log` — til at logge daglig synkronisering
- `synkroniseret_dokument` — tracker hvilke dokumenter er synkroniseret

#### ✅ Checklist
- [ ] PostgreSQL starter via Docker Compose
- [ ] Liquibase kører changesets automatisk ved opstart
- [ ] Alle tre tabeller eksisterer i databasen
- [ ] Quarkus kan connecte til PostgreSQL

#### 💡 Læringspunkt
Liquibase changesets er din databasehistorik — aldrig ret tabeller manuelt. Opret altid et nyt changeset. Det er enterprise-standarden for database migrations.

#### 🤖 Continue.dev prompt
```
Vis mig et Liquibase changeset der opretter dokument_cache tabellen 
med et unikt index på (kilde, ekstern_id).
```

---

### S8 · L2 Cache implementering

| | |
|--|--|
| **Fase** | Fase 3 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | PostgreSQL, Caffeine Cache, Quarkus Cache, TTL strategi |

#### 🎯 Mål
`DokumentCacheService` implementeret. API-kald til Retsinformation reduceres drastisk — kendte dokumenter hentes fra PostgreSQL.

#### 📋 Trin

**1. Opret DokumentCache entity**  
Panache entity der mapper til `dokument_cache` tabellen.

**2. Opret DokumentCacheService**  
`@ApplicationScoped` service med lookup-or-fetch pattern.

**3. Implementér hentMedCache metode**
1. Tjek PostgreSQL cache → 2. Hvis udløbet/ikke fundet: kald API → 3. Gem i cache.

**4. Tilføj L1 Caffeine cache**  
In-memory cache foran PostgreSQL for hyppigt tilgåede dokumenter.

**5. Verificér cache virker**  
Kald det samme dokument to gange — andet kald må ikke ramme Retsinformation API.

#### ✅ Checklist
- [ ] `DokumentCacheService` bruger lookup-or-fetch pattern
- [ ] Cache TTL er konfigurerbar i `application.properties`
- [ ] Andet kald til samme dokument er markant hurtigere
- [ ] Cache-hits er synlige i loggen

#### 💡 Læringspunkt
To-lags cache er et klassisk enterprise-mønster. L1 (Caffeine) er lynhurtig men lever kun i én instans. L2 (PostgreSQL) er delt og overlever genstart. Kend forskel.

#### 🤖 Continue.dev prompt
```
Vis mig lookup-or-fetch mønsteret i Java med Quarkus Cache og PostgreSQL 
som fallback. Inkludér TTL-tjek.
```

---

### S9 · Daglig sync job

| | |
|--|--|
| **Fase** | Fase 3 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Quarkus @Scheduled, Retsinformation høste-API, sync_log tabel |

#### 🎯 Mål
`RetsinformationSyncJob` kører automatisk kl. 03:30 og synkroniserer nye lovændringer til PostgreSQL-cachen.

#### 📋 Trin

**1. Opret RetsinformationSyncJob**  
`@ApplicationScoped` klasse med `@Scheduled(cron="0 30 3 * * ?")`.

**2. Implementér sync-logik**  
Hent ændringer siden sidste sync fra høste-API. Log start og slut i `sync_log`.

**3. Gem synkroniserede dokumenter**  
Opdatér `dokument_cache` og `synkroniseret_dokument` tabellerne.

**4. Test med manuel trigger**
```bash
curl -X POST http://localhost:8080/admin/sync
```

**5. Verificér sync_log**  
Tjek at `sync_log` indeholder korrekte start/slut tidspunkter og antal dokumenter.

#### ✅ Checklist
- [ ] `@Scheduled` job konfigureret til 03:30
- [ ] `sync_log` indeholder poster efter sync
- [ ] Kendte dokumenter opdateres ved ændringer
- [ ] Manuel trigger virker via admin endpoint

#### 💡 Læringspunkt
`@Scheduled` i Quarkus bruger cron-syntax. Sync-jobbet er det der gør platformen selvkørende — efter Session 9 behøver du ikke tænke på at holde lovdata frisk.

#### 🤖 Continue.dev prompt
```
Vis mig en robust @Scheduled metode i Quarkus der logger til en sync_log tabel 
og håndterer fejl uden at crashe hele applikationen.
```

---

### S10 · Open WebUI i Docker

| | |
|--|--|
| **Fase** | Fase 4 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Docker Compose, Open WebUI, Nginx |

#### 🎯 Mål
Open WebUI kørende i Docker og forbundet til din lokale Ollama. Du har et ChatGPT-lignende interface til din egen LLM.

#### 📋 Trin

**1. Opret docker-compose.yml**  
Services: `postgres`, `dansk-lov-mcp`, `open-webui`, `nginx`.

**2. Konfigurér Open WebUI**  
`OLLAMA_BASE_URL: http://host.docker.internal:11434`

**3. Start Docker Compose**
```bash
docker compose up -d
```

**4. Log ind i Open WebUI**  
Åbn `localhost:3000`. Opret admin-bruger.

**5. Test chat med lokal model**  
Skriv et juridisk spørgsmål og verificér at Gemma svarer.

#### ✅ Checklist
- [ ] `docker compose up -d` starter alle services
- [ ] Open WebUI tilgængelig på `localhost:3000`
- [ ] Chat virker med Gemma 3 12B
- [ ] PostgreSQL starter korrekt

#### 💡 Læringspunkt
Docker Compose orkestrerer hele din stak med ét kommando. `host.docker.internal` er den magiske DNS der lader Docker-containers nå Ollama der kører direkte på macOS.

#### 🤖 Continue.dev prompt
```
Vis mig en docker-compose.yml der starter Open WebUI og peger den mod 
Ollama på host.docker.internal:11434.
```

---

### S11 · MCP server i Compose + fuld integration

| | |
|--|--|
| **Fase** | Fase 4 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Docker Compose, MCP Streamable HTTP, Open WebUI External Tools |

#### 🎯 Mål
Hele platformen kørende i Docker Compose. Open WebUI forbundet til MCP-serveren. Et spørgsmål i chatten slår op i Retsinformation.

#### 📋 Trin

**1. Byg dansk-lov-mcp Docker image**
```bash
./mvnw package -Dquarkus.container-image.build=true
```

**2. Tilføj dansk-lov-mcp til Compose**  
Service med korrekte environment-variabler og afhængighed til postgres.

**3. Forbind Open WebUI til MCP**  
Admin Settings → External Tools → Add Server → Type: MCP  
URL: `http://dansk-lov-mcp:8080/mcp`

**4. Opsæt system prompt i Open WebUI**  
Dansk juridisk kontekst der fortæller modellen at den skal bruge MCP tools.

**5. Test fuld flow**  
Spørgsmål i chat → LLM kalder MCP tool → Retsinformation → svar med lovreference.

#### ✅ Checklist
- [ ] `dansk-lov-mcp` bygger som Docker image
- [ ] MCP tool synligt i Open WebUI
- [ ] Chat-svar indeholder referencer til specifikke love
- [ ] Hele stakken starter med `docker compose up -d`

#### 💡 Læringspunkt
Dette er det øjeblik platformen bliver reel. Fra nu af er det en produktionsklar AI-assistent der kører på din hardware og koster nul kr. pr. forespørgsel.

#### 🤖 Continue.dev prompt
```
Hvad er en god system prompt til Open WebUI der instruerer modellen i at 
bruge MCP tools til at slå love op frem for at gætte?
```

---

### S12 · Nginx + API-nøgle + produktionsklar

| | |
|--|--|
| **Fase** | Fase 4 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Nginx, API-nøgle autentifikation, HTTPS, .env filer |

#### 🎯 Mål
Nginx reverse proxy beskytter platformen. MCP endpoint er sikret med API-nøgle. Platformen er klar til at vise frem.

#### 📋 Trin

**1. Tilføj Nginx til Docker Compose**  
`nginx:alpine` som reverse proxy foran Open WebUI og MCP.

**2. Konfigurér Nginx**  
Route `/` til Open WebUI `:3000` og `/mcp` til `dansk-lov-mcp:8080`.

**3. Implementér API-nøgle på /mcp**  
Quarkus filter der tjekker `Authorization` header på alle `/mcp` requests.

**4. Opret .env.example**  
Template med alle environment-variabler — aldrig secrets i Git.

**5. Gennemgå sikkerhed**  
Ingen secrets i kode, ingen åbne porte der ikke er nødvendige.

#### ✅ Checklist
- [ ] Nginx starter og router korrekt
- [ ] `curl` mod `/mcp` uden nøgle returnerer `401`
- [ ] `curl` med korrekt nøgle virker
- [ ] `.env.example` er committed, `.env` er i `.gitignore`

#### 💡 Læringspunkt
Sikkerhed er ikke en feature man tilføjer til sidst — men i dette projekt gør vi det med vilje sent, så vi forstår hvad vi beskytter og hvorfor.

#### 🤖 Continue.dev prompt
```
Vis mig en Quarkus ContainerRequestFilter der validerer en API-nøgle i 
Authorization header og returnerer 401 hvis den mangler.
```

---

### S13 · Folketinget ODA API

| | |
|--|--|
| **Fase** | Fase 5 |
| **Tid** | 3-5 timer |
| **Status** | 🔜 Kommende |
| **Teknologier** | Folketinget ODA API, oda.ft.dk/api, MCP @Tool |

#### 🎯 Mål
Platformen kan nu besvare spørgsmål om lovforslag og afstemninger i Folketinget — ikke kun vedtagne love.

#### 📋 Trin

**1. Opret FolketingetKilde implementering**  
Implementér `LovgivningKilde` interface for Folketinget ODA.

**2. Tilføj søg_lovforslag tool**  
Søg i Folketingets behandlede og igangværende lovforslag.

**3. Tilføj hent_afstemning tool**  
Hent afstemningsresultat for et specifikt lovforslag.

**4. Test med aktuelt lovforslag**  
Stil et spørgsmål om et nyligt lovforslag og verificér svaret.

#### ✅ Checklist
- [ ] `FolketingetKilde` implementerer `LovgivningKilde`
- [ ] `søg_lovforslag` tool svarer korrekt
- [ ] Platformen kan svare på spørgsmål om igangværende lovgivning

#### 💡 Læringspunkt
`LovgivningKilde` interface er din udvidelsesmodel — tilføj en ny datakilde ved at implementere interfacet og registrere den. Resten sker automatisk.

#### 🤖 Continue.dev prompt
```
Jeg har et LovgivningKilde interface. Vis mig hvordan jeg implementerer det 
for Folketinget ODA API med søg_lovforslag metoden.
```

---

## CONTEXT.md — Indsæt i Continue.dev

Opret en fil `CONTEXT.md` i roden af projektet og paste nedenstående øverst i nye Continue-sessioner.

```
Du er en Java/Quarkus AI-assistent der hjælper med at bygge Dansk Lov AI Platform.

Stack: Java 21, Quarkus 3.x, LangChain4j, Ollama/Gemma3:12b, PostgreSQL 16, Liquibase, Docker Compose.

Pakke: dk.hansen | Stil: records, @ApplicationScoped, @RegisterRestClient, ingen lombok.

Mål: Self-hosted AI-platform til dansk lovgivning. Ingen data forlader maskinen.

Nuværende session: [INDSÆT SESSION NR OG TITEL HER]
```

---

*Dansk Lov AI Platform · Sessionguide · April 2026*
