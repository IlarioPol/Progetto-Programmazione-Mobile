# Progetto Programmazione Mobile - Prenotazione Servizi

Benvenuto nel repository di **Progetto Programmazione Mobile**. Questa applicazione è un sistema di prenotazione servizi online sviluppato con **Jetpack Compose**, con gestione multi-utente (Clienti ed Emettitori di servizi).

## 🚀 Funzionalità Implementate

- **Navigazione Avanzata**: Utilizzo di `Navigation Compose` per gestire il flusso tra Login, Registrazione e Home specifiche per ruolo.
- **Gestione Ruoli (Multi-Login)**: Sistema differenziato per:
    - **Cliente**: Può cercare e prenotare servizi.
    - **Emettitore (Provider)**: Gestisce le proprie prenotazioni e servizi.
- **Registrazione con Scelta Ruolo**: Interfaccia di iscrizione con RadioButton per la selezione del profilo utente.
- **Architettura MVVM Clean**: Struttura delle cartelle organizzata per scalabilità:
    - `data/`: Modelli dati e logica di accesso.
    - `ui/screens/auth/`: Login e Registrazione.
    - `ui/screens/client/`: Dashboard Cliente.
    - `ui/screens/provider/`: Dashboard Emettitore.
- **AuthViewModel**: Gestione centralizzata dello stato di autenticazione, caricamento ed errori.

## 📋 Tabella delle Task Generali (Roadmap)

| Sezione              | Task                       | Descrizione                                                     | Stato |
|:---------------------|:---------------------------|:----------------------------------------------------------------|:-----:|
| **Autenticazione**   | Integrazione Firebase Auth | Collegare il sistema di registrazio/login reale a Firebase.     |   ✅   |
| **Autenticazione**   | Email di Verifica          | Inviare email di attivazione per la registrazione.              |   ✅   |
| **Gestione Profilo** | Logout                     | fare il logout una volta entrati con il proprio account         |   ✅   |
| **Autenticazione**   | Recupero Password          | Funzionalità "Password Dimenticata" via email.                  |   ✅   |
| **Gestione Profilo** | Modifica Password          | Permettere all'utente di cambiare password dall'area riservata. |   ✅   |
| **Gestione Profilo** | Eliminazione Account       | Opzione per cancellare definitivamente il proprio profilo.      |   ✅    |
| **Gestione Profilo** | Aggiornamento Dati         | Modifica di nome, foto profilo e contatti.                      |   ⏳   |

## 📋 Tabella Booking & Flussi (Prenotazioni)

| Azione | Attore | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Creazione Prenotazione** | Cliente | Selezionare un servizio e inviare la richiesta (Stato: *Pending*). | ⏳ |
| **Notifica Richiesta** | Provider | Ricevere una notifica/avviso per una nuova prenotazione in attesa. | ⏳ |
| **Conferma Prenotazione** | Provider | Accettare la richiesta del cliente (Stato: *Confirmed*). | ⏳ |
| **Rifiuto Prenotazione** | Provider | Rifiutare la richiesta motivando la scelta (Stato: *Rejected*). | ⏳ |
| **Cancellazione Cliente** | Cliente | Possibilità di annullare una prenotazione prima della data prevista. | ⏳ |
| **Cancellazione Provider** | Provider | Possibilità di annullare un impegno per imprevisti con avviso al cliente. | ⏳ |
| **Storico Stati** | Entrambi | Visualizzare lo storico con i cambi di stato (In attesa -> Confermato -> Completato). | ⏳ |

## 📋 Tabella Task Provider (Emettitore)

| Area | Task | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Calendario** | Visualizzazione Giornaliera | Elenco dettagliato degli appuntamenti del giorno selezionato. | ⏳ |
| **Calendario** | Navigazione Date | Possibilità di navigare tra i giorni (passati e futuri) per consultare l'agenda. | ⏳ |
| **Prenotazioni** | Gestione Richieste (Pending) | Area dedicata per accettare o rifiutare le prenotazioni in arrivo. | ⏳ |
| **Disponibilità** | Rilascio Slot (Cancellazioni) | Automazione: se una prenotazione viene annullata, lo slot torna disponibile nel calendario. | ⏳ |
| **Analisi** | Statistiche Real-time | Grafici e dati reali su fatturato, servizi erogati e performance. | ⏳ |
| **Recensioni** | Feedback Management | Lista funzionale delle recensioni con possibilità di risposta dell'emettitore. | ⏳ |

## 📋 Tabella Task Cliente

| Area | Task | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Esplorazione** | Ricerca Servizi | Visualizzare e filtrare i servizi creati dai diversi provider. | ⏳ |
| **Prenotazione** | Richiesta Appuntamento | Selezionare un servizio e inviare la proposta di data/ora (Stato: *Pending*). | ⏳ |
| **Tracking** | Stato Prenotazione | Monitorare se il provider ha accettato o rifiutato l'appuntamento. | ⏳ |
| **Agenda** | Appuntamenti Confermati | Visualizzare chiaramente i prossimi impegni accettati dal provider. | ⏳ |
| **Storico** | Servizi Passati | Consultare l'elenco delle prestazioni già ricevute e completate. | ⏳ |
| **Feedback** | Recensioni e Voti | Lasciare un commento e una valutazione a 5 stelle per i servizi completati. | ⏳ |
| **Gestione** | Annullamento | Possibilità di cancellare una richiesta pendente o un appuntamento già confermato. | ⏳ |

## 🧪 Come testare l'applicazione

L'app utilizza credenziali "mock" (simulate) per dimostrare i diversi flussi:

1.  **Accesso Cliente**:
    - **Email**: `cliente@test.com`
    - **Password**: `password`
2.  **Accesso Emettitore**:
    - **Email**: `provider@test.com`
    - **Password**: `password`
3.  **Registrazione**: Puoi creare un nuovo profilo scegliendo il ruolo; verrai reindirizzato alla Home corretta in base alla scelta.

## 📦 Tecnologie Utilizzate

- **Kotlin**: Linguaggio di programmazione.
- **Jetpack Compose**: UI Toolkit moderno.
- **ViewModel**: Gestione logica e stato.
- **Navigation Compose**: Routing interno all'app.

---
Sviluppato da [Ilario Polidori](https://github.com/IlarioPol)
