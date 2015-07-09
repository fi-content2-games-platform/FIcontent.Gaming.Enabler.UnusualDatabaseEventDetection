FROM maven:3.3.1-jdk-7

RUN apt-get -y update \
    && DEBIAN_FRONTEND=noninteractive apt-get -y install apg wget netcat \
    && wget https://godist.herokuapp.com/projects/ddollar/forego/releases/current/linux-amd64/forego -P /usr/local/bin \
    && chmod +x /usr/local/bin/forego

WORKDIR /root/target

COPY . /root

RUN cd .. \
    && sed -i 's/^logfile/# logfile/g' bin/config.properties \
    && cp bin/config.properties . \
    && mv etc/waitForPort*.sh /usr/local/bin/ \
    && mvn package

EXPOSE 4567

CMD ["/root/etc/container-entrypoint.sh"]