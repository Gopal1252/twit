# Diagrams

## Class Diagram

```mermaid
classDiagram
    %% Main Entry Point
    class Main {
        -Map~String, Command~ commands
        +main(String[] args)
    }

    %% Command Interface and Implementations
    class Command {
        <<interface>>
        +execute(String[] args)
    }

    class InitCommand {
        +execute(String[] args)
    }

    class CatFileCommand {
        +execute(String[] args)
    }

    class HashObjectCommand {
        +execute(String[] args)
    }

    class LogCommand {
        +execute(String[] args)
        -logGraphviz(repo, sha, seen)
    }

    class CheckoutCommand {
        +execute(String[] args)
        -treeCheckout(repo, tree, path)
    }

    class LsTreeCommand {
        +execute(String[] args)
        -lsTree(repo, ref, recursive, prefix)
    }

    class ShowRefCommand {
        +execute(String[] args)
        -showRef(refs, withHash, prefix)
    }

    class TagCommand {
        +execute(String[] args)
        -showTags(tags, prefix)
        -tagCreate(repo, name, ref, createTagObject)
        -refCreate(repo, refName, sha)
    }

    class RevParseCommand {
        +execute(String[] args)
    }

    class LsFilesCommand {
        +execute(String[] args)
    }

    class CheckIgnoreCommand {
        +execute(String[] args)
    }

    class StatusCommand {
        +execute(String[] args)
        -statusBranch(repo)
        -statusHeadIndex(repo, index)
        -statusIndexWorktree(repo, index)
        -treeToDict(repo, ref, prefix)
    }

    class AddCommand {
        +execute(String[] args)
        -add(repo, paths)
        -rm(repo, paths, delete, skipMissing)
    }

    class RmCommand {
        +execute(String[] args)
        -rm(repo, paths, delete, skipMissing)
    }

    class CommitCommand {
        +execute(String[] args)
        -commit(repo, message)
        -treeFromIndex(repo, index)
        -gitconfigUserGet()
    }

    %% Core - Repository
    class GitRepository {
        -Path worktree
        -Path gitdir
        -Properties config
        +GitRepository(Path path, boolean force)
        +repoPath(String... parts) Path
        +repoFile(boolean mkdir, String... parts) Path
        +repoDir(boolean mkdir, String... parts) Path
        +create(Path path)$ GitRepository
        +find()$ GitRepository
        +find(Path path, boolean required)$ GitRepository
        +getWorktree() Path
        +getGitdir() Path
        +getConfig() Properties
        -defaultConfig()$ Properties
    }

    %% Core - Git Objects
    class GitObject {
        <<abstract>>
        +getFormat() String
        +serialize() byte[]
        +deserialize(byte[] data)
        +init()
    }

    class GitBlob {
        -byte[] blobData
        +GitBlob()
        +GitBlob(byte[] data)
        +getFormat() String
        +serialize() byte[]
        +deserialize(byte[] data)
        +getBlobData() byte[]
        +setBlobData(byte[] data)
    }

    class GitCommit {
        -Map~String, Object~ kvlm
        +GitCommit()
        +GitCommit(byte[] data)
        +getFormat() String
        +serialize() byte[]
        +deserialize(byte[] data)
        +init()
        +getKvlm() Map
        +setKvlm(Map kvlm)
    }

    class GitTree {
        -List~GitTreeLeaf~ items
        +GitTree()
        +GitTree(byte[] data)
        +getFormat() String
        +serialize() byte[]
        +deserialize(byte[] data)
        +init()
        +getItems() List
    }

    class GitTag {
        +GitTag()
        +getFormat() String
    }

    class GitTreeLeaf {
        -String mode
        -String path
        -String sha
        +GitTreeLeaf(String mode, String path, String sha)
        +getMode() String
        +getPath() String
        +getSha() String
    }

    %% Core - Index
    class GitIndex {
        -int version
        -List~GitIndexEntry~ entries
        +GitIndex()
        +GitIndex(int version, List entries)
        +getVersion() int
        +getEntries() List
        +setEntries(List entries)
    }

    class GitIndexEntry {
        -long[] ctime
        -long[] mtime
        -long dev
        -long ino
        -int modeType
        -int modePerms
        -long uid
        -long gid
        -long fsize
        -String sha
        -boolean flagAssumeValid
        -int flagStage
        -String name
        +GitIndexEntry(...)
        +getCtime() long[]
        +getMtime() long[]
        +getDev() long
        +getIno() long
        +getModeType() int
        +getModePerms() int
        +getUid() long
        +getGid() long
        +getFsize() long
        +getSha() String
        +isFlagAssumeValid() boolean
        +getFlagStage() int
        +getName() String
    }

    %% Core - Ignore
    class GitIgnore {
        -List~List~IgnoreRule~~ absolute
        -Map~String, List~IgnoreRule~~ scoped
        +GitIgnore(List absolute, Map scoped)
        +getAbsolute() List
        +getScoped() Map
    }

    class IgnoreRule {
        -String pattern
        -boolean include
        +IgnoreRule(String pattern, boolean include)
        +getPattern() String
        +isInclude() boolean
    }

    %% Utilities
    class ObjectIO {
        <<utility>>
        +objectRead(repo, sha)$ GitObject
        +objectWrite(obj, repo)$ String
        +objectHash(in, fmt, repo)$ String
        -findByte(array, target, start)$ int
        -bytesToHex(bytes)$ String
    }

    class KVLMParser {
        <<utility>>
        +parse(byte[] raw)$ Map
        -parse(raw, start, dict)$ Map
        +serialize(Map kvlm)$ byte[]
        -findByte(array, target, start)$ int
    }

    class TreeParser {
        <<utility>>
        +parse(byte[] raw)$ List
        -parseOne(raw, start)$ ParseResult
        +serialize(List items)$ byte[]
        -findByte(array, target, start)$ int
    }

    class ParseResult {
        +int newPos
        +GitTreeLeaf leaf
        +ParseResult(int newPos, GitTreeLeaf leaf)
    }

    class IndexIO {
        <<utility>>
        +indexRead(GitRepository repo)$ GitIndex
        +indexWrite(repo, index)$ void
        -bytesToHex(bytes)$ String
        -hexToBytes(hex)$ byte[]
    }

    class RefResolver {
        <<utility>>
        +refResolve(repo, ref)$ String
        +objectFind(repo, name)$ String
        +objectFind(repo, name, fmt, follow)$ String
        -objectResolve(repo, name)$ List
        +refList(repo, path)$ Map
    }

    class IgnoreParser {
        <<utility>>
        +gitignoreRead(repo)$ GitIgnore
        -gitignoreParse(lines)$ List
        -gitignoreParse1(raw)$ IgnoreRule
        +checkIgnore(rules, path)$ boolean
        -checkIgnoreScoped(rules, path)$ Boolean
        -checkIgnoreAbsolute(rules, path)$ boolean
        -checkIgnore1(rules, path)$ Boolean
        -fnmatch(path, pattern)$ boolean
    }

    %% Relationships - Main to Commands
    Main --> Command : uses
    Command <|.. InitCommand : implements
    Command <|.. CatFileCommand : implements
    Command <|.. HashObjectCommand : implements
    Command <|.. LogCommand : implements
    Command <|.. CheckoutCommand : implements
    Command <|.. LsTreeCommand : implements
    Command <|.. ShowRefCommand : implements
    Command <|.. TagCommand : implements
    Command <|.. RevParseCommand : implements
    Command <|.. LsFilesCommand : implements
    Command <|.. CheckIgnoreCommand : implements
    Command <|.. StatusCommand : implements
    Command <|.. AddCommand : implements
    Command <|.. RmCommand : implements
    Command <|.. CommitCommand : implements

    %% Relationships - Commands to Core
    InitCommand --> GitRepository : creates
    CatFileCommand --> GitRepository : uses
    CatFileCommand --> ObjectIO : uses
    HashObjectCommand --> GitRepository : uses
    HashObjectCommand --> ObjectIO : uses
    LogCommand --> GitRepository : uses
    LogCommand --> ObjectIO : uses
    LogCommand --> RefResolver : uses
    CheckoutCommand --> GitRepository : uses
    CheckoutCommand --> GitTree : uses
    CheckoutCommand --> ObjectIO : uses
    LsTreeCommand --> GitRepository : uses
    LsTreeCommand --> GitTree : uses
    ShowRefCommand --> GitRepository : uses
    ShowRefCommand --> RefResolver : uses
    TagCommand --> GitRepository : uses
    TagCommand --> GitTag : creates
    RevParseCommand --> GitRepository : uses
    RevParseCommand --> RefResolver : uses
    LsFilesCommand --> GitRepository : uses
    LsFilesCommand --> GitIndex : uses
    LsFilesCommand --> IndexIO : uses
    CheckIgnoreCommand --> GitRepository : uses
    CheckIgnoreCommand --> GitIgnore : uses
    CheckIgnoreCommand --> IgnoreParser : uses
    StatusCommand --> GitRepository : uses
    StatusCommand --> GitIndex : uses
    StatusCommand --> IndexIO : uses
    StatusCommand --> IgnoreParser : uses
    AddCommand --> GitRepository : uses
    AddCommand --> GitIndex : uses
    AddCommand --> IndexIO : uses
    RmCommand --> GitRepository : uses
    RmCommand --> GitIndex : uses
    RmCommand --> IndexIO : uses
    CommitCommand --> GitRepository : uses
    CommitCommand --> GitCommit : creates
    CommitCommand --> GitTree : creates
    CommitCommand --> GitIndex : uses
    CommitCommand --> IndexIO : uses

    %% Relationships - Object Hierarchy
    GitObject <|-- GitBlob : extends
    GitObject <|-- GitCommit : extends
    GitObject <|-- GitTree : extends
    GitCommit <|-- GitTag : extends
    GitTree --> GitTreeLeaf : contains
    TreeParser --> ParseResult : uses
    TreeParser --> GitTreeLeaf : creates

    %% Relationships - Index
    GitIndex --> GitIndexEntry : contains

    %% Relationships - Ignore
    GitIgnore --> IgnoreRule : contains

    %% Relationships - Utilities to Objects
    ObjectIO --> GitObject : reads/writes
    ObjectIO --> GitBlob : creates
    ObjectIO --> GitCommit : creates
    ObjectIO --> GitTree : creates
    ObjectIO --> GitTag : creates
    KVLMParser ..> GitCommit : parses
    KVLMParser ..> GitTag : parses
    TreeParser ..> GitTree : parses
    IndexIO --> GitIndex : reads/writes
    IndexIO --> GitIndexEntry : reads/writes
    RefResolver --> GitRepository : uses
    RefResolver --> ObjectIO : uses
    IgnoreParser --> GitIgnore : creates
    IgnoreParser --> IgnoreRule : creates
    IgnoreParser --> GitRepository : uses
    IgnoreParser --> GitIndex : uses
    IgnoreParser --> ObjectIO : uses
```

