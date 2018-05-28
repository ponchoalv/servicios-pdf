FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/servicios_pdf.jar /servicios_pdf/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/servicios_pdf/app.jar"]
