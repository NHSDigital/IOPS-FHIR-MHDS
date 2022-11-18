
mvn clean install

docker build -t fhir-mhd .

docker tag fhir-mhd:latest 365027538941.dkr.ecr.eu-west-2.amazonaws.com/fhir-mhd:latest
docker tag fhir-mhd:latest 365027538941.dkr.ecr.eu-west-2.amazonaws.com/fhir-mhd:1.0.6

docker push 365027538941.dkr.ecr.eu-west-2.amazonaws.com/fhir-mhd:latest

docker push 365027538941.dkr.ecr.eu-west-2.amazonaws.com/fhir-mhd:1.0.6
