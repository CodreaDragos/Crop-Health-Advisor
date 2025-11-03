# Ghid de Testare - Integrare Sentinel Hub API

## ✅ Ce a fost implementat:

### Backend:
1. **SatelliteDataService** - Integrare cu Sentinel Hub Statistical API
   - Obține NDVI, EVI, NDWI reali din date Sentinel-2
   - Folosește eval script pentru calcul indici
   - Interval: ultimele 30 zile de date

2. **ReportService** - Actualizat să folosească date reale
   - Eliminat mock-urile
   - Folosește `getSatelliteMetrics()` pentru valori reale

3. **SatelliteImageController** - Endpoint pentru imagini NDVI
   - `/api/satellite/image/ndvi?lat=X&lon=Y`
   - Generează imagini PNG colorate

### Frontend:
1. **ResultsPanel** - Afișează hartă NDVI în raportul complet
2. **locationService** - Funcții pentru imagini NDVI
3. Funcții existente funcționează cu datele reale

## 🧪 Ce trebuie testat:

### 1. Generarea Raportului cu Date Reale

**Pași:**
1. Pornește backend-ul: `mvn spring-boot:run`
2. Pornește frontend-ul: `npm run dev`
3. Login în aplicație
4. Navighează la **Harta**
5. Plasează un pin pe hartă (click pe hartă)
6. Click pe **"Salvează Locația"** → dă-i un nume
7. Navighează la **"Locații Salvate"**
8. Click pe **"Generează Raport"** pentru locația ta

**Ce să verifici:**
- ✅ Raportul se generează (nu apare eroare)
- ✅ Valorile NDVI sunt în intervalul **-1.0 până la 1.0** (real, nu random)
- ✅ Valorile sunt consistente pentru aceeași locație (nu se schimbă random la fiecare generare)
- ✅ **Consola backend** - verifică log-urile:
  - `"Error fetching satellite data"` → API-ul Sentinel Hub a eșuat
  - Dacă vezi fallback la mock → API-ul nu funcționează
- ✅ **Consola frontend** - verifică dacă raportul este primit corect

### 2. Verificarea Date Reale vs Mock

**Cum să știi dacă folosește date reale:**
- Valorile NDVI sunt **consistente** pentru aceeași locație (nu se schimbă la fiecare generare)
- Valorile sunt în **interval real** pentru zona ta (ex: pentru România, NDVI este de obicei între 0.2 și 0.8 pentru culturi)
- Dacă vezi valori complet random (-0.99, 0.99, etc.) la fiecare generare → înseamnă că folosește mock

**Test:**
1. Generează raport pentru o locație
2. Notează valoarea NDVI
3. Generează raport din nou pentru aceeași locație
4. Valoarea NDVI ar trebui să fie **similară** (nu identică, dar în același interval)

### 3. Testarea Hărții NDVI

**Pași:**
1. Generează un raport
2. Click pe **"Vezi Raport Complet"** (în LocationsList sau HistoryPanel)
3. Verifică dacă apare secțiunea **"🗺️ Hartă NDVI"**
4. Imaginea ar trebui să fie:
   - O hartă colorată (roșu/portocaliu/galben/verde)
   - Reprezentând zona din jurul locației (buffer de ~500m)

**Ce să verifici:**
- ✅ Secțiunea "Hartă NDVI" apare în raportul complet
- ✅ Imaginea se încarcă (nu apare "Se încarcă harta NDVI..." permanent)
- ✅ Dacă apare eroare → verifică consola frontend pentru detalii

### 4. Verificarea API-ului Sentinel Hub

**Test manual în Postman/curl:**
```bash
# Test endpoint pentru raport (folosește token-ul tău)
GET http://localhost:8081/api/reports?locationId=1
Authorization: Bearer YOUR_TOKEN

# Test endpoint pentru imagine NDVI
GET http://localhost:8081/api/satellite/image/ndvi?lat=46.77&lon=23.62&width=512&height=512
Authorization: Bearer YOUR_TOKEN
```