## Architecural Layers

```mermaid
graph TB
    subgraph "Entry Point Layer"
        Main["<b>Main.java</b><br/>Command Dispatcher"]
    end

    subgraph "Command Layer - 15 Commands"
        Init[InitCommand]
        Add[AddCommand]
        Rm[RmCommand]
        Commit[CommitCommand]
        Status[StatusCommand]
        CatFile[CatFileCommand]
        HashObj[HashObjectCommand]
        Log[LogCommand]
        Checkout[CheckoutCommand]
        LsTree[LsTreeCommand]
        ShowRef[ShowRefCommand]
        Tag[TagCommand]
        RevParse[RevParseCommand]
        LsFiles[LsFilesCommand]
        CheckIgnore[CheckIgnoreCommand]
    end

    subgraph "Core Domain Layer"
        subgraph "Repository"
            Repo["<b>GitRepository</b><br/>- worktree<br/>- gitdir<br/>- config"]
        end

        subgraph "Git Objects"
            GitObj["<b>GitObject</b><br/>abstract"]
            Blob["<b>GitBlob</b><br/>file contents"]
            Commit["<b>GitCommit</b><br/>snapshot + metadata"]
            Tree["<b>GitTree</b><br/>directory listing"]
            Tag2["<b>GitTag</b><br/>named reference"]
            TreeLeaf["<b>GitTreeLeaf</b><br/>tree entry"]
        end

        subgraph "Index - Staging Area"
            Index["<b>GitIndex</b><br/>- version<br/>- entries"]
            IndexEntry["<b>GitIndexEntry</b><br/>- path<br/>- sha<br/>- timestamps<br/>- permissions"]
        end

        subgraph "Ignore System"
            Ignore["<b>GitIgnore</b><br/>- absolute rules<br/>- scoped rules"]
            IgnoreRule["<b>IgnoreRule</b><br/>- pattern<br/>- include flag"]
        end
    end

    subgraph "Utility Layer"
        ObjIO["<b>ObjectIO</b><br/>read/write objects<br/>compression/decompression<br/>SHA-1 hashing"]
        IdxIO["<b>IndexIO</b><br/>read/write index<br/>binary format"]
        KVLM["<b>KVLMParser</b><br/>commit/tag format<br/>key-value parsing"]
        TreeParse["<b>TreeParser</b><br/>tree format<br/>binary parsing"]
        RefRes["<b>RefResolver</b><br/>name resolution<br/>ref management"]
        IgnParse["<b>IgnoreParser</b><br/>.gitignore parsing<br/>pattern matching"]
    end

    subgraph "Storage Layer"
        FS["<b>File System</b><br/>.git/objects/<br/>.git/refs/<br/>.git/index<br/>.git/HEAD"]
    end

    %% Main to Commands
    Main --> Init & Add & Rm & Commit & Status
    Main --> CatFile & HashObj & Log & Checkout
    Main --> LsTree & ShowRef & Tag & RevParse
    Main --> LsFiles & CheckIgnore

    %% Commands to Core
    Init --> Repo
    Add & Rm & Commit & Status & LsFiles --> Repo & Index
    CatFile & HashObj & Log & Checkout --> Repo & GitObj
    LsTree --> Repo & Tree
    ShowRef & Tag & RevParse --> Repo
    CheckIgnore --> Repo & Ignore

    %% Object Hierarchy
    GitObj --> Blob & Commit & Tree
    Commit --> Tag2
    Tree --> TreeLeaf

    %% Index Structure
    Index --> IndexEntry

    %% Ignore Structure
    Ignore --> IgnoreRule

    %% Commands to Utilities
    Add & Rm & Commit --> ObjIO & IdxIO
    Status & LsFiles --> IdxIO
    CatFile & HashObj & Log & Checkout --> ObjIO
    ShowRef & Tag & RevParse --> RefRes
    CheckIgnore & Status --> IgnParse
    Commit & Log --> KVLM
    LsTree & Checkout --> TreeParse

    %% Utilities to Core
    ObjIO --> Blob & Commit & Tree & Tag2
    KVLM --> Commit & Tag2
    TreeParse --> Tree & TreeLeaf
    IdxIO --> Index & IndexEntry
    IgnParse --> Ignore & IgnoreRule

    %% Core to Storage
    Repo --> FS
    ObjIO --> FS
    IdxIO --> FS
    RefRes --> FS
```

