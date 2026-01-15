# cinema-booking
A system to manage e-commerce and general information regarding movies for XYZ cinema

## Running profiles and data persistence

- The default/dev run (`mvn spring-boot:run -Dspring-boot.run.profiles=dev`) now connects to PostgreSQL and leaves existing rows intact across restarts.
- Schema/data SQL scripts still exist but are no longer executed automatically, preventing accidental drops of `movies`, `screenings`, and related tables.
- To rebuild the schema and load the bundled demo content, explicitly opt in to the `demo-data` profile:
	- `mvn spring-boot:run -Dspring-boot.run.profiles=dev,demo-data`
	- Or `SPRING_PROFILES_ACTIVE="dev,demo-data" ./start.sh`
- Keep the demo profile reserved for disposable databases; it runs `schema.sql` and `data.sql`, which drop and recreate every domain table before reseeding.
- Integration tests continue to rely on the in-memory H2 configuration from `application-test.yml`, so no additional action is required when running `mvn test`.
