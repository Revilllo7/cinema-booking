# Cinema Booking System

[Polish version 🇵🇱](#opis)

## Description
System to manage e-commerce and general information regarding movies for the cinema. The system allows users to browse movies, view screening times, book tickets, and manage their bookings.

## Starting the Application
To start the application, ensure you have Java and Maven installed. Then, run the following command
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
This will start the application with the development profile.

or for disposable demo data
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,demo-data
```	

> [!NOTE]  
> Requirements: Java 21 or higher, Maven 3.6 or higher, PostgreSQL database.

## Features
- Browse Movies: View a list of currently available movies with details such as title, genre, duration, and rating.
- Screening Times: Check screening times for each movie at different cinema locations.
- Ticket Booking: Book tickets for selected movies and screening times.
- Booking Management: View and manage your bookings, including cancellations.
- User Authentication: Register and log in to manage your bookings securely.

## Technologies Used
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Thymeleaf
- Maven
- Bootstrap
- Docker
- JUnit and Mockito for testing

## Project requirements (in polish till I translate it)
> [!TIP]
> Formatting explanation:
> - **Quotes**: Literal teacher feedback
> - **Parentheses**: My own comments
> - **✔️**: Completed
> - **❌**: Not completed
> - **⚠️**: Partially completed

### Model danych, Repository, i JdbcTemplate

> Konfiguracja encji JPA, relacje, repozytoria, migracje bazy danych oraz bezpośrednie zapytania SQL

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Encje JPA z relacjami | Encje z @Entity, @Id, @GeneratedValue, @Column. Relacje @OneToMany/@ManyToOne z @JoinColumn, @ManyToMany z @JoinTable | 4% | 3,4 | ✔️ | 70% | 2,38 | "@EntityListeners?" |
| JpaRepository i custom queries | Repository rozszerzające JpaRepository<T, ID> z custom query methods (findBy... lub @Query). Pageable support (Page<T>) dla paginacji. Konfiguracja w application.yml (datasource, jpa, hibernate). Używanie plików .sql do inicjalizacji bazy/danych | 5% | 4,25 | ✔️ | 80% | 3,4 | "skromne" |
| JdbcTemplate - zapytania SQL | JdbcTemplate jako dependency. Zapytania SELECT z query() i RowMapper. Operacje INSERT/UPDATE/DELETE z update(). Użycie w serwisie lub dedykowanym DAO | 5% | 4,25 | ✔️ | 100% | 4,25 |  |

### REST API

> Pełna implementacja REST API z prawidłowymi kodami HTTP i dokumentacją

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| CRUD Endpoints | @RestController z @RequestMapping("/api/v1/..."). GET (lista z paginacją, single), POST (tworzenie), PUT (aktualizacja), DELETE. @PathVariable, @RequestBody, @RequestParam. ResponseEntity z kodami HTTP (200, 201, 204, 400, 404) | 7% | 5,95 | ✔️ | 100% | 5,95 |  |
| Dokumentacja API - Swagger | Springdoc OpenAPI i Swagger UI dostępny pod /swagger-ui/index.html (lub /swagger-ui). Poprawna dokumentacja endpointów | 5% | 4,25 | ✔️ | 100% | 4,25 |  |

### Warstwa aplikacji - Business Logic
> Service, walidacja, obsługa błędów, Thymeleaf i operacje na plikach

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Service i Transactional | @Service, @Transactional(readOnly = true/false). Dependency injection przez konstruktor. Mapowanie Entity ↔ DTO. Własne wyjątki (ResourceNotFoundException). @RestControllerAdvice + @ExceptionHandler dla globalnej obsługi błędów. | 3% | 2,55 | ✔️ | 100% | 2,55 |  |
| Walidacja danych | Walidacja w DTO: @NotNull, @NotBlank, @Size, @Email, @Valid. Bean Validation na poziomie kontrolera i serwisu. Spójne komunikaty błędów | 3% | 2,55 | ✔️ | 50% | 1,275 | "brak custom validtora" |
| Thymeleaf - widoki | @Controller z Model i @ModelAttribute. Widoki z th:each (listy), th:object/th:field (formularze). Wyświetlanie błędów (th:errors). Layout z fragmentami (th:fragment, th:replace). Bootstrap 5 styling | 3% | 2,55 | ✔️ | 100% | 2,55 |  |
| Operacje na plikach i eksport | Upload plików (MultipartFile, enctype="multipart/form-data"). Zapis na dysk (Files.copy). Download plików (Resource, ResponseEntity<byte[]>). Export do CSV/PDF | 3% | 2,55 | ✔️ | 100% | 2,55 |  |

### Spring Security
> Autentykacja, autoryzacja i bezpieczeństwo aplikacji

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Konfiguracja Security | SecurityFilterChain jako @Bean. authorizeHttpRequests z requestMatchers do kontroli dostępu. formLogin z konfiguracją. BCryptPasswordEncoder dla szyfrowania haseł. UserDetailsService z loadUserByUsername | 4% | 3,4 | ✔️ | 100% | 3,4 |  |

### Testowanie
> Testy jednostkowe i integracyjne

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Testy warstwy danych | @DataJpaTest dla testowania repository. Min. 10 testów CRUD. RowMapper i custom queries | 2% | 1,7 | ✔️ | 25% | 0,425 | "mało testów" |
| Testy serwisów | @Mock, @InjectMocks, Mockito: when().thenReturn(), verify(). Unit testy dla logiki biznesowej | 3% | 2,55 | ✔️ | 30% | 1,785 | "mało testów i brak verify" |
| Testy REST i integracyjne | @WebMvcTest lub @SpringBootTest dla controllerów REST. MockMvc: perform(), andExpect(). @WithMockUser dla testów Security. Min. 5 scenariuszy biznesowych | 2% | 1,7 | ✔️ | 50% | 0,85 | "mało testów" |
| Coverage i jakość | JaCoCo - coverage 70%+. Raport z pokryciem kodu. Testy obejmują Happy Path i Error Cases | 1% | 0,85 | ✔️ | 100% | 0,85 |  |

### Wymagania projektowe
> Funkcjonalności specyficzne dla projektu kina

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Strona Powitalna i Repertuar | Ekran startowy z banerem reklamowym (karuzela lub statyczny obraz). Moduł repertuaru umożliwiający wybór daty (kalendarz/zakładki dni). Wyświetlanie listy filmów dostępnych w wybranym dniu wraz z godzinami seansów. Kliknięcie w godzinę przenosi do rezerwacji | 4% | 3,4 | ✔️ | 100% | 3,4 |  |
| Szczegóły Filmu (CMS) | Podstrona filmu zawierająca: Tytuł, Gatunek, Ograniczenie wiekowe, Reżysera, Obsadę. Integracja z odtwarzaczem wideo (np. YouTube embed) dla zwiastuna. Galeria zdjęć (min. 3 zdjęcia) | 6% | 5,1 | ✔️ | 100% | 5,1 |  |
| Wizualna Rezerwacja Miejsc | Interaktywny widok sali kinowej (siatka miejsc). Rozróżnienie graficzne miejsc: Wolne, Zajęte (sprzedane), Wybrane przez użytkownika. Logika frontendowa (JS) i backendowa blokująca wybór zajętych miejsc | 8% | 6,8 | ✔️ | 100% | 6,8 |  |
| Wybór Biletów i Koszyk | Po wybraniu miejsca użytkownik wybiera typ biletu (Normalny/Ulgowy/Rodzinny), co wpływa na cenę. Koszyk sesyjny umożliwiający podgląd wybranych miejsc, zmianę typu biletu lub usunięcie miejsca z rezerwacji przed płatnością | 6% | 5,1 | ✔️ | 100% | 5,1 |  |
| Finalizacja Zamówienia | Formularz podsumowania. Symulacja płatności. Po zatwierdzeniu zmiana statusu miejsc w bazie na stałe 'ZAJĘTE' i wygenerowanie unikalnego numeru rezerwacji (Ticket ID) wyświetlonego użytkownikowi | 6% | 5,1 | ✔️ | 100% | 5,1 |  |
| Admin: Zarządzanie Bazą Filmów | CRUD dla encji Film. Możliwość dodawania nowych tytułów wraz z opisami i linkami do multimediów | 6% | 5,1 | ✔️ | 100% | 5,1 |  |
| Admin: Zarządzanie Seansami | Kluczowa funkcjonalność: Tworzenie seansu poprzez powiązanie Filmu, Sali i Daty/Godziny. Walidacja, czy seanse w tej samej sali nie nakładają się na siebie w czasie. | 8% | 6,8 | ✔️ | 100% | 6,8 |  |
| Admin: Statystyki Sprzedaży | Raportowanie sprzedaży biletów. Tabela lub wykres prezentujący przychód oraz liczbę sprzedanych biletów z podziałem na dni miesiąca (grupowanie danych) | 6% | 5,1 | ✔️ | 100% | 5,1 |  |

### Wymagania dodatkowe
> Funkcjonalności wykraczające poza podstawowy zakres projektu

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent (max z punktów) | Otrzymane punkty | Uwaga prowadzącego |
|---------|------|:-------:|:------:|:--------------:|:----------------:|:----------------:|--------------------|
| Logowanie zdarzeń (SLF4J): | Zastosowanie logowania (poziomy INFO, ERROR, DEBUG) w kluczowych momentach procesów biznesowych (wejście do metody, wystąpienie błędu) | 2% | 1,7 | ✔️ | 0% | 0 | "..." (nie rozumiem czemu) |
| Konteneryzacja (Docker & Compose) | Przygotowanie pliku Dockerfile dla aplikacji oraz docker-compose.yml, który stawia aplikację oraz bazę danych. Umożliwienie uruchomienia całego środowiska jedną komendą docker-compose up | 4% | 3,4 | ⚠️ | 100% | 3,4 | (ma problemy ze startem) |
| Testy Architektury (ArchUnit) | Implementacja testu automatycznego (JUnit + ArchUnit), który pilnuje reguł architektonicznych z Grupy 1 i 2 (np. "Klasy z pakietu Controller nie mogą zależeć od klas z pakietu Entity" lub "Klasy Service muszą być w pakiecie .service") | 4% | 3,4 | ❌ | 0% | 0 |  |
| E2E Test (Selenium): | Prosty test automatyczny uruchamiający przeglądarkę i sprawdzający, czy aplikacja (np. strona logowania lub Swagger) poprawnie się ładuje (tytuł strony, obecność elementu) | 5% | 4,25 | ❌ | 0% | 0% |  |
| Inne rzeczy nie pokazywane na zajęciach | WebSocket/STOMP real-time messaging, session-scoped cart, seat locks with TTL, username-aware lock resolution, broadcast integration with seat status updates | 5% | 4,25 | ✔️ | 100% | 4,25 | (WebSocket wystarczył) |

## Informations

### Swagger UI
The Swagger UI for the REST API documentation is available at:
```bash
http://localhost:8080/swagger-ui/index.html
```

### PGAdmin
If you are using Docker Compose, PGAdmin is available at:
```bash
http://localhost:5050
```

credentials:
```bash
PGADMIN_DEFAULT_EMAIL: admin@admin.com
PGADMIN_DEFAULT_PASSWORD: admin
```

### Jacoco Code Coverage Report
After running tests, the Jacoco code coverage report can be found at:
```bash
mvn verify
```
Then open the report located at:
```bash
https://localhost:5050/target/site/jacoco/index.html
```

![code-coverage at 79%](markdown/coverage/coverage-report.png)


## Screenshots

### Register Page
![register page](markdown/pages/register/register-page.png)

### Login Page
![login page](markdown/pages/login/login-page.png)

Notification about successful registration
![registration successful](markdown/pages/register/registration-successful.png)

### Home Page
![home page](markdown/pages/home/home-page.png)
![home page2](markdown/pages/home/home-page2.png)
![home page3](markdown/pages/home/home-page3.png)

### Movie Details Page
![movie details page](markdown/pages/movie-details/movie-details-page.png)
![movie details page2](markdown/pages/movie-details/movie-details-page2.png)

### Movies Page
![movies page](markdown/pages/movies/movies-page.png)
![movies page](markdown/pages/movies/movies-page2.png)
![movies page](markdown/pages/movies/movies-page3.png)

### Screenings Page
![screenings page](markdown/pages/screenings/screenings-page.png)

### Seat Selection Page
![seat selection page](markdown/pages/seat-selection/seat-selection-page.png)

Seat held view from a different user:
![seat selection page2](markdown/pages/seat-selection/seat-selection-page-held.png)

### Cart Page
![cart page](markdown/pages/cart/cart-page.png)

Empty cart view:
![cart page2](markdown/pages/cart/cart-page-empty.png)

### Checkout Page
![checkout page](markdown/pages/checkout/checkout-page.png)
![checkout page](markdown/pages/checkout/checkout-page2.png)

QR code download:
![checkout page](markdown/pages/checkout/checkout-page-qr.png)

### Bookings Page
![bookings page](markdown/pages/bookings/bookings-page.png)

details of a booking:
![bookings page details](markdown/pages/bookings/bookings-page-details.png)

### Profile Page
![profile page](markdown/pages/profile/profile-page.png)

notification about functionalities:
![profile page notification](markdown/pages/profile/profile-page-notification.png)

### Contact Us Page
![contact us page](markdown/pages/contact-us/contact-us-page.png)

### About Us Page
![about us page](markdown/pages/about-us/about-us-page.png)

### Admin Dashboard Page
![admin dashboard page](markdown/pages/admin-dashboard/admin-dashboard-page.png)

### Admin Manage Movies Page
![manage movies page](markdown/pages/admin-dashboard/admin-manage-movies-page.png)

Adding a new movie:
![add movie page](markdown/pages/admin-dashboard/admin-manage-movies-add-movie.png)

New movie gets added
![movie added notification](markdown/pages/admin-dashboard/admin-manage-movies-refreshed.png)

Editing a movie:
![edit movie page](markdown/pages/admin-dashboard/admin-manage-movies-edit-movie.png)

Adding movie stills:
![add movie stills](markdown/pages/admin-dashboard/admin-manage-movies-gallery.png)

Attaching movie stills:
![movie stills added notification](markdown/pages/admin-dashboard/admin-manage-movies-gallery2.png)

Movie stills displayed:
![movie stills displayed](markdown/pages/admin-dashboard/admin-manage-movies-gallery3.png)

### Admin Manage Screenings Page
![manage screenings page](markdown/pages/admin-dashboard/admin-manage-screenings-page.png)

Adding a new screening:
![add screening page](markdown/pages/admin-dashboard/admin-manage-screenings-add-screening.png)

Editing a screening:
![edit screening page](markdown/pages/admin-dashboard/admin-manage-screenings-edit-screening.png)

### Admin Sales Statistics Page
![sales statistics page](markdown/pages/admin-dashboard/admin-sales-statistics-page.png)

### Admin Manage Bookings Page
![manage bookings page](markdown/pages/admin-dashboard/admin-manage-bookings-page.png)

---


[English version 🇬🇧](#description) 

# Opis

> [!WARNING]  
> Wersja po polsku w trakcie tłumaczenia. Proszę o cierpliwość.