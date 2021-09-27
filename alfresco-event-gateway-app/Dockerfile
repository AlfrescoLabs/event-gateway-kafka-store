FROM alfresco/alfresco-base-java:11.0.12-centos-7@sha256:6abdbfd14492fb78ae5d865eb25627bb241b11da406ef8df91a77716a3538f36
# required to avoid "java.lang.NoClassDefFoundError: Could not initialize class sun.awt.X11FontManager"
RUN yum install -y dejavu-sans-fonts fontconfig
ENV PORT 8080
EXPOSE 8080
ARG JAR_FILE=target/*.jar
WORKDIR /opt
COPY entrypoint.sh ./entrypoint.sh
RUN chmod +x ./entrypoint.sh
COPY $JAR_FILE app.jar

ENTRYPOINT ["./entrypoint.sh"]
CMD ["-jar", "app.jar"]
