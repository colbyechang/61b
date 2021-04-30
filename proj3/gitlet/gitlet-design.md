# Gitlet Design Document
**Name: Colby Chang**

# Classes and Data Structures
## Blob
This class contains the contents of the file it represents.


**Fields:**
1. String _sha: this blob’s hash code
2. String _name: this blob’s filename

## Commit
This class contains the log message and other metadata of a specific commit as well as keeping track of its parent commit and the blobs staged for this commit.


**Fields:**
1. String hash: this blob’s hash code
2. Commit parent: this commit’s parent
3. ArrayList<Commit> children: the children of this commit
        
## StagingArea
This class represents the staging area before pushing.


**Fields:**
1. ArrayList<Blob> add: the blobs to be added
2. ArrayList<Blob> remove: the blobs to be removed
        
## Branch
This class represents a reference to a specific commit node.


**Fields:**
1. String name: this branches name
2. Commit head: the pointer to the commit which this branch points


## Gitlet
This class represents a full gitlet design. 


**Fields:**
1. Branch master: the current working branch of Gitlet
2. ArrayList<Branch> branches: a tree of all the branches
3. StagingArea area: the staging area
4. HashMap<String, Blob> table: table of blobs that are referred to by commits
# Algorithms
## Blob Class
1. getHashCode(): returns the blob’s hashcode according to SHA-1 


## Commit Class
1. addChild(Commit c): adds a child to the commit
2. assignParent(Commit p): assigns parent to the commit
3. getHashCode(): returns the commit’s hashcode according to SHA-1


## StagingArea Class
1. clearArea(): clears the area
2. add(Blob b): adds blob to staging area
3. remove(Blob b): removes the blob from the blobs tracked in the current commit


## Branch Class
1. setCommit(Commit c): sets head to c

## Gitlet Class
1. init(): sets up an initial Commit and a master
2. add(String fileName): creates a Blob and adds to staging area
3. commit(String message): creates a Commit and clear
4. remove(String fileName): removes file if it is currently staged for addition
5. log(): tracks back each and prints out all commits using their parents
6. globalLog(): prints each commit in the commit tree, including all parents and children of current working commit
7. find(String message): prints out all ids of commits with the given commit message, one per line
8. status(): prints out all branches
9. checkout(String input): depending on input, reverts back to previous commits
10. addBranch(String name): adds a new branch with given name and points to the current head node
11. removeBranch(String name): removes the branch named “name”
12. reset: essentially checkout of an arbitrary commit that also changes the current branch head.
13. merge(String name): merges the current commit with the commit of name “name”
14. main(String[] args): determines which method to use, given an input String from the user

# Persistence
1. Write the HashMap table to disk. We can serialize it into bytes that we can eventually write to a specially named file on disk using the writeObject method from the Utils class.
2. Write the ArrayList branches to disk. We can serialize it into bytes that we can eventually write to a specially named file on disk using the writeObject method from the Utils class.
3. Write all Commits to disk. We can serialize the Commit objects and write them to files on disk using the writeObject method from the Utils class. Commit inherits Serializable. 
4. Write StagingArea area to disk. We can serialize it into bytes that we can eventually write to a specially named file on disk using the writeObject method from the Utils class. StagingArea inherits Serializable. 

