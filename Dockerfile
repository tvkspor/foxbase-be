# ---------- STAGE 1: BUILD APP ----------
FROM gradle:8.9-jdk21 AS builder

WORKDIR /app

# Copy toàn bộ project vào container
COPY . .

# Cấp quyền thực thi cho gradlew
RUN chmod +x gradlew

# Build mà không chạy test để giảm thời gian
RUN ./gradlew clean build -x test


# ---------- STAGE 2: RUNTIME ----------
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy file JAR từ giai đoạn build
COPY --from=builder /app/build/libs/*.jar app.jar

# Cấu hình port (Render tự set biến PORT)
ENV PORT=8080
EXPOSE 8080

# Lệnh khởi chạy Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
