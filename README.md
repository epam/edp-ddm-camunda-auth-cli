# camunda-auth-cli

The cli application that creates authorizations in Camunda BPM based on input files configurations.

### Running the application:
1. Build Jar file: `mvn install`
2. Run jar file: `java -jar <file-name>.jar <input parameters>`

### Required input parameters:
* **BPMS_URL** - the url that will be used for communication with `bpms` service (`bpms` service is used for communication with Camunda BPM through REST api)
* **BPMS_TOKEN** - path to file with JWT token for communication with `bpms`. The user MUST have `camunda-admin` role (only `camunda-admin` users have permission for authorization creation)
* **AUTH_FILES** - the list of config files (YAML) divided by comma. *The example below.*

#### Optional input parameters:
* `logging.level.com.epam.digital.data.platform.auth.generator` (`string|default - info`) - change logging level  
Example: `--logging.level.com.epam.digital.data.platform.auth.generator=debug`
### Example of usage:
`java -jar app.jar --BPMS_URL=http://localhost:8080 --BPMS_TOKEN=C:/token.txt --AUTH_FILES=C:/officer-config.yml,C:/citizen-config.yml`

### Example of the file configuration:
*officer.yml*
```
authorization:
  realm: 'officer'
  process_definitions:
    - process_definition_id: 'business-process1'
      process_name: '<name>'
      process_description: '<description>'
      roles:
        - officer
        - citizen
    - process_definition_id: 'business-process2'
      process_name: '<name>'
      process_description: '<description>'
      roles:
        - officer_1
        - officer_2
        - officer_3
    - process_definition_id: 'business-process3'
      process_name: '<name>'
      process_description: '<description>'
      roles:
        - officer_1
        - officer_2
        - officer_3
```