# CarParks #
Progetto Sistemi Distribuiti (1819-2-E3101Q112) - Traccia A: Socket &amp; Concurrent Programming

# Autori #
- Chouak Ayoub a.k.a [@ntauth](https://github.com/ntauth) (829749)
- Ali Farjad a.k.a [@Buggy97](https://github.com/Buggy97) (829940)

# Utilizzo #

### 1) Avviare per la classe SocketServerMain (server) ###

### 2) Avviare la classe del parcheggio. ###
   In questo caso si possono scegliere tra due classi da avviare: *ParcheggioSocketClientMain* oppure *ParcheggioSimulatorMain*

##### ParcheggioSimulatorMain #####
Simula un parcheggio con 10 posti auto e 5 parcheggiatori. Oltre al parcheggio vengono anche
simulati degli automobilisti che si presentano e ritirano la propria auto in maniera autonoma.

##### ParcheggioSocketClientMain #####
Predispone 5 parcheggi con 5 posti auto e 10 parcheggiatori ciascuno, e' ideale se si vuole testare l'interfaccia dell'automobilista senza essere intralciati da altri automobilisti 'simulati' che parcheggiano.

Entrambe le classi Parcheggio hanno dunque funzionalita' di rete e permettono la prenotazione di posti mediante il client.

### 3) Avviare AutomobilistaSocketClientMain (client) ###
Possono essere avviati piu' client insieme poiche' il server e' in grado di gestire le richieste concorrenti, l'interfaccia e' via linea di comando e fornisce informazioni su che tipologia di input vuole nel caso l'utente sbagli ad inserire qualche dato richiesto.

L'indirizzo e porta di default per trovare il server sono rispettivamente localhost e 4242.
