# Argo CD Applications

Bu papka Autonline dasturi uchun Argo CD Application manifest larni o'z ichiga oladi.

## Struktura

```
argocd/
└── applications/
    ├── autonline-dev.yaml      # Development environment
    ├── autonline-prod.yaml     # Production environment
    └── app-of-apps.yaml        # App of Apps pattern
```

## O'rnatish

### Usul 1: App of Apps Pattern (Tavsiya etiladi)

Barcha Application larni bir vaqtning o'zida o'rnatish:

```bash
kubectl apply -f argocd/applications/app-of-apps.yaml -n argo-cd
```

Bu `argocd/applications/` papkasidagi barcha Application larni (app-of-apps.yaml dan tashqari) avtomatik o'rnatadi.

### Usul 2: Alohida o'rnatish

```bash
# Development
kubectl apply -f argocd/applications/autonline-dev.yaml -n argo-cd

# Production
kubectl apply -f argocd/applications/autonline-prod.yaml -n argo-cd
```

## Secret Management

Production Application da secret values larni o'rnatish kerak. Quyidagi usullardan birini tanlang:

### Usul 1: Argo CD CLI orqali

```bash
argocd app set autonline-prod \
  -p database.password=YOUR_DB_PASSWORD \
  -p jwt.secret=YOUR_JWT_SECRET \
  -p storage.accessKey=YOUR_STORAGE_ACCESS_KEY \
  -p storage.secretKey=YOUR_STORAGE_SECRET_KEY
```

### Usul 2: Argo CD UI orqali

1. Argo CD UI ga kiring
2. `autonline-prod` Application ni tanlang
3. "Edit" tugmasini bosing
4. "Parameters" bo'limida secret values larni kiriting
5. "Save" tugmasini bosing

### Usul 3: External Secrets Operator

Agar External Secrets Operator o'rnatilgan bo'lsa, Kubernetes Secret yarating va Argo CD Application da reference qiling.

### Usul 4: Sealed Secrets

Sealed Secrets ishlatib, secret larni Git ga commit qilishingiz mumkin:

```bash
kubectl create secret generic autonline-secrets \
  --from-literal=database-password=YOUR_PASSWORD \
  --from-literal=jwt-secret=YOUR_JWT_SECRET \
  --dry-run=client -o yaml | kubeseal -o yaml > sealed-secret.yaml
```

## Konfiguratsiya

### Git Repository

Application manifest larda quyidagi sozlamalarni yangilang:

- `repoURL`: Git repository URL (hozir: `https://github.com/asadbek-abdinazarov/autonline`)
- `targetRevision`: Branch yoki tag (hozir: `main`)

### Image Repository

`autonline-dev.yaml` va `autonline-prod.yaml` da image repository ni yangilang:

```yaml
parameters:
  - name: image.repository
    value: your-registry/autonline  # O'zgartiring
```

## Sync Policy

Barcha Application lar quyidagi sync policy ga ega:

- **Automated**: Avtomatik sync yoqilgan
- **Prune**: O'chirilgan resurslar avtomatik o'chiriladi
- **SelfHeal**: Manual o'zgarishlar qaytariladi
- **CreateNamespace**: Namespace avtomatik yaratiladi
- **Retry**: 5 marta, exponential backoff

## Status tekshirish

```bash
# Argo CD CLI orqali
argocd app get autonline-prod
argocd app list

# kubectl orqali
kubectl get application -n argo-cd
kubectl describe application autonline-prod -n argo-cd
```

## Manual Sync

Agar avtomatik sync o'chirilgan bo'lsa:

```bash
argocd app sync autonline-prod
```

## Rollback

Oldingi versiyaga qaytish:

```bash
# Argo CD CLI orqali
argocd app rollback autonline-prod <revision>

# Yoki UI dan "History" bo'limida rollback qiling
```

## Troubleshooting

### Application sync qilinmayapti

1. Git repository ga ulanishni tekshiring:
```bash
kubectl get application autonline-prod -n argo-cd -o yaml
```

2. Repository credentials ni tekshiring (Argo CD UI dan)

3. Helm Chart path to'g'riligini tekshiring

### Secret values ishlamayapti

1. Parameter values larni tekshiring:
```bash
argocd app get autonline-prod
```

2. Secret values larni qayta o'rnating (yuqoridagi usullardan birini ishlating)

### Podlar ishlamayapti

1. Pod loglarini ko'ring:
```bash
kubectl logs -f deployment/autonline -n production
```

2. Application events ni ko'ring:
```bash
kubectl describe application autonline-prod -n argo-cd
```

## Qo'shimcha ma'lumotlar

- [Argo CD Documentation](https://argo-cd.readthedocs.io/)
- [Argo CD Application Spec](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/#applications)
- [Helm Chart Documentation](../helm/autonline/README.md)