## Data Flow Diagrams

### init Command

```mermaid
graph TD
    subgraph "Flow 7: git init (Initializing Repository)"
        G1["<b>User: twit init [path]</b>"] --> G2["<b>InitCommand.execute</b>"]
        G2 --> G3["<b>GitRepository.create</b>"]
        G3 --> G4["<b>Create .git/ directory structure</b>"]
        G4 --> G5["<b>dirs: branches, objects, refs/heads, refs/tags</b>"]
        G3 --> G6["<b>Create 'description' file</b>"]
        G3 --> G7["<b>Create 'HEAD' (ref: refs/heads/master)</b>"]
        G3 --> G8["<b>Create 'config' (repoversion: 0)</b>"]
        G5 & G6 & G7 & G8 --> G9["<b>Success: Output initialized path</b>"]
    end
```

### cat-file Command

```mermaid
graph TD
    subgraph "Flow 5: git cat-file (Reading Object)"
        E1["<b>User: wyag cat-file blob SHA</b>"]
        E2["<b>CatFileCommand.execute</b>"]
        E3["<b>ObjectIO.objectRead</b>"]
        E4["<b>Build path .git/objects/XX/YYY</b>"]
        E5["<b>Read compressed file</b>"]
        E6["<b>Decompress with zlib</b>"]
        E7["<b>Parse header: type size null</b>"]
        E8["<b>Extract content</b>"]
        E9["<b>Create appropriate object</b>"]
        E10["<b>Deserialize content</b>"]
        E11["<b>Return GitBlob</b>"]
        E12["<b>Output to stdout</b>"]

        E1 --> E2 --> E3 --> E4 --> E5
        E5 --> E6 --> E7 --> E8 --> E9 --> E10 --> E11
        E2 --> E12
    end
```

