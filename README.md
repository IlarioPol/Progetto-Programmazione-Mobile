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
| **Email Verification** | Invio obbligatorio del link di verifica alla registrazione. | ✅ |
| **Recupero Password** | Reset della password tramite link via email. | ✅ |
| **Gestione Sessione** | Logout sicuro e persistenza dell'utente all'avvio. | ✅ |
| **Sicurezza Profilo** | Modifica password dall'area riservata. | ✅ |
| **Cancellazione Dati** | Eliminazione definitiva account e dati personali. | ✅ |

### 👑 2. Struttura Gerarchica (B2B Management)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Ruoli Multipli** | Gestione accessi per Cliente, Provider e Manager. | ✅ |
| **Business Setup** | Schermata di configurazione Azienda per il Manager (Nome, Categoria). | ✅ |
| **Sistema Inviti** | Il Manager invita Provider alla propria Azienda via email. | ✅ |
| **Accettazione Inviti** | Binding automatico tra Provider, Manager e Business ID. | ✅ |

### 📅 3. Flusso Prenotazioni (Booking Flow)
| Fase | Attore | Descrizione | Stato |
| :--- | :--- | :--- | :---: |
| **Richiesta** | Cliente | Selezione servizio e invio proposta data/ora. | ✅ |
| **Notifica** | Provider | Ricezione automatica della richiesta nella dashboard. | ✅ |
| **Approvazione** | Provider | Accettazione (Confirmed) o Rifiuto (Rejected) della richiesta. | ✅ |
| **Completamento** | Provider | Chiusura dell'appuntamento con aggiornamento fatturato. | ✅ |
| **Feedback** | Cliente | Invio recensione e valutazione (1-5 stelle). | ✅ |
| **Cancellazione** | Cliente | Possibilità di annullare una prenotazione prima dell'esecuzione. | ⏳ |

### 🛠️ 4. Funzionalità Avanzate per Ruolo

#### 👤 Cliente (User)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Ricerca Cross-Entity** | Cerca contemporaneamente per Nome Azienda, Professionista o Servizio. | ⏳ |
| **Filtri Macro-Categoria** | Navigazione strutturata (es. Medicina, Legal, Beauty). | ✅ |
| **Business Page** | Pagina dettaglio dell'azienda con lista del team e catalogo servizi. | ⏳ |
| **Tracking Agenda** | Vista dei propri impegni futuri con stato aggiornato. | ✅ |

#### 💼 Emettitore (Provider)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Gestione Catalogo** | CRUD completo (Aggiungi, Modifica, Elimina) dei propri servizi. | ✅ |
| **Dashboard Agenda** | Gestione delle richieste in entrata e storico lavori. | ✅ |
| **Statistiche Personali** | Monitoraggio dei propri guadagni e delle recensioni ricevute. | ✅ |

#### 👑 Responsabile (Manager)
| Task | Descrizione | Stato |
| :--- | :--- | :---: |
| **Supervisione Team** | Monitoraggio dell'elenco dei professionisti associati. | ✅ |
| **Analytics Team** | Visualizzazione statistiche individuali per ogni dipendente. | ⏳ |
| **Azienda Analytics** | Dashboard fatturato totale, trend prenotazioni e media voti azienda. | ⏳ |

---

## 🧪 Come Testare

### 1. Account Demo (Già verificati)
Puoi utilizzare i seguenti account per testare le funzionalità senza registrarti (password per tutti: `password123`):
- **Manager**: `bzgpgvpqubeppqldrw@vtmpj.com`
- **Provider**: `bkivqrltdlacxvlnej@onldm.net`
- **Cliente**: `wanox64415@mypethealh.com`

### 2. Guida ai Flussi
1. **Manager**: Entra e configura l'azienda. Invita il provider tramite email.
2. **Provider**: Accetta l'invito. Crea un servizio nel catalogo.
3. **Cliente**: Cerca l'azienda o il servizio, prenota inserendo data/ora.
4. **Provider**: Accetta la prenotazione e, una volta eseguita, segnala come "Completata".
5. **Manager**: Controlla le statistiche di fatturato aggiornate.

---
