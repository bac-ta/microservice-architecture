# Microservice architecture (gRPC, K8s, AWS...)
![Image architecture](assets/eks-architecture.drawio.png)


## Docker registry build Nexus
```bash
docker compose up -d
```

## Find admin password when starting:

```bash
sudo docker exec -it microservice-architecture-starter-kit-nexus-1 cat /nexus-data/admin.password
```
## Nexus Url localhost:
http://localhost:8081

## Example account local Nexus: 
admin / 68666206-11b8-4949-a325-dc3a6c69969d