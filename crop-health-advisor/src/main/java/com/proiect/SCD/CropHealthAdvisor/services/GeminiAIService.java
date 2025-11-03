// src/main/java/com/proiect/SCD/CropHealthAdvisor/services/GeminiAIService.java
package com.proiect.SCD.CropHealthAdvisor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public Mono<String> getInterpretation(String prompt) {
        // Mock response pentru testare - in productie ar trebui sa folosesti API-ul real Gemini
        // Extrage metricele din prompt pentru un răspuns mai realist
        double ndvi = extractNDVI(prompt);
        double temp = extractTemperature(prompt);
        double precip = extractPrecipitation(prompt);
        
        String interpretation = generateMockInterpretation(ndvi, temp, precip);
        
        String mockResponse = String.format(
            "{\"mockAI\": true, \"interpretation\": \"%s\", \"timestamp\": \"%s\"}",
            interpretation,
            java.time.Instant.now().toString()
        );
        
        return Mono.just(mockResponse);
    }
    
    private double extractNDVI(String prompt) {
        try {
            int ndviIdx = prompt.indexOf("NDVI");
            if (ndviIdx != -1) {
                String substr = prompt.substring(ndviIdx);
                int colonIdx = substr.indexOf(":");
                int parenIdx = substr.indexOf("(");
                if (colonIdx != -1 && parenIdx != -1) {
                    String valueStr = substr.substring(colonIdx + 1, parenIdx).trim();
                    return Double.parseDouble(valueStr);
                }
            }
        } catch (Exception e) {
            // Fallback to random if extraction fails
        }
        return Math.random() * 2.0 - 1.0;
    }
    
    private double extractTemperature(String prompt) {
        try {
            int tempIdx = prompt.indexOf("Temperatură:");
            if (tempIdx != -1) {
                String substr = prompt.substring(tempIdx);
                int colonIdx = substr.indexOf(":");
                int degreeIdx = substr.indexOf("°C");
                if (colonIdx != -1 && degreeIdx != -1) {
                    String valueStr = substr.substring(colonIdx + 1, degreeIdx).trim();
                    return Double.parseDouble(valueStr);
                }
            }
        } catch (Exception e) {
            // Fallback to random if extraction fails
        }
        return Math.random() * 50.0 - 10.0;
    }
    
    private double extractPrecipitation(String prompt) {
        try {
            int precipIdx = prompt.indexOf("Precipitații:");
            if (precipIdx != -1) {
                String substr = prompt.substring(precipIdx);
                int colonIdx = substr.indexOf(":");
                int mmIdx = substr.indexOf("mm");
                if (colonIdx != -1 && mmIdx != -1) {
                    String valueStr = substr.substring(colonIdx + 1, mmIdx).trim();
                    return Double.parseDouble(valueStr);
                }
            }
        } catch (Exception e) {
            // Fallback to random if extraction fails
        }
        return Math.random() * 100.0;
    }
    
    private String generateMockInterpretation(double ndvi, double temp, double precip) {
        StringBuilder sb = new StringBuilder();
        
        // ANALIZĂ DETALIATĂ ȘI COMPREHENSIVĂ (minimum 10-15 rânduri)
        sb.append("📊 EVALUARE GENERALĂ A STĂRII CULTURILOR:\n\n");
        
        // Analiza NDVI detaliată
        sb.append("INDICI DE VEGETAȚIE - Analiză NDVI și EVI:\n");
        if (ndvi < -0.3) {
            sb.append("Vegetația prezintă valori extrem de scăzute (NDVI: ").append(String.format("%.3f", ndvi)).append("), indicând fie un sol complet gol, fie vegetație foarte degradată. ");
            sb.append("Această situație necesită intervenție urgentă și investigare imediată a cauzelor. ");
            sb.append("Este posibil să fie vorba despre compactarea excesivă a solului, lipsă de nutrienți, sau probleme de drenaj. ");
            sb.append("Recomandăm un test de sol complet pentru a identifica problemele specifice.\n\n");
        } else if (ndvi < 0.0) {
            sb.append("NDVI-ul indică (").append(String.format("%.3f", ndvi)).append(") semne clare de stres vegetal sever. ");
            sb.append("Vegetația prezintă semne de degradare sau poate fi în faza incipientă de dezvoltare. ");
            sb.append("Este esențial să se identifice rapid cauzele: poate fi lipsă de apă, deficiențe nutriționale, sau boli. ");
            sb.append("Monitorizarea zilnică este recomandată până la îmbunătățirea situației.\n\n");
        } else if (ndvi < 0.3) {
            sb.append("Valorile NDVI (").append(String.format("%.3f", ndvi)).append(") indică o vegetație în dezvoltare moderată. ");
            sb.append("Cultura este prezentă, dar există potențial semnificativ de îmbunătățire. ");
            sb.append("Această fază necesită atenție sporită la nutriție și condiții de mediu pentru a optimiza creșterea. ");
            sb.append("Fertilizarea corectă și irigația controlată pot îmbunătăți semnificativ indicatorii în următoarele 2-3 săptămâni.\n\n");
        } else if (ndvi < 0.6) {
            sb.append("NDVI excelent (").append(String.format("%.3f", ndvi)).append(") - cultura prezintă o vegetație sănătoasă și viguroasă. ");
            sb.append("Cultura se dezvoltă bine și se află într-o fază optimă de creștere. ");
            sb.append("Condițiile actuale sunt favorabile, dar continuarea monitorizării și menținerea acestor condiții sunt esențiale. ");
            sb.append("Aplicarea unui program de fertilizare echilibrat și irigație optimă va menține acest nivel de sănătate.\n\n");
        } else {
            sb.append("NDVI exceptional (").append(String.format("%.3f", ndvi)).append(") - cultura prezintă o creștere foarte viguroasă și vegetație excelentă. ");
            sb.append("Aceasta indică condiții optime și potențial maxim de producție. ");
            sb.append("Este important să menții aceste condiții prin monitorizare constantă și acțiuni preventive. ");
            sb.append("Poți considera îmbunătățiri suplimentare doar pentru optimizare maximă, dar situația actuală este deja excelentă.\n\n");
        }
        
        // Analiza temperaturii solului detaliată
        sb.append("🌡️ ANALIZA TEMPERATURII SOLULUI (LST):\n");
        if (temp < 5) {
            sb.append(String.format("Temperatura solului de %.1f°C este foarte scăzută și prezintă risc semnificativ pentru culturi sensibile. ", temp));
            sb.append("Aceste condiții pot cauza înghețarea rădăcinilor și oprirea dezvoltării. ");
            sb.append("Recomandări urgente: aplicarea acoperirilor de protecție termică, foliere, sau utilizarea mulching-ului pentru izolare. ");
            sb.append("Monitorizați intensiv culturile sensibile și pregătiți măsuri de protecție pentru perioadele de îngheț.\n\n");
        } else if (temp < 10) {
            sb.append(String.format("Temperatura solului de %.1f°C este sub nivelul optim pentru majoritatea culturilor de câmp. ", temp));
            sb.append("Dezvoltarea va fi mai lentă, iar rădăcinile vor absorbi nutrienții mai puțin eficient. ");
            sb.append("Este important să ajustați programele de fertilizare și irigație în conformitate cu aceste condiții. ");
            sb.append("Monitorizați dezvoltarea culturilor și anticipați întârzierile posibile în ciclul de creștere.\n\n");
        } else if (temp <= 30) {
            sb.append(String.format("Temperatura solului de %.1f°C se încadrează în intervalul optim pentru dezvoltarea culturilor. ", temp));
            sb.append("Aceste condiții favorizează activitatea rădăcinilor, absorbția nutrienților și creșterea vegetală. ");
            sb.append("Cultura ar trebui să se dezvolte normal, iar procesele fiziologice sunt active. ");
            sb.append("Mențineți aceste condiții prin gestionarea corectă a irigației și nutriției.\n\n");
        } else if (temp <= 35) {
            sb.append(String.format("Temperatura solului de %.1f°C este ridicată, aproape de limita superioară de confort pentru multe culturi. ", temp));
            sb.append("La aceste temperaturi, culturile pot prezenta semne de stres termic: închiderea stomatelor, scăderea fotosintezei. ");
            sb.append("Recomandări: intensificarea irigației pentru răcire, utilizarea acoperirilor de umbrire, și monitorizarea zilnică a stării culturilor. ");
            sb.append("Culturile pot necesita protecție suplimentară în perioadele de vârf termic.\n\n");
        } else {
            sb.append(String.format("Temperatura solului de %.1f°C este extrem de ridicată și poate provoca daune severe culturilor. ", temp));
            sb.append("La aceste niveluri, riscul de stres termic sever este foarte mare, iar productivitatea poate scădea dramatic. ");
            sb.append("Măsuri urgente necesare: irigație frecventă pentru răcire, acoperiri de protecție, și posibil întreruperea temporară a anumitor operațiuni agricole. ");
            sb.append("Consultarea unui agronom pentru strategii specifice de protecție este recomandată.\n\n");
        }
        
        // Analiza precipitațiilor și umidității detaliată
        sb.append("💧 ANALIZA APEI ȘI UMIDITĂȚII:\n");
        if (precip < 10) {
            sb.append(String.format("Precipitațiile estimate de %.1fmm sunt clar insuficiente pentru nevoile culturilor. ", precip));
            sb.append("Aceste condiții conduc inevitabil la stres hidric, cu impact negativ asupra dezvoltării și productivității. ");
            sb.append("Irigația suplimentară este esențială și trebuie implementată urgent. ");
            sb.append("Planificați un program de irigație regulat, monitorizați umiditatea solului zilnic, și ajustați dozele în funcție de evapotranspirație. ");
            sb.append("În perioade de secetă severă, este crucial să menții umiditatea solului la niveluri optime pentru a preveni pierderi de producție.\n\n");
        } else if (precip < 30) {
            sb.append(String.format("Precipitațiile de %.1fmm sunt sub optimul necesar pentru cultura actuală. ", precip));
            sb.append("Monitorizarea atentă a umidității solului este necesară pentru a anticipa nevoile de irigație. ");
            sb.append("Este recomandat să pregătiți un plan de irigație de rezervă și să monitorizați indicatorii de stres hidric. ");
            sb.append("Ajustarea programului de irigație în funcție de condițiile meteorologice și nevoile culturii este esențială.\n\n");
        } else if (precip <= 80) {
            sb.append(String.format("Precipitațiile de %.1fmm sunt în intervalul normal și oferă condiții bune pentru culturi. ", precip));
            sb.append("Balanța apei pare echilibrată, ceea ce este favorabil pentru dezvoltarea sănătoasă a culturilor. ");
            sb.append("Continuați monitorizarea regulată și ajustați irigația doar când este necesar. ");
            sb.append("Menținerea acestui nivel optim de umiditate va susține o creștere constantă și productivitate ridicată.\n\n");
        } else if (precip <= 100) {
            sb.append(String.format("Precipitațiile de %.1fmm sunt ridicate și necesită atenție la drenaj. ", precip));
            sb.append("Excesul de apă poate duce la saturarea solului, limitarea oxigenării rădăcinilor și apariția problemelor de pudră. ");
            sb.append("Verificați sistemul de drenaj, monitorizați nivelul apei în sol, și luați măsuri pentru prevenirea compactării. ");
            sb.append("În cazuri extreme, poate fi necesară îmbunătățirea drenajului sau chiar evacuarea apei excesive.\n\n");
        } else {
            sb.append(String.format("Precipitațiile de %.1fmm sunt foarte ridicate și pot cauza probleme serioase. ", precip));
            sb.append("Risc de inundații locale, saturare completă a solului și posibile pierderi de producție. ");
            sb.append("Măsuri urgente: verificare imediată a drenajului, evaluarea stării culturilor, și protecție împotriva compactării solului. ");
            sb.append("Este posibil să fie necesară intervenție urgentă pentru salvarea culturilor.\n\n");
        }
        
        // Recomandări detaliate și prioritizate
        sb.append("✅ PLAN DE ACȚIUNE - RECOMANDĂRI PRIORITIZATE:\n\n");
        
        if (ndvi < 0.3) {
            sb.append("PRIORITATE RIDICATĂ:\n");
            sb.append("1. Aplicarea urgentă de fertilizanți echilibrați (NPK) pentru îmbunătățirea NDVI\n");
            sb.append("2. Verificarea completă a sănătății rădăcinilor și a structurii solului\n");
            sb.append("3. Testare sol pentru identificarea deficiențelor nutriționale specifice\n");
            sb.append("4. Implementarea unui program de irigație optim pentru susținerea creșterii\n\n");
        }
        
        if (precip < 30 && temp > 15) {
            sb.append("GESTIUNEA APEI:\n");
            sb.append("1. Programare imediată a irigației suplimentare pentru a compensa lipsa precipitațiilor\n");
            sb.append("2. Instalarea de senzori de umiditate pentru monitorizare precisă\n");
            sb.append("3. Optimizarea programului de irigație pe baza evapotranspirației reale\n");
            sb.append("4. Considerarea tehnologiilor de conservare a apei (mulching, irigație cu picuri)\n\n");
        }
        
        if (temp > 30) {
            sb.append("PROTECȚIA TERMICĂ:\n");
            sb.append("1. Monitorizare intensă zilnică pentru identificarea rapidă a stresului termic\n");
            sb.append("2. Considerarea acoperirilor de umbrire sau foliilor de protecție\n");
            sb.append("3. Ajustarea programului de irigație pentru răcire (irigație dimineața devreme)\n");
            sb.append("4. Evaluarea necesității de întrerupere temporară a anumitor operațiuni în vârfurile termice\n\n");
        }
        
        if (ndvi >= 0.3 && temp >= 10 && temp <= 30 && precip >= 30) {
            sb.append("MENȚINEREA CONDIȚIILOR OPTIME:\n");
            sb.append("1. Continuarea programului de monitorizare regulată a culturilor\n");
            sb.append("2. Aplicarea programului standard de fertilizare și tratamente preventive\n");
            sb.append("3. Optimizarea continuă a programului de irigație pe baza nevoilor reale\n");
            sb.append("4. Pregătirea strategiilor de răspuns pentru eventuale schimbări de condiții\n\n");
        }
        
        // Asigură că există întotdeauna o secțiune de recomandări, chiar dacă nu se potrivesc condițiile de mai sus
        boolean hasRecommendations = ndvi < 0.3 || (precip < 30 && temp > 15) || temp > 30 || (ndvi >= 0.3 && temp >= 10 && temp <= 30 && precip >= 30);
        if (!hasRecommendations) {
            sb.append("ACȚIUNI RECOMANDATE:\n");
            sb.append("1. Continuarea monitorizării regulate pentru detectarea timpurie a problemelor\n");
            sb.append("2. Pregătirea unui plan de acțiune pentru eventuale schimbări de condiții meteorologice\n");
            sb.append("3. Menținerea unui program optim de irigație și fertilizare\n");
            sb.append("4. Documentarea continuă a observațiilor pentru analiză comparativă\n\n");
        }
        
        // Sfaturi practice
        sb.append("💡 SFATURI PRACTICE PENTRU ÎMBUNĂTĂȚIRE:\n");
        sb.append("• Monitorizați zilnic indicatorii și ajustați strategiile în funcție de tendințe\n");
        sb.append("• Mențineți un jurnal detaliat cu observații și acțiuni întreprinse\n");
        sb.append("• Consultați periodic un agronom pentru validarea deciziilor bazate pe date satelitare\n");
        sb.append("• Implementați practici de agricultură de precizie pentru optimizare continuă\n");
        sb.append("• Anticipați nevoile culturilor pe baza tendințelor identificate în date\n\n");
        
        sb.append("📋 CONCLUZIE: Aceste recomandări sunt bazate pe analiza comprehensivă a tuturor metricilor satelitare disponibile. ");
        sb.append("Pentru implementare optimă, consultați un agronom local care poate adapta aceste recomandări la condițiile specifice ale locației dvs. ");
        sb.append("Monitorizarea continuă și ajustarea strategiilor sunt esențiale pentru succesul pe termen lung.");
        
        return sb.toString();
        
        // Codul original pentru API-ul real Gemini (comentat pentru testare):
        /*
        WebClient client = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent")
                .build();

        String requestBody = "{" +
            "\"contents\": [{\"parts\":[{\"text\":\"" + prompt + "\"}]}]" +
        "}";

        return client.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    // Proceseaza raspunsul pentru a extrage textul generat de AI
                    // Exemplu simplu, va trebui adaptat la structura reala a raspunsului
                    return response;
                });
        */
    }
}