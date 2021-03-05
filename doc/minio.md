how to setup local [[S3]] server with [[Minio]] [[Electron]]

- https://meetalva.io/doc/docs/contributors/test-autoupdater.html
- https://github.com/electron-userland/electron-builder/issues/3053#issuecomment-401001573
- https://www.electron.build/auto-update.html#debugging
- build new version with lein compile, not lein dev (which watches)
- build new app with
AWS_ACCESS_KEY_ID=admin AWS_SECRET_ACCESS_KEY=password yarn run electron-builder -l -p always
- run server with
sudo MINIO_ROOT_USER=admin MINIO_ROOT_PASSWORD=password ./minio server test-update 
- create bucket with 
mc config host add electron-builder http://192.168.2.103:9000 [some-key] [some-secret]
mc mb electron-builder/electron-builder
- add Read policy: https://github.com/electron-userland/electron-builder/issues/2233#issuecomment-341882952
    - 
