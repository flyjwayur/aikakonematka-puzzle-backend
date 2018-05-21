# aikakonematka-puzzle-backend

'Aikakonematka-puzzle' is a collaborative multiplayers' puzzle game.

It is composed of ['front end'(written in Clojurescript)](https://github.com/flyjwayur/aikakoneMatka-puzzle) and 'backend'(written in Clojure).

The Backend part includes 'server, websocket' to communicate with clients.


## Running Locally

* In REPL

```clj
(start-server)
```

* In the Terminal

```sh
$ lein run
```

## Deploying to Heroku

```sh
heroku create aikakonematka-puzzle-backend
git push heroku master
heroku ps:scale web=1
heroku open
heroku logs --tail
```

### Might be Useful
#### Reference
*clojure-getting-started*
[https://github.com/heroku/clojure-getting-started]

## License

Copyright © 2018 HyeSoo Park

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