### hash-object Command

```mermaid
graph TD
    subgraph "Flow 8: git hash-object (Hashing Content)"
        H1["<b>User: twit hash-object -w file.txt</b>"] --> H2["<b>HashObjectCommand.execute</b>"]
        H2 --> H3["<b>Read file InputStream</b>"]
        H3 --> H4["<b>ObjectIO.objectHash</b>"]
        H4 --> H5["<b>ObjectIO.objectWrite (if -w)</b>"]
        H5 --> H6["<b>Compute SHA-1 & Compress</b>"]
        H6 --> H7["<b>Store in .git/objects/</b>"]
        H4 --> H8["<b>Print SHA-1 to stdout</b>"]
    end
```

### log Command

```mermaid
graph TD
    subgraph "Flow 4: git log (Traversing History)"
        D1["<b>User: wyag log HEAD</b>"]
        D2["<b>LogCommand.execute</b>"]
        D3["<b>RefResolver.objectFind HEAD</b>"]
        D4["<b>Read .git/HEAD</b>"]
        D5["<b>Follow ref to commit SHA</b>"]
        D6["<b>logGraphviz recursively</b>"]
        D7["<b>ObjectIO.objectRead commit</b>"]
        D8["<b>Parse commit with KVLMParser</b>"]
        D9["<b>Get parent SHA from commit</b>"]
        D10["<b>Recurse to parent</b>"]
        D11["<b>Output Graphviz format</b>"]

        D1 --> D2 --> D3 --> D4 --> D5 --> D6
        D6 --> D7 --> D8 --> D9 --> D10
        D10 --> D6
        D6 --> D11
    end
```

