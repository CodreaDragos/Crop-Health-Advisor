# Ghid: Configurare OpenWeatherMap API pentru Temperatură și Precipitații Reale

## 📋 Pași pentru a obține API Key OpenWeatherMap

### 1. Creează cont gratuit

1. Mergi la: **https://openweathermap.org/api**
2. Click pe **"Sign Up"** (în dreapta sus)
3. Completează formularul:
   - Username
   - Email
   - Password
4. Confirmă email-ul

### 2. Obține API Key

1. După login, mergi la: **https://home.openweathermap.org/api_keys**
2. Vei vedea secțiunea "API keys"
3. Click pe **"Create key"** sau folosește key-ul default generat
4. Copiază API key-ul (format: `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)

### 3. Verifică planul

- **Free tier**: 60 calls/minut, 1,000,000 calls/lună
- ✅ **Suficient pentru demo și testare!**

### 4. Adaugă în `application.properties`

```properties
# OpenWeatherMap API Key (pentru temperatură și precipitații reale)
openweathermap.api.key=YOUR_API_KEY_HERE
```

### 5. Repornește Backend-ul

```bash
cd crop-health-advisor
mvn spring-boot:run
```

## ✅ Verificare

După configurare, când generezi un raport:
- Temperatura ar trebui să fie **reală** (corespunde cu vremea actuală)
- Precipitațiile ar trebui să fie **reale** (0mm dacă nu plouă, sau valoarea reală)

## 🔍 Testare

Poți testa API-ul direct în browser:
```
http://api.openweathermap.org/data/2.5/weather?lat=46.77&lon=23.62&appid=YOUR_API_KEY&units=metric
```

Răspunsul va conține:
- `main.temp` - temperatura în °C
- `rain.1h` - precipitații în ultima oră (mm)

## 📝 Note

1. **Free tier** este suficient pentru demo
2. API key-ul este **gratuit** și poți face multe request-uri
3. Dacă nu adaugi API key, aplicația va folosi valori mock
4. Pentru producție, folosește variabile de mediu în loc de `application.properties`

## 🆘 Dacă nu vrei să folosești OpenWeatherMap

Aplicația funcționează și cu valori mock pentru temperatură/precipitații. Doar NDVI/EVI/NDWI sunt critice pentru analiza culturilor și acestea sunt deja reale de la Sentinel Hub.



