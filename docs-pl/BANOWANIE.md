# Banowanie

Banowanie użytkowników, którzy nie stosują się do zasad korzystania z aplikacji, odbywa się nie tylko poprzez zablokowanie dostępu z danego konta użytkownika (oflagowanie konta w bazie danych zmienną `isBanned = true`) ale również poprzez zapis unikalnego identyfikatora urządzenia w celu zablokowania dostępu również z urządzenia użytkownika.
Unikalny identyfikator, który jest używany w celu identyfikacji urządzenia to adres adaptera modułu Bluetooth, który jest następnie szyfrowany poprzez algorytm AES i zapisywany w bazie danych. Kod który pobiera adres oraz go szyfruje, znajduje się w pliku [DeviceID.kt](https://github.com/HotShotsApp/HotShots/blob/master/app/src/main/java/tw/app/hotshots/util/DeviceID.kt)

Aplikacja sprawdza status blokady użytkownika/urządzenia głównie w pliku [AuthFragment.kt](https://github.com/HotShotsApp/HotShots/blob/master/app/src/main/java/tw/app/hotshots/activity/auth/fragment/AuthFragment.kt). Przy próbie rejestracji, sprawdzane jest czy urządzenie nie zostało już wcześniej zablokowane na innym koncie. Przy logowaniu zaś sprawdzane czy nie jest zablokowane samo konto. Nie jest wtedy sprawdzane czy zostało zablokowane urządzenie, ponieważ jeśli zostanie zdjęta blokada na użytkownika, to zostanie też zdjęta blokada na urządzenie - i odwrotnie.
Czasami aby zdjąć blokadę z konta/urządzenia, wymagane jest aby podjąć próbe zalogowania się na konto - wtedy jeśli czas zbanowania już upłynął, konto oraz urządzenie zostanie od razu i automatycznie odblokowane. Odblokowanie konta/urządzenia może zająć dłużej jeśli użytkownik nie podejmie próby zalogowania się na konto.

Od zablokowania konta/użytkownika można się odwołać. Można też poprosić o odblokowanie samego urządzenia, bez odblokowywania konta.