### checkout Command

```mermaid
graph TD
    subgraph "Flow 6: git checkout (Extracting Tree)"
        F1["<b>User: wyag checkout SHA path</b>"]
        F2["<b>CheckoutCommand.execute</b>"]
        F3["<b>RefResolver.objectFind SHA</b>"]
        F4["<b>ObjectIO.objectRead commit/tree</b>"]
        F5["<b>If commit, get tree SHA</b>"]
        F6["<b>treeCheckout recursively</b>"]
        F7["<b>For each GitTreeLeaf</b>"]
        F8["<b>ObjectIO.objectRead leaf</b>"]
        F9["<b>If blob, write file</b>"]
        F10["<b>If tree, recurse + mkdir</b>"]
        F11["<b>Write to filesystem</b>"]

        F1 --> F2 --> F3 --> F4 --> F5 --> F6
        F6 --> F7 --> F8
        F8 --> F9 --> F11
        F8 --> F10 --> F6
    end
```

### ls-tree Command

```mermaid
graph TD
    subgraph "Flow 9: git ls-tree (Listing Tree Contents)"
        I1["<b>User: twit ls-tree [-r] SHA</b>"] --> I2["<b>LsTreeCommand.execute</b>"]
        I2 --> I3["<b>RefResolver.objectFind (ref -> SHA)</b>"]
        I3 --> I4["<b>ObjectIO.objectRead (SHA -> GitTree)</b>"]
        I4 --> I5["<b>Parse Tree Content</b>"]
        I5 --> I6["<b>Loop through GitTreeLeaf items</b>"]
        I6 --> I7{"<b>Recursive?</b>"}
        I7 -- "Yes (and is tree)" --> I4
        I7 -- "No (or is blob)" --> I8["<b>Format: [mode] [type] [SHA] [path]</b>"]
        I8 --> I9["<b>Print to stdout</b>"]
    end
```

### show-ref Command

```mermaid
graph TD
    subgraph "Flow 10: git show-ref (Listing All Refs)"
        J1["<b>User: twit show-ref</b>"] --> J2["<b>ShowRefCommand.execute</b>"]
        J2 --> J3["<b>GitRepository.find</b>"]
        J3 --> J4["<b>RefResolver.refList (.git/refs)</b>"]
        J4 --> J5["<b>Recursive directory walk</b>"]
        J5 --> J6["<b>Read SHA from each ref file</b>"]
        J6 --> J7["<b>Build Map of refs</b>"]
        J7 --> J8["<b>showRef: Print SHA + path</b>"]
    end
```

### tag Command

```mermaid
graph TD
    subgraph "Flow 11: git tag (Creating/Listing Tags)"
        K1["<b>User: twit tag [-a] name [obj]</b>"] --> K2["<b>TagCommand.execute</b>"]
        K2 --> K3{"<b>Create or List?</b>"}
        K3 -- "List" --> K4["<b>RefResolver.refList (refs/tags)</b>"]
        K4 --> K5["<b>Print tag names</b>"]
        K3 -- "Create" --> K6["<b>tagCreate</b>"]
        K6 --> K7{"<b>-a (Annotated)?</b>"}
        K7 -- "No" --> K8["<b>Lightweight: Write SHA to refs/tags/name</b>"]
        K7 -- "Yes" --> K9["<b>Create GitTag object (KVLM)</b>"]
        K9 --> K10["<b>ObjectIO.objectWrite tag</b>"]
        K10 --> K11["<b>Write Tag SHA to refs/tags/name</b>"]
    end
```

