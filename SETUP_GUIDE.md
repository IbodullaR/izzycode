# Algonix - Sozlash Qo'llanmasi

## Git'dan pull qilgandan keyin

### 1. application.properties faylini yaratish

Git'dan pull qilgandan keyin `application.properties` fayli yo'q bo'ladi (xavfsizlik uchun). Uni yaratish kerak:

```bash
# Windows
copy src\main\resources\application.properties.example src\main\resources\application.properties

# Linux/Mac
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 2. Majburiy sozlamalar

`src/main/resources/application.properties` faylini oching va quyidagilarni o'zgartiring:

```properties
# Database parolingizni kiriting
spring.datasource.password=sizning_parolingiz

# JWT secret (ixtiyoriy, default bor)
jwt.secret=2lVo5TcjgTco3dUIwmYEIWcLfeiBR7QgjS9fFyn1Jdg=
```

### 3. Ixtiyoriy sozlamalar

Agar email yoki OAuth kerak bo'lsa:

```properties
# Email (password reset uchun)
spring.mail.username=sizning_emailingiz@gmail.com
spring.mail.password=gmail_app_password

# GitHub OAuth
spring.security.oauth2.client.registration.github.client-id=github_client_id
spring.security.oauth2.client.registration.github.client-secret=github_secret

# Google OAuth
spring.security.oauth2.client.registration.google.client-id=google_client_id
spring.security.oauth2.client.registration.google.client-secret=google_secret
```

### 4. PostgreSQL sozlash

```sql
-- Database yaratish
CREATE DATABASE algonix;

-- User yaratish (ixtiyoriy)
CREATE USER algonix_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE algonix TO algonix_user;
```

### 5. Loyihani ishga tushirish

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## Muhim!

- `application.properties` faylini **hech qachon** Git'ga commit qilmang
- Bu fayl `.gitignore` da bor
- Har bir developer o'z local sozlamalarini yaratadi
- Maxfiy ma'lumotlarni (parol, API key) faqat local faylda saqlang

## Tez sozlash (minimal)

Agar faqat test qilmoqchi bo'lsangiz:

1. `application.properties.example` ni `application.properties` ga nusxalang
2. Faqat database parolni o'zgartiring
3. Ishga tushiring

Qolgan sozlamalar default qiymatlar bilan ishlaydi.
