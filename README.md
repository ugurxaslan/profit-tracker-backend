## Docker Compose ile Başlatma

Projeyi başlatmak için aşağıdaki komutu kullanarak `docker-compose.yml` dosyasını çalıştırabilirsiniz:

```bash
docker-compose up -d
```

Bu komut, gerekli servisleri (veritabanını ve veritabanı arayüzünü) başlatacaktır.

## Spring Boot Projesini Başlatma

Servisler başlatıldıktan sonra, Spring Boot uygulamasını başlatmak için proje dizininde aşağıdaki komutu çalıştırabilirsiniz:

```bash
./mvnw spring-boot:run
```

spring projesi 8080 portundan hizmet vermektedir.

## Swagger UI'ya Erişim

Spring Boot uygulaması çalıştıktan sonra, Swagger UI arayüzüne aşağıdaki adresten erişebilirsiniz:

http://localhost:8080/swagger-ui.html

önce kayıt olun ve login olup tokeni auth yerine yapıştırın.

## Adminer Panelinden Veritabanına Erişim

Adminer arayüzüne erişmek için tarayıcınızda aşağıdaki adresi açabilirsiniz: http://localhost:8081

Bağlantı ayarları genellikle şu şekildedir:

- **Sistem** PostgreSQL
- **Sunucu:** db
- **Kullanıcı Adı:** user
- **Şifre:** user
- **Veritabanı:** profit-tracker-db