### rev-parse Command

```mermaid
graph TD
    subgraph "Flow 12: git rev-parse (Resolving Names)"
        L1["<b>User: twit rev-parse name</b>"] --> L2["<b>RevParseCommand.execute</b>"]
        L2 --> L3["<b>RefResolver.objectFind</b>"]
        L3 --> L4["<b>objectResolve (Check HEAD, Hash, Tag, Branch)</b>"]
        L4 --> L5["<b>Return full 40-char SHA</b>"]
        L5 --> L6["<b>Print SHA to stdout</b>"]
    end
```

### ls-files Command

```mermaid
graph TD
    subgraph "Flow 13: git ls-files (Listing Staged Files)"
        M1["<b>User: twit ls-files [--verbose]</b>"] --> M2["<b>LsFilesCommand.execute</b>"]
        M2 --> M3["<b>IndexIO.indexRead</b>"]
        M3 --> M4["<b>Parse binary .git/index</b>"]
        M4 --> M5["<b>Loop through GitIndexEntry list</b>"]
        M5 --> M6["<b>Print file name</b>"]
        M6 --> M7{"<b>Verbose?</b>"}
        M7 -- "Yes" --> M8["<b>Print metadata (ctime, mtime, SHA, flags)</b>"]
    end
```

### check-ignore Command

```mermaid
graph TD
    subgraph "Flow 14: git check-ignore (Testing Ignore Rules)"
        N1["<b>User: twit check-ignore path</b>"] --> N2["<b>CheckIgnoreCommand.execute</b>"]
        N2 --> N3["<b>IgnoreParser.gitIgnoreRead</b>"]
        N3 --> N4["<b>Read .git/info/exclude + global ignore</b>"]
        N4 --> N5["<b>Read all .gitignore files from Index</b>"]
        N5 --> N6["<b>IgnoreParser.checkIgnore(rules, path)</b>"]
        N6 --> N7["<b>Regex/Glob matching (fnmatch)</b>"]
        N7 --> N8{"<b>Ignored?</b>"}
        N8 -- "Yes" --> N9["<b>Print path</b>"]
    end
```

### status Command

```mermaid
graph TD
    subgraph "Flow 3: git status (Comparing States)"
        C1["<b>User: wyag status</b>"]
        C2["<b>StatusCommand.execute</b>"]
        C3["<b>statusBranch</b>"]
        C4["<b>Read .git/HEAD</b>"]
        C5["<b>Display current branch</b>"]
        C6["<b>statusHeadIndex</b>"]
        C7["<b>Get HEAD commit SHA</b>"]
        C8["<b>ObjectIO.objectRead commit</b>"]
        C9["<b>Get tree from commit</b>"]
        C10["<b>treeToDict - flatten tree</b>"]
        C11["<b>IndexIO.indexRead</b>"]
        C12["<b>Compare HEAD tree vs Index</b>"]
        C13["<b>Show staged changes</b>"]
        C14["<b>statusIndexWorktree</b>"]
        C15["<b>Walk filesystem</b>"]
        C16["<b>Compare timestamps/content</b>"]
        C17["<b>Show unstaged changes</b>"]

        C1 --> C2
        C2 --> C3 --> C4 --> C5
        C2 --> C6 --> C7 --> C8 --> C9 --> C10
        C6 --> C11 --> C12 --> C13
        C2 --> C14 --> C15 --> C16 --> C17
    end
```

### add Command

```mermaid
graph TD
    subgraph "Flow 1: git add (Staging a File)"
        A1["<b>User: wyag add file.txt</b>"]
        A2["<b>AddCommand.execute</b>"]
        A3["<b>Read file from worktree</b>"]
        A4["<b>ObjectIO.objectHash</b>"]
        A5["<b>Compute SHA-1</b>"]
        A6["<b>Create GitBlob</b>"]
        A7["<b>Compress with zlib</b>"]
        A8["<b>Write to .git/objects/XX/YYY</b>"]
        A9["<b>IndexIO.indexRead</b>"]
        A10["<b>Read .git/index</b>"]
        A11["<b>Create GitIndexEntry</b>"]
        A12["<b>Add entry to index</b>"]
        A13["<b>IndexIO.indexWrite</b>"]
        A14["<b>Write .git/index</b>"]

        A1 --> A2 --> A3 --> A4
        A4 --> A5 --> A6 --> A7 --> A8
        A2 --> A9 --> A10 --> A11 --> A12 --> A13 --> A14
    end
```

