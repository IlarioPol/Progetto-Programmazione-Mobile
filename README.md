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

## ✅ Funzionalità Già Implementate (Testate)
- **Autenticazione Completa**: Registrazione con ruoli, Login, Verifica Email, Reset Password ed Eliminazione Account.
- **Gerarchia Aziendale**: Setup Business (Manager), Sistema di Invito collaboratori e binding automatico Business-User.
- **Gestione Catalogo**: CRUD completo (Crea, Leggi, Modifica, Elimina) dei servizi lato Professionista.
- **Booking Flow**: Richiesta (Pending), Accettazione (Confirmed), Completamento (Completed), Rifiuto (Rejected) e Annullamento (Canceled).
- **Feedback & Stats**: Sistema di recensioni reali e calcolo fatturato automatico basato sui lavori completati.
- **Ricerca Intelligente**: Filtri per macro-categoria (Medicina, Beauty, ecc.) e ricerca testuale dinamica.

---

## ⏳ Prossimi Obiettivi (Nuove Task)

### 📅 1. Logica Avanzata Slot & Calendario
| Task | Descrizione | Priorità |
| :--- | :--- | :---: |
| **Configurazione Orari** | Il Provider imposta i propri orari di lavoro e pause per ogni giorno. | 🔥 Alta |
| **Generazione Slot** | Algoritmo che calcola gli spazi liberi in base alla durata del servizio scelto. | 🔥 Alta |
| **Blocco Concorrenza** | Impedire la prenotazione dello stesso orario a più utenti (Transazioni DB). | 🔥 Alta |
| **Auto-Release** | Rilascio istantaneo dello slot nel calendario in caso di cancellazione. | Media |

### 🔍 2. Esplorazione Avanzata & Business Page
| Task | Descrizione | Priorità |
| :--- | :--- | :---: |
| **Ricerca Cross-Entity** | Cerca contemporaneamente per Nome Azienda, Professionista o Servizio. | 🔥 Alta |
| **Business Page** | Pagina dedicata dell'azienda con lista del team e catalogo completo servizi. | 🔥 Alta |
| **Booking UI Grafica** | Interfaccia a calendario visivo per scegliere giorni e ore disponibili. | Media |

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