**Ce să verifici:**
- ✅ Răspunsul pentru raport conține `ndviValue` cu valori reale
- ✅ Endpoint-ul pentru imagine returnează PNG (verifică în browser sau Postman)

### 5. Testarea în Frontend - Flow Complet

**Pași:**
1. Login
2. Harta → Plasează pin → Salvează locație
3. Locații Salvate → Generează Raport
4. Așteaptă ~5-30 secunde (API-ul Sentinel Hub poate dura)
5. Verifică raportul generat:
   - NDVI are valoare reală
   - Temperatură și precipitații (momentan mock)
   - Interpretare AI (curățată de JSON)
6. Click "Vezi Raport Complet"
7. Verifică dacă apare hartă NDVI

## 🔍 Debugging - Probleme comune:

### Problema: Valorile NDVI sunt încă random

**Cauză:** API-ul Sentinel Hub nu funcționează sau returnează eroare

**Soluție:**
1. Verifică **consola backend** pentru erori:
   - `Error fetching satellite data: ...`
   - `Error parsing satellite data response: ...`
2. Verifică cheia API în `application.properties`:
   - `sentinelhub.api.key=...`
3. Testează manual request-ul către Sentinel Hub (vezi secțiunea de mai sus)
4. Dacă API-ul eșuează → aplicația folosește automat mock ca fallback

### Problema: Hartă NDVI nu apare

**Cauză:** Locația nu este serializată în răspunsul raportului

**Soluție:**
1. Verifică în **Network tab** (F12) dacă răspunsul raportului conține `location` object
2. Dacă nu, verifică `Reports.java` - ar trebui să aibă `FetchType.EAGER`
3. Verifică consola frontend pentru erori la încărcarea imaginii

### Problema: Generarea raportului eșuează cu timeout

**Cauză:** API-ul Sentinel Hub durează prea mult

**Soluție:**
1. Timeout-ul este setat la 60 secunde în `ReportController`
2. Poți crește timeout-ul sau verifica viteza conexiunii la Sentinel Hub

### Problema: Imagini NDVI nu se încarcă

**Cauză:** Endpoint-ul necesită autentificare sau API-ul eșuează

**Soluție:**
1. Verifică în **Network tab** răspunsul la request-ul pentru imagine
2. Verifică status code (200 OK vs 401/403/500)
3. Verifică consola backend pentru erori

## 📊 Rezultate așteptate:

### Dacă totul funcționează corect:
- ✅ NDVI valori între -1.0 și 1.0 (de obicei 0.2-0.8 pentru zone cu vegetație)
- ✅ Valorile sunt **consistente** pentru aceeași locație
- ✅ Hartă NDVI apare în raportul complet
- ✅ Interpretarea AI este curățată (fără JSON, fără "Analiza pentru prompt:")
- ✅ Rapoartele se salvează corect în baza de date

### Dacă folosește mock (fallback):
- ⚠️ Valorile NDVI sunt complet random la fiecare generare
- ⚠️ Nu apar erori în consolă, dar valorile nu sunt reale
- ⚠️ Hartă NDVI poate să nu apară sau să arate eroare

## 🔧 Pași suplimentari pentru troubleshooting:

1. **Verifică logs backend:**
   ```
   Caută în consolă: "Error fetching satellite data" sau "Error parsing satellite data"
   ```

2. **Testează API-ul direct:**
   - Folosește Postman pentru a testa endpoint-ul Sentinel Hub direct
   - Verifică dacă cheia API funcționează

3. **Verifică structura request-ului:**
   - Deschide `SatelliteDataService.java`
   - Verifică dacă formatul request-ului este corect conform documentației Sentinel Hub

4. **Monitorizează Network tab:**
   - F12 → Network
   - Generează un raport
   - Verifică request-ul către `/api/reports?locationId=X`
   - Verifică răspunsul și timpul de răspuns

