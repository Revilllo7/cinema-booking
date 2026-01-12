# Wymagania wspólne
## Model danych, Repository, i JdbcTemplate

> Konfiguracja encji JPA, relacje, repozytoria, migracje bazy danych oraz bezpośrednie zapytania SQL

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------:|
| Encje JPA z relacjami | Encje z @Entity, @Id, @GeneratedValue, @Column. Relacje @OneToMany/@ManyToOne z @JoinColumn, @ManyToMany z @JoinTable | 4% | 3,4 | ✔️ | x |
| JpaRepository i custom queries | Repository rozszerzające JpaRepository<T, ID> z custom query methods (findBy... lub @Query). Pageable support (Page<T>) dla paginacji. Konfiguracja w application.yml (datasource, jpa, hibernate). Używanie plików .sql do inicjalizacji bazy/danych | 5% | 4,25 | ✔️ | x |
| JdbcTemplate - zapytania SQL | JdbcTemplate jako dependency. Zapytania SELECT z query() i RowMapper. Operacje INSERT/UPDATE/DELETE z update(). Użycie w serwisie lub dedykowanym DAO | 5% | 4,25 | ❌ | x |

## REST API

> Pełna implementacja REST API z prawidłowymi kodami HTTP i dokumentacją

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------:|
| CRUD Endpoints | @RestController z @RequestMapping("/api/v1/..."). GET (lista z paginacją, single), POST (tworzenie), PUT (aktualizacja), DELETE. @PathVariable, @RequestBody, @RequestParam. ResponseEntity z kodami HTTP (200, 201, 204, 400, 404) | 7% | 5,95 | ✔️ | x |
| Dokumentacja API - Swagger | Springdoc OpenAPI i Swagger UI dostępny pod /swagger-ui/index.html (lub /swagger-ui). Poprawna dokumentacja endpointów | 5% | 4,25 | ✔️ | x |

## Warstwa aplikacji - Business Logic
> Service, walidacja, obsługa błędów, Thymeleaf i operacje na plikach

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------:|
| Service i Transactional | @Service, @Transactional(readOnly = true/false). Dependency injection przez konstruktor. Mapowanie Entity ↔ DTO. Własne wyjątki (ResourceNotFoundException). @RestControllerAdvice + @ExceptionHandler dla globalnej obsługi błędów. | 3% | 2,55 | ✔️ | x |
| Walidacja danych | Walidacja w DTO: @NotNull, @NotBlank, @Size, @Email, @Valid. Bean Validation na poziomie kontrolera i serwisu. Spójne komunikaty błędów | 3% | 2,55 | ✔️ | x |
| Thymeleaf - widoki | @Controller z Model i @ModelAttribute. Widoki z th:each (listy), th:object/th:field (formularze). Wyświetlanie błędów (th:errors). Layout z fragmentami (th:fragment, th:replace). Bootstrap 5 styling | 3% | 2,55 | ✔️ | x |
| Operacje na plikach i eksport | Upload plików (MultipartFile, enctype="multipart/form-data"). Zapis na dysk (Files.copy). Download plików (Resource, ResponseEntity<byte[]>). Export do CSV/PDF | 3% | 2,55 | ❌ | x |

## Spring Security
> Autentykacja, autoryzacja i bezpieczeństwo aplikacji

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------
| Konfiguracja Security | SecurityFilterChain jako @Bean. authorizeHttpRequests z requestMatchers do kontroli dostępu. formLogin z konfiguracją. BCryptPasswordEncoder dla szyfrowania haseł. UserDetailsService z loadUserByUsername | 4% | 3,4 | ✔️ | x |

## Testowanie
> Testy jednostkowe i integracyjne

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------
| Testy warstwy danych | @DataJpaTest dla testowania repository. Min. 10 testów CRUD. RowMapper i custom queries | 2% | 1,7 | ✔️ | x |
| Testy serwisów | @Mock, @InjectMocks, Mockito: when().thenReturn(), verify(). Unit testy dla logiki biznesowej | 3% | 2,55 | x | x |
| Testy REST i integracyjne | @WebMvcTest lub @SpringBootTest dla controllerów REST. MockMvc: perform(), andExpect(). @WithMockUser dla testów Security. Min. 5 scenariuszy biznesowych | 2% | 1,7 | x | x |
| Coverage i jakość | JaCoCo - coverage 70%+. Raport z pokryciem kodu. Testy obejmują Happy Path i Error Cases | 1% | 0,85 | x | x |

