# VDT 2025 - Product Management Microservices System

## Tổng Quan

VDT 2025 Product Management System là một hệ thống quản lý sản phẩm được xây dựng dựa trên kiến trúc microservices. Hệ thống cung cấp các chức năng quản lý người dùng, sản phẩm, danh mục và file một cách hiệu quả với khả năng mở rộng cao.

### Kiến trúc tổng thể

- **Kiến trúc**: Microservices
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL
- **Cache**: Redis
- **Framework**: Spring Boot 3.2.2
- **Java Version**: 21

## Tính Năng

### User Service (Port: 8081)

- Đăng ký và đăng nhập người dùng
- Quản lý thông tin cá nhân
- Thay đổi mật khẩu
- Quản lý vai trò (Role-based access control)
- Xác thực JWT với refresh token
- Admin dashboard

### Product Service (Port: 8082)

- Quản lý sản phẩm (CRUD operations)
- Quản lý danh mục sản phẩm
- Tìm kiếm và lọc sản phẩm
- Phân trang kết quả
- Upload và quản lý thumbnail sản phẩm

### File Service (Port: 8083)

- Upload và download file
- Quản lý metadata file
- Hỗ trợ multiple file formats
- Bảo mật file access
- Storage optimization

### API Gateway (Port: 8080)

- Single entry point cho tất cả requests
- Load balancing
- Route management
- Request/Response filtering

### Discovery Service (Port: 8761)

- Service registration và discovery
- Health monitoring
- Auto-scaling support

### Common DTO

- Shared DTOs và models
- Feign Client configurations
- Common utilities và validations

## Cấu Trúc Dự Án

```
vdt_2025_microservice_and_kafka/
├── api-gateway/              # API Gateway service
│   ├── src/main/java/com/vdt2025/
│   └── src/main/resources/
├── discovery-service/        # Eureka Discovery service
│   ├── src/main/java/com/vdt2025/
│   └── src/main/resources/
├── user-service/            # User management service
│   ├── src/main/java/com/vdt2025/user_service/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access layer
│   │   ├── entity/          # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── config/         # Configuration classes
│   │   └── exception/      # Exception handling
│   └── src/main/resources/
├── product-service/         # Product management service
│   ├── src/main/java/com/vdt2025/product_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── config/
│   │   └── exception/
│   └── src/main/resources/
├── file-service/           # File storage service
│   ├── src/main/java/com/vdt2025/file_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── config/
│   │   └── exception/
│   ├── src/main/resources/
│   └── uploads/           # File storage directory
├── common-dto/            # Shared DTOs and utilities
│   └── src/main/java/com/vdt2025/common_dto/
└── redis-data/           # Redis data files
```

## Yêu Cầu Hệ Thống

### Phần mềm cần thiết

- **Java**: JDK 21 hoặc cao hơn
- **Maven**: 3.6+
- **PostgreSQL**: 12+ (3 databases riêng biệt)
- **Redis**: 6.0+
- **IDE**: IntelliJ IDEA hoặc Eclipse (khuyến nghị)

### Database Requirements

```sql
-- Tạo 3 databases riêng biệt
CREATE DATABASE "user-service";
CREATE DATABASE "product-service";
CREATE DATABASE "file-service";
```

### Cấu hình PostgreSQL

- Host: `localhost`
- Port: `5432`
- Username: `postgres`
- Password: `admin`

### Cấu hình Redis

- Host: `localhost`
- Port: `6379`

## Cài Đặt và Chạy

### 1. Chuẩn bị môi trường

```bash
# Clone repository
git clone https://github.com/ntabodoiqua/VDT2025_ProductManagement_Kafka-Micro.git
cd VDT2025_ProductManagement_Kafka-Micro

# Đảm bảo PostgreSQL và Redis đang chạy
# Tạo các databases cần thiết
```

### 2. Build common-dto module trước

```bash
cd common-dto
mvn clean install
cd ..
```

### 3. Thứ tự khởi động services

#### Bước 1: Discovery Service

```bash
cd discovery-service
mvn spring-boot:run
```

Chờ service khởi động hoàn tất trước khi chuyển bước tiếp theo.

#### Bước 2: Các Business Services (có thể chạy song song)

**Terminal 2 - User Service:**

```bash
cd user-service
mvn spring-boot:run
```

