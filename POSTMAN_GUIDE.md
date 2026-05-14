## 🧪 GUIDE COMPLET - TEST AVEC POSTMAN

### **PRÉREQUIS**

✅ Postman installé (https://www.postman.com/downloads/)  
✅ MongoDB actif  
✅ Application Spring Boot démarrée sur port 8081  

```bash
# Terminal 1: Démarrer MongoDB
mongod

# Terminal 2: Démarrer l'application
cd C:\Users\MSI\Downloads\demo\demo
./mvnw spring-boot:run
```

**Vérification**: Application accessible sur `http://localhost:8081`

---

## 📊 CONFIGURATION POSTMAN

### **Étape 1: Créer un Environment**

1. Cliquez sur **"Environments"** en haut à gauche
2. Cliquez sur **"+"** pour créer un nouvel environment
3. Nommez-le: **`Demo-Login-API`**
4. Ajoutez les variables suivantes:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8081` | `http://localhost:8081` |
| `token` | `` | `` |
| `username` | `admin` | `admin` |

5. Cliquez sur **"Save"**

### **Étape 2: Sélectionner l'Environment**

En haut à droite, sélectionnez **`Demo-Login-API`** dans le dropdown

---

## 🧪 TEST DES ENDPOINTS

### **TEST 1: REGISTER (Créer un compte)**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/register`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (JSON):
  ```json
  {
    "username": "admin",
    "password": "admin123",
    "confirmPassword": "admin123"
  }
  ```

#### Étapes:
1. Créez une nouvelle requête: **Ctrl+T** ou **New → Request**
2. Nommez-la: **Register User**
3. Sélectionnez **POST**
4. URL: `{{baseUrl}}/api/auth/register`
5. Allez dans **Headers** et ajoutez: `Content-Type: application/json`
6. Allez dans **Body → raw → JSON**
7. Collez le JSON ci-dessus
8. Cliquez sur **"Send"**

#### Réponse Attendue (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMjcyODAwMCwiZXhwIjoxNzEyODE0NDAwfQ.abc123...",
  "username": "admin",
  "message": "Registration successful"
}
```

#### Tests Automatisés (Optional):
Allez dans l'onglet **Tests** et ajoutez:
```javascript
pm.test("Status code is 201", function() {
    pm.response.to.have.status(201);
});

pm.test("Response has token", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.exist;
});

pm.test("Response has username", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.username).to.equal("admin");
});

// Sauvegarder le token pour les requêtes suivantes
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.token);
```

**💡 Tip**: Le token sera automatiquement sauvegardé pour les tests suivants!

---

### **TEST 2: LOGIN (Se connecter)**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/login`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (JSON):
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```

#### Étapes:
1. Créez une nouvelle requête: **New → Request**
2. Nommez-la: **Login User**
3. Sélectionnez **POST**
4. URL: `{{baseUrl}}/api/auth/login`
5. Headers: `Content-Type: application/json`
6. Body (JSON):
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
7. Cliquez sur **"Send"**

#### Réponse Attendue (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMjcyODAwMCwiZXhwIjoxNzEyODE0NDAwfQ.xyz789...",
  "username": "admin",
  "message": "Login successful"
}
```

#### Tests Automatisés:
```javascript
pm.test("Status code is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("Login message is correct", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.equal("Login successful");
});

// Sauvegarder le token
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.token);
```

---

### **TEST 3: GET PROFILE (Requête Protégée)**

#### Configuration:
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/auth/profile`
- **Headers**:
  ```
  Authorization: Bearer {{token}}
  ```

#### Étapes:
1. Créez une nouvelle requête: **New → Request**
2. Nommez-la: **Get Profile**
3. Sélectionnez **GET**
4. URL: `{{baseUrl}}/api/auth/profile`
5. Allez dans **Headers**
6. Ajoutez une nouvelle ligne:
   - **Key**: `Authorization`
   - **Value**: `Bearer {{token}}`
7. Cliquez sur **"Send"**

#### Réponse Attendue (200 OK):
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "admin",
  "createdAt": "2026-04-09T10:30:00"
}
```

