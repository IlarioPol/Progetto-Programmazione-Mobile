# Progetto Programmazione Mobile

Benvenuto nel repository di **Progetto Programmazione Mobile**. Questa applicazione è un punto di partenza per un sistema di autenticazione moderno sviluppato con **Jetpack Compose**.

## 🚀 Funzionalità Implementate

- **Navigazione**: Utilizzo di `Navigation Compose` per gestire il flusso tra le schermate.
- **Schermata di Login**: Interfaccia utente completa con validazione dei campi.
- **Schermata di Registrazione**: Interfaccia per la creazione di nuovi account.
- **Architettura MVVM**: Implementazione di un `AuthViewModel` per separare la logica di business dall'interfaccia utente.
- **Gestione dello Stato**: Utilizzo di `State` per gestire il caricamento, gli errori e il successo dell'autenticazione.

## 🛠️ Cosa c'è da fare (Roadmap)

- [ ] **Integrazione Firebase**: Collegare l'app a Firebase Auth per un'autenticazione reale.
- [ ] **Database Locale**: Implementazione di Room per salvare i dati dell'utente o impostazioni offline.
- [ ] **Design Avanzato**: Personalizzazione del tema Material3, aggiunta di icone e animazioni.
- [ ] **Dashboard Home**: Sviluppo della schermata principale post-login con contenuti dinamici.
- [ ] **Recupero Password**: Aggiunta della logica per il reset della password.

## 🧪 Come testare l'applicazione

Al momento l'app utilizza una logica di autenticazione "mock" (simulata). Per testare il corretto funzionamento della navigazione e dello stato:

1.  Avvia l'applicazione su un emulatore o dispositivo fisico.
2.  Nella schermata di **Login**, inserisci le seguenti credenziali:
    -   **Email**: `test@example.com`
    -   **Password**: `password`
3.  Clicca su **Accedi** per essere indirizzato alla schermata di benvenuto.
4.  Prova a lasciare i campi vuoti o inserire credenziali errate per visualizzare i messaggi di errore.
5.  Nella schermata di **Registrazione**, compila tutti i campi per simulare la creazione di un account e accedere automaticamente.

## 📦 Tecnologie Utilizzate

- **Kotlin**: Linguaggio di programmazione principale.
- **Jetpack Compose**: Toolkit moderno per la creazione di UI native.
- **ViewModel & LiveData**: Per la gestione del ciclo di vita e dei dati.
- **Navigation Compose**: Per la navigazione interna.

---