# DWH - Data warehouse [![Build Status](https://travis-ci.com/marecica2/dwh.svg?branch=develop)](https://travis-ci.com/marecica2/dwh)

Showcase app for following stack dealing with multi-tenancy, 
datawarehouse solution with microservice architecture with CI pipeline and automated deployemnt to kubernetes cluster
- Spring boot (Data, Rest, Repositories)
- Spring Batch
- Postgres
- Redis
- ReactJS
- Maven
- Docker
- Kubernetes
- Travis 

### Local development
```bash
# start infrastructure (Redis, Postgres)
./dev stack-start 
# stop infrastructure
./dev stack-stop 
```

### Databases

Running database migrations

```bash
./dev migrations
./dev migrations-test
```

Usefull docker commands

```bash
docker ps # find your CONTAINER ID
docker exec -it <container_id> bash # run bash command inside that container
```
