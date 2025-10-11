# ezLDAP - easy read access to LDAP

**ezLDAP** enables easy read access to the internal LDAP directory of the City of Munich (LHM) by providing a custom tailored HTTP-based API.

## Run

To function properly, ezLDAP requires a directory schema with LHM customized attributes ([see example schema data](lib-core/src/test/resources/ldap)). To spin up such an environment, you can use the [Docker Compose file provided](docker-compose.yml):

```bash
# using Docker
docker compose up
# using Podman
podman compose up
```

This spins up a openLDAP server with LHM schema extensions and some testdata on port `8389` and phpLDAPadmin on <http://localhost:8090>. You can use the [default admin credentials of osixia/openldap](https://github.com/osixia/docker-openldap) (User-DN `cn=admin,dc=example,dc=org`, Password `admin`) to connect.

```bash
cd microservice
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dezldap.ldap.url=ldap://localhost:8389 -Dezldap.ldap.user-dn=cn=admin,dc=example,dc=org -Dezldap.ldap.password=admin -Dezldap.ldap.user-search-base=o=users,dc=example,dc=org -Dezldap.ldap.ou-search-base=o=oubase,dc=example,dc=org"
```

## API Documentation

The API of ezLDAP is documented via OpenAPI v3 and can be accessed via Swagger-UI: <http://localhost:8080/swagger-ui/index.html>

### Server cache

Server caching is disabled by default, but can be activated with property `ezldap.cache.enabled=true`.

When enabled, the LDAP queries are cached on the JVM heap, off-heap and on disk.

The folder for disk caching can be configured by adjusting `ezldap.cache.disk.dir`.

To further customize the caching, you can also store your own, adapted [`ezldap-ehcache.xml`](lib-spring\src\main\resources\ezldap-ehcache.xml) on the class path.

## Integrate

**ezLDAP** can also be embedded in existing applications using the `lib-core` or `lib-spring` modules.

### lib-core

Provides direct access to the LDAP via `LdapService`.

Maven Dependency:

```xml
<dependency>
  <groupId>de.muenchen.oss.ezldap</groupId>
  <artifactId>ezLDAP-lib-core</artifactId>
  <version>latest-version</version><!-- see Maven Central -->
</dependency>
```

Usage:

```java
LdapService ldapService = new LdapService("ldaps://ldap.example.org:636", "<<ldap-user-dn>>", "<<ldap-password>>",
  "ou=users,dc=example,dc=org",           // User Search Base
  "ou=organisation,dc=example,dc=org"     // OU Search Base
);

// start using
Optional<LdapUserDTO> = ldapService.getPersonWithUID("erika.musterfrau");
```

#### lib-spring

Activates the REST API controller endpoint `/v1/ldap` in a Spring Boot application via a Spring `AutoConfiguration`.

Maven Dependency:

```xml
<dependency>
  <groupId>de.muenchen.oss.ezldap</groupId>
  <artifactId>ezLDAP-lib-spring</artifactId>
  <version>latest-version</version><!-- see Maven Central -->
</dependency>
```

Usage:

```java
@SpringBootApplication
@EnableEzLDAP // enables '/v1/ldap' REST endpoints
public class MySpringBootApplication {
  // [...]
}
```

required properties (e.g. in `application.properties`):

```ini
ezldap.ldap.url=ldaps://ldap.example.org:636
ezldap.ldap.user-dn=<<ldap-user-dn>>
ezldap.ldap.password=<<ldap-password>>
ezldap.ldap.user-search-base="ou=users,dc=example,dc=org"
ezldap.ldap.ou-search-base="ou=organisation,dc=example,dc=org"
ezldap.cache.enabled=true
ezldap.cache.disk.dir=./target/cache
```

**Note**: When integrating into an existing Spring Boot application, the authentication configuration must be done via the Spring Security configuration of the application! For an example, see [SecurityConfiguration.java of the microservice](microservice/src/main/java/de/muenchen/oss/ezldap/config/SecurityConfiguration.java).

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Open an issue with the tag "enhancement"
2. Fork the Project
3. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

## License

Distributed under the MIT License. See [LICENSE](LICENSE) file for more information.

## Contact

it@M - <opensource@muenchen.de>