### rm Command

```mermaid
graph TD
    subgraph "Flow 15: git rm (Removing Files)"
        O1["<b>User: twit rm path</b>"] --> O2["<b>RmCommand.execute</b>"]
        O2 --> O3["<b>IndexIO.indexRead</b>"]
        O3 --> O4["<b>Filter entries (remove matching path)</b>"]
        O4 --> O5["<b>Files.delete (from worktree)</b>"]
        O5 --> O6["<b>IndexIO.indexWrite</b>"]
        O6 --> O7["<b>Updated index without the file</b>"]
    end
```
### commit Command

```mermaid
graph TD
    subgraph "Flow 2: git commit (Creating a Commit)"
        B1["<b>User: wyag commit -m 'msg'</b>"]
        B2["<b>CommitCommand.execute</b>"]
        B3["<b>IndexIO.indexRead</b>"]
        B4["<b>Read staged files from index</b>"]
        B5["<b>treeFromIndex</b>"]
        B6["<b>Group files by directory</b>"]
        B7["<b>Create GitTree for each dir</b>"]
        B8["<b>TreeParser.serialize</b>"]
        B9["<b>ObjectIO.objectWrite tree</b>"]
        B10["<b>Create GitCommit</b>"]
        B11["<b>Set tree SHA, parent, author</b>"]
        B12["<b>KVLMParser.serialize</b>"]
        B13["<b>ObjectIO.objectWrite commit</b>"]
        B14["<b>Update .git/refs/heads/master</b>"]

        B1 --> B2 --> B3 --> B4 --> B5
        B5 --> B6 --> B7 --> B8 --> B9
        B2 --> B10 --> B11 --> B12 --> B13 --> B14
    end
```

## Storage Structure Diagram

