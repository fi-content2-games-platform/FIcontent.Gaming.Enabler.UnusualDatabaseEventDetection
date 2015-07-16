FROM maven:3.3.1-jdk-7

WORKDIR /root

ENV UDED_DB_HOST='' UDED_DB_USER='' UDED_DB_PASSWORD='' UDED_DB_NAME=''

COPY . /root

RUN sed -i 's/^logfile/# logfile/g' bin/config.properties \
    && mvn package \
    && ln -s "$(pwd)/bin/config.properties" \
    && ln -s "$(pwd)/bin/leaderboard-rules.txt"

EXPOSE 4567

CMD ["etc/cmd.sh", "bin/config.properties"]