FROM tomcat:9.0.83-jdk8-temurin-jammy

ENV PORT=8080
ENV NODE_VERSION=v14
ENV DIR=/home/javaStudy
ENV PATH="${PATH}:${DIR}/node_modules/.bin/"

WORKDIR ${DIR}

ADD . .

RUN apt update
RUN apt install -y git curl

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
CMD nvm install ${NODE_VERSION} && nvm use ${NODE_VERSION} ; npm install @vue/cli

CMD catalina.sh run
