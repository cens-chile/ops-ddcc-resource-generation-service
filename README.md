# ops-ddcc-resource-generation-service

## Configuración variable de entorno

   base.path = ${BASE_PATH:fhir}

   Se puede configurar el path que viene después del puerto en esta variable, para cambiar en el archivo "application.properties", es suficiente con reemplazar donde dice fhir. Tambien se puede configurar con la variable de entorno llamada "BASE_PATH".


## Crear Imagen docker

```bash
docker build -t censcl/ops-ddcc-resource-generation-service:v1.0 .
```

## Subir imagen a docker hub

```bash
docker push censcl/ops-ddcc-resource-generation-service:v1.0
```

# URL de Consumo para transformadores

## QuestionnaireResponse a DDCCCoreDataSet

Para consumir este sericio debe realizar lo siguiente:

* Request POST
* Apuntar a la URL = http://localhost:8080/fhir/StructureMap/$transform?source=http://worldhealthorganization.github.io/ddcc/StructureMap/QRespToVSCoreDataSet 
* Y finalmente poner un body como el del ejemplo-01.json del que se encuentra en la carpeta ejemplos.



## DDCCCoreDataSet a addBundle

Para consumir este sericio debe realizar lo siguiente:

* Request POST
* Apuntar a la URL = http://localhost:8080/fhir/StructureMap/$transform?source=http://worldhealthorganization.github.io/ddcc/StructureMap/CoreDataSetVSToAddBundle
* Y finalmente poner un body como el del ejemplo-02.json del que se encuentra en la carpeta ejemplos.

## Resource a DDCCCoreDataSet

Para consumir este sericio debe realizar lo siguiente:

* Request POST
* Apuntar a la URL = http://localhost:8080/fhir/StructureMap/$transform?source=http://worldhealthorganization.github.io/ddcc/StructureMap/ResourcesToVSCoreDataSet
* Y finalmente poner un body como el del ejemplo-03.json del que se encuentra en la carpeta ejemplos.