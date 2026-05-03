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

## 📈 Roadmap di Sviluppo

### 🔐 1. Autenticazione & Sicurezza
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Integrazione Firebase** | Collegamento reale a Firebase Auth e Firestore. | ✅ |
| **Email Verification** | Invio link di verifica alla registrazione. | ✅ |
| **Recupero Password** | Reset della password tramite link via email. | ✅ |
| **Gestione Sessione** | Logout sicuro e persistenza utente. | ✅ |
| **Sicurezza Profilo** | Modifica password ed eliminazione account. | ✅ |

### 👑 2. Struttura Gerarchica & Business
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Business Setup** | Creazione dell'entità Azienda (Nome, Macro-Categoria). | ✅ |
| **Gestione Team** | Sistema di inviti/accettazione tra Manager e Provider. | ✅ |
| **Binding Gerarchico** | Collegamento logico tra Servizi, Professionisti e Azienda. | ✅ |
| **Business Page** | Pagina dedicata all'azienda con lista team e catalogo completo. | ⏳ |

### 📅 3. Sistema di Prenotazione Avanzato (Slots & Calendar)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Working Hours** | Configurazione orari di apertura e durata slot per il Provider. | ⏳ |
| **Slot Generation** | Generazione automatica degli slot liberi basata sull'orario. | ⏳ |
| **Real-time Locking** | Impedire la prenotazione dello stesso slot a più utenti. | ⏳ |
| **Slot Release** | Liberazione automatica dello slot in caso di cancellazione. | ⏳ |
| **Booking UI** | Interfaccia a calendario per la scelta di giorni e ore disponibili. | ⏳ |
| **Status Flow** | Ciclo completo: Pending -> Confirmed -> Completed/Canceled. | ✅ |

### 🔍 4. Ricerca & Discovery
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Filtri Categoria** | Navigazione per settori (Medicina, Beauty, ecc.). | ✅ |
| **Cross-Entity Search** | Ricerca globale per Nome Azienda, Professionista o Servizio. | ⏳ |
| **Geolocalizzazione** | (Opzionale) Ricerca delle attività più vicine all'utente. | ⏳ |

### 📊 5. Analytics & Feedback
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Recensioni** | Sistema di valutazione e commenti post-servizio. | ✅ |
| **Provider Stats** | Monitoraggio guadagni e lavori fatti per singolo professionista. | ✅ |
| **Manager Dashboard** | Statistiche aggregate dell'intero team e fatturato aziendale. | ⏳ |

---

## 📱 Flusso per Ruolo

### 👤 Cliente (User)
- Esplora per categorie o ricerca specifica.
- Visualizza la **Business Page** per scegliere il professionista preferito.
- Seleziona data e ora da un **calendario dinamico** di slot liberi.
- Gestisce le cancellazioni e lascia feedback.

### 💼 Professionista (Provider)
- Definisce i propri orari di disponibilità.
- Gestisce il catalogo servizi e l'agenda delle richieste.
- Visualizza il proprio rendimento e i feedback ricevuti.

### 👑 Titolare (Manager)
- Amministra la struttura e invita nuovi collaboratori.
- Monitora le performance di ogni dipendente.
- Visualizza i trend di fatturato e la soddisfazione clienti globale.

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
| **Provider** | `bkivqrltdlacxvlnej@onldm.net` | `password123` |
| **Cliente** | `wanox64415@mypethealh.com` | `password123` |

---
