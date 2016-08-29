# nodewheel

Demo of figwheel and lambda functions in cljs. 

## Usage
Requires 0.7.0-SNAPSHOT of [eulalie](https://github.com/nervous-systems/eulalie), which isn't released on maven as of yet. You'll need to clone and install that locally to use this:

```
$ git clone git@github.com:nervous-systems/eulalie.git
$ cd eulalie
$ lein install
```

Switch back to the directory this is cloned in and create the creds.edn file, which should contain:

```
{:access-key "" #AWS access key (used for deployment)
 :secret-key "" #AWS secret key (used for deployment)
 :role ""} #ARN for an IAM role that the function will use.
```

After that's done you can run:

```
$ lein npm install
$ lein cljsbuild once
$ node nodewheelDeployable.js nodewheelDeployable.js
```

And the code should deploy itself to AWS, and invoke itself

## License

Copyright © 2016 Liam Falconer

Distributed under the MIT License 