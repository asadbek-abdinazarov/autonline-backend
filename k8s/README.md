# PostgreSQL Setup

Bu papka PostgreSQL sozlamalarini o'z ichiga oladi.

## Variant 1: Zalando PostgreSQL Operator (Tavsiya etiladi)

### O'rnatish

```bash
# 1. Zalando PostgreSQL operator ni o'rnatish
kubectl apply -f https://raw.githubusercontent.com/zalando/postgres-operator/master/manifests/postgresql.crd.yaml
kubectl apply -f https://raw.githubusercontent.com/zalando/postgres-operator/master/manifests/operator-service-account-rbac.yaml
kubectl apply -f https://raw.githubusercontent.com/zalando/postgres-operator/master/manifests/postgres-operator.yaml

# 2. PostgreSQL instance yaratish
kubectl apply -f k8s/postgresql-dev.yaml
kubectl apply -f k8s/postgresql-prod.yaml

# 3. PostgreSQL holatini tekshirish
kubectl get postgresql -n dev
kubectl get postgresql -n production

# 4. Service va podlarni ko'rish
kubectl get svc -n dev | grep autonline-dev
kubectl get pods -n dev | grep autonline-dev
```

### Parol olish

Zalando operator avtomatik parol yaratadi. Parolni olish:

```bash
# Dev environment
kubectl get secret autonline-dev.autonline-dev.credentials.postgresql.acid.zalan.do -n dev -o jsonpath='{.data.password}' | base64 -d

# Production environment
kubectl get secret autonline-prod.autonline-prod.credentials.postgresql.acid.zalan.do -n production -o jsonpath='{.data.password}' | base64 -d
```

### Connection String

- **Dev**: `autonline-dev.dev.svc.cluster.local:5432`
- **Prod**: `autonline-prod.production.svc.cluster.local:5432`

## Variant 2: Local PostgreSQL (Port-forward)

Agar local PostgreSQL ishlatmoqchi bo'lsangiz:

```bash
# 1. Local PostgreSQL ga port-forward qilish
kubectl port-forward svc/YOUR_POSTGRES_SERVICE -n YOUR_NAMESPACE 5432:5432

# 2. Values fayllarda database host ni yangilash:
#    host: "host.docker.internal" (Docker Desktop uchun)
#    yoki
#    host: "YOUR_LOCAL_IP"
```

## Database Host Sozlash

Helm values fayllarda database host:

- **Dev**: `helm/autonline/values-dev.yaml`
- **Prod**: `helm/autonline/values-prod.yaml`

```yaml
database:
  host: "autonline-dev.dev.svc.cluster.local"  # Zalando operator uchun
  # yoki
  host: "YOUR_EXTERNAL_DB_HOST"  # Tashqi database uchun
```
