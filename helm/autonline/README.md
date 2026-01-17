# Autonline Helm Chart

Bu Helm Chart Autonline Spring Boot dasturini Kubernetes klasterida deploy qilish uchun yaratilgan.

## Talablar

- Kubernetes 1.19+
- Helm 3.0+
- Tashqi PostgreSQL ma'lumotlar bazasi
- Docker image (autonline:tag)

## O'rnatish

### Development environment

```bash
helm install autonline ./helm/autonline \
  -f ./helm/autonline/values-dev.yaml \
  --namespace dev \
  --create-namespace
```

### Production environment

Production uchun secret values larni `--set` orqali yoki external secret management orqali o'rnatish kerak:

```bash
helm install autonline ./helm/autonline \
  -f ./helm/autonline/values-prod.yaml \
  --set database.password=YOUR_DB_PASSWORD \
  --set jwt.secret=YOUR_JWT_SECRET \
  --set storage.accessKey=YOUR_STORAGE_ACCESS_KEY \
  --set storage.secretKey=YOUR_STORAGE_SECRET_KEY \
  --namespace production \
  --create-namespace
```

## Konfiguratsiya

### Asosiy parametrlar

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `replicaCount` | Pod replicalar soni | `2` |
| `image.repository` | Docker image repository | `autonline` |
| `image.tag` | Docker image tag | `latest` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |

### Database konfiguratsiyasi

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `database.host` | PostgreSQL host | `postgresql.default.svc.cluster.local` |
| `database.port` | PostgreSQL port | `5432` |
| `database.name` | Database nomi | `autonline` |
| `database.username` | Database foydalanuvchi nomi | `autonline_user` |
| `database.password` | Database paroli (secret) | `""` |
| `database.hibernateDdlAuto` | Hibernate DDL auto | `update` |

### JWT konfiguratsiyasi

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `jwt.secret` | JWT secret key (secret) | `""` |
| `jwt.expiration` | JWT expiration (ms) | `86400000` |
| `jwt.refreshExpiration` | Refresh token expiration (ms) | `604800000` |

### Storage konfiguratsiyasi

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `storage.accessKey` | Storage access key (secret) | `""` |
| `storage.secretKey` | Storage secret key (secret) | `""` |
| `storage.endpoint` | Storage endpoint | `https://sfo3.digitaloceanspaces.com` |
| `storage.region` | Storage region | `sfo3` |
| `storage.bucket` | Storage bucket nomi | `autonline` |

### Application konfiguratsiyasi

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `app.profile` | Spring profile | `prod` |
| `app.serverPort` | Server port | `8080` |
| `app.cors.allowedOrigins` | CORS allowed origins | `["https://autonline.uz"]` |
| `app.logging.level` | Logging level | `INFO` |

### Resources

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `2Gi` |
| `resources.requests.cpu` | CPU request | `500m` |
| `resources.requests.memory` | Memory request | `1Gi` |

### Autoscaling

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `autoscaling.enabled` | HPA yoqilganmi | `false` |
| `autoscaling.minReplicas` | Minimal replicalar | `2` |
| `autoscaling.maxReplicas` | Maksimal replicalar | `5` |
| `autoscaling.targetCPUUtilizationPercentage` | CPU target | `80` |
| `autoscaling.targetMemoryUtilizationPercentage` | Memory target | `80` |

### Monitoring

| Parameter | Tavsif | Default |
|-----------|--------|---------|
| `monitoring.enabled` | Monitoring yoqilganmi | `false` |
| `monitoring.serviceMonitor.enabled` | ServiceMonitor yoqilganmi | `false` |
| `monitoring.serviceMonitor.interval` | Scrape interval | `30s` |
| `monitoring.serviceMonitor.path` | Metrics path | `/actuator/prometheus` |

## Health Checks

Chart Spring Boot Actuator health check endpointlaridan foydalanadi:

- **Liveness Probe**: `/actuator/health` - container tirikligini tekshiradi
- **Readiness Probe**: `/actuator/health` - container tayyorligini tekshiradi

## Environment Variables

Chart quyidagi environment variables larni o'rnatadi:

### Database
- `PGHOST` - PostgreSQL host
- `PGPORT` - PostgreSQL port
- `POSTGRES_DB` - Database nomi
- `PGUSER` - Database foydalanuvchi (Secret dan)
- `PGPASSWORD` - Database parol (Secret dan)
- `A_HIBERNATE_DDL_AUTO` - Hibernate DDL auto

### JWT
- `JWT_SECRET` - JWT secret (Secret dan)
- `JWT_EXPIRATION` - JWT expiration
- `JWT_REFRESH_EXPIRATION` - Refresh token expiration

### Storage
- `STORAGE_ACCESS_KEY` - Storage access key (Secret dan)
- `STORAGE_SECRET_KEY` - Storage secret key (Secret dan)
- `STORAGE_ENDPOINT` - Storage endpoint
- `STORAGE_REGION` - Storage region
- `STORAGE_BUCKET` - Storage bucket

### Application
- `SPRING_PROFILES_ACTIVE` - Spring profile
- `SERVER_PORT` - Server port
- `ALLOWED_ORIGINS` - CORS origins (ConfigMap dan)
- `LOG_LEVEL` - Logging level (ConfigMap dan)
- `SECURITY_LOG_LEVEL` - Security logging level (ConfigMap dan)
- `CONFIG_LOG_LEVEL` - Config logging level (ConfigMap dan)

## Upgrade

```bash
helm upgrade autonline ./helm/autonline \
  -f ./helm/autonline/values-prod.yaml \
  --namespace production
```

## Uninstall

```bash
helm uninstall autonline --namespace production
```

## Troubleshooting

### Podlar ishlamayapti

1. Pod statusini tekshiring:
```bash
kubectl get pods -n production
kubectl describe pod <pod-name> -n production
```

2. Loglarni ko'ring:
```bash
kubectl logs <pod-name> -n production
```

3. ConfigMap va Secret larni tekshiring:
```bash
kubectl get configmap -n production
kubectl get secret -n production
```

### Database connection muammolari

1. Database host va port to'g'riligini tekshiring
2. Database credentials to'g'riligini tekshiring
3. Network connectivity ni tekshiring:
```bash
kubectl exec -it <pod-name> -n production -- ping <database-host>
```

### Health check muammolari

1. Actuator endpoint ishlayotganini tekshiring:
```bash
kubectl exec -it <pod-name> -n production -- curl http://localhost:8080/actuator/health
```

2. Health check sozlamalarini tekshiring (values.yaml da)

## Xavfsizlik

- Secret values larni hech qachon values.yaml faylida saqlamang
- Production da `--set` yoki external secret management (HashiCorp Vault, AWS Secrets Manager, etc.) ishlating
- Non-root user ishlatiladi (podSecurityContext)
- Read-only root filesystem ni yoqish mumkin (securityContext)

## Monitoring

Agar Prometheus operator o'rnatilgan bo'lsa, ServiceMonitor ni yoqing:

```yaml
monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
```

Bu `/actuator/prometheus` endpoint dan metrics larni scrape qiladi.

## Qo'shimcha resurslar

- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
