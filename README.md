# Shuttle Backend

##About Shuttle

Shuttle makes easy, secure and fast mobile apps distribution possible. It was created by Polidea as an internal tool, 
which allows delivering iOS and Android mobile applications to stakeholders from the very beginning of the cooperation 
with the app creators. To try out our solution, simply use our code and if you need frontend and mobile app—contact 
us directly! We’ll be more than happy to help, integrate the solution or create a brand new frontend. 
More at [www.polidea.com](www.polidea.com).

## Prerequisites

* JDK 8
* Docker with `docker-compose`

## How to run?

Run app locally:
* `./gradlew clean test`: build project and run all tests
* `./gradlew bootRun`: start app and serve it on [http://localhost:8080](http://localhost:8080)

You can turn on one of defined profiles, eg.: `./gradlew bootRun -Dspring.profiles.active=development`

There are following profiles meant to be used:
* `development` (default): **for local development**, with embedded database
* `testing`: for testing environment **to play around on prepared data**, with real database, to be used in pair with development Docker containers
* `production`: for production environment **to provide released app for clients**, with real database, to be used in pair with production Docker containers
* `integrationTests`: **for integration tests**, with embedded database

Run app in development environment inside Docker containers with real database and proxy server:

* `docker-compose up --build`: build and run Docker images

## IntelliJ IDEA configuration

After you import a project to IntelliJ IDEA, you want to perform following steps:

1. "Add VCS root" so IDEA will synchronize with Git repository (there will be prompt to add VCS root)
2. Revert changes made on files inside `.idea/` directory: IntelliJ make some configuration changes
   when project is imported but we want to use project configuration provided in repository.
   
If you want to run app from the IntelliJ IDEA, you need to do the following:

1. Install `Spring Boot` plugin.
2. Create "Run Configuration" of type `Spring Boot` with following parameters:
   - "Name": it's your choice, eg. `ShuttleApplication`
   - "Single instance only": yes
   - "Main class": `com.polidea.shuttle.ShuttleApplication`
   - "Use classpath of module": `Backend_main`

## Initial database setup

When Backend starts up, it checks whether required data is loaded into database.
Which data is loaded depends on active Spring profiles.

In general:
* **There is user `admin@your-shuttle.com` with `admin_access` Global Permission.** They can log-in
  into Admin Panel, he can receive Verification Code too (and Access Token with use of this code).
* **There is user `continuous.deployment@your-shuttle.com` with `can_create_build` Global
  Permission.** He can create Builds with use of token defined in `application.yml` as
  `shuttle.tokens.continuous-deployment`.

## E-mails

We are working on e-mail templates (for ease of development) but our code is using
inlined versions of these e-mails.

To use e-mail feature you have to create account on [mailgun]( https://www.mailgun.com/ ) 
and add your credentials to application-development.yml.

To update e-mail please go through following steps:

1. Edit `<email_name>_template.html`
2. Use [Responsive Email Inliner]( http://foundation.zurb.com/emails/inliner-v2.html )
   to prepare inlined version your template
3. Update `<email_name>_inlined.html` with inlined result
4. Commit **without automatic file formatting** (to not break inlined file)

## Avatars and AWS S3

Shuttle comes with set of default Avatars for users (copies of them available
in `assets/default-avatars/` directory). Moreover new avatars
can be uploaded by users. All these image files are stored on
[AWS S3]( http://docs.aws.amazon.com/cli/latest/reference/s3/ ).

We have to make sure that default avatars have same 9 URLs as provided 
in `application.yml`.

In order to work with S3 outside the Shuttle code you will need
[AWS CLI]( https://aws.amazon.com/cli/ ). It can be installed with `brew install awscli`.
You would probably configure auto-completion too with `complete -C '/usr/local/bin/aws_completer' aws`
([for more details look here]( http://docs.aws.amazon.com/cli/latest/userguide/cli-command-completion.html#cli-command-completion-enable )).
[High-level commands reference]( http://docs.aws.amazon.com/cli/latest/userguide/using-s3-commands.html )
can be helpful too.

To access S3 with AWS CLI, you will need credentials configured with `aws configure`.

To check that you have access to S3:

1. `aws s3 ls s3://<bucket_name_for_given_environment>` to list top-level files in a bucket
2. `echo $?` to check that error code is `0`, so there were no errors

## Database schema and initial data

For all profiles database is configured with [Flyway]( https://flywaydb.org/ )
migration files placed in `src/main/resources/db/migration`. Valid migration
files have name matching pattern `V<version_number>__<description>.sql`.
You can read more about "Versioned Migrations"
[in Flyway documentation]( https://flywaydb.org/documentation/migration/java ).

Moreover there are two sets of data which are loaded into database on application
start. Every time application is started it checks whether those data sets are
present in database, and creates them if not. These data sets are described in
class `InitialDataLoad` and turned on by having profiles active:
* `mandatoryData`: data required for application to work properly
* `developmentData`: extra data for development and testing purpose

## Push Notifications

For push notification feature it is required to create FCM 
([Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)) project and add credentials 
to `application-development.yml`.

## Authorization via Google

To use Google OAuth Authorization you have to create account and add google token to `application.yml` file.
Read more [here](https://developers.google.com/identity/protocols/OAuth2).

## Documentation on Postman

It is possible to check API via Postman by importing collections
and environment from `./postman` directory.

## Documentation on Apiary

We are using Apiary for documentation of API here: [http://docs.shuttle2.apiary.io/](http://docs.shuttle2.apiary.io/)
