# Twitter Redis
## Task 1
Docker cmd til Redis container - docker run --name some-redis -d redis:6.2.3-alpine redis-server --appendonly yes
Docker pull til Ryuk - docker pull testcontainers/ryuk:0.3.0

## Task 2
Implement the tiny twitter clone found at this repo, using only Redis as a data store.

Se PostManagementImpl and UserManagementImpl

## Task 3
In a readme, write a short explanation of your redis data model. It should be clear enough for a developer to be able to implement the same thing.

```users: set af usernames

user:username:
  firstname:String
  lastname:String
  passwordHash:String
  birthday:String
  followers:String
  following:String
  
follow: set af usernames
followed: set af usernames
  
posts: set af timestamp og message
```
