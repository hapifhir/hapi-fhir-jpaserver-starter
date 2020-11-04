# Deploying fhir server to hyper protect virtual server
## Prerequisites
* You have provisioned a hyper protect virtual server on IBM cloud
* You have installed docker on the virtual server. 
# Intalling docker 
Add the apt-repository using command below before:
```bash 
sudo add-apt-repository "deb [arch=s390x] https://download.docker.com/linux/ubuntu bionic stable”
```
After that run the command below:
```bash
sudo apt install docker
```
## Steps to deploy to Hyper Protect Server
* Download  cert.pem of the hyper protect database server from the IBM cloud console after provision the PostgreSQL database. Save it in the root of the project.
* Run the command below at the root of the project to build an image that can run on the hyper protect server.
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
On the virtual server you will need to allow access to gcr.io. More details can be found on this [link](https://cloud.google.com/container-registry/docs/advanced-authentication)

Create an .env.target_environment server using the .env.sample template. Docker will load environment variables from the file.
* Running the docker image 
```bash
docker run -e "--spring.config.location=classpath:/application-deploy.yaml" --env-file .env.dev -d -p 80:8080 gcr.io/ilara-main/hapi-fhir/hapi-fhir-jpaserver-starter:tag
```
## Viewing logs 
Run the command below and get the name of the container for example wonderful_bose
```bash
docker ps
```
Run the command below using container name.
```bash
docker logs --follow wonderful_bose
```
