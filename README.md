# twit - a git clone

## Add a shell script
Add a shell script to execute the jar file

```bash
java -jar root/build/libs/twit-1.0.jar "$@"
```

## Make twit a Global Command
 - Name the file twit (don't add sh extention)
 - Move the file to /usr/local/bin/
 - Make it executable

```bash
sudo mv twit /usr/local/bin/
sudo chmod +x /usr/local/bin/twit
```

## Commands

Build Command (to build the jar file)

```shell
./gradlew clean build
```

You can run commands like:
```bash
twit init 
twit commit
```