**Terminal 3 - Product Service:**

```bash
cd product-service
mvn spring-boot:run
```

**Terminal 4 - File Service:**

```bash
cd file-service
mvn spring-boot:run
```

#### Bước 3: API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### 4. Kiểm tra hệ thống

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **File Service**: http://localhost:8083

## Hướng Dẫn Sử Dụng

### Authentication Flow

1. **Đăng ký tài khoản mới:**

```bash
POST http://localhost:8080/api/user/auth/register
Content-Type: application/json

{
    "username": "user123",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
}
```

2. **Đăng nhập:**

```bash
POST http://localhost:8080/api/user/auth/login
Content-Type: application/json

{
    "username": "user123",
    "password": "password123"
}
```

3. **Sử dụng access token trong header:**

```bash
Authorization: Bearer <your-access-token>
```

### Product Management

1. **Tạo danh mục sản phẩm:**

```bash
POST http://localhost:8080/api/product/categories
Authorization: Bearer <token>
Content-Type: application/json

{
    "name": "Electronics",
    "description": "Electronic devices"
}
```

2. **Tạo sản phẩm mới:**

```bash
POST http://localhost:8080/api/product/products
Authorization: Bearer <token>
Content-Type: application/json

{
    "name": "iPhone 15",
    "description": "Latest iPhone model",
    "price": 999.99,
    "categoryId": "category-uuid",
    "stock": 100
}
```

3. **Lấy danh sách sản phẩm với pagination:**

```bash
GET http://localhost:8080/api/product/products?page=0&size=10&sort=name,asc
Authorization: Bearer <token>
```

### File Management

1. **Upload file:**

```bash
POST http://localhost:8080/api/file/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <select-your-file>
```

2. **Download file:**

```bash
GET http://localhost:8080/api/file/files/download/{fileId}
Authorization: Bearer <token>
```

## API Documentation

### Swagger UI Endpoints

Sau khi khởi động các services, bạn có thể truy cập API documentation tại:

- **User Service API**: http://localhost:8081/swagger-ui/index.html
- **Product Service API**: http://localhost:8082/swagger-ui/index.html
- **File Service API**: http://localhost:8083/swagger-ui/index.html

### API Routes via Gateway

Tất cả các API có thể được truy cập thông qua API Gateway với format:

```
http://localhost:8080/api/{service-name}/{endpoint}
```

#### User Service Routes

- **Base URL**: `http://localhost:8080/api/user/`
- **Authentication**: `/auth/login`, `/auth/register`, `/auth/refresh`
- **User Management**: `/users/`, `/users/myInfo`, `/users/{username}`
- **Admin**: `/admin/users`, `/admin/roles`

#### Product Service Routes

- **Base URL**: `http://localhost:8080/api/product/`
- **Products**: `/products/`, `/products/{id}`, `/products/search`
- **Categories**: `/categories/`, `/categories/{id}`

#### File Service Routes

- **Base URL**: `http://localhost:8080/api/file/`
- **File Operations**: `/files/upload`, `/files/download/{id}`, `/files/{id}`

### Common Response Format

```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    // Response data here
  }
}
```

### Security

- **JWT Authentication**: Required cho hầu hết endpoints
- **Role-based access**: ADMIN, USER roles
- **Token expiration**: 1 hour (configurable)
- **Refresh token**: 24 hours (configurable)

### Health Checks

```bash
# Check service health
GET http://localhost:8081/actuator/health
GET http://localhost:8082/actuator/health
GET http://localhost:8083/actuator/health
```

---

## Troubleshooting

### Common Issues

1. **Service không register với Eureka**

   - Kiểm tra Discovery Service đã khởi động
   - Verify network connectivity

2. **Database connection errors**

   - Đảm bảo PostgreSQL đang chạy
   - Kiểm tra database credentials
   - Tạo đủ 3 databases

3. **Redis connection issues**

   - Verify Redis service status
   - Check port 6379 availability

4. **Port conflicts**
   - Đảm bảo các port 8080, 8081, 8082, 8083, 8761 không bị chiếm

### Logs

Kiểm tra logs của từng service để debug:

```bash
# Tail logs
tail -f logs/spring.log
```

---

## Contributors

- **VDT 2025**
- **Project Type**: Microservices Training Project

---
