#Microservices backend

This project can be considered as example of microservices approach. It covers online shopping domain and consists of several modules or microservices: cart-service, catalog-service, customer-service.

Main purpose of this project is to try and study some technologies.  Following languages, libraries and tools are used here: Kotlin, Java, Spring, Ktor, Kafka, Redis, MongoDB, JUnit, KotlinTest, TestContainers.

| Module | Description |
|----------|---------|
| [`cart-service`](./cart-service)| Module provides support of buying products. It includes features: add product to cart, remove product from cart, calculate price of products, checkout cart.|
| [`catalog-service`](./catalog-service)| Module holds information about products to sell and implements features add product for selling, retrieve products for selling.|
| [`customer-service`](./customer-service)| Module contains api and data regarding to customer profile. It includes features: create customer profile, update profile.|
| [`int-test`](./int-test)| Integration tests. Goal of module is application testing. It produces HTTP-requests to modules according to some users scenario and validates response result and system state.|
| [`tech-check`](./tech-check)| Module for experiments with technologies, libraries, etc.|

Every module is placed into docker image (see dockerfile at module directory) and instantiated by docker-compose (see [`docker-compose.yaml`](https://github.com/gusarov-aleksei/mcs-backend/blob/master/docker-compose.yaml)).

###Scripts to operate with modules
Build and create docker image for all modules
```
./build.sh
```
Build and create docker image for one module, for example `cart-service`
```
./build_module.sh cart-service
```
Clean compiled artifacts of all modules and remove docker images
```
./clean.sh
```
Clean compiled artifacts and remove docker image of one module, for example `cart-service`
```
./clean_module.sh cart-service
```
Start all modules of application
```
docker-compose -f ./docker-compose.yaml up -d
```
Stop all modules of application
```
docker-compose -f ./docker-compose.yaml down
```
Stop one module, for example `cart-service`
```
docker-compose -f ./docker-compose.yaml stop cart-service
```
Start one module and modules on which this one depends, for example `cart-service`
```
docker-compose -f ./docker-compose.yaml up -d cart-service
```
Perform integration tests of `int-test`
```
./int_test.sh
```