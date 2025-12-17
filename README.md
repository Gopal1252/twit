# twit - A minimal git implementation in java for learning purposes

## Add a shell script
Add a shell script (twit.sh) to execute the jar file

```bash
java -cp "root/build/libs/twit-1.0.jar" com.yourName.root.Main "$@"
```

## Make twit a Global Command
 - Rename the twit.sh shell script to twit (remove the extention)
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

## Acknowledgements
Based on "Write Yourself a Git" tutorial by Thibault Polge