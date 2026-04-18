# 📌 Progetto Programmazione Mobile - Prenotazione Servizi

Applicazione Android moderna sviluppata con **Jetpack Compose** e **Firebase**, progettata per la gestione professionale di prenotazioni tra Clienti, Emettitori (Provider) e Responsabili (Manager).

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
| **Email Verification** | Invio obbligatorio del link di verifica alla registrazione. | ✅ |
| **Recupero Password** | Reset della password tramite link via email. | ✅ |
| **Gestione Sessione** | Logout sicuro e persistenza dell'utente all'avvio. | ✅ |
| **Sicurezza Profilo** | Modifica password dall'area riservata. | ✅ |
| **Cancellazione Dati** | Eliminazione definitiva account e dati personali. | ✅ |

### 👑 2. Struttura Gerarchica (B2B Management)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Ruoli Multipli** | Gestione accessi per Cliente, Provider e Manager. | ✅ |
| **Sistema Inviti** | Il Manager può invitare un Provider tramite email. | ✅ |
| **Accettazione Inviti** | Dialog automatico lato Provider per accettare la supervisione. | ✅ |
| **Binding Manager-User** | Collegamento logico nel DB tra dipendente e supervisore. | ✅ |

### 📅 3. Flusso Prenotazioni (Booking Flow)
| Fase | Attore | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Richiesta** | Cliente | Selezione servizio e invio proposta data/ora (Stato: *Pending*). | ✅ |
| **Notifica** | Provider | Ricezione automatica della richiesta nella dashboard. | ✅ |
| **Approvazione** | Provider | Accettazione della richiesta (Stato: *Confirmed*). | ✅ |
| **Rifiuto** | Provider | Rifiuto della richiesta (Stato: *Rejected*). | ✅ |
| **Completamento** | Provider | Chiusura dell'appuntamento eseguito (Stato: *Completed*). | ✅ |
| **Feedback** | Cliente | Invio recensione post-servizio. | ✅ |
| **Cancellazione** | Entrambi | Possibilità di annullare un impegno prima della data prevista. | ⏳ |

### 🛠️ 4. Funzionalità per Ruolo

#### 👤 Cliente (User)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Esplorazione** | Ricerca e filtri sui servizi caricati dai Provider. | ✅ |
| **Tracking** | Monitoraggio in tempo reale dello stato (Pending/Confirmed/Rejected). | ✅ |
| **Agenda Personale** | Vista dei prossimi appuntamenti confermati. | ✅ |
| **Recensioni** | Sistema di feedback per i servizi conclusi. | ✅ |

#### 💼 Emettitore (Provider)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Catalogo Servizi** | Creazione e modifica servizi (Nome, Prezzo, Durata). | ✅ |
| **Gestione Agenda** | Dashboard per accettare/rifiutare le prenotazioni in arrivo. | ✅ |
| **Statistiche** | Monitoraggio guadagni e performance personali. | ✅ |
| **Feedback** | Visualizzazione delle recensioni ricevute dai clienti. | ✅ |

#### 👑 Responsabile (Manager)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Supervisione Team** | Lista dei Provider associati e stato inviti. | ✅ |
| **Monitoraggio Servizi** | Visualizzazione dei cataloghi dei propri Provider. | ⏳ |
| **Analisi Globale** | Statistiche di fatturato aggregate per l'intero team. | ⏳ |

---

## 🧪 Come Testare

### 1. Account Demo (Già verificati)
Puoi utilizzare i seguenti account per testare le funzionalità senza registrarti (password per tutti: `password123`):
- **Manager**: `bzgpgvpqubeppqldrw@vtmpj.com`
- **Provider**: `bkivqrltdlacxvlnej@onldm.net`
- **Cliente**: `wanox64415@mypethealh.com`

---
