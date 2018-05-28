# servicios-pdf

Sistema desarrollado usando Clojure 1.9.

El sistema una vez iniciado expone la siguiente lista de end-points:

`POST /api/pdf-from-jasper-template`

`POST /api/add-fields-on-all-pages`
 
`POST /api/pdf-from-word-template`
 
` POST /api/pfd-from-word-upload`
 
 `GET /api/converter-list`
 
 `PUT /api/add-converter`

 `DELETE /api/remove-converter`
   
   La aplicación cuenta con un portal swagger donde se documentan y se permite probar los end-points:
   `/swagger-ui`
   
   El puerto por defecto es el 3000.
   
 La arquitectura es la siguiente:
 1 instacia de servicios-pdf.jar administrar *(0 o varias) instancias de `documents4j-server-standalone-1.0.3-shaded.jar`
 ([jdocuments4j][2]). Es necesario que este instalado MS WORD 2010 o superior en el servidor donde este corriendo el jar de documents4j.     

[2]: http://www.documents4j.com

## Pre-requisitos

Para compilar el código es necesario installar la JDK 1.8 y el aplicativo  [Leiningen][1] 2.0+.

Para ejecutar se necesita una JRE 1.8. También se deben configurar las variables de entorno JASPER_TEMPLATES donde se alojan los templates .jasper y TEMPLATE_FOLDER donde se alojan los templates .docx (ambas variables pueden apuntar al mismo directorio).

* Ejemplo: SET TEMPLATE_FOLDER=C:/Users/aalvarez/Desktop/programas/servicios-pdf/template/

*Usar como separador de directorio "/" o "\\\\"

Para convertir los documentos de word a PDF se precisa una instalación de MS Word con licencia y `documents4j-server-standalone-1.0.3-shaded.jar`

[1]: https://github.com/technomancy/leiningen

## Iniciar el sistema

Para inciar la aplicación ejecutar:

    java -jar servicios-pdf.jar 

Luego, si se requiere utilizar los end-points:

`POST /api/pdf-from-word-template`
 
`POST /api/pfd-from-word-upload`

Se debe iniciar al menos una instancia de `documents4j-server-standalone-1.0.3-shaded.jar`:

    `java -jar documents4j-server-standalone-1.0.3-shaded.jar http://hostname:puerto` (usar el hostname del equipo no localhost, sino no podra ser accedido desde otro servidor).
    
Una vez iniciado el servidor de conversión, se puede agregar el mismo usando el end-point  `PUT /api/add-converter` 
con el cuerpo:
 
 `{ "converter-name": "nombre-1",
"converter-uri": "http://hostname:puerto"}`.

*aclaraciones: El portal swagger no maneja tipos de archivos 'application/pdf' por lo tanto, para comprobar los archivos generados deberán usar las herramientas para desarrolladores de google chrome (copiar respuesta, en trafico de red y luego pegar en la barra de direcciones del mismo) 
                                                                                                                    
                                                                                                                  
## Referencias del código

Para ejecutar la aplicación desde el código, utilizando Leiningen ejecutar parado en el directorio raíz del proyecto (servicios-pdf):
`lein run`.

Para compilar y generar un JAR con todas las librerías dentro (uberjar) ejecutar: `lein uberjar` el jar se generará en la carpeta `target/uberjar`.

La lógica de conversión de word a PDF se encuentra en los archivos `docx-reports/core.clj` y `utils/core.clj`.

La lógica implementada para agregar campos de firma a los pdf y generar un pdf desde un template jasper esta en `jasper/core.clj`.

La lógica implementada para el balanceador de de conversores pdf se encuentra en el archivo `converter/core.clj`.

La lógica donde se exponen los endpoints se encuentra en `routes/services.clj`. 

                                                                                                                 