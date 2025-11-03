# Ghid: Configurare Sentinel Hub API Credentials

## 📋 Pași pentru a obține Client ID și Client Secret

### 1. Accesează Sentinel Hub Dashboard

1. Mergi la: **https://www.sentinel-hub.com/**
2. Fă login cu contul tău
3. După login, mergi la **Dashboard** sau **Settings**

### 2. Creează o Aplicație (OAuth Client)

1. În dashboard, caută secțiunea **"Applications"** sau **"OAuth clients"**
   - Poate fi sub: **Settings → Applications** 
   - Sau direct în meniu: **Dashboard → Applications**
   
2. Click pe **"Create New Application"** sau **"Add OAuth Client"**

3. Completează formularul:
   - **Application Name**: `CropHealthAdvisor` (sau orice nume vrei)
   - **Redirect URI**: `http://localhost:8081` (pentru dezvoltare locală)
   - **Scopes**: Selectează cel puțin:
     - ✅ `process`
     - ✅ `visualize`
     - ✅ `statistics` (dacă este disponibil)

4. Click pe **"Create"** sau **"Save"**

### 3. Obține Client ID și Client Secret

După crearea aplicației, vei vedea:

- **Client ID** (OAuth Client ID) - acesta este ID-ul aplicației tale
  - Format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` (UUID)
  - ✅ **Acesta este ceea ce ai deja în `application.properties`**
  
- **Client Secret** - o cheie secretă generată automat
  - Format: `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
  - ⚠️ **Acesta este ceea ce TREBUIE să adaugi**

### 4. Copiază Client Secret

⚠️ **IMPORTANT**: Client Secret este afișat **o singură dată** când creezi aplicația!

- Dacă nu l-ai salvat, va trebui să:
  1. Ștergi aplicația veche
  2. Creezi una nouă
  3. Copiezi Client Secret imediat

### 5. Adaugă în `application.properties`

După ce ai obținut Client Secret:

```properties
# Client ID (ai deja acesta)
sentinelhub.api.key=88c2af12-e912-4eb4-a0cc-ba6c5315766b

# Client Secret (ADĂUGĂ ACESTA!)
sentinelhub.client.secret=PASTE_CLIENT_SECRET_HERE
```

### 6. Repornește Backend-ul

După adăugarea Client Secret:

```bash
cd crop-health-advisor
mvn spring-boot:run
```

## 🔍 Locuri alternative pentru a găsi aplicațiile

Dacă nu găsești secțiunea "Applications" direct:

1. **Settings → API Credentials**
2. **Developer → Applications**
3. **User Settings → OAuth Clients**
4. **Account → API Keys**

## ⚠️ Diferență importantă: Configuration ID vs OAuth Client

**Configuration ID** (Service Endpoint ID):
- Este ID-ul configurării tale din Sentinel Hub Configuration Utility
- Format: `88c2af12-e912-4eb4-a0cc-ba6c5315766b` (similar cu Client ID)
- ❌ **NU este suficient pentru Process API**
- ✅ Este folosit pentru OGC API (WMS/WCS) - alt protocol

**OAuth Client ID + Client Secret**:
- Este ce trebuie pentru Process API (pe care îl folosim)
- Client ID: similar cu Configuration ID, dar specific pentru OAuth
- Client Secret: o cheie secretă generată când creezi aplicația OAuth
- ✅ **Acestea sunt necesare pentru autentificare OAuth2**

## 📍 Unde să cauți OAuth Clients în Dashboard

În Sentinel Hub Dashboard, OAuth Clients sunt de obicei în:
1. **Account** (dreapta sus, click pe numele tău) → **Settings** → **OAuth Clients**
2. **Dashboard** → **Applications** sau **OAuth Applications**
3. **Developer Portal** → **Applications**

Dacă vezi doar "Configuration Utility" și "Service endpoints", înseamnă că trebuie să cauți în altă secțiune pentru OAuth Clients.

## 🆘 Dacă nu ai Client Secret și nu poți crea aplicație nouă

### Opțiunea 1: Folosește OGC API (mai simplu, dar limitat)

Sentinel Hub oferă și **OGC API** care poate funcționa cu un token mai simplu:
- Nu necesită neapărat Client Secret pentru anumite operații
- Dar procesarea este mai limitată

### Opțiunea 2: Folosește imaginea mock (pentru demo)

Codul actual generează automat imagini mock NDVI când API-ul eșuează, deci aplicația funcționează și fără Client Secret (doar cu imagini simulate).

### Opțiunea 3: Request manual de access token

Poți încerca să obții manual un access token folosind Client ID:

```bash
curl -X POST https://services.sentinel-hub.com/oauth/token \
  -d "grant_type=client_credentials" \
  -d "client_id=88c2af12-e912-4eb4-a0cc-ba6c5315766b" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

Dar acest lucru tot necesită Client Secret.

## ✅ Verificare

După configurare, verifică în backend console:
- **Dacă vezi**: `"Error getting OAuth token"` → Client Secret este greșit
- **Dacă vezi**: `"Sentinel Hub API returned error"` → Problema cu API-ul (va folosi mock)
- **Dacă nu vezi erori** → Token-ul OAuth2 funcționează! 🎉

## 📝 Note importante

1. **Client Secret** este confidențial - nu-l împărtăși public
2. Dacă îl uiți, trebuie să creezi o aplicație nouă
3. Pentru producție, folosește variabile de mediu în loc de `application.properties`
4. Pentru demo/testing, imaginea mock funcționează perfect

## 🔗 Link-uri utile

- **Sentinel Hub Dashboard**: https://www.sentinel-hub.com/
- **Documentație OAuth2**: https://docs.sentinel-hub.com/api/overview/authentication/
- **API Documentation**: https://docs.sentinel-hub.com/api/