#### Tests Automatisés:
```javascript
pm.test("Status code is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("Response has user id", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
});

pm.test("Response has correct username", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.username).to.equal("admin");
});

pm.test("Response has createdAt", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.createdAt).to.exist;
});
```

---

### **TEST 4: LOGOUT (Déconnexion)**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/logout?username={{username}}`
- **Headers**:
  ```
  Authorization: Bearer {{token}}
  ```

#### Étapes:
1. Créez une nouvelle requête: **New → Request**
2. Nommez-la: **Logout User**
3. Sélectionnez **POST**
4. URL: `{{baseUrl}}/api/auth/logout?username={{username}}`
5. Headers: `Authorization: Bearer {{token}}`
6. Cliquez sur **"Send"**

#### Réponse Attendue (200 OK):
```
"Logout successful"
```

#### Tests Automatisés:
```javascript
pm.test("Status code is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("Logout message is correct", function() {
    pm.response.to.have.body("Logout successful");
});

// Effacer le token après logout
pm.environment.set("token", "");
```

---

## ⚠️ CAS D'ERREUR - À TESTER

### **Erreur 1: Username non trouvé au login**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/login`
- **Body**:
  ```json
  {
    "username": "nonexistent",
    "password": "admin123"
  }
  ```

#### Réponse Attendue (404 Not Found):
```json
{
  "error": "User not found with username: nonexistent",
  "status": "404"
}
```

---

### **Erreur 2: Mauvais mot de passe**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/login`
- **Body**:
  ```json
  {
    "username": "admin",
    "password": "wrongpassword"
  }
  ```

#### Réponse Attendue (404 Not Found):
```json
{
  "error": "Invalid password",
  "status": "404"
}
```

---

### **Erreur 3: Username déjà existant**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/register`
- **Body**:
  ```json
  {
    "username": "admin",
    "password": "newpass123",
    "confirmPassword": "newpass123"
  }
  ```

#### Réponse Attendue (400 Bad Request):
```json
{
  "error": "Username already exists",
  "status": "400"
}
```

---

### **Erreur 4: Mots de passe ne correspondent pas**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/register`
- **Body**:
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "confirmPassword": "differentpass"
  }
  ```

#### Réponse Attendue (400 Bad Request):
```json
{
  "error": "Passwords do not match",
  "status": "400"
}
```

---

### **Erreur 5: Token invalide ou expiré**

#### Configuration:
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/auth/profile`
- **Headers**:
  ```
  Authorization: Bearer invalidtoken123
  ```

#### Réponse Attendue:
Pas d'authentification (erreur 401 ou accès refusé)

---

### **Erreur 6: Champs vides**

#### Configuration:
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/login`
- **Body**:
  ```json
  {
    "username": "",
    "password": ""
  }
  ```

#### Réponse Attendue (400 Bad Request):
```json
{
  "errors": {
    "username": "Username is required",
    "password": "Password is required"
  },
  "status": "400"
}
```

---

## 🔄 FLUX DE TEST COMPLET RECOMMANDÉ

### **Scénario 1: Nouvel utilisateur**
```
1. POST /register → Créer "testuser"
   ✓ Récupérer token
   
2. POST /login → Connecter avec "testuser"
   ✓ Vérifier token différent (ou même)
   
3. GET /profile → Récupérer profil
   ✓ Vérifier username = "testuser"
   
4. POST /logout → Déconnexion
   ✓ Effacer token
```

### **Scénario 2: Utilisateur existant**
```
1. POST /login → Connecter avec "admin"
   ✓ Récupérer token
   
2. GET /profile → Vérifier profil
   ✓ Vérifier username = "admin"
   
3. POST /logout → Déconnexion
   ✓ Confirmer message
