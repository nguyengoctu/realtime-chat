# Chat Application

A modern, scalable chat application built with microservices architecture featuring user management, file uploads, and real-time messaging capabilities.

## 🏗️ Architecture

### Microservices
- **API Gateway** - Route management and load balancing
- **User Service** - User authentication, registration, and profile management
- **Frontend** - Angular-based web interface

### Infrastructure
- **Database** - MySQL with Flyway migrations
- **File Storage** - MinIO (S3-compatible) for avatar uploads
- **Containerization** - Docker & Docker Compose
- **Web Server** - Nginx reverse proxy

## 🚀 Features

### ✅ Implemented
- **User Authentication**
  - Registration with email validation
  - Login/logout with JWT tokens
  - Refresh token mechanism
  - Password security

- **User Profile Management**
  - View user profile with real data
  - Edit profile (email, full name)
  - Avatar upload with MinIO storage
  - Username protection (read-only)

- **File Upload System**
  - Image upload (PNG, JPG, GIF)
  - 10MB file size limit
  - Automatic image optimization
  - S3-compatible storage (easily switchable to AWS S3)

- **Infrastructure**
  - Microservices with API Gateway routing
  - Database migrations with Flyway
  - Environment-based configuration
  - CORS handling
  - Docker containerization

### 🔄 In Development
- Real-time chat messaging
- User search and friend system
- Group chat functionality
- Message history and persistence

## 🛠️ Technology Stack

### Backend
- **Java 17** with Spring Boot 3.1.5
- **Spring Security** for authentication
- **Spring Data JPA** with MySQL
- **JWT** for token-based auth
- **MinIO Java SDK** for file storage
- **Flyway** for database migrations

### Frontend
- **Angular 18** with TypeScript
- **Tailwind CSS** for styling
- **RxJS** for reactive programming
- **HTTP Client** with interceptors
- **File upload** with preview

### Infrastructure
- **Docker & Docker Compose**
- **Nginx** reverse proxy
- **MySQL 8.0** database
- **MinIO** object storage
- **Multi-stage Docker builds**

## 📦 Quick Start

### Prerequisites
- Docker & Docker Compose
- Git

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd chat-app
```

2. **Environment Setup**
```bash
# Copy environment template (optional)
cp .env.example .env

# Edit environment variables if needed
# Default values work for local development
```

3. **Start the application**
```bash
docker compose up -d
```

4. **Access the application**
- **Frontend**: http://localhost
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **API Gateway**: http://localhost:9080

### Default Ports
- Frontend (Nginx): `80`
- API Gateway: `9080`
- User Service: `8081` (internal)
- MySQL: `3306`
- MinIO API: `9000`
- MinIO Console: `9001`

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_URL` | Frontend application URL | `http://localhost` |
| `DB_NAME` | MySQL database name | `chatapp_db` |
| `DB_USERNAME` | MySQL username | `chatapp` |
| `DB_PASSWORD` | MySQL password | `chatapp123` |
| `JWT_SECRET` | JWT signing secret | Auto-generated |
| `MINIO_ACCESS_KEY` | MinIO access key | `minioadmin` |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin` |
| `MINIO_BUCKET_AVATARS` | Avatar storage bucket | `avatars` |

### File Upload Configuration
- **Max file size**: 10MB (configurable in both nginx and Spring Boot)
- **Allowed formats**: PNG, JPG, JPEG, GIF, WebP
- **Storage path**: `/storage/avatars/{uuid}.{ext}`
- **Public access**: Images are publicly accessible via nginx proxy

## 📱 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh access token

### User Management
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `GET /api/users/search?keyword={query}` - Search users

### File Upload
- `POST /api/upload/avatar` - Upload avatar image

## 🔨 Development

### Backend Development
```bash
# Build specific service
docker compose build user-service

# View logs
docker compose logs user-service -f

# Restart service
docker compose restart user-service
```

### Frontend Development
```bash
# Build frontend
docker compose build chat-app

# Development with hot reload (if configured)
cd frontend
npm install
ng serve
```

### Database Management
```bash
# Access MySQL
docker exec -it chat-app-mysql mysql -u chatapp -p chatapp_db

# Run migrations manually
docker compose run flyway migrate
```

## 🗄️ Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password_hash`
- `full_name`
- `avatar_url`
- `status` (ACTIVE/INACTIVE)
- `created_at`, `updated_at`

### Refresh Tokens Table
- `id` (Primary Key)
- `token`
- `user_id` (Foreign Key)
- `expires_at`
- `created_at`

## 🔒 Security Features

- **JWT Authentication** with access and refresh tokens
- **Password hashing** with Spring Security
- **CORS configuration** for cross-origin requests
- **File upload validation** (type, size)
- **SQL injection protection** with JPA
- **Environment-based secrets** management

## 🐛 Troubleshooting

### Common Issues

1. **Port conflicts**
```bash
# Check if ports are in use
netstat -tlnp | grep :80
netstat -tlnp | grep :9080
```

2. **Database connection issues**
```bash
# Check MySQL logs
docker compose logs mysql

# Verify database is healthy
docker compose ps
```

3. **File upload issues**
```bash
# Check MinIO status
docker compose logs minio

# Verify bucket policy
docker exec chat-app-minio mc anonymous get minio/avatars
```

4. **Container build issues**
```bash
# Clean rebuild
docker compose down
docker compose build --no-cache
docker compose up -d
```

### Logs
```bash
# View all logs
docker compose logs -f

# Specific service logs
docker compose logs user-service -f
docker compose logs api-gateway -f
docker compose logs chat-app -f
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🎯 Roadmap

- [ ] Real-time messaging with WebSocket
- [ ] Group chat functionality
- [ ] Message encryption
- [ ] Mobile app development
- [ ] Kubernetes deployment
- [ ] Monitoring and logging
- [ ] CI/CD pipeline
- [ ] Performance optimization

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check existing documentation
- Review troubleshooting section

---

Built with ❤️ using modern microservices architecture