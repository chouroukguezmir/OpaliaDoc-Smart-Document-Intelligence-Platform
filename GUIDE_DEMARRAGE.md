# 🚀 GUIDE DÉMARRAGE - SYSTÈME DE LOGIN

## ✅ Étapes de Configuration

### **1️⃣ PRÉREQUIS**

- ✅ Java 17 ou supérieur
- ✅ MongoDB actif (port 27017)
- ✅ Maven (ou utiliser mvnw)

### **2️⃣ VÉRIFIER MONGODB**

```bash
# Vérifier que MongoDB est actif
mongo --version

# Démarrer MongoDB (si arrêté)
mongod

# Vérifier la connexion
mongo mongodb://localhost:27017
```

### **3️⃣ CONFIGURER APPLICATION.PROPERTIES**

`src/main/resources/application.properties`

```properties
# ==================== SPRING ====================
spring.application.name=demo
server.port=8081

# ==================== MONGODB ====================
spring.data.mongodb.uri=mongodb://localhost:27017/document-analyzer

# ==================== JWT ====================
jwt.secret=MySuperSecretKeyForJWTThatIsAtLeast256BitsLong!
jwt.expiration=86400000

# ==================== CORS ====================
cors.allowed-origins=http://localhost:4200

# ==================== FILE UPLOAD ====================
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
file.upload-dir=uploads/
```

### **4️⃣ COMPILER LE PROJET**

```bash
# Compiler
./mvnw clean compile

# Ou avec Maven installé
mvn clean compile
```

### **5️⃣ DÉMARRER L'APPLICATION**

```bash
# Option 1: Avec Maven
./mvnw spring-boot:run

# Option 2: Exécuter le JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Option 3: Depuis l'IDE
Clic droit sur DemoApplication.java > Run
```

**Vérification**: L'app démarre sur `http://localhost:8081`

---

## 🧪 TESTER LES ENDPOINTS

### **OPTION 1: Avec cURL**

#### Register (Créer un utilisateur)
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "confirmPassword": "admin123"
  }'

# Réponse attendue:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "message": "Registration successful"
}
```

#### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Réponse attendue:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "message": "Login successful"
}
```

#### Get Profile (Requête protégée)
```bash
# Remplacer TOKEN par le token reçu du login
curl -X GET http://localhost:8081/api/auth/profile \
  -H "Authorization: Bearer TOKEN"

# Réponse attendue:
{
  "id": "507f1f77bcf86cd799439011",
  "username": "admin",
  "createdAt": "2026-04-09T10:30:00"
}
```

#### Logout
```bash
curl -X POST "http://localhost:8081/api/auth/logout?username=admin" \
  -H "Authorization: Bearer TOKEN"

# Réponse attendue:
"Logout successful"
```

---

### **OPTION 2: Avec Postman**

#### 1. Register
- **URL**: `POST http://localhost:8081/api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "username": "admin",
  "password": "admin123",
  "confirmPassword": "admin123"
}
```

#### 2. Login
- **URL**: `POST http://localhost:8081/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **Copier le token de la réponse**

#### 3. Get Profile
- **URL**: `GET http://localhost:8081/api/auth/profile`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {TOKEN_COPIÉ}`

#### 4. Logout
- **URL**: `POST http://localhost:8081/api/auth/logout?username=admin`
- **Headers**: `Authorization: Bearer {TOKEN_COPIÉ}`

---

### **OPTION 3: Avec Insomnia**

(Similaire à Postman)

---

## 🗄️ VÉRIFIER LES DONNÉES MONGODB

### **Accéder à MongoDB**

```bash
# Connexion
mongo mongodb://localhost:27017

# Sélectionner la BDD
use document-analyzer

# Voir les collections
show collections

# Voir les utilisateurs
db.admin_users.find()

# Voir un utilisateur spécifique
db.admin_users.findOne({ username: "admin" })

# Quitter
exit
```

### **Exemple de Document MongoDB**
```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "username": "admin",
  "password": "$2a$10$...",
  "createdAt": ISODate("2026-04-09T10:30:00.000Z")
}
```

---

## 🔍 DÉPANNAGE

### **Erreur: "Cannot resolve symbol 'LoginRequest'"**
- ✅ Vérifier que les DTOs sont créés
- ✅ Nettoyer/Recompiler le projet: `./mvnw clean compile`
- ✅ Rafraîchir l'IDE (F5 ou Maven reload)

### **Erreur: "Connection refused to MongoDB"**
- ✅ Vérifier que MongoDB est actif: `mongod`
- ✅ Vérifier le port: `mongodb://localhost:27017`
- ✅ Dans `application.properties`: vérifier `spring.data.mongodb.uri`

### **Erreur: "Unauthorized" au login**
- ✅ Vérifier le username/password existent
- ✅ Vérifier que le compte a été créé (register d'abord)
- ✅ Vérifier que le mot de passe est correct

### **Erreur: "Invalid token" sur profil**
- ✅ Vérifier que le token commence par "Bearer "
- ✅ Vérifier que le token n'est pas expiré (24h)
- ✅ Vérifier la signature JWT (secret key)

### **Port 8081 déjà utilisé**
```bash
# Changer le port dans application.properties
server.port=8082

# Ou kill le processus sur 8081
# Windows:
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8081
kill -9 <PID>
```

---

## 📊 VÉRIFICATIONS COMPLÈTES

### ✅ Checklist d'Intégration

```
☐ MongoDB est actif
☐ Application démarre sans erreurs
☐ Port 8081 est accessible
☐ Endpoint /api/auth/register fonctionne
☐ Endpoint /api/auth/login fonctionne
☐ Token JWT est généré correctement
☐ Endpoint /api/auth/profile nécessite le token
☐ Endpoint /api/auth/logout fonctionne
☐ Les données sont sauvegardées dans MongoDB
☐ Les mots de passe sont hashés (BCrypt)
```

---

## 📈 PERFORMANCE & SÉCURITÉ

### 🔐 Points de Sécurité
- ✅ Passwords hashés avec BCrypt
- ✅ JWT tokens avec expiration
- ✅ CORS limité à localhost:4200
- ✅ Stateless authentication
- ✅ Validation des entrées
- ✅ Exception handling sécurisé

### ⚡ Optimisations
- ✅ Username indexé dans MongoDB
- ✅ Queries optimisées avec findByUsername
- ✅ Stateless pour scalabilité
- ✅ Caching possible sur le frontend

---

## 📚 RESSOURCES UTILES

- **JWT**: https://jwt.io/
- **Spring Security**: https://spring.io/projects/spring-security
- **MongoDB**: https://docs.mongodb.com/
- **JJWT**: https://github.com/jwtk/jjwt
- **Postman**: https://www.postman.com/

---

## 🎯 PROCHAINES ÉTAPES

1. **Frontend Angular**
   - Créer service d'authentification
   - Implémenter guards pour les routes
   - Stocker le token dans localStorage
   - Ajouter Interceptor HTTP pour le token

2. **Améliorations Backend**
   - Ajouter refresh token
   - Rate limiting sur login
   - Audit logging
   - 2FA (Two-Factor Authentication)

3. **Tests**
   - Unit tests (JUnit 5)
   - Integration tests
   - Tests d'API (RestAssured)

---

## 💡 CONSEILS

- 📝 Garder le secret JWT en variable d'environnement en production
- 🔐 Utiliser HTTPS en production (pas HTTP)
- 📊 Monitorer les tentatives de login échouées
- 🔄 Implémenter refresh token pour l'expiration
- 📱 Adapter pour mobile (CORS, tokens)


