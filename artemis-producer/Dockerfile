FROM openjdk:8-jre-slim-buster
LABEL maintainer="labs@aspirecsl.com"
# Make sure pipes are considered to determine success, see: https://github.com/hadolint/hadolint/wiki/DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

######################################
###     Set proxy if necessary     ###
#------------------------------------#
#ENV http_proxy http://proxy:port/
#ENV https_proxy http://proxy:port/
#ENV no_proxy="127.0.0.1, localhost"
######################################

ARG JAR_FILE
COPY ${JAR_FILE} /artemis-producer-app.jar
COPY entrypoint.sh /entrypoint.sh
RUN chmod ugo+x /entrypoint.sh
############################################################
###          N/W debug tools for DEV phase ONLY          ###
#----------------------------------------------------------#
#RUN apt-get update && \
#apt-get install -y --no-install-recommends telnet \
#dnsutils net-tools iputils-ping inetutils-traceroute && \
#rm -rf /var/lib/apt/lists/*
############################################################

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java -jar /artemis-producer-app.jar"]