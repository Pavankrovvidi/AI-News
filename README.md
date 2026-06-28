# News Report Agent 📱

5 AI agents, ఒక్కో agent ఒక్కో category news report ఇస్తుంది:

1. 🏏 **Sports Agent** — latest sports news
2. 🤖 **AI News Agent** — latest AI & tech news
3. ⚖️ **Politics Agent** — latest political news
4. 📈 **Stock Market Agent** — latest market news
5. 🎬 **Movies Agent** — latest movie news

Provider: **Claude / OpenAI / Gemini / Groq** — ఏదో ఒక API key పెడితే చాలు.

---

## APK ఎలా తయారు చేసుకోవాలి (5 steps)

> ⚠️ APK ని GitHub build చేస్తుంది (Google build tools అక్కడ work అవుతాయి).
> ఈ folder మొత్తం ready project — just upload చేసి push చేస్తే చాలు.

### 1. GitHub repo create చెయ్యి
github.com → **New repository** → పేరు: `news-report-agent` → Create.

### 2. ఈ folder మొత్తం upload చెయ్యి
Repo page లో **Add file → Upload files** → ఈ `news-report-agent` లోపల ఉన్న
అన్ని files & folders ని drag చేసి drop చెయ్యి → **Commit changes**.

(ముఖ్యం: `app/`, `.github/`, `settings.gradle.kts`, `build.gradle.kts`,
`gradle.properties` — అన్నీ ఎక్కించాలి, folder structure అలాగే ఉండాలి.)

### 3. Build start అవుతుంది automatic ga
Push అవ్వగానే GitHub → **Actions** tab లో "Build APK" run మొదలవుతుంది.
~3–5 నిమిషాలు ఆగు (పచ్చ ✓ వచ్చేవరకు).

### 4. APK download చెయ్యి
**Actions** tab → latest run click → కింద **Artifacts** → **NewsReportAgent-apk**
download → zip లోపల `app-debug.apk` ఉంటుంది.

### 5. Phone లో install చెయ్యి
APK ని phone కి పంపు → open → "Unknown sources / Install anyway" → **Install**.
App open చేసి, కుడి పైన ⚙️ **Settings** → provider select → API key paste →
**Save**. తరువాత ఏ agent click చేసినా report వస్తుంది. ✅

---

## ⭐ New: real sources + search

ఇప్పుడు agents **నిజమైన news articles** ని తెస్తాయి (GNews ద్వారా) — ప్రతి story కింద
**source పేరు + date + clickable link** ఉంటుంది. ఏ card మీద tap చేసినా అసలు article
open అవుతుంది → **real vs fake నువ్వే verify చేయొచ్చు**. AI summary కూడా ఆ real
articles నుంచే తయారవుతుంది (hallucination ఉండదు).

ప్రతి agent లో **Search bar** ఉంది — ఏ topic కావాలో type చేసి search నొక్కు.

### Free GNews key (real news కోసం — ముఖ్యం)
1. https://gnews.io కి వెళ్ళు → **Sign up** (free, card అవసరం లేదు)
2. Dashboard లో **API key** copy చెయ్యి
3. App → ⚙️ Settings → **GNews API key** field లో paste → **Save**

> GNews లేకపోతే news రాదు. AI provider key (Claude/OpenAI/Gemini/Groq) **AI summary**
> కోసం మాత్రమే — అది optional. Real news కి GNews key ముఖ్యం.

## Project structure
```
news-report-agent/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── .github/workflows/build-apk.yml      ← CI builds the APK
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/ (icon, theme, strings)
        └── java/com/purevibe/newsagent/
            ├── MainActivity.kt
            ├── ai/         (AiProvider, ApiKeyStore, AiClient)
            ├── agents/     (NewsAgent — the 5 agents)
            └── ui/         (HomeScreen, AgentReportScreen, SettingsScreen, theme)
```
