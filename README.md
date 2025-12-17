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

| Command | Description | Example |
|---------|-------------|---------|
| `init` | Initialize a new repository | `twit init [path]` |
| `hash-object` | Compute object ID and optionally store it | `twit hash-object [-w] [-t TYPE] FILE` |
| `cat-file` | Display object contents | `twit cat-file <type> <object>` |
| `ls-tree` | List contents of a tree object | `twit ls-tree [-r] <tree>` |
| `checkout` | Checkout a commit into a directory | `twit checkout <commit> <path>` |
| `log` | Show commit history (Graphviz format) | `twit log [commit]` |
| `show-ref` | List references | `twit show-ref` |
| `tag` | Create or list tags | `twit tag [-a] [name] [object]` |
| `rev-parse` | Parse revision identifiers | `twit rev-parse [--twit-type TYPE] <name>` |
| `ls-files` | List files in the index | `twit ls-files [--verbose]` |
| `check-ignore` | Check if paths are ignored | `twit check-ignore <path>...` |
| `status` | Show working tree status | `twit status` |
| `add` | Add file contents to the index | `twit add <path>...` |
| `rm` | Remove files from index and worktree | `twit rm <path>...` |
| `commit` | Record changes to the repository | `twit commit -m <message>` |

## Acknowledgements
Based on "Write Yourself a Git" tutorial by Thibault Polge