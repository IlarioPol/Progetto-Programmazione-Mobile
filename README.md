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

### 1. Sistema di Autenticazione & Sicurezza
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Integrazione Firebase** | Collegamento reale a Firebase Auth e Firestore. | ✅ |
| **Email Verification** | Invio obbligatorio del link di verifica alla registrazione. | ✅ |
| **Recupero Password** | Reset della password tramite link via email. | ✅ |
| **Gestione Sessione** | Logout sicuro e persistenza dell'utente all'avvio. | ✅ |
| **Sicurezza Profilo** | Modifica password dall'area riservata. | ✅ |
| **Cancellazione Dati** | Eliminazione definitiva account e dati personali (GDPR compliance). | ✅ |

### 2. Struttura Gerarchica (B2B Management)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Ruoli Multipli** | Gestione accessi per Cliente, Provider e Manager. | ✅ |
| **Sistema Inviti** | Il Manager può invitare un Provider tramite email. | ✅ |
| **Accettazione Inviti** | Dialog automatico lato Provider per accettare la supervisione. | ✅ |
| **Binding Manager-User** | Collegamento logico nel DB tra dipendente e supervisore. | ✅ |

### 3. Core Business (Prenotazioni & Servizi) ⏳ *In Sviluppo*
| Area | Task | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Provider** | Creazione Servizi | Pubblicazione di servizi (nome, prezzo, durata) su Firestore. | ⏳ |
| **Cliente** | Esplorazione | Visualizzazione dei servizi reali caricati dai Provider. | ⏳ |
| **Booking** | Flusso Richiesta | Invio richiesta di prenotazione (Pending -> Confermato). | ⏳ |
| **Calendar** | Agenda | Visualizzazione degli impegni confermati in formato lista/calendario. | ⏳ |
| **Feedback** | Recensioni | Sistema di valutazione (1-5 stelle) e commenti per i servizi conclusi. | ⏳ |

---

## 📱 Dashboards per Ruolo

### 👤 Cliente (User)
- Esplora i servizi disponibili.
- Gestisce le proprie prenotazioni e lo storico.
- Lascia recensioni ai professionisti.

### 💼 Emettitore (Provider)
- Crea e gestisce il proprio catalogo servizi.
- Gestisce le richieste di prenotazione (Accetta/Rifiuta).
- Visualizza le proprie statistiche di guadagno.

### 👑 Responsabile (Manager)
- Supervisiona i Provider associati.
- Monitora le performance del team.
- Gestisce l'espansione del network tramite inviti.

---

## 🧪 Come Testare
1. **Registrazione**: Crea un account scegliendo il ruolo desiderato.
2. **Verifica**: Clicca sul link ricevuto via email (controlla lo Spam).
3. **Login**: Accedi per essere indirizzato alla dashboard specifica del tuo ruolo.
4. **Gerarchia**: Un Manager può invitare la tua email Provider; vedrai il pop-up di accettazione al prossimo login.

---
