# TWIT Java Cheat Sheet

## Quick Command Reference

### Repository Operations
```bash
twit init [path]              # Initialize new repository
twit status                   # Show working tree status
```

### Object Commands
```bash
twit hash-object FILE         # Compute SHA-1 of file
twit hash-object -w FILE      # Store object in repository
twit hash-object -t TYPE FILE # Specify object type (blob/commit/tree/tag)

twit cat-file TYPE SHA        # Display object contents
```

### Tree Operations
```bash
twit ls-tree TREE             # List tree contents
twit ls-tree -r TREE          # Recursive listing
twit checkout COMMIT PATH     # Extract commit to directory
```

### History and References
```bash
twit log [COMMIT]             # Show commit history (Graphviz)
twit show-ref                 # List all references
twit rev-parse NAME           # Resolve name to SHA-1
twit rev-parse --twit-type TYPE NAME  # Resolve and follow to type
```

### Staging and Committing
```bash
twit add FILE...              # Stage file(s)
twit rm FILE...               # Remove and unstage file(s)
twit commit -m "message"      # Create commit from index
twit ls-files                 # List staged files
twit ls-files --verbose       # Show detailed index info
```

### Tags
```bash
twit tag                      # List tags
twit tag NAME [OBJECT]        # Create lightweight tag
twit tag -a NAME [OBJECT]     # Create annotated tag
```

### Ignoring Files
```bash
twit check-ignore PATH...     # Check if paths are ignored
```

## Git Internals Quick Reference

### Object Storage Format
```
┌─────────────────────────────────────┐
│ Object Structure                    │
├─────────────────────────────────────┤
│ Header: <type> <size>\0             │
│ Content: <actual data>              │
│ ─────────────────────────────       │
│ Compressed: zlib                    │
│ Hashed: SHA-1                       │
│ Stored: .git/objects/XX/YYYY...     │
└─────────────────────────────────────┘
```

### Object Types

#### Blob (File)
```
blob 13\0Hello, World!
```

#### Tree (Directory)
```
<mode> blob <sha> filename1
<mode> tree <sha> dirname
<mode> blob <sha> filename2
```

#### Commit (Snapshot)
```
tree <tree-sha>
parent <parent-sha>
author Name <email> timestamp timezone
committer Name <email> timestamp timezone

Commit message
```

#### Tag (Named Reference)
```
object <object-sha>
type <object-type>
tag <tag-name>
tagger Name <email> timestamp timezone

Tag message
```

## File System Layout

```
.git/
├── HEAD                    # Current branch or commit
├── config                  # Repository configuration
├── description             # Repository description
├── index                   # Staging area (binary)
├── objects/                # Object database
│   ├── XX/                 # First 2 chars of SHA
│   │   └── YYYY...         # Remaining 38 chars
│   └── pack/               # Packed objects (not implemented)
├── refs/                   # References
│   ├── heads/              # Branches
│   │   └── master          # Master branch
│   └── tags/               # Tags
│       └── v1.0            # Tag v1.0
└── info/
    └── exclude             # Local ignore patterns
```

## Common Patterns

### Create Repository and First Commit
```bash
twit init myproject
cd myproject
echo "Hello" > README.md
twit add README.md
twit commit -m "Initial commit"
```

### View Object Chain
```bash
# Get commit
COMMIT=$(cat .git/refs/heads/master)

# Get tree from commit
TREE=$(twit cat-file commit $COMMIT | grep tree | cut -d' ' -f2)

# List tree
twit ls-tree $TREE

# Get blob from tree
BLOB=$(twit ls-tree $TREE | head -1 | awk '{print $3}')

# View blob
twit cat-file blob $BLOB
```

### Check Object Type
```bash
SHA="abc123..."
TYPE=$(cat .git/objects/${SHA:0:2}/${SHA:2} | \
  python3 -c "import zlib,sys; \
  data=zlib.decompress(sys.stdin.buffer.read()); \
  print(data.split(b' ')[0].decode())")
echo $TYPE  # blob, tree, commit, or tag
```

### Manual Object Creation
```bash
# Create blob manually
echo -n "Hello" | \
  (echo -n "blob 5\0"; cat) | \
  sha1sum
# Output: 5ab2f8a4323abfdde... (without compression)

# Store it
echo -n "Hello" | \
  (echo -n "blob 5\0"; cat) | \
  python3 -c "import zlib,sys; \
  sys.stdout.buffer.write(zlib.compress(sys.stdin.buffer.read()))" \
  > .git/objects/5a/b2f8a4323abfdde...
```

## Index File Format

```
┌──────────────────────────────────────┐
│ HEADER (12 bytes)                    │
├──────────────────────────────────────┤
│ "DIRC"                   (4 bytes)   │
│ Version (2)              (4 bytes)   │
│ Entry count              (4 bytes)   │
├──────────────────────────────────────┤
│ ENTRIES (variable)                   │
├──────────────────────────────────────┤
│ For each entry:                      │
│   - ctime                (8 bytes)   │
│   - mtime                (8 bytes)   │
│   - dev                  (4 bytes)   │
│   - ino                  (4 bytes)   │
│   - mode                 (4 bytes)   │
│   - uid                  (4 bytes)   │
│   - gid                  (4 bytes)   │
│   - size                 (4 bytes)   │
│   - sha1                (20 bytes)   │
│   - flags                (2 bytes)   │
│   - path         (variable + null)   │
│   - padding       (to 8-byte align)  │
└──────────────────────────────────────┘
```

