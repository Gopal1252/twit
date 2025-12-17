# Data Flow Diagrams

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