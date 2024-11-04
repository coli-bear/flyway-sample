# Flyway 적용 샘플 

## 1. 프로젝트 설정 

먼저 스프링 프로젝트를 생성한다. Spring Initializr 를 사용하여 프로젝트를 생성하게 되는데 이때 flyway 를 추가한다.

- `build.gradle` 파일에 `flyway-core` 의존성을 추가한다.

```groovy
dependencies {
    // Spring Boot 프로젝트 의존성 추가
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // Flyway 의존성 추가
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql' 
    
    // mariadb jdbc client 의존성 추가
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
    
    compileOnly 'org.projectlombok:lombok'
}
```

## 2. datasource 설정 

먼저 datasource 를 설정한 후 `flyway` 설정을 추가한다. 아래와 같이 `application.properties` 파일에서 `flyway` 설정을 추가하기 위해 위에서 `flyway-mysql` 의존성을 추가한 것이다.

> 주의 : flyway-mysql 을 application.yaml 에서 사용하기위해 의존성을 추가한 것이라고 했지만 편의상이고 실제로는 mysql(mariadb) 와 연동하기 위해서 추가한 것이다. 
> 해당 의존성은 mysql 8 이상의 버전 사용을 위해 추가한 것이고 부수적인 효과로 설정이 쉽다는 것임을 알아두자.


```properties
spring.application.name=flyway
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3307/flyway?characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=flyway
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:database/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0.0.1
spring.flyway.ignore-migration-patterns=*:Ignored
```

위에서 설정한 `flyway` 설정은 다음과 같다.

- `spring.flyway.enabled` : flyway 를 활성화 할지 여부를 설정한다.
- `spring.flyway.locations` : 마이그레이션 파일이 위치할 디렉토리를 설정한다.
- `spring.flyway.baseline-on-migrate` : 데이터페이스가 초기 상태가 아닐 때 자동으로 베이스라인을 생성하도록 설정한다.
- `spring.flyway.baseline-version` : 베이스라인 버전을 설정한다.
- `spring.flyway.ignore-migration-patterns` : 마이그레이션 파일을 무시할 패턴을 설정한다. ([Ignore Migration Patterns](https://documentation.red-gate.com/flyway/flyway-cli-and-api/configuration/parameters/flyway/ignore-migration-patterns))

### Baseline 에 대한 간단한 정리 - 동작 방식

이 옵션을 활성화하면 기존에 데이터가 있거나, flyway 관리 이전에 수동으로 생성된 데이터베이스에 대해서도 마이그레이션을 쉽게 적용할 수 있다. 

- 기존 데이터 베이스에 flyway 적용 : `baseline-on-migrate=true` 인 경우 flyway 가 데이터베이스에 기존 데이터나 스키마가 존재하는 경우 먼저 베이스라인을 자동으로 생성한다. 이를 통해 기존 데이터 베이스를 flyway 의 기준점으로 삼을 수 있다 
- baseline version 설정 : `baseline-version` (default : 1) 을 기준으로 베이스라인을 생성하고 그 이후 마이그레이션만 적용한다. 따라서 이 설정은 baseline-version 옵션과 함께 사용하는 것을 고려해봐야 한다. 
- 마이그레이션 실행 시 자동 베이스 라인 : 데이터베이스가 빈 상태가 아니라고 판단되면 자동으로 베이스라인을 생성하고, 이후 마이그레이션을 진행한다. 

## 3. 마이그레이션 파일 생성

`src/main/resources/database/migration` 디렉토리를 생성하고 마이그레이션 파일을 생성한다.

이 때 파일의 생성 규칙은 다음과 같다.

- 파일명 : `V{version}__{description}.sql`
- version : 마이그레이션 파일의 버전을 나타낸다. 버전은 숫자로 표현되며, 마이그레이션 파일은 버전 순으로 실행된다.
- description : 마이그레이션 파일의 설명을 나타낸다.

**[V0.0.1__initialize_member_table.sql]**

```sql
CREATE TABLE member
(
    member_id   bigint  NOT NULL AUTO_INCREMENT COMMENT '멤버를 식별하기 위한 식별자로 고유 번호를 갖는다.',
    member_name varchar(64) NOT NULL COMMENT '멤버의 이름을 나타낸다.',
    CONSTRAINT pk_member PRIMARY KEY (member_id)
);
```

**[V0.0.2__initialize_member_data.sql]**

```sql
CREATE TABLE notification (
    notification_id bigint  NOT NULL AUTO_INCREMENT COMMENT '알림 결과를 식별하기 위한 식별자로 고유 번호를 갖는다.',
    member_id integer not null,
    message text not null,
    created_at timestamp not null,
    constraint pk_notification primary key (notification_id)
);
```

**[V0.0.3__initialize_member_data.sql]**
```sql
INSERT INTO member (member_name) VALUES ('member1');
```

> 주의 1 : 버전은 낮은 버전에서 높은 버전으로 오름차순으로 올라가야한다 그렇지 않으면 에러가 발생하게 된다. 이때 해당 버전을 무시하고 싶다고 하면 위 설정 중 `spring.flyway.ignore-migration-patterns` 옵션을 사용하면 된다.

> 주의 2 : Flyway 는 동일한 버전의 마이그레이션 파일이 존재하면 에러를 발생시킨다. 따라서 버전을 중복해서 사용하지 않도록 주의해야한다.

실행결과는 별도로 캡처하지 않았다. 

## FlywayMigrationStrategy 

`FlywayMigrationStrategy` 는 `Flyway` 빈을 사용하여 마이그레이션을 수행하는 전략을 정의한다.

```java
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationStrategyConfiguration {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
```
여기서 `repair()` 을 호출하게 되는데 이는 flyway 가 마이그레이션을 수행할 때 발생하는 에러를 해결하는데 사용된다.

예를들어 아래와 같은 상황에 사용된다.

- V0.3.3 버전의 마이그레이션 파일이 실패 
- `flyway_schema_history` 테이블에 V0.3.3 버전의 마이그레이션 정보가 실패로 표시되어 있다. 
- 이때 `repair()` 를 호출하면 `flyway_schema_history` 실패한 마이그레이션 파일을 다시 실행하고 처리 결과를 `flyway_schema_history` 에 반영한다.