## SHA-1 Hash Examples

| Content | SHA-1 Hash |
|---------|------------|
| Empty blob | `e69de29bb2d1d6434b8b29ae775ad8c2e48c5391` |
| "test\n" | `9daeafb9864cf43055ae93beb0afd6c7d144bfa4` |
| "Hello, World!\n" | `8ab686eaf3c0915caf5c68d12f560a9fe3e4d670` |

## Common Operations Explained

### git add
```
Worktree → Read file → Create blob → Update index
```

### git commit
```
Index → Create tree → Create commit → Update branch
```

### git status
```
Compare: HEAD ↔ Index ↔ Worktree
```

### git checkout
```
Commit → Get tree → Recursively extract → Worktree
```

## File Modes

```
100644  Regular file (non-executable)
100755  Regular file (executable)
120000  Symbolic link
040000  Directory (tree)
160000  Gitlink (submodule)
```

## Configuration Files

### .git/config
```ini
[core]
    repositoryformatversion = 0
    filemode = false
    bare = false
```

### ~/.gitconfig
```ini
[user]
    name = Your Name
    email = your.email@example.com
```

## Debugging Tips

### View Raw Object
```bash
SHA="abc123..."
python3 -c "import zlib; \
  f=open('.git/objects/${SHA:0:2}/${SHA:2}','rb'); \
  print(zlib.decompress(f.read()))"
```

### Verify Object Integrity
```bash
# Compute hash of decompressed object
python3 -c "import zlib,hashlib; \
  f=open('.git/objects/XX/YYY','rb'); \
  data=zlib.decompress(f.read()); \
  print(hashlib.sha1(data).hexdigest())"
# Should match XXYYYY...
```

### Find All Objects
```bash
find .git/objects -type f | \
  sed 's|.git/objects/||; s|/||' | \
  sort
```

### Count Objects by Type
```bash
for obj in .git/objects/*/*; do
  python3 -c "import zlib; \
    f=open('$obj','rb'); \
    data=zlib.decompress(f.read()); \
    print(data.split(b' ')[0].decode())"
done | sort | uniq -c
```

## Performance Tips

1. **Batch Operations**: Use `twit add file1 file2 file3` instead of multiple calls
2. **Cache Objects**: Keep frequently-used objects in memory
3. **Use Index**: Check timestamps before computing hashes
4. **Minimize I/O**: Read full files once, not in chunks

## Common Errors and Solutions

### Error: "Not a Git repository"
```bash
# Solution: Initialize or cd to repository root
twit init .
# or
cd $(git rev-parse --show-toplevel)
```

### Error: "Malformed object"
```bash
# Cause: Corrupted object or wrong hash
# Solution: Re-create object or restore from backup
twit hash-object -w file.txt
```

### Error: "Index file missing"
```bash
# Cause: Never staged anything
# Solution: Normal for new repository, stage something
twit add file.txt
```

## Testing Compatibility

```bash
# Create with twit
twit init test
cd test
echo "test" > file.txt
twit add file.txt
twit commit -m "Test"

# Verify with git
git log --oneline
git cat-file -p HEAD
git ls-tree HEAD
git status

# Should all work!
```

## Quick Build and Run

```bash
# Build
mvn clean package

# Run
java -jar target/twit-1.0-SNAPSHOT.jar <command> [args]

# Or with alias
alias twit='java -jar target/twit-1.0-SNAPSHOT.jar'
twit <command> [args]
```

## Java API Usage Example

```java
// Initialize repository
GitRepository repo = GitRepository.create(Paths.get("."));

// Store a blob
GitBlob blob = new GitBlob();
blob.setBlobData("Hello, World!".getBytes());
String sha = ObjectIO.objectWrite(blob, repo);

// Read it back
GitBlob read = (GitBlob) ObjectIO.objectRead(repo, sha);
System.out.println(new String(read.getBlobData()));

// Create commit
GitCommit commit = new GitCommit();
commit.init();
Map<String, Object> kvlm = commit.getKvlm();
kvlm.put("tree", treeSha.getBytes());
kvlm.put("author", "You <you@example.com>".getBytes());
kvlm.put(null, "Commit message\n".getBytes());
String commitSha = ObjectIO.objectWrite(commit, repo);
```

## Remember

- **Objects are immutable** - never modify, always create new
- **Branches are pointers** - just refs to commits
- **Index is a snapshot** - what you'll commit
- **Three trees** - HEAD, Index, Worktree
- **Content-addressed** - same content = same hash

---

**Quick Reference**: [twit.thb.lt](https://twit.thb.lt) | **Docs**: [git-scm.com](https://git-scm.com)