```

### **Scénario 3: Gestion des erreurs**
```
1. POST /register → Username existant (Erreur 400)
2. POST /login → Username non trouvé (Erreur 404)
3. POST /login → Mauvais password (Erreur 404)
4. POST /login → Champs vides (Erreur 400)
5. GET /profile → Sans token (Erreur Auth)
```

---

## 💾 EXPORTER LA COLLECTION POSTMAN

### **Pour partager ou sauvegarder:**

1. Cliquez sur le **menu (...)** à côté du nom de la collection
2. Sélectionnez **"Export"**
3. Choisissez le format **"Collection v2.1"**
4. Cliquez sur **"Export"**
5. Sauvegardez le fichier JSON

### **Pour importer:**

1. Dans Postman: **File → Import**
2. Sélectionnez le fichier JSON téléchargé
3. Cliquez sur **"Import"**

---

## 📈 CONSEILS PRATIQUES

### **Astuce 1: Utiliser des Variables**
- Définissez `{{baseUrl}}` une seule fois
- Utilisez-la dans toutes les requêtes
- Facile de changer (dev → prod)

### **Astuce 2: Chaînage de requêtes**
- Utilisez **Tests** pour sauvegarder le token
- Chaque requête passe le token à la suivante
- Pas besoin de copier-coller

### **Astuce 3: Tester les Erreurs**
- Testez les cas d'erreur intentionnellement
- Vérifiez les codes HTTP
- Validez les messages d'erreur

### **Astuce 4: Organiser les Requêtes**
Créez des **Folders**:
```
Demo-Login-API/
├── Auth
│   ├── Register User
│   ├── Login User
│   ├── Get Profile
│   └── Logout User
└── Error Cases
    ├── Invalid Username
    ├── Invalid Password
    ├── Duplicate Username
    └── Empty Fields
```

### **Astuce 5: Utiliser Runner**
- Cliquez sur **"Runner"** en haut à gauche
- Sélectionnez la collection
- Cliquez sur **"Run"** pour exécuter toutes les requêtes
- Vérifiez les résultats

---

## 🖼️ CAPTURE D'ÉCRAN - SETUP HEADER

```
┌─────────────────────────────────────────────┐
│ Headers                                     │
├─────────────────────────────────────────────┤
│ Key              | Value                    │
├─────────────────────────────────────────────┤
│ Content-Type     | application/json         │
│ Authorization    | Bearer {{token}}         │
└─────────────────────────────────────────────┘
```

---

## 📝 TEMPLATE REQUÊTE VIERGE

### **Pour ajouter rapidement une nouvelle requête:**

1. **Informations de base:**
   - Nom: `[API_NAME]`
   - Méthode: GET/POST/PUT/DELETE
   - URL: `{{baseUrl}}/api/...`

2. **Headers (si POST/PUT):**
   - Content-Type: application/json

3. **Body (si POST/PUT):**
   - Format: JSON

4. **Authorization (si protégé):**
   - Type: Bearer Token
   - Token: `{{token}}`

5. **Tests (optionnel):**
   - Vérifier status code
   - Vérifier structure réponse
   - Sauvegarder variables

---

## ✅ CHECKLIST DE TEST

```
☐ Register endpoint (201)
☐ Register - Username existant (400)
☐ Login endpoint (200)
☐ Login - Username invalide (404)
☐ Login - Password invalide (404)
☐ Get Profile avec token (200)
☐ Get Profile sans token (Erreur)
☐ Logout endpoint (200)
☐ Token sauvegardé automatiquement
☐ Variable baseUrl fonctionne
☐ Cas d'erreur testés
☐ Tests automatisés en place
```

---

## 🎯 RÉSUMÉ DES ENDPOINTS

| N° | Endpoint | Méthode | Auth | Status | Body |
|----|----------|---------|------|--------|------|
| 1 | `/api/auth/register` | POST | ❌ | 201 | username, password, confirmPassword |
| 2 | `/api/auth/login` | POST | ❌ | 200 | username, password |
| 3 | `/api/auth/profile` | GET | ✅ | 200 | - |
| 4 | `/api/auth/logout` | POST | ✅ | 200 | - |

---

## 🚀 DÉMARRAGE RAPIDE

1. **Copier les configurations ci-dessus**
2. **Créer les requêtes dans Postman**
3. **Ajouter les tests automatisés**
4. **Tester dans l'ordre recommandé**
5. **Vérifier les résultats**

**Total: ~10 minutes pour configurer tout!**


