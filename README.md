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

## 🛠️ Cosa c'è da fare (Roadmap)

- [ ] **Integrazione Firebase Auth**: Sostituire il mock login con autenticazione reale.
- [ ] **Firestore Database**: Memorizzare i servizi offerti e le prenotazioni effettuate.
- [ ] **UI Calendario**: Interfaccia per la scelta della data e ora del servizio.
- [ ] **Notifiche Push**: Avvisi per nuove prenotazioni o conferme.
- [ ] **Design Avanzato**: Personalizzazione completa con Material3 e animazioni.

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