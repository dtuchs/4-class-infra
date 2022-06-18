Интеграция с БД (Postgres , Mysql, Vertica), Расширения для работы с http (Retrofit2), gRPC сервисами. Расширения для работы с моками (wiremock, mock-server)
=================

[retrofit](https://github.com/square/retrofit) 
[spring-jdbc](https://github.com/spring-projects/spring-framework/tree/master/spring-jdbc) 
[hibernate](https://github.com/hibernate/hibernate-orm) 
[grpc](https://github.com/grpc/grpc-java) 
[wiremock](https://github.com/wiremock/wiremock) 
[mock-server](https://github.com/mock-server/mockserver) 
[allure-java](https://github.com/allure-framework/allure-java) 

## Доклад нв Heisenbug 2022 Spring offline
### Описание доклада
Опыт «разработки и поддержки QA-фреймворка» в резюме автоматизатора — скорее недостаток, чем преимущество. В докладе представлен авторский взгляд на QA-фреймворки. Вы узнаете, почему «идеальный» фреймворк должен иметь около 4-х публичных классов.
Рассматриваются практические вопросы по работе с базами данных (JPA/Hibernate), с REST и gRPC. Обсуждается, насколько справедлива мысль: «У нас чистый Selenium: без своего фреймворка не обойтись!»

Спойлер: Selenide или Playwright не предлагаются.

### Видел доклада
    (comig soon)

### Прещентация
    (comig soon)

## Решаемые задачи
* Задача #1 - Минимальный и взаимно рабочий набор зависимостей для работы каждого фреймворка в одном месте
* Задача #2 - Для всех фреймворков предоставить публичный класс Builder позволяющий создавать инстансы обьектов с минимальным и достаточным набором параметров
* Задача #3 - Добавить логгирование в allure всех реквестов и респонсов 
* Задача #4 - Для EntityManagerFactory добавить декоратор, дающий возможность создания ThreadLocal EntityManager
* Задача #4 - Для Retrofit Response добавить декораторт, дающий простой API описания проверок внутри тестов


## retrofit-all
* Встроенная поддержка маршалинга / анмаршаллинга
* Декоратор для Reponse позволяющий описывать любые проверки через assertJ / логгировать их в Allure
* Логгирование в консоль / Allure
* Класс ApiServiceBuilder для конфигурации и создания ядра retrofit - ServiceFactory 
* Класс CommonExecutor для выполнения запросов 
* Класс AllureExecutor для выполнения запросов внутри шага Allure (для тестов рекомендуется по-умолчанию)
* [Примеры использования retrofit в интернете](https://www.baeldung.com/retrofit)

## Использование
```java
public interface TestService {
  @PUT("/adapter")
  Call<TestData> typeAdapter(@Body TestData testData);
}

class ApiServiceFactoryTest {
  static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());
  Executor executor = new AllureExecutor();

  @BeforeAll
  static void setUp() {
    wireMockServer.start();
    setUpStubs();
  }

  @Test
  void apiServiceFactoryWithTypeAdapterTest() throws Exception {
    ApiServiceFactory apiServiceFactory = new ApiServiceFactory
            .ApiServiceBuilder("http://localhost:" + wireMockServer.port())
            .withTypeAdapter(Date.class, new DateSerializer())
            .build();

    Call<TestData> call = apiServiceFactory.getService(TestService.class)
            .typeAdapter(new TestData(new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2017")));

    final Date expectedDate = new SimpleDateFormat("dd/MM/yyyy").parse("20/11/2010");
    
    executor.toVerifiableResponse(call)
            .checkCode(201)
            .check(response -> assertThat(response.body()).isNotNull())
            .check(response -> assertThat(response.body().getDateTime()).isEqualTo(expectedDate));
  }
}
```

### Внешние зависимости
* retrofit - ядро retrofit
```xml
 <artifactId>retrofit</artifactId>
```
* converter-gson - сериализация / десериализация запросов и ответов
```xml
<artifactId>converter-gson</artifactId>
```
* converter-jackson - сериализация / десериализация запросов и ответов
```xml
 <artifactId>converter-jackson</artifactId>
```
* logging-interceptor - логгирование запросов и ответов в консоль
```xml
 <artifactId>logging-interceptor</artifactId>
```
* allure-okhttp3 - логгирование запросов и ответов в Allure
```xml
 <artifactId>allure-okhttp3</artifactId>
```
* allure-assertj - логгирование проверок assertj в Allure
```xml
 <artifactId>allure-assertj</artifactId>
```
* assertj-core - ядро assertj
```xml
 <artifactId>assertj-core</artifactId>
```
* jsr305 - аннотации javax.annotation
```xml
 <artifactId>jsr305</artifactId>
```

## grpc-all
* Логгирование в консоль / Allure
* Класс GrpcStubBuilder для конфигурации и создания обьекта, отвечающего за запросы gRPC (Stub)
* Класс ConsoleInterceptor реализация логгирования в консоль
* Если GrpcStubBuilder.build() будет вызван несколько раз для одних и тех же параметров подключения, будет возвращен один и тот же instance Stub
* [Примеры использования gRPC в интернете](https://www.baeldung.com/grpc-introduction)

## Использование
```java
class GrpcStubBuilderTest {

  @Test
  void testStub() {
    TestServiceGrpc.TestServiceBlockingStub stub = new GrpcStubBuilder().forStub(TestServiceGrpc.TestServiceBlockingStub.class)
            .withHost("localhost")
            .withPort(GrpcMock.getGlobalPort())
            .build();

    Assertions.assertDoesNotThrow(() ->
            stub.calculate(Request.newBuilder().setTopic("topic").build())
    );
  }
}
```

### Внешние зависимости
* protobuf-java - работа с .proto структурами
```xml
 <artifactId>protobuf-java</artifactId>
```
* grpc-all - общая зависимость на все библиотеки gRPC
```xml
 <artifactId>grpc-all</artifactId>
```
* javax.annotation-api - поддержка аннотаций в generated классах
```xml
 <artifactId>javax.annotation-api</artifactId>
```
* allure-attachments - работа с вложениями Allure
```xml
 <artifactId>allure-attachments</artifactId>
```
* allure-assertj - логгирование проверок assertj в Allure
```xml
 <artifactId>allure-assertj</artifactId>
```
* assertj-core - ядро assertj
```xml
 <artifactId>assertj-core</artifactId>
```

## wiremock-all
* Логгирование в  Allure (вызов verifyThat(), register(), find(), getServeEvents() и хранящиеся в памяти запросы / ответы)
* Класс AWiremock - расширение над стандартным классом Wiremock
* [Документация к wiremock](http://wiremock.org/docs/)
* [Документация к grpc-wiremock](https://github.com/Adven27/grpc-wiremock)

## Использование
Запуск http мок-сервера в докере
```dockerfile
  adp-mock:
    image: rodolpheche/wiremock:latest
    container_name: test-mock
    ports:
      - "7777:8080"
      qa_env:
```
Запуск grpc мок-сервера в докере
```dockerfile
  mcalc_mock:
    image: adven27/grpc-wiremock
    container_name: test-mock
    ports:
      - "18080:50000" # grpc
      - "18088:8888" # http
    volumes:
      - ./volumes/proto/mcalc:/proto
    networks:
      qa_env:
```

```java
public class Test {

  private static  WireMock mock = new AWireMock(CFG.getMockUrl(), CFG.getMockPort())
          .saveServeEvents(true)
          .saveStubMappings(true);
  
  @Test
  void test() {
    mock.register(post("/TestService/testMethod")
            .withRequestBody(matchingJsonPath("..."))
            .willReturn(
                    aResponse()
                            .withStatus(OK_200.code())
                            .withBody(new ResponseEntity().withResult("1").toString()
                            )));

    // do smth
  }
}
```

### Внешние зависимости
* wiremock-jre8 - java клиент для взаимодействия с удаленным мок-сервером
```xml
 <artifactId>wiremock-jre8</artifactId>
```
* allure-attachments - работа с вложениями Allure
```xml
 <artifactId>allure-attachments</artifactId>
```

## mock-server-all
* Логгирование в  Allure (вызов verify() и хранящиеся в памяти запросы / ответы)
* Класс AllureMockClient - расширение над стандартным классом MockServerClient
* [Документация к mock-server](https://www.mock-server.com/#what-is-mockserver)

## Использование
Запуск мок-сервера в докере
```dockerfile
  adp-mock:
    image: mockserver/mockserver:mockserver-5.11.2
    container_name: test-mock
    ports:
      - "7777:8080"
    command: "-logLevel DEBUG -serverPort 8080"
    networks:
      qa-net:
```

```java
public class Test {

  private static MockServerClient mock = new AllureMockClient(CFG.getAdpMockHost(), CFG.getAdpMockPort())
          .enableAllureLog();

  @BeforeEach
  void beforeEach() {
    mock.reset();
    mock.when(request(), Times.unlimited(), TimeToLive.unlimited(), -10)
            .respond(response().withStatusCode(OK_200.code())); // default response with lowest priority
  }

  @Test
  void test() {
    sendRequest(1, 2);
    mock.verify(
            request()
                    .withPath("api/v1/test/1048692")
                    .withMethod("GET"),
            request()
                    .withPath("api/v1/test/1048692/duplicate")
                    .withMethod("POST")
                    .withBody("{\"body\": \"ok\"}")
    );
  }
}
```

### Внешние зависимости
* mockserver-client-java - java клиент для взаимодействия с удаленным мок-сервером
```xml
 <artifactId>mockserver-client-java</artifactId>
```
* allure-attachments - работа с вложениями Allure
```xml
 <artifactId>allure-attachments</artifactId>
```

## spring-jdbc-all
* Встроенные Allure attachments для SQL statement и query. Настройка - через файл `spy.properties`.
* Класс DSBuilder возвращает instance P6DataSource для заданных параметров подключения
* Если DSBuilder.build() будет вызван несколько раз для одних и тех же параметров подключения, будет возвращен один и тот же instance DataSource
* [Примеры использования JdbcTemplate в интернете](https://www.baeldung.com/spring-jdbc-jdbctemplate)

## Использование
```java
public class Test {

  @Test
  void dsBuilderTest() {
    final int testId = 2;
    final String testName = "4 cheeses";

    DataSource dataSource = new DSBuilder()
            .h2()
            .withJdbcUrl("jdbc:h2:mem:testdb")
            .withUsername("")
            .withPassword("")
            .build();

    JdbcTemplate template = new JdbcTemplate(dataSource);
    final int count = template.update("INSERT INTO PIZZA(id, name) VALUES(?,?)",
            testId,
            testName
    );

    assertEquals(1, count);
    assertEquals(testName, template.queryForObject("SELECT name from PIZZA where id = ?", String.class, testId));
  }
}
```

### Внешние зависимости
* HikariCP - Используется для создания DataSource
```xml
 <artifactId>HikariCP</artifactId>
```
* P6DataSource - обертка над DataSource, предоставляет доступ к отправляемым SQL statement и query
```xml
 <artifactId>p6spy</artifactId>
```
* AttachmentProcessor - форматирование attach для Allure 
```xml
 <artifactId>allure-attachments</artifactId>
```
* SqlFormatter - форматирование SQL запросов для Allure attachment
```xml
 <artifactId>sql-formatter</artifactId>
```
* mysql-connector-java - jdbc драйвер mysql
```xml
 <artifactId>mysql-connector-java</artifactId>
```
* postgresql - jdbc драйвер postgres
```xml
 <artifactId>postgresql</artifactId>
```
* vertica - jdbc драйвер vertica
```xml
 <artifactId>vertica-jdbc</artifactId>
```
* spring-jdbc - ядро spring-jdbc
```xml
 <artifactId>spring-jdbc</artifactId>
```

## orm-all
* Класс JpaService реализующий транзакционность для работы с EntityManager
* Класс EmfBuilder возвращает декоратор над EntityManagerFactory для заданных параметров подключения, который предоставляет создание и хранение Thread-local EntityManager. 
* Если EmfBuilder.build() будет вызван несколько раз для одних и тех же параметров подключения, будет возвращен один и тот же instance EntityManagerFactory / EmfThreadLocal
* [Примеры использования EntityManager в интернете](https://www.baeldung.com/hibernate-entitymanager)

## Использование
```java
public class PizzaService extends JpaService {
    
  public PizzaService() {
    super(new EmfBuilder()
            .h2()
            .jdbcUrl("jdbc:h2:mem:EmfBuilderTest")
            .username("")
            .password("")
            .persistenceUnitName("test")
            .build()
            .createEntityManager());
  }

  public Pizza get(int id) {
    return em.find(Pizza.class, id);
  }

  public void save(Pizza pizza) {
    persist(pizza);
  }

  public Pizza getPizzaByName(String name) {
    return em.createQuery(
                    "select a from Pizza a where a.name=:name",
                    Pizza.class)
            .setParameter("name", name)
            .getSingleResult();
  }

  public void updatePizzaName(String oldName, String newName) {
    Pizza p = getPizzaByName(oldName);
    p.setName(newName);
    merge(p);
  }
}
```
Для работы с Hibernate / JPA в проекте должен быть файл persistence.xml в директории resources/META-INF
```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd"
             version="2.1">
  <persistence-unit name="test" transaction-type="RESOURCE_LOCAL">
    <class>com.dtuchs.libs.orm.service.Pizza</class>
  </persistence-unit>
</persistence>
```

### Внешние зависимости
* HikariCP - Используется для создания DataSource
```xml
 <artifactId>HikariCP</artifactId>
```
* mysql-connector-java - jdbc драйвер mysql
```xml
 <artifactId>mysql-connector-java</artifactId>
```
* postgresql - jdbc драйвер postgres
```xml
 <artifactId>postgresql</artifactId>
```
* hibernate-core - ядро Hibernate
```xml
 <artifactId>hibernate-core</artifactId>
```
* hibernate-hikaricp - интеграция Hibernate и HikariCP
```xml
 <artifactId>hibernate-hikaricp</artifactId>
```
* hibernate-types-52 - open source адаптер для типов json/jsonb, т.к. эти типы не поддерживаются "из коробки"
```xml
 <artifactId>hibernate-types-52</artifactId>
```

## selenium-all
Максимально легковесная обертка над Selenium4, позволяющая писать тесты не думая об ожиданиях
* Класс SmartElement - декоратор над WebElement реализующий ожидания.
* Класс SmartElementList - декоратор над List<WebElement> реализующий ожидания для коллекций
* Класс SmartElementLocator - биндинг элементов на страницы с объектами SmartElement при первом образении к ним. Так же реализует ожидания
* Класс WebDriverContainer - хранение и открытие / закрытие WebDriver и привязка его к потоку выполнения 
* Класс WebDriverFactory - инстацирование WebDriver
* Класс Core - статические методы для описания локаторов элемента и рабоыт с навигацией WebDriver
* Класс SmartElementMatcher - позволяет описывать проверки с ожиданиями для SmartElement.
* Класс SmartElementListMatcher - позволяет описывать проверки с ожиданиями для SmartElementList.
* Класс Config - конфигурация таймаутов, браузера и т.д.

## Использование
```java
class WebTest {
    @Test
    void webTest() {
        Core.navigate("https://github.com/dtuchs");
        SmartElementListMatcher
                .assertThat(Core.locateAll("li.flex-content-stretch"))
                .containsTextInAnyElement("heisenbug-2021-piter");
    }
}
```

### Внешние зависимости
* Selenium - Ядро Selenium
```xml
 <artifactId>selenium-java</artifactId>
```
* WebDriverManager - позволяет автоматически скачивать бинариники WebDriver
```xml
 <artifactId>webdrivermanager</artifactId>
```
* jsr305 - аннотации javax.annotation
```xml
 <artifactId>jsr305</artifactId>
```