# Wymagania projektowe
> Funkcjonalności specyficzne dla projektu kina

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------
| Strona Powitalna i Repertuar | Ekran startowy z banerem reklamowym (karuzela lub statyczny obraz). Moduł repertuaru umożliwiający wybór daty (kalendarz/zakładki dni). Wyświetlanie listy filmów dostępnych w wybranym dniu wraz z godzinami seansów. Kliknięcie w godzinę przenosi do rezerwacji | 4% | 3,4 | ❌ | x |
| Szczegóły Filmu (CMS) | Podstrona filmu zawierająca: Tytuł, Gatunek, Ograniczenie wiekowe, Reżysera, Obsadę. Integracja z odtwarzaczem wideo (np. YouTube embed) dla zwiastuna. Galeria zdjęć (min. 3 zdjęcia) | 6% | 5,1 | ❌ | x |
| Wizualna Rezerwacja Miejsc | Interaktywny widok sali kinowej (siatka miejsc). Rozróżnienie graficzne miejsc: Wolne, Zajęte (sprzedane), Wybrane przez użytkownika. Logika frontendowa (JS) i backendowa blokująca wybór zajętych miejsc | 8% | 6,8 | ❌ | x |
| Wybór Biletów i Koszyk | Po wybraniu miejsca użytkownik wybiera typ biletu (Normalny/Ulgowy/Rodzinny), co wpływa na cenę. Koszyk sesyjny umożliwiający podgląd wybranych miejsc, zmianę typu biletu lub usunięcie miejsca z rezerwacji przed płatnością | 6% | 5,1 | ❌ | x |
| Finalizacja Zamówienia | Formularz podsumowania. Symulacja płatności. Po zatwierdzeniu zmiana statusu miejsc w bazie na stałe 'ZAJĘTE' i wygenerowanie unikalnego numeru rezerwacji (Ticket ID) wyświetlonego użytkownikowi | 6% | 5,1 | ❌ | x |
| Admin: Zarządzanie Bazą Filmów | CRUD dla encji Film. Możliwość dodawania nowych tytułów wraz z opisami i linkami do multimediów | 6% | 5,1 | ✔️ | x |
| Admin: Zarządzanie Seansami | Kluczowa funkcjonalność: Tworzenie seansu poprzez powiązanie Filmu, Sali i Daty/Godziny. Walidacja, czy seanse w tej samej sali nie nakładają się na siebie w czasie. | 8% | 6,8 | ✔️ | x |
| Admin: Statystyki Sprzedaży | Raportowanie sprzedaży biletów. Tabela lub wykres prezentujący przychód oraz liczbę sprzedanych biletów z podziałem na dni miesiąca (grupowanie danych) | 6% | 5,1 | ❌ | x |

# Wymagania dodatkowe
> Funkcjonalności wykraczające poza podstawowy zakres projektu

| Element | opis | Procent | Punkty | Czy skończone? | Otrzymany procent |
|---------|------|:-------:|:------:|:--------------:|:----------------
| Logowanie zdarzeń (SLF4J): | Zastosowanie logowania (poziomy INFO, ERROR, DEBUG) w kluczowych momentach procesów biznesowych (wejście do metody, wystąpienie błędu) | 2% | 1,7 | ✔️ | x |
| Konteneryzacja (Docker & Compose) | Przygotowanie pliku Dockerfile dla aplikacji oraz docker-compose.yml, który stawia aplikację oraz bazę danych. Umożliwienie uruchomienia całego środowiska jedną komendą docker-compose up | 4% | 3,4 | ✔️ | x |
| Testy Architektury (ArchUnit) | Implementacja testu automatycznego (JUnit + ArchUnit), który pilnuje reguł architektonicznych z Grupy 1 i 2 (np. "Klasy z pakietu Controller nie mogą zależeć od klas z pakietu Entity" lub "Klasy Service muszą być w pakiecie .service") | 4% | 3,4 | ❌ | x |
| E2E Test (Selenium): | Prosty test automatyczny uruchamiający przeglądarkę i sprawdzający, czy aplikacja (np. strona logowania lub Swagger) poprawnie się ładuje (tytuł strony, obecność elementu) | 5% | 4,25 | ❌ | x |
| Inne rzeczy nie pokazywane na zajęciach | | 5% | 4,25 | ❌ | x |
