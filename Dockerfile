FROM tomcat:9.0.83-jdk8-temurin-jammy

ENV DIR=/home/javaStudy
ENV PORT=8080
ENV NODE_VERSION=v10

WORKDIR ${DIR}

ADD . .

RUN apt update
RUN apt install -y git curl

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
CMD nvm install ${NODE_VERSION} && nvm use ${NODE_VERSION}
