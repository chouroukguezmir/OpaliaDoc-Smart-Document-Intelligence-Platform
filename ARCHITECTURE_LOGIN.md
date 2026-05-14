# Architecture du Système de Login - Documentation

## Vue d'ensemble
Système d'authentification Spring Boot avec JWT, MongoDB et Spring Security.

---

## 🏗️ Architecture en Couches

### 1. **COUCHE PRÉSENTATION (Controller)**
**Fichier**: `AuthController.java`
- Point d'entrée HTTP de l'application
- Endpoints:
  - `POST /api/auth/login` - Authentification
  - `POST /api/auth/register` - Inscription
  - `POST /api/auth/logout` - Déconnexion
  - `GET /api/auth/profile` - Récupérer le profil utilisateur

---

### 2. **COUCHE MÉTIER (Service)**
**Fichiers**: 
- `AuthService.java` (Interface)
- `AuthServiceImpl.java` (Implémentation)

**Responsabilités**:
- Logique d'authentification
- Validation des identifiants
- Gestion du mot de passe (hashage/comparaison)
- Génération des tokens JWT
- Gestion des utilisateurs

---

### 3. **COUCHE DATA (Repository & Model)**

#### Model - `AdminUser.java`
```
- id: String (MongoDB ID)
- username: String (unique, indexed)
- password: String (encodé)
- createdAt: LocalDateTime
```

#### Repository - `AdminUserRepository.java`
- Extends `MongoRepository<AdminUser, String>`
- Méthodes:
  - `findByUsername(String username)` - Rechercher par nom d'utilisateur
  - `existsByUsername(String username)` - Vérifier l'existence

---

### 4. **COUCHE DTO (Data Transfer Object)**

| Classe | Rôle |
|--------|------|
| `LoginRequest` | Données de connexion (username, password) |
| `LoginResponse` | Réponse d'authentification (token, username, message) |
| `RegisterRequest` | Données d'inscription (username, password, confirmPassword) |
| `AdminUserDTO` | Profil utilisateur (id, username, createdAt) |
| `ErrorResponse` | Gestion des erreurs |

---

### 5. **COUCHE SÉCURITÉ & CONFIGURATION**

#### `SecurityConfig.java`
- Configuration Spring Security
- Politique CORS
- Filtres d'authentification
- BCryptPasswordEncoder pour le hashage

#### `JwtAuthFilter.java`
- Filtre JWT personnalisé
- Extraction et validation du token
- Configuration de l'authentification

#### `JwtUtil.java`
- Génération des tokens JWT
- Extraction des claims
- Validation des tokens

---

## 🔐 Flux d'Authentification

### 1. **LOGIN**
```
POST /api/auth/login
├─ LoginRequest (username, password)
├─ AuthServiceImpl.login()
│  ├─ Chercher l'utilisateur
│  ├─ Comparer le mot de passe
│  └─ Générer JWT token
└─ LoginResponse (token, username, message)
```

### 2. **REGISTER**
```
POST /api/auth/register
├─ RegisterRequest (username, password, confirmPassword)
├─ AuthServiceImpl.register()
│  ├─ Valider les mots de passe
│  ├─ Vérifier l'unicité du username
│  ├─ Hasher le mot de passe
│  ├─ Sauvegarder l'utilisateur
│  └─ Générer JWT token
└─ LoginResponse (token, username, message)
```

### 3. **PROTECTED REQUEST**
```
GET /api/auth/profile
├─ Header: "Authorization: Bearer {token}"
├─ JwtAuthFilter
│  ├─ Extraire le token
│  ├─ Valider le token
│  └─ Créer UsernamePasswordAuthenticationToken
├─ SecurityContext setup
└─ Accès autorisé aux ressources
```

---

## 🔑 Endpoints API

### Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "message": "Login successful"
}
```

### Register
```bash
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "confirmPassword": "password123"
}

Response: (201 Created)
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "newuser",
  "message": "Registration successful"
}
```

### Get Profile
```bash
GET http://localhost:8081/api/auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Response:
{
  "id": "507f1f77bcf86cd799439011",
  "username": "admin",
  "createdAt": "2026-04-09T10:30:00"
}
```

### Logout
```bash
POST http://localhost:8081/api/auth/logout?username=admin
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Response:
"Logout successful"
```

---

## 🛠️ Configuration (application.properties)

```properties
# JWT
jwt.secret=MySuperSecretKeyForJWTThatIsAtLeast256BitsLong!
jwt.expiration=86400000

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/document-analyzer

# Server
server.port=8081

# CORS
cors.allowed-origins=http://localhost:4200
```

---

## ⚙️ Gestion des Exceptions

**GlobalExceptionHandler.java** gère:
- `ResourceNotFoundException` (404)
- `IllegalArgumentException` (400)
- `MethodArgumentNotValidException` (400)
- Exception générale (500)

---

## 📦 Dépendances Maven

```xml
- Spring Security
- JWT (jjwt)
- Spring Data MongoDB
- Lombok
- Validation
```

---

## 🚀 Utilisation du Token JWT

1. **Après login/register**, le client reçoit un token
2. **Pour les requêtes protégées**, inclure le header:
   ```
   Authorization: Bearer {token}
   ```
3. **Le filtre JwtAuthFilter** valide le token automatiquement
4. **SecurityContext** est configuré avec l'utilisateur authentifié

---

## 📋 Résumé des Fichiers

| Fichier | Type | Rôle |
|---------|------|------|
| AuthController | Controller | Endpoints HTTP |
| AuthService/AuthServiceImpl | Service | Logique métier |
| AdminUserRepository | Repository | Accès données |
| AdminUser | Model | Entité MongoDB |
| LoginRequest/LoginResponse/RegisterRequest/AdminUserDTO | DTO | Transfert données |
| SecurityConfig | Config | Sécurité Spring |
| JwtAuthFilter | Filter | Validation JWT |
| JwtUtil | Util | Génération/Validation JWT |
| GlobalExceptionHandler | Exception Handler | Gestion erreurs |

---

## 🎯 Fonctionnalités

✅ Login avec username/password  
✅ Register avec validation  
✅ JWT Token generation  
✅ JWT Token validation  
✅ Password encoding (BCrypt)  
✅ Stateless authentication  
✅ CORS configuration  
✅ Global exception handling  
✅ Profile retrieval  
✅ MongoDB integration  


