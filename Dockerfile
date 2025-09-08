# -------------------- Build stage --------------------
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /src

# 의존성 캐시
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# 소스 복사 후 빌드
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# -------------------- Runtime stage --------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 산출물 복사
COPY --from=build /src/target/*.jar /app/app.jar

# 무료/저사양 환경 메모리 가드
ENV JAVA_OPTS="-Xms128m -Xmx384m"

# 문서용
EXPOSE 8080

# 기본은 h2 프로필로; 외부에서 SPRING_PROFILES_ACTIVE로 덮어쓸 수 있음
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-h2} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
