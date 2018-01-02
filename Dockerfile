FROM openjdk:8

EXPOSE 8080

WORKDIR /shuttle

# Copy all sources (except those ignored by .dockerignore) into `<WORKDIR>/.`
COPY . .

# In tests we use embedded Postgresq which cannot be run under root. For more info see:
# https://github.com/yandex-qatools/postgresql-embedded/commit/ef75c5a60ec73347792b383c4c903942af95a9fb#diff-04c6e90faac2675aa89e2176d2eec7d8R69
# In order to fix it we can add non-root user and make him owner of sources and run app in his name.
RUN useradd -ms /bin/bash non_root_user \
    && chown -Rv non_root_user .
USER non_root_user

# Download all dependencies and build project. In result:
# 1) there will be less to do when starting container,
# 2) dependencies will be frozen, not downloaded at runtime.
RUN ./gradlew clean assemble

ARG environment="testing"
RUN echo "Selected environment: ${environment}"
ENV main_spring_profile=${environment}

CMD ./gradlew bootRun -Dspring.profiles.active=${main_spring_profile}