```mermaid
graph TB
    subgraph "Git Repository Structure"
        Root[".git/"]
        
        subgraph "Objects Storage"
            ObjDir["objects/"]
            Obj00["00/"]
            Obj01["01/"]
            ObjFF["ff/"]
            ObjDots["..."]
            BlobFile["a1b2c3d4e5f6... (blob)"]
            CommitFile["f1e2d3c4b5a6... (commit)"]
            TreeFile["1a2b3c4d5e6f... (tree)"]
        end
        
        subgraph "References"
            RefDir["refs/"]
            HeadsDir["heads/"]
            TagsDir["tags/"]
            Master["master -> SHA"]
            Feature["feature -> SHA"]
            V10["v1.0 -> SHA"]
        end
        
        subgraph "Working Area"
            Index["index (binary)"]
            Head["HEAD -> ref: refs/heads/master"]
            Config["config"]
            Desc["description"]
        end
    end

    Root --> ObjDir & RefDir & Index & Head & Config & Desc
    ObjDir --> Obj00 & Obj01 & ObjFF & ObjDots
    Obj00 --> BlobFile
    Obj01 --> CommitFile
    ObjFF --> TreeFile
    RefDir --> HeadsDir & TagsDir
    HeadsDir --> Master & Feature
    TagsDir --> V10

    subgraph "Object Content Examples"
        subgraph "Blob Object"
            B1["Header: blob 13 (null)"]
            B2["Content: Hello, World!"]
            B3["Stored as: .git/objects/8a/b686ea..."]
        end
        
        subgraph "Tree Object"
            T1["100644 blob a1b2c3... README.md"]
            T2["100644 blob d4e5f6... file.txt"]
            T3["040000 tree 1a2b3c... src"]
            T4["Stored as: .git/objects/7f/8e9d0c..."]
        end
        
        subgraph "Commit Object"
            C1["tree 7f8e9d0c..."]
            C2["parent 4b3c2a1f..."]
            C3["author Name email timestamp"]
            C4["committer Name email timestamp"]
            C5["(Empty Line)"]
            C6["Commit message"]
            C7["Stored as: .git/objects/2c/3d4e5f..."]
        end
    end
    B1 --> B2 --> B3
    T1 --> T2 --> T3 --> T4
    C1 --> C2 --> C3 --> C4 --> C5 --> C6 --> C7

    subgraph "Index Structure"
        I1["Header: DIRC version=2 count=3"]
        I2["Entry 1: README.md -> blob a1b2c3..."]
        I3["ctime, mtime, dev, ino, mode, size"]
        I4["Entry 2: file.txt -> blob d4e5f6..."]
        I5["ctime, mtime, dev, ino, mode, size"]
        I6["Entry 3: src/Main.java -> blob 9a8b7c..."]
        I7["ctime, mtime, dev, ino, mode, size"]
    end
    I1 --> I2 --> I3 --> I4 --> I5 --> I6 --> I7

    subgraph "Object Relationships"
        Commit1["Commit (2c3d4e)"]
        Tree1["Tree (7f8e9d)"]
        Blob1["Blob (a1b2c3) README.md"]
        Blob2["Blob (d4e5f6) file.txt"]
        SubTree["Tree (1a2b3c) src/"]
        Blob3["Blob (9a8b7c) Main.java"]
        
        Commit1 -->|tree| Tree1
        Tree1 -->|100644 README.md| Blob1
        Tree1 -->|100644 file.txt| Blob2
        Tree1 -->|040000 src| SubTree
        SubTree -->|100644 Main.java| Blob3
    end

    subgraph "Commit Graph"
        C_3["Commit 3 (master)"]
        C_2["Commit 2"]
        C_1["Commit 1"]
        C_0["Commit 0 (initial)"]
        
        C_3 -->|parent| C_2
        C_2 -->|parent| C_1
        C_1 -->|parent| C_0
        
        Master2["refs/heads/master"]
        Master2 -.-> C_3
        Head2["HEAD"]
        Head2 -.-> Master2
    end

    subgraph "Three States Comparison"
        subgraph "HEAD_State"
            H_Tree["Tree from commit"]
            H_File1["file1.txt -> blob abc"]
            H_File2["file2.txt -> blob def"]
        end
        
        subgraph "Index_State"
            Idx_Entry1["file1.txt -> blob abc"]
            Idx_Entry2["file2.txt -> blob xyz"]
            Idx_Entry3["file3.txt -> blob 123"]
        end
        
        subgraph "Worktree_State"
            W_File1["file1.txt (content abc)"]
            W_File2["file2.txt (modified!)"]
            W_File3["file3.txt (content 123)"]
            W_File4["file4.txt (untracked)"]
        end
        
        H_Tree --> H_File1 & H_File2
        H_File1 -.->|same| Idx_Entry1
        H_File2 -.->|modified| Idx_Entry2
        Idx_Entry3 -.->|new| W_File3
        Idx_Entry1 -.->|same| W_File1
        Idx_Entry2 -.->|same| W_File2
        Idx_Entry3 -.->|same| W_File3
    end

    subgraph "SHA1_Hash_Computation"
        Hash1["1. Raw content: Hello"]
        Hash2["2. Add header: blob 5 (null) Hello"]
        Hash3["3. Compute SHA-1: 5ab2f8a4..."]
        Hash4["4. Compress with zlib"]
        Hash5["5. Store at: objects/5a/b2f8a4..."]
        
        Hash1 --> Hash2 --> Hash3 --> Hash4 --> Hash5
    end

    subgraph "Parsing Examples"
        subgraph "Commit_Parsing"
            CP1["Raw bytes"]
            CP2["Find 'tree '"]
            CP3["Extract tree SHA"]
            CP4["Find 'parent '"]
            CP5["Extract parent SHA"]
            CP6["Find message start"]
            CP7["Build Map"]
            
            CP1 --> CP2 --> CP3 --> CP4 --> CP5 --> CP6 --> CP7
        end
        
        subgraph "Tree_Parsing_Logic"
            TP1["Raw bytes"]
            TP2["Find space"]
            TP3["Extract mode"]
            TP4["Find null"]
            TP5["Extract path"]
            TP6["Read 20 bytes SHA"]
            TP7["Create GitTreeLeaf"]
            
            TP1 --> TP2 --> TP3 --> TP4 --> TP5 --> TP6 --> TP7
        end
    end

    subgraph "Object_Type_Decision"
        OT1["Read from storage"]
        OT2["Decompress"]
        OT3["Parse header"]
        OT4{"Check type"}
        OT5["GitBlob"]
        OT6["GitCommit"]
        OT7["GitTree"]
        OT8["GitTag"]
        OT9["Deserialize"]
        
        OT1 --> OT2 --> OT3 --> OT4
        OT4 -->|blob| OT5 --> OT9
        OT4 -->|commit| OT6 --> OT9
        OT4 -->|tree| OT7 --> OT9
        OT4 -->|tag| OT8 --> OT9
    end
```