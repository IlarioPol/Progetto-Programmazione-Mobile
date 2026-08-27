# 📌 Progetto Programmazione Mobile - Prenotazione Servizi

Applicazione Android moderna sviluppata con **Jetpack Compose** e **Firebase**, progettata per la gestione professionale di prenotazioni tra Clienti, Professionisti (Provider) e Titolari (Manager).

---

## 🛠️ Architettura e Tecnologie
- **UI**: Jetpack Compose (Material 3)
- **Logica**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Firestore (NoSQL Real-time)
- **Auth**: Firebase Authentication (Email/Password, Email Verification)
- **Navigation**: Type-safe Navigation Compose

---

## 📈 Roadmap di Sviluppo (Prossimi Step)

### 👑 1. Business Profile & Discovery (Alta Priorità)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Business Page** | Pagina dedicata all'azienda con lista del team, galleria immagini e catalogo servizi completo. | ⏳ |
| **Geolocalizzazione** | Ricerca delle attività basata sulla posizione GPS dell'utente e calcolo distanza. | ⏳ |
| **Preferiti** | Possibilità per il cliente di salvare Aziende o Professionisti tra i preferiti. | 📅 |

### 📊 2. Management & Analytics (Dashboard Manager)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Analisi Fatturato** | Visualizzazione del fatturato totale, mensile e per singolo professionista con grafici. | ⏳ |
| **Rating & Performance** | Statistiche sui feedback dei clienti, rating medio del team e dei singoli provider. | ⏳ |
| **Trend Servizi** | Analisi dei servizi più richiesti e delle fasce orarie di maggior affluenza. | ⏳ |
| **Esportazione Dati** | Generazione di report (CSV/PDF) per il riepilogo contabile e gestionale. | 📅 |

### 🔔 3. Comunicazione & Notifiche
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Cloud Messaging (FCM)** | Invio di notifiche push al cambio stato prenotazione (es. "Il professionista ha accettato"). | 📅 |
| **Promemoria automatici** | Notifica automatica al cliente prima dell'appuntamento per ridurre i "no-show". | 📅 |

### 💎 4. UX & Refactoring
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Dark Mode** | Ottimizzazione completa dei colori e degli asset per il supporto al tema scuro. | ⏳ |
| **Skeleton Loaders** | Miglioramento della UX durante il caricamento asincrono dei dati da Firestore. | 📅 |

---

## ✅ Obiettivi Raggiunti
- [x] **Autenticazione**: Integrazione Firebase Auth, verifica email e recupero password.
- [x] **Gerarchia Business**: Sistema di inviti/accettazione tra Manager e Provider e binding aziendale.
- [x] **Booking Core**: Generazione dinamica degli slot, gestione orari di lavoro e workflow stati.
- [x] **Feedback & Stats**: Sistema di recensioni e statistiche base per il singolo Provider.
- [x] **Ricerca & Discovery**: Filtri per categorie e ricerca globale (Azienda/Servizio).
- [x] **Profilo Utente**: Modifica password, cancellazione account e gestione sessione sicura.
- [x] **Fix Tecnici**: Risoluzione criticità KTX (Firebase BoM) e osservabilità del Locale nelle UI.

---

## 📱 Flusso per Ruolo

### 👤 Cliente (User)
- Esplora per categorie o ricerca specifica.
- Seleziona data e ora da un **calendario dinamico** di slot liberi.
- Gestisce le cancellazioni e lascia feedback.

### 💼 Professionista (Provider)
- Definisce i propri orari di disponibilità.
- Gestisce il catalogo servizi e l'agenda delle richieste.
- Visualizza il proprio rendimento e i feedback ricevuti.

### 👑 Titolare (Manager)
- Amministra la struttura e invita nuovi collaboratori.
- Monitora le performance di ogni dipendente (Fatturato, Rating).
- Gestisce il profilo pubblico dell'azienda.

---

## 🤝 Guida alla Collaborazione (GitHub Workflow)

Segui questi passaggi per contribuire al progetto in modo ordinato:

### 1. Clonare il Progetto
Scarica il progetto sul tuo computer locale:
```bash
git clone https://github.com/TuoUsername/ProgettoProgrammazioneMobile.git
```

### 2. Creare una Branch Personale
**Mai lavorare direttamente sul `main`**. Crea una branch dedicata alla tua task:
```bash
git checkout -b feature/nome-tua-funzionalita
```

### 3. Lavorare e Salvare le Modifiche
Dopo aver scritto il codice, salva le modifiche localmente:
```bash
git add .
git commit -m "Descrizione chiara di cosa hai fatto"
```

### 4. Caricare la Branch online
Invia il tuo lavoro su GitHub:
```bash
git push origin feature/nome-tua-funzionalita
```

### 5. Pull Request (PR) & Merge
1. Vai sulla pagina del repository su GitHub.
2. Vedrai un avviso "Compare & pull request", cliccaci.
3. Descrivi brevemente le modifiche e invia la **Pull Request**.
4. Se il codice è corretto e non ci sono conflitti, verrà effettuato il **Merge** nel ramo `main`.

---

## 🧪 Account Demo (Verificati)
| Ruolo | Email | Password |
| :--- | :--- | :--- |
| **Manager** | `bzgpgvpqubeppqldrw@vtmpj.com` | `password123` |
| **Manager** | `gfd8c6@uqu.me` | `password123` |
| **Provider** | `bkivqrltdlacxvlnej@onldm.net` | `password123` |
| **Provider** | `a313ds@uqu.me` | `password123` |
| **Cliente** | `wanox64415@mypethealh.com` | `password123` |
