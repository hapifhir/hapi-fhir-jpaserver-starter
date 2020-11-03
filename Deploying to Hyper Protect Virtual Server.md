## Steps to deploy to Hyper Protect Server
* Download cert.pem from the IBM cloud console after provision the PostgreSQL database. Save it in the root of the project.
* Run the command below at the root of the project
```bash 
./build-docker-image-for-ibm.sh
```
* Tagging the image
```bash 
docker tag hapi-fhir/hapi-fhir-jpaserver-starter gcr.io/ilara-main/hapi-fhir/hapi-fhir-jpaserver-starter:tag
```
* Push image to GCR
```bash 
docker push gcr.io/ilara-main/hapi-fhir/hapi-fhir-jpaserver-starter:tag
```
* Running the docker image 
```bash
docker run -e "--spring.config.location=classpath:/application-deploy.yaml" --env-file .env.dev -d -p 80:8080 gcr.io/ilara-main/hapi-fhir/hapi-fhir-jpaserver-starter:0.0.9
```
```bash
docker ps
```
```bash
docker logs --follow wonderful_bose
```
